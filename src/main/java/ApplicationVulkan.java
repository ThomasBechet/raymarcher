import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.*;
import org.lwjgl.vulkan.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import renderer.vulkan.RenderContext;
import window.GLFWWindow;
import window.WindowContext;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK11.*;

public class ApplicationVulkan {
    private static final boolean ENABLE_VALIDATION_LAYER = true;
    private static final ByteBuffer KHR_SWAPCHAIN_EXTENSION_NAME    = memASCII(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
    private static final ByteBuffer EXT_DEBUG_REPORT_EXTENSION_NAME = memASCII(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);

    private final IntBuffer  ip    = memAllocInt(1);
    private final LongBuffer lp    = memAllocLong(1);
    private final PointerBuffer pp = memAllocPointer(1);

    private PointerBuffer extensionNames = memAllocPointer(64);

    private VkInstance instance;
    private VkPhysicalDevice physicalDevice;

    private long messageCallback;

    private VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.malloc();
    private VkPhysicalDeviceFeatures physicalDeviceFeatures     = VkPhysicalDeviceFeatures.malloc();

    private VkQueueFamilyProperties.Buffer queueFamilyProperties;

    private int width  = 1600;
    private int height = 800;

    private long window;
    private long surface;

    private int graphicsQueueNodeIndex;

    private VkDevice device;
    private VkQueue queue;

    private int format;
    private int colorSpace;

    private VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.malloc();

    private long commandPool;
    private VkCommandBuffer commandBuffer;

    private long swapchain;
    private int swapchainImageCount;
//    private SwapchainBuffers[] buffers;
    private int currentBuffer;

    private VkCommandBuffer setupCommandBuffer;

    private long descriptorLayout;
    private long pipelineLayout;

    private long renderPass;
    private long pipeline;
    private LongBuffer framebuffers;

    private static void check(int errorCode) {
        if (errorCode != 0) {
            throw new IllegalStateException(String.format("Vulkan error [0x%X]", errorCode));
        }
    }

    private static void initGLFW() {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Untable to initialize GLFW.");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("Vulkan not supported.");
        }
    }

    private static PointerBuffer checkLayers(MemoryStack stack, VkLayerProperties.Buffer available, String... requiredLayers) {
        PointerBuffer required = stack.mallocPointer(requiredLayers.length);
        for (int i = 0; i < requiredLayers.length; i++) {
            boolean found = false;
            for (int j = 0; j < available.capacity(); j++) {
                available.position(j);
                if (requiredLayers[i].equals(available.layerNameString())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.err.format("Cannot find layer: %s\n", requiredLayers[i]);
                return null;
            }
            required.put(i, stack.ASCII(requiredLayers[i]));
        }
        return required;
    }

    private final VkDebugReportCallbackEXT debugFunction = VkDebugReportCallbackEXT.create((flags, objectType, object, location, messageCode, pLayerPrefix, pMessage, pUserData) -> {
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
                System.err.format("%s: [%s] Code %d : %s\n",
                        type, memASCII(pLayerPrefix), messageCode, VkDebugReportCallbackEXT.getString(pMessage));
                return VK_FALSE;
            }
    );

    private void initVulkan() {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Required layers
            PointerBuffer requiredLayers = null;
            if (ENABLE_VALIDATION_LAYER) {
                check(vkEnumerateInstanceLayerProperties(this.ip, null));
                if (this.ip.get(0) > 0) {
                    VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(this.ip.get(0), stack);
                    check(vkEnumerateInstanceLayerProperties(this.ip, availableLayers));

                    requiredLayers = checkLayers(stack, availableLayers,"VK_LAYER_KHRONOS_validation");
                    if (requiredLayers == null) {
                        requiredLayers = checkLayers(stack, availableLayers, "VK_LAYER_LUNARG_standard_validation");
                    }
                }
                if (requiredLayers == null) {
                    throw new IllegalStateException("Failed to find required validation layer.");
                }
            }

            // Required Extensions
            PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
            if (requiredExtensions == null) {
                throw new IllegalStateException("Failed to find GLFW required extensions.");
            }

            for (int i = 0; i < requiredExtensions.capacity(); i++) {
                this.extensionNames.put(requiredExtensions.get(i));
            }

            check(vkEnumerateInstanceExtensionProperties((String)null, this.ip, null));

            if (this.ip.get(0) != 0) {
                VkExtensionProperties.Buffer instanceExtensions = VkExtensionProperties.mallocStack(this.ip.get(0), stack);
                check(vkEnumerateInstanceExtensionProperties((String)null, this.ip, instanceExtensions));

                for (int i = 0; i < ip.get(0); i++) {
                    instanceExtensions.position(i);
                    if (VK_EXT_DEBUG_REPORT_EXTENSION_NAME.equals(instanceExtensions.extensionNameString())) {
                        if (ENABLE_VALIDATION_LAYER) {
                            this.extensionNames.put(EXT_DEBUG_REPORT_EXTENSION_NAME);
                        }
                    }
                }
            }

            // Create app
            ByteBuffer appShortName = stack.UTF8("Vulkan app");
            VkApplicationInfo applicationInfo = VkApplicationInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pNext(NULL)
                    .pApplicationName(appShortName)
                    .applicationVersion(0)
                    .pEngineName(appShortName)
                    .engineVersion(0)
                    .apiVersion(VK.getInstanceVersionSupported());

            this.extensionNames.flip();
            VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(NULL)
                    .flags(0)
                    .pApplicationInfo(applicationInfo)
                    .ppEnabledLayerNames(requiredLayers)
                    .ppEnabledExtensionNames(this.extensionNames);
            this.extensionNames.clear();

            VkDebugReportCallbackCreateInfoEXT debugCreateInfo;
            if (ENABLE_VALIDATION_LAYER) {
                debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.mallocStack(stack)
                        .sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
                        .pNext(NULL)
                        .flags(VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT)
                        .pfnCallback(this.debugFunction)
                        .pUserData(NULL);
                instanceInfo.pNext(debugCreateInfo.address());
            }

            int error = vkCreateInstance(instanceInfo, null, this.pp);
            if (error == VK_ERROR_EXTENSION_NOT_PRESENT) {
                throw new IllegalStateException("Missing instance extension, failed to create instance.");
            } else if (error != 0) {
                throw new IllegalStateException("Failed to create instance.");
            }

            this.instance = new VkInstance(this.pp.get(0), instanceInfo);

            // Find physical device
            check(vkEnumeratePhysicalDevices(this.instance, this.ip, null));
            if (this.ip.get(0) > 0) {
                PointerBuffer physicalDevices = stack.mallocPointer(this.ip.get(0));
                check(vkEnumeratePhysicalDevices(this.instance, this.ip, physicalDevices));
                this.physicalDevice = new VkPhysicalDevice(physicalDevices.get(0), this.instance);
            } else {
                throw new IllegalStateException("No physical device accessible.");
            }

            boolean swapchainExtensionFound = false;
            check(vkEnumerateDeviceExtensionProperties(this.physicalDevice, (String)null, this.ip, null));
            if (this.ip.get(0) > 0) {
                VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(this.ip.get(0), stack);
                check(vkEnumerateDeviceExtensionProperties(this.physicalDevice, (String)null, this.ip, deviceExtensions));
                for (int i = 0; i < this.ip.get(0); i++) {
                    deviceExtensions.position(i);
                    if (VK_KHR_SWAPCHAIN_EXTENSION_NAME.equals(deviceExtensions.extensionNameString())) {
                        swapchainExtensionFound = true;
                        extensionNames.put(KHR_SWAPCHAIN_EXTENSION_NAME);
                    }
                }
            }
            if (!swapchainExtensionFound) {
                throw new IllegalStateException("Failed to find swapchain extension.");
            }

            // Create debug report callback
            if (ENABLE_VALIDATION_LAYER) {
                error = vkCreateDebugReportCallbackEXT(this.instance, debugCreateInfo, null, this.lp);
                if (error == VK_SUCCESS) {
                    this.messageCallback = lp.get(0);
                } else {
                    throw new IllegalStateException("Failed to create debug report callback.");
                }
            }

            // Get physical device properties
            vkGetPhysicalDeviceProperties(this.physicalDevice, this.physicalDeviceProperties);
            // Get physical device queues
            vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, this.ip, null);
            this.queueFamilyProperties = VkQueueFamilyProperties.malloc(this.ip.get(0));
            vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, this.ip, this.queueFamilyProperties);
            if (this.ip.get(0) == 0) {
                throw new IllegalStateException("Failed to find queue family properties.");
            }
            // Get physical device features
            vkGetPhysicalDeviceFeatures(this.physicalDevice, this.physicalDeviceFeatures);
        }
    }

    private void initWindow() {
        // Create window
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        this.window = glfwCreateWindow(this.width, this.height, "Vulkan Application", NULL, NULL);
        if (this.window == NULL) {
            throw new IllegalStateException("Failed to create GLFW window.");
        }

        // Setup callbacks
        glfwSetFramebufferSizeCallback(this.window, (window, width, height) -> {
            this.width = width;
            this.height = height;
            if (this.width != 0 && this.height != 0) {
                resize();
            }
        });

        glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key == GLFW_KEY_F11 && action == GLFW_PRESS) {
                GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                glfwWindowHint(GLFW_RED_BITS, mode.redBits());
                glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
                glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
                glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
                glfwSetWindowMonitor(this.window, glfwGetPrimaryMonitor(), 0, 0,
                        mode.width(), mode.height(), mode.refreshRate());
            }
        });
    }

    private void initDevice() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDeviceQueueCreateInfo.Buffer queue = VkDeviceQueueCreateInfo.mallocStack(1, stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .pNext(NULL)
                    .flags(0)
                    .queueFamilyIndex(this.graphicsQueueNodeIndex)
                    .pQueuePriorities(stack.floats(0.0f));

            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.callocStack(stack);
            // Check/Enable features

            this.extensionNames.flip();
            VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.mallocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pNext(NULL)
                    .flags(0)
                    .pQueueCreateInfos(queue)
                    .ppEnabledLayerNames(null)
                    .ppEnabledExtensionNames(this.extensionNames)
                    .pEnabledFeatures(features);

            check(vkCreateDevice(this.physicalDevice, deviceInfo, null, this.pp));
            this.device = new VkDevice(this.pp.get(0), this.physicalDevice, deviceInfo);
        }
    }

    private void initSwapchain() {
        // Create WSI window
        glfwCreateWindowSurface(this.instance, this.window, null, this.lp);
        this.surface = this.lp.get(0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Find presentation queue
            IntBuffer supportsPresent = stack.mallocInt(this.queueFamilyProperties.capacity());
            for (int i = 0; i < supportsPresent.capacity(); i++) {
                supportsPresent.position(i);
                vkGetPhysicalDeviceSurfaceSupportKHR(this.physicalDevice, i, this.surface, supportsPresent);
            }

            int graphicsQueueIndex = Integer.MAX_VALUE;
            int presentQueueIndex  = Integer.MAX_VALUE;
            for (int i = 0; i < supportsPresent.capacity(); i++) {
                if ((this.queueFamilyProperties.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    if (graphicsQueueIndex == Integer.MAX_VALUE) {
                        graphicsQueueIndex = i;
                    }
                    if (supportsPresent.get(i) == VK_TRUE) {
                        graphicsQueueIndex = i;
                        presentQueueIndex = i;
                        break;
                    }
                }
            }
            if (presentQueueIndex == Integer.MAX_VALUE) {
                for (int i = 0; i < supportsPresent.capacity(); i++) {
                    if (supportsPresent.get(i) == VK_TRUE) {
                        presentQueueIndex = i;
                        break;
                    }
                }
            }

            if (graphicsQueueIndex == Integer.MAX_VALUE || presentQueueIndex == Integer.MAX_VALUE) {
                throw new IllegalStateException("Failed to find graphics or present queue.");
            }

            // TODO : Use seperate queue
            if (graphicsQueueIndex != presentQueueIndex) {
                throw new IllegalStateException("Seperated graphics and present queue not allowed...");
            }
        }
    }

    private void resize() {

    }

    public ApplicationVulkan() {
//        initGLFW();
//        initVulkan();
//        initWindow();
//        initDevice();
        WindowContext windowContext = new GLFWWindow();
        RenderContext context = new RenderContext(windowContext);
    }

    public static void main(String[] args) {
        new ApplicationVulkan();
    }
}