package renderer;

import org.lwjgl.vulkan.VkCommandBuffer;
import renderer.engine.*;
import renderer.entity.*;
import renderer.render.geometry.GeometryPipeline;
import window.Window;

import static org.lwjgl.vulkan.VK11.*;

public class Renderer {

    private boolean renderContextOutOfDate;
    private Window window;

    private Context context;
    private MemoryManager memoryManager;
    private ShaderManager shaderManager;
    private CommandPool commandPool;

    private Swapchain swapchain;
    private RenderPass renderPass;
    private GeometryPipeline geometryPipeline;
    private RenderContext renderContext;

    private Camera camera;

    public Renderer(Window window) {
        this.renderContextOutOfDate = false;
        this.window = window;

        this.context = new Context(window);
        this.memoryManager = new MemoryManager(this.context);
        this.shaderManager = new ShaderManager(this.context);
        this.commandPool = new CommandPool(this.context, this.context.getGraphicsQueueFamilyIndex());

        this.swapchain = new Swapchain(this.context, window.getWidth(), window.getHeight());
        this.renderPass = new RenderPass(this.context, this.swapchain);
        this.geometryPipeline = new GeometryPipeline(this.context, this.renderPass, this.shaderManager, this.swapchain);
        this.renderContext = new RenderContext(this.context, this.swapchain, this.renderPass, this.commandPool);

        this.camera = new Camera();
    }

    public void cleanup() {
        vkDeviceWaitIdle(this.context.getDevice());

        this.renderContext.cleanup();
        this.geometryPipeline.cleanup();
        this.renderPass.cleanup();
        this.swapchain.cleanup();

        this.commandPool.cleanup();
        this.shaderManager.cleanup();
        this.memoryManager.cleanup();
        this.context.cleanup();
    }

    private boolean tryRender() {
        if (!this.renderContext.beginRender()) return false;

        VkCommandBuffer cmd = this.renderContext.getActiveCommandBuffer();
        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, this.geometryPipeline.getPipeline());
        vkCmdDraw(cmd, 3, 1, 0, 0);

        if (!this.renderContext.endRender()) return false;

        return true;
    }

    public void render() {
        if (!this.renderContextOutOfDate) {
            this.renderContextOutOfDate = !tryRender();
        } else {
            // Device out of date, recreation required
            vkDeviceWaitIdle(this.context.getDevice());
            int width = this.window.getWidth();
            int height = this.window.getHeight();
            if (width != 0 && height != 0) {
                System.out.println("Resize " + width + " " + height);

                // Recreate swapchain
                this.swapchain.recreate(width, height);

                // Notify swapchain update
                this.renderPass.updateSwapchain(this.swapchain);
                this.geometryPipeline.updateSwapchain(this.swapchain);
                this.renderContext.updateSwapchain(this.swapchain);

                this.renderContextOutOfDate = false;
            }
        }
    }

    public RenderCamera getActiveCamera() {
        return this.camera;
    }
}