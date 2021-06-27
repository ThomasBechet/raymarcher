package renderer.engine;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;

public abstract class Pipeline {

    protected Context ctx;
    protected RenderPass renderPass;

    private long pipelineLayout;
    private long pipeline;

    public Pipeline(Context context, RenderPass renderPass) {
        this.ctx = context;
        this.renderPass = renderPass;
        this.pipelineLayout = VK_NULL_HANDLE;
        this.pipeline = VK_NULL_HANDLE;
    }

    public void cleanup() {
        vkDestroyPipeline(this.ctx.getDevice(), this.pipeline, null);
        vkDestroyPipelineLayout(this.ctx.getDevice(), this.pipelineLayout, null);
        this.pipeline = VK_NULL_HANDLE;
        this.pipelineLayout = VK_NULL_HANDLE;
    }

    protected void createPipelineLayout(LongBuffer descriptorSetLayouts) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Create pipeline layout
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pPushConstantRanges(null)
                    .pSetLayouts(descriptorSetLayouts);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreatePipelineLayout(this.ctx.getDevice(), pipelineLayoutInfo, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create pipeline layout.");
            }
            this.pipelineLayout = lp.get(0);
        }
    }

    protected void createPipeline(
        VkPipelineShaderStageCreateInfo.Buffer shaderStageInfo,
        VkViewport.Buffer viewports,
        VkRect2D.Buffer scissors
    ) {
        if (this.pipelineLayout == VK_NULL_HANDLE) {
            throw new IllegalStateException("Failed to create pipeline: pipeline layout was not created.");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Create vertex input state
            VkPipelineVertexInputStateCreateInfo vertexInfoState = VkPipelineVertexInputStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                    .pVertexBindingDescriptions(null)
                    .pVertexAttributeDescriptions(null);

            // Create input assembly state
            VkPipelineInputAssemblyStateCreateInfo inputAssemblyState = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

            // Create viewport state
            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .pViewports(viewports)
                    .pScissors(scissors);

            // Create rasterization state
            VkPipelineRasterizationStateCreateInfo rasterizationState = VkPipelineRasterizationStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .polygonMode(VK_POLYGON_MODE_FILL)
                    .cullMode(VK_CULL_MODE_NONE)
                    .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                    .lineWidth(1.0f);

            // Create multisampling state
            VkPipelineMultisampleStateCreateInfo multisamplingState = VkPipelineMultisampleStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
                    .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

            // Create color blend state
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachmentState = VkPipelineColorBlendAttachmentState.callocStack(1, stack)
                    .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
                    .blendEnable(false);

            VkPipelineColorBlendStateCreateInfo colorBlendState = VkPipelineColorBlendStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(false)
                    .logicOp(VK_LOGIC_OP_COPY)
                    .pAttachments(colorBlendAttachmentState);

            // Create dynamic states
            VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
            int dynamicFlags = 0x0;
            if (viewports == null) dynamicFlags |= VK_DYNAMIC_STATE_VIEWPORT;
            if (scissors == null) dynamicFlags |= VK_DYNAMIC_STATE_SCISSOR;
            dynamicState.flags(dynamicFlags);

            // Create pipeline
            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack)
                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(shaderStageInfo)
                    .pVertexInputState(vertexInfoState)
                    .pInputAssemblyState(inputAssemblyState)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizationState)
                    .pMultisampleState(multisamplingState)
                    .pColorBlendState(colorBlendState)
                    .layout(this.pipelineLayout)
                    .renderPass(this.renderPass.getRenderPass())
                    .subpass(0)
                    .pDynamicState(dynamicState)
                    .basePipelineHandle(VK_NULL_HANDLE);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateGraphicsPipelines(this.ctx.getDevice(), VK_NULL_HANDLE, pipelineInfo, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create pipeline");
            }
            this.pipeline = lp.get(0);
        }
    }

    public long getPipeline() {
        return this.pipeline;
    }

    public long getPipelineLayout() {
        return this.pipelineLayout;
    }
}