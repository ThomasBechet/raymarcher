package input;

import org.joml.Vector2f;
import window.GLFWWindow;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWInput implements Input {
    long window;
    Vector2f mouseMotion = new Vector2f();
    Vector2f mousePosition = new Vector2f();
    Vector2f mouseOldPosition = new Vector2f();

    public GLFWInput(GLFWWindow glfwWindow) {
        this.window = glfwWindow.getWindow();
        glfwSetCursorPosCallback(this.window, (w, x, y) -> {
            this.mousePosition.x = (float)x;
            this.mousePosition.y = (float)y;
        });
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void update() {
        this.mouseMotion.set(this.mousePosition).sub(this.mouseOldPosition);
        this.mouseOldPosition.set(this.mousePosition);
    }

    @Override
    public Vector2f getMouseMotion() {
        return this.mouseMotion;
    }

    @Override
    public int getKeyboardState(int key) {
        return glfwGetKey(this.window, key);
    }
}