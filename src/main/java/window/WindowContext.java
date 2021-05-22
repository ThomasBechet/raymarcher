package window;

import org.lwjgl.vulkan.VkInstance;

public interface WindowContext {
    long createWindowSurfaceWSI(VkInstance instance);
}