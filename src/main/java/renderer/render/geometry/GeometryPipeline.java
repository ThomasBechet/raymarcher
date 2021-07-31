package renderer.render.geometry;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import renderer.core.*;
import renderer.buffer.DescriptorSetManager;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;

public class GeometryPipeline extends Pipeline {

    private ShaderManager shaderManager;
    private Swapchain swapchain;
    private DescriptorSetManager descriptorSetManager;

    private long vertexModule;
    private long fragmentModule;

    private VkPipelineShaderStageCreateInfo.Buffer shaderStageInfo;
    private ByteBuffer shaderEntryPointName;

    public GeometryPipeline(Context context, RenderPass renderPass, ShaderManager shaderManager, Swapchain swapchain,
                            DescriptorSetManager descriptorSetManager
    ) {
        super(context, renderPass);

        this.shaderManager = shaderManager;
        this.swapchain = swapchain;
        this.descriptorSetManager = descriptorSetManager;

        // Create shaders
        this.vertexModule = this.shaderManager.createShaderModule("test.vert", VK_SHADER_STAGE_VERTEX_BIT);
        this.fragmentModule = this.shaderManager.createShaderModule("test.frag", VK_SHADER_STAGE_FRAGMENT_BIT);

        // Create shader stage info
        this.shaderStageInfo = VkPipelineShaderStageCreateInfo.calloc(2);
        this.shaderEntryPointName = MemoryUtil.memUTF8("main");

        this.shaderStageInfo.get(0).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        this.shaderStageInfo.get(0).stage(VK_SHADER_STAGE_VERTEX_BIT);
        this.shaderStageInfo.get(0).module(this.vertexModule);
        this.shaderStageInfo.get(0).pName(this.shaderEntryPointName);

        this.shaderStageInfo.get(1).sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        this.shaderStageInfo.get(1).stage(VK_SHADER_STAGE_FRAGMENT_BIT);
        this.shaderStageInfo.get(1).module(this.fragmentModule);
        this.shaderStageInfo.get(1).pName(this.shaderEntryPointName);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            setViewport(
                    0,
                    this.swapchain.getExtent().height(),
                    this.swapchain.getExtent().width(),
                    -this.swapchain.getExtent().height()
            );

            setScissor(
                    0,
                    0,
                    this.swapchain.getExtent().width(),
                    this.swapchain.getExtent().height()
            );

            buildPipelineLayout(stack.longs(this.descriptorSetManager.getHFDescriptorSetLayout()));
            buildPipeline(this.shaderStageInfo);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();

        this.shaderStageInfo.free();
        MemoryUtil.memFree(this.shaderEntryPointName);

        this.shaderManager.destroyShaderModule(this.vertexModule);
        this.shaderManager.destroyShaderModule(this.fragmentModule);
    }

    public void updateSwapchain(Swapchain swapchain) {
        this.swapchain = swapchain;

        setViewport(
                0,
                this.swapchain.getExtent().height(),
                this.swapchain.getExtent().width(),
                -this.swapchain.getExtent().height()
        );

        setScissor(
                0,
                0,
                this.swapchain.getExtent().width(),
                this.swapchain.getExtent().height()
        );

        try (MemoryStack stack = MemoryStack.stackPush()) {
            buildPipelineLayout(stack.longs(this.descriptorSetManager.getHFDescriptorSetLayout()));
            buildPipeline(this.shaderStageInfo);
        }
    }
}