package input;

import org.joml.Vector2f;

public interface Input {
    void cleanup();
    void update();
    Vector2f getMouseMotion();
    int getKeyboardState(int key);
}