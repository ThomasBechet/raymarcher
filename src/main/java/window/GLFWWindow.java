package window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.*;

public class GLFWWindow implements Window {

    private long window;
    private int width;
    private int height;

    private void initGLFW() {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }
        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("Vulkan not supported.");
        }
    }

    private void createWindow() {
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        this.window = glfwCreateWindow(this.width, this.height, "Vulkan Application", NULL, NULL);
        if (this.window == NULL) {
            throw new IllegalStateException("Failed to create GLFW window.");
        }

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

    public GLFWWindow() {
        this.width = 1600;
        this.height = 900;

        initGLFW();
        createWindow();
    }

    @Override
    public void cleanup() {
        glfwDestroyWindow(this.window);
    }

    public long getWindow() {
        return this.window;
    }

    @Override
    public long createWindowSurfaceWSI(VkInstance instance) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer lp = stack.mallocLong(1);
            int err = glfwCreateWindowSurface(instance, this.window, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create WSI.");
            }
            return lp.get(0);
        }
    }

    @Override
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(this.window);
    }

    @Override
    public void update() {
        glfwPollEvents();
    }

    @Override
    public int getWidth() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(this.window, width, height);
            return width.get(0);
        }
    }

    @Override
    public int getHeight() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(this.window, width, height);
            return height.get(0);
        }
    }
}