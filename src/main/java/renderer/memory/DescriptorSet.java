package renderer.memory;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import renderer.engine.Context;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class DescriptorSet {

    private Context ctx;

    private long[] descriptorSets;
    private long descriptorSetLayout;

    public DescriptorSet(Context context, DescriptorPool pool) {
        this.ctx = context;

        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Create descriptor set layout
            VkDescriptorSetLayoutBinding.Buffer binding = VkDescriptorSetLayoutBinding.callocStack(1, stack)
                    .binding(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
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
            this.descriptorSetLayout = lp.get(0);

            // Create descriptor sets
//            LongBuffer pLayouts = stack.longs(this.descriptorSetLayout);
//
//            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
//                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
//                    .descriptorPool(pool.getDescriptorPool())
//                    .pSetLayouts(pLayouts);
//
//            err = vkAllocateDescriptorSets(this.ctx.getDevice(), allocateInfo, lp);
//            if (err != VK_SUCCESS) {
//                throw new IllegalStateException("Failed to create descriptor set.");
//            }
//            this.lowFrequencyDescriptorSet = lp.get(0);
        }
    }

    public void cleanup() {
        vkDestroyDescriptorSetLayout(this.ctx.getDevice(), this.descriptorSetLayout, null);
    }

//    public long getLowFrequencyDescriptorSet(int frameResourceIndex) {
//
//    }
//
//    public long getLowFrequencyDescriptorSetLayout() {
//        return this.lowFrequencyDescriptorSetLayout;
//    }
}