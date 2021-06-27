package renderer.memory;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import renderer.engine.Context;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;

public class DescriptorPool {

    private Context ctx;

    private long descriptorPool;

    public DescriptorPool(Context context, int frameResourceCount) {
        this.ctx = context;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(1, stack)
                    .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    .descriptorCount(frameResourceCount * 1);

            VkDescriptorPoolCreateInfo info = VkDescriptorPoolCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(poolSizes)
                    .maxSets(frameResourceCount);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateDescriptorPool(this.ctx.getDevice(), info, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create descriptor pool.");
            }
            this.descriptorPool = lp.get(0);
        }
    }

    public void cleanup() {
        vkDestroyDescriptorPool(this.ctx.getDevice(), this.descriptorPool, null);
    }

    public long getDescriptorPool() {
        return this.descriptorPool;
    }
}