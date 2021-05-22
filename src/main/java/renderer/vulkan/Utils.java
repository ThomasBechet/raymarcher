package renderer.vulkan;

public class Utils {
    public static void check(int errorCode) {
        if (errorCode != 0) {
            throw new IllegalStateException(String.format("Vulkan error [0x%X]", errorCode));
        }
    }
}