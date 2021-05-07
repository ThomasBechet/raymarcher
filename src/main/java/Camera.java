import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.RenderCamera;
import renderer.RenderContext;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float fov = 70.0f;
    private float speed = 15.0f * 0.001f;
    private float sensibility = 0.1f;

    private Vector3f velocity;
    private Transform transform;

    private Input input;
    private RenderCamera camera;

    public Camera(Input input, RenderContext context) {
        this.input = input;
        this.transform = new Transform();
        this.transform.setTranslation(new Vector3f(0, 0, 5));
        this.camera = context.getActiveCamera();
    }

    public void update(float delta) {

        // Get inputs
        int forwardState = this.input.getKeyboardState(GLFW_KEY_W);
        int backwardState = this.input.getKeyboardState(GLFW_KEY_S);
        int leftState = this.input.getKeyboardState(GLFW_KEY_A);
        int rightState = this.input.getKeyboardState(GLFW_KEY_D);
        int upState = this.input.getKeyboardState(GLFW_KEY_X);
        int downState = this.input.getKeyboardState(GLFW_KEY_Z);

        // Translation
        Vector3f direction = new Vector3f(0, 0, 0);
        if (forwardState == GLFW_PRESS) direction.add(this.transform.getForwardVector());
        if (backwardState == GLFW_PRESS) direction.add(this.transform.getBackwardVector());
        if (leftState == GLFW_PRESS) direction.add(this.transform.getLeftVector());
        if (rightState == GLFW_PRESS) direction.add(this.transform.getRightVector());
        if (upState == GLFW_PRESS) direction.add(this.transform.getUpVector());
        if (downState == GLFW_PRESS) direction.add(this.transform.getDownVector());
        if (direction.length() > 0.0f) direction.normalize();

        this.transform.translate(direction.mul(delta * this.speed));

        // Rotation
        Vector2f motion = this.input.getMouseMotion();
        this.yaw += motion.x * this.sensibility;
        this.pitch += motion.y * this.sensibility;
        this.pitch = Math.max(-90.0f, Math.min(this.pitch, 90.0f));
        this.transform.setRotation((float)(-Math.toRadians(this.yaw)), new Vector3f(0, 1, 0));
        this.transform.rotate((float)Math.toRadians(this.pitch), new Vector3f(-1, 0, 0));

        // Update camera
        Vector3f eye = this.transform.getTranslation();
        Vector3f center = new Vector3f(eye).add(this.transform.getForwardVector());
        Vector3f up = this.transform.getUpVector();
        this.camera.setEye(eye.x, eye.y, eye.z);
        this.camera.setCenter(center.x, center.y, center.z);
        this.camera.setUp(up.x, up.y, up.z);
    }

    public void setRatio(float ratio) {
        this.camera.setRatio(ratio);
    }
}