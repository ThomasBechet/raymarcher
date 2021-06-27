package renderer.engine;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;

public class CommandPool {

    private Context ctx;
    long commandPool;

    public CommandPool(Context ctx, int queueFamilyIndex) {
        this.ctx = ctx;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queueFamilyIndex);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateCommandPool(ctx.getDevice(), commandPoolCreateInfo, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create commandpool.");
            }
            this.commandPool = lp.get();
        }
    }

    public void cleanup() {
        vkDestroyCommandPool(this.ctx.getDevice(), this.commandPool, null);
    }

    public VkCommandBuffer beginSingleCommandBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(this.commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pp = stack.mallocPointer(1);
            int err = vkAllocateCommandBuffers(this.ctx.getDevice(), allocateInfo, pp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to allocate command buffer.");
            }
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pp.get(), this.ctx.getDevice());

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
                    .pInheritanceInfo(null);

            err = vkBeginCommandBuffer(commandBuffer, beginInfo);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to begin command buffer.");
            }

            return commandBuffer;
        }
    }

    public void endSingleCommandBuffer(VkCommandBuffer commandBuffer, VkQueue queue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vkEndCommandBuffer(commandBuffer);

            PointerBuffer pCommandBuffers = stack.mallocPointer(1);
            pCommandBuffers.put(0, commandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .waitSemaphoreCount(0)
                    .pWaitSemaphores(null)
                    .pWaitDstStageMask(null)
                    .pSignalSemaphores(null)
                    .pCommandBuffers(pCommandBuffers);

            vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE);
            vkQueueWaitIdle(queue);
        }
    }

    public VkCommandBuffer[] createCommandBuffers(int count) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(this.commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(count);

            VkCommandBuffer[] commandBuffers = new VkCommandBuffer[count];
            PointerBuffer pp = stack.mallocPointer(count);
            int err = vkAllocateCommandBuffers(this.ctx.getDevice(), allocateInfo, pp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to allocate command buffers.");
            }
            for (int i = 0; i < count; i++) {
                commandBuffers[i] = new VkCommandBuffer(pp.get(i), this.ctx.getDevice());
            }

            return commandBuffers;
        }
    }
}