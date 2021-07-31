package renderer.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK11.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;

public class RenderContext {

    private Context ctx;
    private Swapchain swapchain;

//    private long[] framebuffers;
    private VkCommandBuffer[] commandBuffers;
    private long[] imageAvailableSemaphores;
    private long[] renderFinishedSemaphores;
    private long[] inFlightFences;

    private int maxInFlightFrameCount;
    private int activeInFlightFrameIndex;
    private int activeSwapchainImageIndex;

    private IntBuffer ip;
    private LongBuffer lp0;
    private LongBuffer lp1;
    private PointerBuffer pp;
    private VkCommandBufferBeginInfo.Buffer commandBufferBeginInfo;
//    private VkRenderPassBeginInfo.Buffer renderPassBeginInfo;
    private VkSubmitInfo.Buffer submitInfo;
    private VkPresentInfoKHR.Buffer presentInfo;

//    private VkOffset2D.Buffer offset2D;
//    private VkRect2D.Buffer scissor;
//    private VkViewport.Buffer viewport;
//    private VkClearColorValue.Buffer clearColorValue;
//    private VkClearValue.Buffer clearValue;

    public RenderContext(Context context, Swapchain swapchain, CommandPool commandPool, int maxInFlightFrameCount) {

        this.ctx = context;
        this.swapchain = swapchain;

        this.maxInFlightFrameCount = maxInFlightFrameCount;
        this.activeInFlightFrameIndex = 0;
        this.activeSwapchainImageIndex = 0;

        // Allocate nio memory
        this.ip = MemoryUtil.memAllocInt(1);
        this.pp = MemoryUtil.memAllocPointer(1);
        this.lp0 = MemoryUtil.memAllocLong(1);
        this.lp1 = MemoryUtil.memAllocLong(1);
        this.commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(1);
//        this.renderPassBeginInfo = VkRenderPassBeginInfo.calloc(1);
        this.submitInfo = VkSubmitInfo.calloc(1);
        this.presentInfo = VkPresentInfoKHR.calloc(1);

//        this.offset2D = VkOffset2D.calloc(1);
//        this.scissor = VkRect2D.calloc(1);
//        this.viewport = VkViewport.calloc(1);
//        this.clearColorValue = VkClearColorValue.calloc(1);
//        this.clearValue = VkClearValue.calloc(1);

        // Allocate handles
        this.imageAvailableSemaphores = new long[this.maxInFlightFrameCount];
        this.renderFinishedSemaphores = new long[this.maxInFlightFrameCount];
        this.inFlightFences = new long[this.maxInFlightFrameCount];

        // Create command buffers
        this.commandBuffers = commandPool.createCommandBuffers(this.maxInFlightFrameCount);

        create();
    }

    public void cleanup() {
        destroy();

        // Free nio memory
        MemoryUtil.memFree(this.ip);
        MemoryUtil.memFree(this.pp);
        MemoryUtil.memFree(this.lp0);
        MemoryUtil.memFree(this.lp1);
        this.commandBufferBeginInfo.free();
//        this.renderPassBeginInfo.free();
        this.submitInfo.free();
        this.presentInfo.free();

//        this.offset2D.free();
//        this.scissor.free();
//        this.viewport.free();
//        this.clearColorValue.free();
//        this.clearValue.free();
    }

    private void create() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Create framebuffers
//            long[] imageViews = this.swapchain.getImageViews();
//            this.framebuffers = new long[imageViews.length];

//            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack)
//                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
//                    .height(this.swapchain.getExtent().height())
//                    .width(this.swapchain.getExtent().width())
//                    .layers(1)
//                    .renderPass(this.renderPass.getRenderPass());

//            for (int i = 0; i < imageViews.length; i++) {
//                framebufferInfo.pAttachments(this.lp0.put(0, imageViews[i]));
//                int err = vkCreateFramebuffer(this.ctx.getDevice(), framebufferInfo, null, this.lp1);
//                if (err != VK_SUCCESS) {
//                    throw new IllegalStateException("Failed to create framebuffer.");
//                }
//                this.framebuffers[i] = lp1.get(0);
//            }

            // Create synchronization objects
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(VK_FENCE_CREATE_SIGNALED_BIT);
            for (int i = 0; i < this.maxInFlightFrameCount; i++) {
                vkCreateSemaphore(this.ctx.getDevice(), semaphoreInfo, null, this.lp0);
                this.imageAvailableSemaphores[i] = this.lp0.get(0);
                vkCreateSemaphore(this.ctx.getDevice(), semaphoreInfo, null, this.lp0);
                this.renderFinishedSemaphores[i] = this.lp0.get(0);
                vkCreateFence(this.ctx.getDevice(), fenceInfo, null, this.lp0);
                this.inFlightFences[i] = this.lp0.get(0);
            }

            // Create viewport, scissor and clear colors
//            this.offset2D
//                    .x(0)
//                    .y(0);
//            this.scissor
//                    .offset(this.offset2D.get(0))
//                    .extent(this.swapchain.getExtent());
//            this.viewport
//                    .x(0).y(0)
//                    .width(this.swapchain.getExtent().width())
//                    .height(this.swapchain.getExtent().height())
//                    .minDepth(0)
//                    .maxDepth(0);
//            this.clearColorValue
//                    .float32(0, 0.5f)
//                    .float32(1, 0.0f)
//                    .float32(2, 0.0f)
//                    .float32(3, 0.0f);
//            this.clearValue.color(this.clearColorValue.get(0));
        }
    }

    private void destroy() {
        vkDeviceWaitIdle(this.ctx.getDevice());

        for (long fence : this.inFlightFences) {
            vkDestroyFence(this.ctx.getDevice(), fence, null);
        }
        for (long semaphore : this.renderFinishedSemaphores) {
            vkDestroySemaphore(this.ctx.getDevice(), semaphore, null);
        }
        for (long semaphore : this.imageAvailableSemaphores) {
            vkDestroySemaphore(this.ctx.getDevice(), semaphore, null);
        }
//        for (long framebuffer : this.framebuffers) {
//            vkDestroyFramebuffer(this.ctx.getDevice(), framebuffer, null);
//        }
    }

    public void updateSwapchain(Swapchain swapchain) {
        vkDeviceWaitIdle(this.ctx.getDevice());
        this.swapchain = swapchain;
    }

    public boolean beginRender() {
        // Acquire next frame resource
        long inFlightFence = this.inFlightFences[this.activeInFlightFrameIndex];
        long imageAvailableSemaphore = this.imageAvailableSemaphores[this.activeInFlightFrameIndex];

        if (vkWaitForFences(this.ctx.getDevice(), inFlightFence, true, Long.MAX_VALUE) != VK_SUCCESS) {
            throw new IllegalStateException("Failed to wait fence.");
        }
        vkResetFences(this.ctx.getDevice(), inFlightFence);

        int err = vkAcquireNextImageKHR(this.ctx.getDevice(), this.swapchain.getSwapchain(), Long.MAX_VALUE,
                imageAvailableSemaphore, VK_NULL_HANDLE, this.ip);
        if (err != VK_SUCCESS) {
            if (err == VK_ERROR_OUT_OF_DATE_KHR) {
                return false;
            } else {
                throw new IllegalStateException("Failed to acquire next image KHR.");
            }
        }
        this.activeSwapchainImageIndex = this.ip.get(0);

        // Record the beginning of command buffer
        VkCommandBuffer commandBuffer = getActiveCommandBuffer();
        vkResetCommandBuffer(commandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);

        this.commandBufferBeginInfo
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
                .pInheritanceInfo(null);
        err = vkBeginCommandBuffer(commandBuffer, this.commandBufferBeginInfo.get(0));
        if (err != VK_SUCCESS) {
            throw new IllegalStateException("Failed to begin command buffer.");
        }

//        vkCmdSetScissor(commandBuffer, 0, this.scissor);
//        vkCmdSetViewport(commandBuffer, 0, this.viewport);
//
//        this.renderPassBeginInfo
//                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
//                .renderPass(this.renderPass.getRenderPass())
//                .framebuffer(this.framebuffers[this.currentSwapchainImageIndex])
//                .renderArea(this.scissor.get(0))
//                .pClearValues(this.clearValue);
//        vkCmdBeginRenderPass(commandBuffer, this.renderPassBeginInfo.get(0), VK_SUBPASS_CONTENTS_INLINE);

        return true;
    }

    public boolean endRender() {
        // Record end command buffer
        VkCommandBuffer commandBuffer = getActiveCommandBuffer();
//        vkCmdEndRenderPass(commandBuffer);
        vkEndCommandBuffer(commandBuffer);

        // Submit command buffer
        long inFlightFence = this.inFlightFences[this.activeInFlightFrameIndex];
        long imageAvailableSemaphore = this.imageAvailableSemaphores[this.activeInFlightFrameIndex];
        long renderFinishedSemaphore = this.renderFinishedSemaphores[this.activeInFlightFrameIndex];

        this.submitInfo
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .waitSemaphoreCount(1)
                .pWaitSemaphores(this.lp0.put(0, imageAvailableSemaphore))
                .pWaitDstStageMask(this.ip.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(this.pp.put(0, commandBuffer))
                .pSignalSemaphores(this.lp1.put(0, renderFinishedSemaphore));

        int err = vkQueueSubmit(this.ctx.getGraphicsQueue(), this.submitInfo, inFlightFence);
        if (err != VK_SUCCESS) {
            throw new IllegalStateException("Failed to submit draw command buffer.");
        }

        this.presentInfo
                .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pWaitSemaphores(this.lp0.put(0, renderFinishedSemaphore))
                .swapchainCount(1)
                .pSwapchains(this.lp1.put(0, this.swapchain.getSwapchain()))
                .pImageIndices(this.ip.put(0, this.activeSwapchainImageIndex))
                .pResults(null);

        err = vkQueuePresentKHR(this.ctx.getPresentQueue(), this.presentInfo.get(0));
        if (err != VK_SUCCESS) {
            if (err == VK_SUBOPTIMAL_KHR) {
                return false;
            } else {
                return false;
            }
        }

        this.activeInFlightFrameIndex = (this.activeInFlightFrameIndex + 1) % this.maxInFlightFrameCount;

        return true;
    }

    public VkCommandBuffer getActiveCommandBuffer() {
        return this.commandBuffers[this.activeInFlightFrameIndex];
    }

    public int getActiveInFlightFrameIndex() {
        return this.activeInFlightFrameIndex;
    }

    public int getActiveSwapchainImageIndex() {
        return this.activeSwapchainImageIndex;
    }
}