import input.*;
import renderer.*;
import window.*;

public class ApplicationVulkan {

    public ApplicationVulkan() {

        Window window = new GLFWWindow();
        Input input = new GLFWInput((GLFWWindow)window);
        Renderer renderer = new Renderer(window);
        Camera camera = new Camera(input, renderer);

        long lastTime = System.nanoTime();
        while (!window.windowShouldClose()) {
            // Compute deltatime
            long time = System.nanoTime();
            float deltaTime = (float)(time - lastTime) / 1000000.0f;
            lastTime = time;

            // Update systems
            window.update();
            camera.update(deltaTime);
            renderer.render();
        }

        renderer.cleanup();
        input.cleanup();
        window.cleanup();

//        long buffer = memory.createBuffer(1024, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VMA_MEMORY_USAGE_GPU_TO_CPU);
//        PointerBuffer data = memPointerBuffer(memory.mapBuffer(buffer), 1024);
//        memory.unmapBuffer(buffer);
    }

    public static void main(String[] args) {
        new ApplicationVulkan();
    }
}