package renderer.engine;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDescription;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK11.*;

public class RenderPass {

    private Context ctx;
    private Swapchain swapchain;
    private long renderPass;

    public RenderPass(Context context, Swapchain swapchain) {
        this.ctx = context;
        this.swapchain = swapchain;

        create();
    }

    public void cleanup() {
        destroy();
    }

    private void create() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack)
                    .format(this.swapchain.getSurfaceColorFormat())
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                    .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference.Buffer colorRef = VkAttachmentReference.callocStack(1, stack)
                    .attachment(0)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack)
                    .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(colorRef.capacity())
                    .pColorAttachments(colorRef);

            VkRenderPassCreateInfo info = VkRenderPassCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subpass);

            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateRenderPass(this.ctx.getDevice(), info, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create render pass.");
            }
            this.renderPass = lp.get();
        }
    }

    private void destroy() {
        vkDestroyRenderPass(this.ctx.getDevice(), this.renderPass, null);
    }

    public void updateSwapchain(Swapchain swapchain) {
        destroy();
        this.swapchain = swapchain;
        create();
    }

    public long getRenderPass() {
        return this.renderPass;
    }
}