package rendererOld;

import rendererOld.asset.AssetManager;
import rendererOld.entity.SceneManager;
import org.lwjgl.opengl.GL;
import rendererOld.memory.ColorBuffer;
import rendererOld.memory.GBuffer;
import rendererOld.memory.MemoryManager;
import rendererOld.shader.ComputeShader;
import rendererOld.shader.RenderShader;
import rendererOld.shader.Shader;
import rendererOld.shader.ShaderGenerator;

import static org.lwjgl.opengl.GL45.*;

public class RenderContext {

    private final static int FRAME_COUNT = 2;

    private int width;
    private int height;

    // Managers
    private MemoryManager memoryManager;
    private ShaderGenerator shaderGenerator;
    private SceneManager sceneManager;
    private AssetManager assetManager;

    // Shaders
    private Shader gBufferShader;
    private Shader indirectLightShader;
    private Shader lightShader;
    private Shader postProcessShader;
    private ComputeShader computeShader;

    // Framebuffers
    private GBuffer[] gBuffers;
    private ColorBuffer[] indirectLightBuffers;
    private ColorBuffer colorBuffer;
    private int currentFrame;
    private int frameIndex;

    // VAO
    private int emptyVAO;

    public RenderContext(int width, int height) {
        GL.createCapabilities();

        this.width = width;
        this.height = height;

        this.gBuffers = new GBuffer[FRAME_COUNT];
        this.indirectLightBuffers = new ColorBuffer[FRAME_COUNT];

        this.currentFrame = 0;
        this.frameIndex = 0;

        try {
            // Generate quad resources
            updateResolution(this.width, this.height);
            createEmptyVAO();

            // Create main systems
            this.memoryManager = new MemoryManager();
            this.shaderGenerator = new ShaderGenerator();
            this.assetManager = new AssetManager(this.memoryManager);
            this.sceneManager = new SceneManager(this.memoryManager);

            // Create shaders
            createShaders();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerShapeType(RenderShapeType type) {
        this.shaderGenerator.registerShapeType(type);
        try {
            deleteShaders();
            createShaders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RenderMaterial createMaterial() {
        return this.assetManager.createMaterial();
    }

    public RenderShape createShape() {
        return this.sceneManager.createShape();
    }

    public RenderCamera getActiveCamera() {
        return this.sceneManager.getActiveCamera();
    }

    public void cleanup() {
        for (int i = 0; i < FRAME_COUNT; i++) {
            this.gBuffers[i].cleanup();
            this.indirectLightBuffers[i].cleanup();
        }
        this.colorBuffer.cleanup();
        deleteEmptyVAO();
        deleteShaders();
    }

    public void updateResolution(int width, int height) {
        this.width = width;
        this.height = height;

        System.out.println("request new resolution " + this.width + " " + this.height);

        for (int i = 0; i < FRAME_COUNT; i++) {
            if (this.gBuffers[i] != null) {
                this.gBuffers[i].cleanup();
                this.gBuffers[i] = null;
            }
            if (this.indirectLightBuffers[i] != null) {
                this.indirectLightBuffers[i].cleanup();
                this.indirectLightBuffers[i] = null;
            }
        }
        if (this.colorBuffer != null) {
            this.colorBuffer.cleanup();
            this.colorBuffer = null;
        }

//        int bufferWidth = this.width; int bufferHeight = this.height;
//        int bufferWidth = 1024; int bufferHeight = 576;
//        int bufferWidth = 1280; int bufferHeight = 720;
        int bufferWidth = 1920; int bufferHeight = 1080;
//        int bufferWidth = 1600; int bufferHeight = 800;
//        int bufferWidth = 3840; int bufferHeight = 2160;
//        int bufferWidth = 7680; int bufferHeight = 4320;

        System.out.println("choosen resolution " + bufferWidth + " " + bufferHeight);

        for (int i = 0; i < FRAME_COUNT; i++) {
            this.gBuffers[i] = new GBuffer(bufferWidth, bufferHeight);
            this.indirectLightBuffers[i] = new ColorBuffer(bufferWidth, bufferHeight);
        }
        this.colorBuffer = new ColorBuffer(bufferWidth, bufferHeight);
    }

    public void update(float deltaTime) {

    }

    public void render() {

        assert (glGetError() == 0);

        // Compute previous frame indice
        int previousFrame = Math.floorMod(this.currentFrame - 1, FRAME_COUNT);

        // Update camera
        this.sceneManager.getActiveCamera().setRatio((float)this.width / (float)this.height);
        this.sceneManager.getActiveCamera().startFrame();
        if (this.sceneManager.getActiveCamera().hasMoved()) {
            this.frameIndex = 0;
        }

        /*------------------*/
        /*   GBUFFER PASS   */
        /*------------------*/

        this.gBufferShader.bind();
        this.gBuffers[this.currentFrame].setViewport();
        this.gBuffers[this.currentFrame].bindFramebuffer();
        this.sceneManager.getActiveCamera().pushGBufferPassUniforms();
        glClear(GL_COLOR_BUFFER_BIT);
        drawEmptyVAO();

        /*-------------------------*/
        /*   INDIRECT LIGHT PASS   */
        /*-------------------------*/

        this.indirectLightShader.bind();
        this.indirectLightBuffers[this.currentFrame].setViewport();
        this.indirectLightBuffers[this.currentFrame].bindFramebuffer();
        this.sceneManager.getActiveCamera().pushIndirectLightPassUniforms();
        this.gBuffers[this.currentFrame].bindAllSamplers(0);
        this.gBuffers[previousFrame].bindAllSamplers(2);
        this.indirectLightBuffers[previousFrame].bindSampler(4);
        glUniform1i(6, this.frameIndex);
        drawEmptyVAO();

        /*----------------*/
        /*   LIGHT PASS   */
        /*----------------*/

        this.lightShader.bind();
        this.colorBuffer.setViewport();
        this.colorBuffer.bindFramebuffer();
        this.gBuffers[this.currentFrame].bindAllSamplers(0);
        this.indirectLightBuffers[this.currentFrame].bindSampler(2);
        drawEmptyVAO();

        /*-----------------------*/
        /*   POST-PROCESS PASS   */
        /*-----------------------*/

        this.postProcessShader.bind();
        glViewport(0, 0, this.width, this.height);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        this.colorBuffer.bindSampler(0);
        drawEmptyVAO();

        // Next frame
        this.sceneManager.getActiveCamera().endFrame();
        this.currentFrame = (this.currentFrame + 1) % FRAME_COUNT;
        this.frameIndex++;
    }

    private void createShaders() throws Exception {
        this.gBufferShader = new RenderShader(this.shaderGenerator.getCode("gbuffer_pass.vert"),
                this.shaderGenerator.getCode("gbuffer_pass.frag"));
        this.indirectLightShader = new RenderShader(this.shaderGenerator.getCode("indirect_light_pass.vert"),
                this.shaderGenerator.getCode("indirect_light_pass.frag"));
        this.lightShader = new RenderShader(this.shaderGenerator.getCode("light_pass.vert"),
                this.shaderGenerator.getCode("light_pass.frag"));
        this.postProcessShader = new RenderShader(this.shaderGenerator.getCode("postprocess_pass.vert"),
                this.shaderGenerator.getCode("postprocess_pass.frag"));
    }
    private void deleteShaders() {
        this.gBufferShader.cleanup();
        this.indirectLightShader.cleanup();
        this.lightShader.cleanup();
        this.postProcessShader.cleanup();
    }

    private void createEmptyVAO() {
        this.emptyVAO = glGenVertexArrays();
    }
    private void deleteEmptyVAO() {
        glDeleteVertexArrays(this.emptyVAO);
    }

    private void drawEmptyVAO() {
        glBindVertexArray(this.emptyVAO);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }
}