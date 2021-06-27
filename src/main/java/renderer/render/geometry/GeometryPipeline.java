package renderer.render.geometry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import renderer.engine.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;

public class GeometryPipeline extends Pipeline {

    private ShaderManager shaderManager;
    private Swapchain swapchain;

    private long vertexModule;
    private long fragmentModule;
    private long descriptorSetLayout;

    private VkPipelineShaderStageCreateInfo.Buffer shaderStageInfo;
    private ByteBuffer shaderEntryPointName;

    private VkViewport.Buffer viewports;
    private VkRect2D.Buffer scissors;

    public GeometryPipeline(Context context, RenderPass renderPass, ShaderManager shaderManager, Swapchain swapchain) {
        super(context, renderPass);

        this.shaderManager = shaderManager;
        this.swapchain = swapchain;

        // Create shaders
        this.vertexModule = this.shaderManager.createShaderModule("test.vert", VK_SHADER_STAGE_VERTEX_BIT);
        this.fragmentModule = this.shaderManager.createShaderModule("test.frag", VK_SHADER_STAGE_FRAGMENT_BIT);

        // Create shader stage info
        this.shaderStageInfo = VkPipelineShaderStageCreateInfo.calloc(2);
        this.shaderEntryPointName = MemoryUtil.memUTF8("main");

        shaderStageInfo.get(0).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        shaderStageInfo.get(0).stage(VK_SHADER_STAGE_VERTEX_BIT);
        shaderStageInfo.get(0).module(this.vertexModule);
        shaderStageInfo.get(0).pName(this.shaderEntryPointName);

        shaderStageInfo.get(1).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        shaderStageInfo.get(1).stage(VK_SHADER_STAGE_FRAGMENT_BIT);
        shaderStageInfo.get(1).module(this.fragmentModule);
        shaderStageInfo.get(1).pName(this.shaderEntryPointName);

        // Create viewport and scissor
        this.viewports = VkViewport.calloc(1)
                .x(0.0f)
                .y(this.swapchain.getExtent().height())
                .width(this.swapchain.getExtent().width())
                .height(-this.swapchain.getExtent().height())
                .minDepth(0.0f)
                .maxDepth(1.0f);

        this.scissors = VkRect2D.calloc(1)
                .extent(this.swapchain.getExtent());
        scissors.offset().set(0, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {



            // Create pipeline layout and pipeline
            createPipelineLayout(stack.longs(this.descriptorSetLayout));
            createPipeline(this.shaderStageInfo, this.viewports, this.scissors);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        vkDestroyDescriptorSetLayout(this.ctx.getDevice(), this.descriptorSetLayout, null);

        this.shaderStageInfo.free();
        MemoryUtil.memFree(this.shaderEntryPointName);

        this.viewports.free();
        this.scissors.free();

        this.shaderManager.destroyShaderModule(this.vertexModule);
        this.shaderManager.destroyShaderModule(this.fragmentModule);
    }

    public void updateSwapchain(Swapchain swapchain) {
        this.swapchain = swapchain;

        super.cleanup();
        this.viewports
                .x(0.0f)
                .y(this.swapchain.getExtent().height())
                .width(this.swapchain.getExtent().width())
                .height(-this.swapchain.getExtent().height())
                .minDepth(0.0f)
                .maxDepth(1.0f);

        this.scissors.extent(this.swapchain.getExtent());
        scissors.offset().set(0, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            createPipelineLayout(stack.longs(this.descriptorSetLayout));
            createPipeline(this.shaderStageInfo, this.viewports, this.scissors);
        }
    }
}