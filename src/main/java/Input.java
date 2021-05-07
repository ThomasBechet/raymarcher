import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    long window;
    Vector2f mouseMotion = new Vector2f();
    Vector2f mousePosition = new Vector2f();
    Vector2f mouseOldPosition = new Vector2f();

    public Input(long window) {
        this.window = window;
        glfwSetCursorPosCallback(this.window, (w, x, y) -> {
            this.mousePosition.x = (float)x;
            this.mousePosition.y = (float)y;
        });
    }

    public void update() {
        this.mouseMotion.set(this.mousePosition).sub(this.mouseOldPosition);
        this.mouseOldPosition.set(this.mousePosition);
    }

    public Vector2f getMouseMotion() {
        return this.mouseMotion;
    }

    public int getKeyboardState(int key) {
        return glfwGetKey(this.window, key);
    }
}