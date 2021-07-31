package renderer.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK11.*;

public class Swapchain {

    private Context ctx;
    private long swapchain;
    private long[] images;
    private long[] imageViews;
    private int surfaceColorFormat;
    private int surfaceColorSpace;
    private VkExtent2D.Buffer extent;

    public Swapchain(Context ctx, int width, int height) {
        this.ctx = ctx;
        this.extent = VkExtent2D.calloc(1);
        create(width, height, VK_NULL_HANDLE);
    }

    public void cleanup() {
        vkDestroySwapchainKHR(this.ctx.getDevice(), this.swapchain, null);
        for (long imageView : this.imageViews) {
            vkDestroyImageView(this.ctx.getDevice(), imageView, null);
        }
        this.extent.clear();
    }

    private void create(int width, int height, long oldSwapchain) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ip = stack.mallocInt(1);
            int err;

            // Get surface capabilities
            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.callocStack(stack);
            err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.ctx.getPhysicalDevice(), this.ctx.getSurface(), surfaceCapabilities);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get device surface capatibilities.");
            }

            // Get present modes
            err = vkGetPhysicalDeviceSurfacePresentModesKHR(this.ctx.getPhysicalDevice(), this.ctx.getSurface(), ip, null);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get number of surface presentation modes.");
            }
            int presentModeCount = ip.get(0);

            IntBuffer presentModes = stack.mallocInt(presentModeCount);
            err = vkGetPhysicalDeviceSurfacePresentModesKHR(this.ctx.getPhysicalDevice(), this.ctx.getSurface(), ip, presentModes);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get surface presentation modes.");
            }

            int swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR;
            for (int i = 0; i < presentModeCount; i++) {
                if (presentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                    swapchainPresentMode = VK_PRESENT_MODE_MAILBOX_KHR;
                    break;
                }
                if (presentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                    swapchainPresentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
                    break;
                }
            }
            swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR;

            // Get surface format and color space
            err = vkGetPhysicalDeviceSurfaceFormatsKHR(this.ctx.getPhysicalDevice(), this.ctx.getSurface(), ip, null);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get number of device surface format.");
            }

            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.callocStack(ip.get(0), stack);

            err = vkGetPhysicalDeviceSurfaceFormatsKHR(this.ctx.getPhysicalDevice(), this.ctx.getSurface(), ip, surfaceFormats);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get device surface formats.");
            }

            if (surfaceFormats.capacity() == 1 && surfaceFormats.get(0).format() == VK_FORMAT_UNDEFINED) {
                this.surfaceColorFormat = VK_FORMAT_B8G8R8A8_UNORM;
                this.surfaceColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
            } else {
                this.surfaceColorFormat = surfaceFormats.get(0).format();
                this.surfaceColorSpace = surfaceFormats.get(0).colorSpace();
            }

            // Find number of images
            int desiredNumberOfImages = surfaceCapabilities.minImageCount() + 1;
            if ((surfaceCapabilities.maxImageCount() > 0) && (desiredNumberOfImages > surfaceCapabilities.maxImageCount())) {
                desiredNumberOfImages = surfaceCapabilities.maxImageCount();
            }

            // Find extent
            VkExtent2D currentExtent = surfaceCapabilities.currentExtent();
            if (currentExtent.width() != Integer.MAX_VALUE) {
                this.extent.get(0).set(currentExtent);
            } else {
                VkExtent2D min = surfaceCapabilities.minImageExtent();
                VkExtent2D max = surfaceCapabilities.maxImageExtent();
                width = Math.max(width, min.width());
                width = Math.min(width, max.width());
                height = Math.max(height, min.height());
                height = Math.min(height, max.height());
                this.extent.get(0).set(width, height);
            }

            // Find transform
            int preTransform;
            if ((surfaceCapabilities.supportedTransforms() & VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR) != 0) {
                preTransform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
            } else {
                preTransform = surfaceCapabilities.currentTransform();
            }

            // Create swapchain
            VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(this.ctx.getSurface())
                    .minImageCount(desiredNumberOfImages)
                    .imageFormat(this.surfaceColorFormat)
                    .imageColorSpace(this.surfaceColorSpace)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .preTransform(preTransform)
                    .imageArrayLayers(1)
                    .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .presentMode(swapchainPresentMode)
                    .oldSwapchain(oldSwapchain)
                    .clipped(true)
                    .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            swapchainCreateInfo.imageExtent().set(this.extent.get(0));

            LongBuffer pSwapchain = stack.mallocLong(1);
            err = vkCreateSwapchainKHR(this.ctx.getDevice(), swapchainCreateInfo, null, pSwapchain);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create swapchain.");
            }
            this.swapchain = pSwapchain.get(0);

            // Destroy previous swapchain
            if (oldSwapchain != VK_NULL_HANDLE) {
                vkDestroySwapchainKHR(this.ctx.getDevice(), oldSwapchain, null);
                for (long imageView : this.imageViews) {
                    vkDestroyImageView(this.ctx.getDevice(), imageView, null);
                }
            }

            // Recover images
            err = vkGetSwapchainImagesKHR(this.ctx.getDevice(), this.swapchain, ip, null);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get swapchain image count.");
            }

            LongBuffer pSwapchainImages = stack.mallocLong(ip.get(0));
            err = vkGetSwapchainImagesKHR(this.ctx.getDevice(), this.swapchain, ip, pSwapchainImages);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to get swapchain images.");
            }

            this.images = new long[pSwapchainImages.capacity()];
            this.imageViews = new long[pSwapchainImages.capacity()];

            LongBuffer lp = stack.mallocLong(1);
            VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .format(this.surfaceColorFormat)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D);
            imageViewCreateInfo.subresourceRange()
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .levelCount(1)
                    .layerCount(1);

            for (int i = 0; i < this.imageViews.length; i++) {
                this.images[i] = pSwapchainImages.get(i);
                imageViewCreateInfo.image(this.images[i]);
                err = vkCreateImageView(this.ctx.getDevice(), imageViewCreateInfo, null, lp);
                if (err != VK_SUCCESS) {
                    throw new IllegalStateException("Failed to create swapchain image view.");
                }
                this.imageViews[i] = lp.get(0);
            }
        }
    }

    public void recreate(int width, int height) {
        create(width, height, this.swapchain);
    }

    public int getSurfaceColorFormat() {
        return this.surfaceColorFormat;
    }
    public int getSurfaceColorSpace() {
        return this.surfaceColorSpace;
    }
    public VkExtent2D getExtent() {
        return this.extent.get(0);
    }
    public long[] getImageViews() {
        return this.imageViews;
    }
    public long getSwapchain() {
        return this.swapchain;
    }
}