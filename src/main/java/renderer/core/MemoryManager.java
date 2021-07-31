package renderer.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.*;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK11.*;

public class MemoryManager {

    private Context ctx;
    private Map<Long, Long> allocations;
    private Map<Long, Long> addresses;
    private long vmaAllocator;

    public MemoryManager(Context ctx) {
        this.ctx = ctx;
        this.allocations = new HashMap<>();
        this.addresses = new HashMap<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.callocStack(stack)
                    .set(this.ctx.getInstance(), this.ctx.getDevice());

            VmaAllocatorCreateInfo createInfo = VmaAllocatorCreateInfo.callocStack(stack)
                    .physicalDevice(this.ctx.getPhysicalDevice())
                    .device(this.ctx.getDevice())
                    .pVulkanFunctions(vmaVulkanFunctions);

            PointerBuffer pp = stack.mallocPointer(1);
            int err = vmaCreateAllocator(createInfo, pp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create memory allocator.");
            }
            this.vmaAllocator = pp.get();
        }
    }

    public void cleanup() {
        for (long buffer : this.allocations.keySet()) {
            destroyBuffer(buffer);
        }
        vmaDestroyAllocator(this.vmaAllocator);
    }

    public long createBuffer(long size, int bufferUsage, int memoryUsage) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(bufferUsage)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .pQueueFamilyIndices(null);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.callocStack(stack)
                    .usage(memoryUsage);

            LongBuffer lp = stack.mallocLong(1);
            PointerBuffer pp = stack.mallocPointer(1);

            int err = vmaCreateBuffer(this.vmaAllocator, bufferCreateInfo, allocationCreateInfo, lp, pp, null);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create buffer.");
            }
            long buffer = lp.get();
            long allocation = pp.get();

            this.allocations.put(buffer, allocation);
            return buffer;
        }
    }

    public void destroyBuffer(long buffer) {
        if (!this.allocations.containsKey(buffer)) {
            throw new IllegalStateException("Failed to delete buffer because it is now owned by this memory manager.");
        }
        unmapBuffer(buffer);
        vmaDestroyBuffer(this.vmaAllocator, buffer, this.allocations.get(buffer));
        this.allocations.remove(buffer);
    }

    public long mapBuffer(long buffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (this.addresses.containsKey(buffer)) {
                return this.addresses.get(buffer);
            }

            PointerBuffer ppData = stack.mallocPointer(1);
            int err = vmaMapMemory(this.vmaAllocator, this.allocations.get(buffer), ppData);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to map memory.");
            }
            long address = ppData.get();
            this.addresses.put(buffer, address);

            return address;
        }
    }

    public void unmapBuffer(long buffer) {
        if (!this.allocations.containsKey(buffer)) {
            throw new IllegalStateException("Failed to find buffer to unmap.");
        }
        if (this.addresses.containsKey(buffer)) {
            vmaUnmapMemory(this.vmaAllocator, this.allocations.get(buffer));
            this.addresses.remove(buffer);
        }
    }

    public long createDeviceLocalBuffer(CommandPool submitCommandPool, VkQueue queue, int bufferUsage, int size, PointerBuffer data) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long stagingBuffer = createBuffer(size, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VMA_MEMORY_USAGE_CPU_ONLY);
            long deviceLocalBuffer = createBuffer(size, VK_BUFFER_USAGE_TRANSFER_DST_BIT | bufferUsage, VMA_MEMORY_USAGE_GPU_ONLY);

            VkCommandBuffer commandBuffer = submitCommandPool.beginSingleCommandBuffer();
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack)
                    .srcOffset(0)
                    .dstOffset(0)
                    .size(size);
            vkCmdCopyBuffer(commandBuffer, stagingBuffer, deviceLocalBuffer, copyRegion);

            submitCommandPool.endSingleCommandBuffer(commandBuffer, queue);

            destroyBuffer(stagingBuffer);

            return deviceLocalBuffer;
        }
    }
}