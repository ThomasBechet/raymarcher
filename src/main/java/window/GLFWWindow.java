package window;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;
import renderer.vulkan.Utils;

import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWWindow implements WindowContext {

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
    }

    public GLFWWindow() {
        this.width = 1600;
        this.height = 900;

        initGLFW();
        createWindow();
    }

    @Override
    public long createWindowSurfaceWSI(VkInstance instance) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer lp = stack.mallocLong(1);
            Utils.check(glfwCreateWindowSurface(instance, this.window, null, lp));
            return lp.get(0);
        }
    }
}