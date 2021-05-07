import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import java.nio.*;

import renderer.RenderContext;
import renderer.RenderMaterial;
import shape.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Application {

    private long window;
    private RenderContext renderContext;
    private Input input;
    private Camera camera;

    private Shape shapeRef;

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(this.window);
        glfwDestroyWindow(this.window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.window = glfwCreateWindow(1600, 900, "Raymarcher!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key == GLFW_KEY_F11 && action == GLFW_PRESS) {
                GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                glfwWindowHint(GLFW_RED_BITS, mode.redBits());
                glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
                glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
                glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
                glfwSetWindowMonitor(this.window, glfwGetPrimaryMonitor(), 0, 0,
                        mode.width(), mode.height(), mode.refreshRate());
                glfwSwapInterval(1);
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(this.window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    this.window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(this.window);
        glfwSwapInterval(2);
        glfwShowWindow(this.window);
        glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        this.input = new Input(this.window);
        this.renderContext = new RenderContext(1600, 900);

        glfwSetWindowSizeCallback(this.window, (window, width, height) -> {
            this.renderContext.updateResolution(width, height);
        });

        // Initialize camera
        this.camera = new Camera(this.input, this.renderContext);

        // Initialize scene
        RenderMaterial material0 = this.renderContext.createMaterial();
        //material0.setAlbedo(53.0f / 256.0f, 129.0f / 256.0f, 184.0f / 256.0f);
        //material0.setAlbedo(0.48f, 0.44f, 0.4f);
        material0.setAlbedo(1.0f, 0.0f, 0.0f);
        material0.setMetallic(0.5f);
        material0.setRoughness(0.9f);
        RenderMaterial material1 = this.renderContext.createMaterial();
        material1.setAlbedo(1.0f, 1.0f, 1.0f);
        material1.setMetallic(0.0f);
        material1.setRoughness(0.0f);
        RenderMaterial material2 = this.renderContext.createMaterial();
        material2.setAlbedo(0, 0, 0);
        material2.setMetallic(0.0f);
        material2.setRoughness(0.0f);

        SphereShape.register(this.renderContext);
        PlaneShape.register(this.renderContext);
        RoundBoxShape.register(this.renderContext);
        MengerSpongeShape.register(this.renderContext);
        BoxFrameShape.register(this.renderContext);

//        for (int i = 0; i < 20; i++) {
//            RenderMaterial material = this.renderContext.createMaterial();
//            material.setAlbedo(1, 0, 0);
//            material.setRoughness((float)i / 50f + 0.01f);
//            SphereShape shape = new SphereShape(this.renderContext, 1.0f);
//            shape.setMaterial(material);
//            shape.setPosition(i * 2.2f, 0, 0);
//        }

        Shape sponge = new MengerSpongeShape(this.renderContext);
        sponge.setMaterial(material1);

        SphereShape shape0 = new SphereShape(this.renderContext, 1.0f);
        shape0.setMaterial(material0);
        shape0.setPosition(0, 15, 0);

        PlaneShape plane = new PlaneShape(this.renderContext, new Vector3f(0, 1, 0), -7);
        plane.setMaterial(material1);
        plane.setPosition(0, 0, 0);

        RoundBoxShape box = new RoundBoxShape(this.renderContext, new Vector3f(10, 30, 1), 0);
        box.setMaterial(material1);
        box.setPosition(0, 0, 5);

        BoxFrameShape boxFrame = new BoxFrameShape(this.renderContext, new Vector3f(3, 3, 5), 0.3f);
        boxFrame.setMaterial(material0);
        boxFrame.setPosition(30, 3, 0);

//        this.shapeRef = shape0;
    }

    private float totalTime = 0.0f;

    private void loop() {
        long lastTime = System.nanoTime();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(this.window)) {
            // Compute deltatime
            long time = System.nanoTime();
            float deltaTime = (float)(time - lastTime) / 1000000.0f;
            lastTime = time;

            if (this.shapeRef != null) {
                this.totalTime += deltaTime;
                float p = (float)Math.cos(totalTime * 0.001f) * 2.0f;
                this.shapeRef.setPosition(0, p + 1.0f, p);
            }

            // Update events
            glfwPollEvents();
            this.input.update();
            this.camera.update(deltaTime);
            this.renderContext.update(deltaTime);

            // Render scene
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            this.renderContext.render();
            glfwSwapBuffers(window);
        }
    }

    public static void main(String[] args) {
        new Application().run();
    }
}