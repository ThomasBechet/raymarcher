package renderer.engine;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import window.Window;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK11.*;

public class Context {

    private static final boolean ENABLE_VALIDATION_LAYER = true;

    private Window window;

    private VkInstance instance;
    private long debugReportCallback;
    private VkPhysicalDevice physicalDevice;
    private long surface;
    private int graphicsQueueFamilyIndex;
    private int presentQueueFamilyIndex;
    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;

    private final static VkDebugReportCallbackEXT reportCallbackFunction = VkDebugReportCallbackEXT.create((flags, objectType, object, location, messageCode, pLayerPrefix, pMessage, pUserData) ->
        {
            String type;
            if ((flags & VK_DEBUG_REPORT_INFORMATION_BIT_EXT) != 0) {
                type = "INFORMATION";
            } else if ((flags & VK_DEBUG_REPORT_WARNING_BIT_EXT) != 0) {
                type = "WARNING";
            } else if ((flags & VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT) != 0) {
                type = "PERFORMANCE WARNING";
            } else if ((flags & VK_DEBUG_REPORT_ERROR_BIT_EXT) != 0) {
                type = "ERROR";
            } else if ((flags & VK_DEBUG_REPORT_DEBUG_BIT_EXT) != 0) {
                type = "DEBUG";
            } else {
                type = "UNKNOWN";
            }

            String layerPrefix = memASCII(pLayerPrefix);
            String message     = VkDebugReportCallbackEXT.getString(pMessage);
            System.err.format("%s: [%s] Code %d : %s\n", type, layerPrefix, messageCode, message);

            return VK_FALSE;
        }
    );

    private static PointerBuffer getRequiredInstanceLayers(MemoryStack stack) {
        String[] requiredLayers;
        if (ENABLE_VALIDATION_LAYER) {
            requiredLayers = new String[] {
                    "VK_LAYER_KHRONOS_validation"
            };
        } else {
            requiredLayers = new String[] {};
        }
        PointerBuffer layers = stack.mallocPointer(requiredLayers.length);
        for (int i = 0; i < requiredLayers.length; i++) {
            layers.put(0, stack.ASCII(requiredLayers[i]));
        }
        return layers;
    }

    private static PointerBuffer getRequiredInstanceExtensions(MemoryStack stack) {
        String[] requiredInstanceExtensions;
        if (ENABLE_VALIDATION_LAYER) {
            requiredInstanceExtensions = new String[] {
                    VK_EXT_DEBUG_REPORT_EXTENSION_NAME
            };
        } else {
            requiredInstanceExtensions = new String[] {};
        }

        PointerBuffer glfwInstanceExtensions = glfwGetRequiredInstanceExtensions();

        int extensionCount = requiredInstanceExtensions.length + glfwInstanceExtensions.capacity();

        PointerBuffer instanceExtensions = stack.mallocPointer(extensionCount);
        for (String extension : requiredInstanceExtensions) {
            instanceExtensions.put(stack.ASCII(extension));
        }
        instanceExtensions.put(glfwInstanceExtensions);

        return instanceExtensions;
    }

    private static PointerBuffer getRequiredDeviceExtensions(MemoryStack stack) {
        String[] requiredLayers = new String[] {
                VK_KHR_SWAPCHAIN_EXTENSION_NAME,
                "VK_KHR_maintenance1"
        };
        PointerBuffer layers = stack.mallocPointer(requiredLayers.length);
        for (int i = 0; i < requiredLayers.length; i++) {
            layers.put(stack.ASCII(requiredLayers[i]));
        }
        layers.flip();
        return layers;
    }

    private static VkPhysicalDeviceFeatures getRequiredDeviceFeatures(MemoryStack stack) {
        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.callocStack(stack);
        features.shaderClipDistance(true);
        return features;
    }

    private static boolean isPhysicalDeviceSuitable(MemoryStack stack, VkPhysicalDevice physicalDevice) {
        IntBuffer ip = stack.mallocInt(1);

        // Check has extensions
        int err = vkEnumerateDeviceExtensionProperties(physicalDevice, (String)null, ip, null);
        if (err != VK_SUCCESS) {
            throw new IllegalStateException("Failed to enumerate device extension properties.");
        }
        if (ip.get(0) > 0) {
            // Get available extensions
            VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(ip.get(0), stack);
            err = vkEnumerateDeviceExtensionProperties(physicalDevice, (String)null, ip, deviceExtensions);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed enumerate device extension properties.");
            }

            // Get required extensions
            PointerBuffer requiredDeviceExtensions = getRequiredDeviceExtensions(stack);
            for (int i = 0; i < requiredDeviceExtensions.capacity(); i++) {
                String requiredDeviceExtension = memASCII(requiredDeviceExtensions.get(i));
                // Check required extensions available
                boolean extensionFound = false;
                for (int j = 0; j < deviceExtensions.capacity(); j++) {
                    if (deviceExtensions.get(j).extensionNameString().equals(requiredDeviceExtension)) {
                        extensionFound = true;
                        break;
                    }
                }
                if (!extensionFound) {
                    return false;
                }
            }

            // All required extensions have been found
            return true;
        } else {
            return false;
        }
    }

    private void createInstance() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            // Required layers
            PointerBuffer requiredLayers = getRequiredInstanceLayers(stack);

            // Required Extensions
            PointerBuffer requiredInstanceExtensions = getRequiredInstanceExtensions(stack);

            // Create app
            ByteBuffer appShortName = stack.UTF8("Vulkan app");
            VkApplicationInfo applicationInfo = VkApplicationInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pNext(NULL)
                    .pApplicationName(appShortName)
                    .applicationVersion(0)
                    .pEngineName(appShortName)
                    .engineVersion(0)
                    .apiVersion(VK_API_VERSION_1_0);

            requiredInstanceExtensions.flip();
            VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(NULL)
                    .flags(0)
                    .pApplicationInfo(applicationInfo)
                    .ppEnabledLayerNames(requiredLayers)
                    .ppEnabledExtensionNames(requiredInstanceExtensions);

            int error = vkCreateInstance(instanceInfo, null, pp);
            if (error == VK_ERROR_EXTENSION_NOT_PRESENT) {
                throw new IllegalStateException("Missing instance extension, failed to create instance.");
            } else if (error == VK_ERROR_LAYER_NOT_PRESENT) {
                throw new IllegalStateException("Missing instance layer, failed to create instance.");
            } else if (error != 0) {
                throw new IllegalStateException("Failed to create instance.");
            }

            this.instance = new VkInstance(pp.get(0), instanceInfo);
        }
    }

    private void createDebugReportCallback() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (ENABLE_VALIDATION_LAYER) {
                LongBuffer lp = stack.mallocLong(1);

                VkDebugReportCallbackCreateInfoEXT debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.mallocStack(stack)
                        .sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
                        .pNext(NULL)
                        .flags(VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT)
                        .pfnCallback(reportCallbackFunction)
                        .pUserData(NULL);

                int error = vkCreateDebugReportCallbackEXT(this.instance, debugCreateInfo, null, lp);
                if (error == VK_SUCCESS) {
                    this.debugReportCallback = lp.get(0);
                } else {
                    throw new IllegalStateException("Failed to create debug report callback.");
                }
            }
        }
    }

    private void pickPhysicalDevice() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ip = stack.mallocInt(1);

            int err = vkEnumeratePhysicalDevices(this.instance, ip, null);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to enumerate physical devices.");
            }
            if (ip.get(0) > 0) {
                PointerBuffer physicalDevices = stack.mallocPointer(ip.get(0));
                vkEnumeratePhysicalDevices(this.instance, ip, physicalDevices);

                for (int i = 0; i < physicalDevices.capacity(); i++) {
                    VkPhysicalDevice physicalDevice = new VkPhysicalDevice(physicalDevices.get(i), this.instance);
                    if (isPhysicalDeviceSuitable(stack, physicalDevice)) {
                        this.physicalDevice = physicalDevice;
                        break;
                    }
                }

                if (this.physicalDevice == null) {
                    throw new IllegalStateException("Failed to find suitable physical device.");
                }
            } else {
                throw new IllegalStateException("No physical device accessible.");
            }
        }
    }

    private void pickQueueFamilyIndices() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ip = stack.mallocInt(1);

            // Initialize values
            this.graphicsQueueFamilyIndex = Integer.MAX_VALUE;
            this.presentQueueFamilyIndex = Integer.MAX_VALUE;

            // Get queue family properties
            vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, ip, null);
            if (ip.get(0) > 0) {
                int queueFamilyCount = ip.get(0);
                VkQueueFamilyProperties.Buffer queueFamilyProperties = VkQueueFamilyProperties.mallocStack(queueFamilyCount);
                vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, ip, queueFamilyProperties);

                for (int i = 0; i < queueFamilyCount; i++) {
                    if (queueFamilyProperties.get(i).queueCount() > 0 && (queueFamilyProperties.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                        this.graphicsQueueFamilyIndex = i;
                    }

                    vkGetPhysicalDeviceSurfaceSupportKHR(this.physicalDevice, i, this.surface, ip);
                    if (queueFamilyProperties.get(i).queueCount() > 0 && ip.get(0) == VK_TRUE) {
                        this.presentQueueFamilyIndex = i;
                    }

                    if (this.graphicsQueueFamilyIndex != Integer.MAX_VALUE && this.presentQueueFamilyIndex != Integer.MAX_VALUE) {
                        break;
                    }
                }
            } else {
                throw new IllegalStateException("Failed to find queue family properties.");
            }

            if (this.graphicsQueueFamilyIndex == Integer.MAX_VALUE) {
                throw new IllegalStateException("Failed to find graphics queue family index.");
            }
            if (this.presentQueueFamilyIndex == Integer.MAX_VALUE) {
                throw new IllegalStateException("Failed to find present queue family index.");
            }
        }
    }

    private void createDevice() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.mallocStack(2, stack);

            queueCreateInfo.position(0);
            queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            queueCreateInfo.pNext(NULL);
            queueCreateInfo.flags(0);
            queueCreateInfo.queueFamilyIndex(this.graphicsQueueFamilyIndex);
            queueCreateInfo.pQueuePriorities(stack.floats(0.0f));

            queueCreateInfo.position(1);
            queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            queueCreateInfo.pNext(NULL);
            queueCreateInfo.flags(0);
            queueCreateInfo.queueFamilyIndex(this.presentQueueFamilyIndex);
            queueCreateInfo.pQueuePriorities(stack.floats(0.0f));

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pNext(NULL)
                    .flags(0)
                    .pQueueCreateInfos(queueCreateInfo)
                    .ppEnabledLayerNames(null)
                    .ppEnabledExtensionNames(getRequiredDeviceExtensions(stack))
                    .pEnabledFeatures(getRequiredDeviceFeatures(stack));

            int err = vkCreateDevice(this.physicalDevice, createInfo, null, pp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create device.");
            }
            this.device = new VkDevice(pp.get(0), this.physicalDevice, createInfo);
        }
    }

    public void pickQueues() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            vkGetDeviceQueue(this.device, this.graphicsQueueFamilyIndex, 0, pp);
            this.graphicsQueue = new VkQueue(pp.get(0), this.device);

            vkGetDeviceQueue(this.device, this.presentQueueFamilyIndex, 0, pp);
            this.presentQueue = new VkQueue(pp.get(0), this.device);
        }
    }

    public Context(Window window) {
        this.window = window;

        createInstance();
        this.surface = this.window.createWindowSurfaceWSI(this.instance);
        createDebugReportCallback();
        pickPhysicalDevice();
        pickQueueFamilyIndices();
        createDevice();
        pickQueues();
    }

    public void cleanup() {
        vkDestroyDevice(this.device, null);
        if (ENABLE_VALIDATION_LAYER) {
            vkDestroyDebugReportCallbackEXT(this.instance, this.debugReportCallback, null);
        }
        vkDestroySurfaceKHR(this.instance, this.surface, null);
        vkDestroyInstance(this.instance, null);
    }

    public VkInstance getInstance() {
        return this.instance;
    }
    public long getSurface() {
        return this.surface;
    }
    public VkPhysicalDevice getPhysicalDevice() {
        return this.physicalDevice;
    }
    public VkDevice getDevice() {
        return this.device;
    }
    public VkQueue getGraphicsQueue() {
        return this.graphicsQueue;
    }
    public int getGraphicsQueueFamilyIndex() {
        return this.graphicsQueueFamilyIndex;
    }
    public VkQueue getPresentQueue() {
        return this.presentQueue;
    }
    public int getPresentQueueFamilyIndex() {
        return this.presentQueueFamilyIndex;
    }
}