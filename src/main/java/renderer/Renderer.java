package renderer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkCommandBuffer;
import renderer.core.*;
import renderer.entity.*;
import window.Window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;

public class Renderer {

    private Window window;
    private boolean renderContextOutOfDate;
    private int maxInFlightFrameCount;
    private int historyImageCount;

    // Context
    private Context context;
    private MemoryManager memoryManager;
    private ShaderManager shaderManager;
    private CommandPool commandPool;
    private DescriptorPool descriptorPool;
    private Swapchain swapchain;
    private RenderContext renderContext;

    // Resources

    private Camera camera;

    private LongBuffer lp;
    private IntBuffer ip;

    public Renderer(Window window) {
        this.window = window;
        this.renderContextOutOfDate = false;
        this.maxInFlightFrameCount = 2;
        this.historyImageCount = 2;

        this.context = new Context(window);
        this.memoryManager = new MemoryManager(this.context);
        this.shaderManager = new ShaderManager(this.context);
        this.commandPool = new CommandPool(this.context, this.context.getGraphicsQueueFamilyIndex());
        this.descriptorPool = new DescriptorPool(this.context, this.maxInFlightFrameCount);
        this.swapchain = new Swapchain(this.context, this.window.getWidth(), this.window.getHeight());
        this.renderContext = new RenderContext(this.context, this.swapchain, this.commandPool, this.maxInFlightFrameCount);

        this.camera = new Camera();

        this.lp = MemoryUtil.memAllocLong(1);
        this.ip = MemoryUtil.memAllocInt(1);
    }

    public void cleanup() {
        vkDeviceWaitIdle(this.context.getDevice());

        MemoryUtil.memFree(this.lp);
        MemoryUtil.memFree(this.ip);

        this.renderContext.cleanup();
        this.swapchain.cleanup();
        this.descriptorPool.cleanup();
        this.commandPool.cleanup();
        this.shaderManager.cleanup();
        this.memoryManager.cleanup();
        this.context.cleanup();
    }

    private boolean tryRender() {
        if (!this.renderContext.beginRender()) return false;

        this.camera.startFrame();
//        this.camera.writeUniformBuffer(this.renderContext.getActiveInFlightFrameIndex(), this.highFrequencyUniformBuffer);

        VkCommandBuffer cmd = this.renderContext.getActiveCommandBuffer();
//        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, this.geometryPipeline.getPipeline());
//        this.lp.put(0, this.descriptorSetManager.getHFDescriptorSet());
//        this.ip.put(0, this.highFrequencyUniformBuffer.getBufferSize() * this.renderContext.getActiveFrameResourceIndex());
//        vkCmdBindDescriptorSets(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, this.geometryPipeline.getPipelineLayout(),
//                0, this.lp, this.ip);
//        vkCmdDraw(cmd, 3, 1, 0, 0);

        this.camera.endFrame();

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
                this.renderContext.updateSwapchain(this.swapchain);

                // Reset out of date flag
                this.renderContextOutOfDate = false;
            }
        }
    }

    public RenderCamera getActiveCamera() {
        return this.camera;
    }
}