package window;

import org.lwjgl.vulkan.VkInstance;

public interface Window {
    void cleanup();
    long createWindowSurfaceWSI(VkInstance instance);
    boolean windowShouldClose();
    void update();
    int getWidth();
    int getHeight();
}