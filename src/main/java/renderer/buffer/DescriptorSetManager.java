package renderer.buffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import renderer.core.Context;
import renderer.core.DescriptorPool;
import renderer.core.RenderContext;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class DescriptorSetManager {

    private Context ctx;

    private DescriptorPool HFPool;
    private long HFDescriptorSet;
    private long HFDescriptorSetLayout;

    public DescriptorSetManager(Context context, HighFrequencyUniformBuffer HFUB, int maxInFlightFrameCount) {
        this.ctx = context;
        this.HFPool = new DescriptorPool(this.ctx, maxInFlightFrameCount);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Create high frequency descriptor set layout
            VkDescriptorSetLayoutBinding.Buffer binding = VkDescriptorSetLayoutBinding.callocStack(1, stack)
                    .binding(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                    .descriptorCount(1)
                    .stageFlags(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo info = VkDescriptorSetLayoutCreateInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                    .pBindings(binding);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateDescriptorSetLayout(this.ctx.getDevice(), info, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create descriptor set layout.");
            }
            this.HFDescriptorSetLayout = lp.get(0);

            // Create high frequency descriptor sets
            LongBuffer pLayouts = stack.longs(this.HFDescriptorSetLayout);

            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                    .descriptorPool(this.HFPool.getDescriptorPool())
                    .pSetLayouts(pLayouts);

            err = vkAllocateDescriptorSets(this.ctx.getDevice(), allocateInfo, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create descriptor set.");
            }
            this.HFDescriptorSet = lp.get(0);

            // Write descriptor set
            VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack)
                    .buffer(HFUB.getBuffer())
                    .offset(0)
                    .range(HFUB.getBufferSize());

            VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.callocStack(1, stack)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(0)
                    .descriptorCount(1)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                    .dstSet(this.HFDescriptorSet)
                    .pBufferInfo(descriptorBufferInfo);

            vkUpdateDescriptorSets(this.ctx.getDevice(), writeDescriptorSet, null);
        }
    }

    public void cleanup() {
        // Destroy layouts
        vkDestroyDescriptorSetLayout(this.ctx.getDevice(), this.HFDescriptorSetLayout, null);
        // Destroy descriptor pool
        this.HFPool.cleanup();
    }

    public long getHFDescriptorSetLayout() {
        return this.HFDescriptorSetLayout;
    }

    public long getHFDescriptorSet() {
        return this.HFDescriptorSet;
    }
}