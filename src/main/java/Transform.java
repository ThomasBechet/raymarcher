import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {
    private Vector3f translation = new Vector3f();
    private Quaternionf rotation = new Quaternionf();
    private Vector3f scale = new Vector3f(1, 1, 1);
    private Matrix4f transform = new Matrix4f();
    private boolean computeRequired = true;

    public Vector3f getTranslation() {
        return this.translation;
    }
    public void setTranslation(Vector3f translation) {
        this.translation = translation;
        this.computeRequired = true;
    }
    public Quaternionf getRotation() {
        return this.rotation;
    }
    public void setRotation(float angle, Vector3f axis) {
        this.rotation = new Quaternionf(new AxisAngle4f(angle, axis));
        this.computeRequired = true;
    }
    public Vector3f getScale() {
        return this.scale;
    }
    public void setScale(Vector3f scale) {
        this.scale = scale;
        this.computeRequired = true;
    }

    public void rotate(float angle, Vector3f axis) {
        this.rotation.rotateAxis(angle, axis);
        this.computeRequired = true;
    }
    public void translate(Vector3f translation) {
        this.translation.add(translation);
        this.computeRequired = true;
    }

    public Vector3f getForwardVector() {
        return this.rotation.transform(new Vector3f(0, 0, -1));
    }
    public Vector3f getBackwardVector() {
        return this.rotation.transform(new Vector3f(0, 0, 1));
    }
    public Vector3f getLeftVector() {
        return this.rotation.transform(new Vector3f(-1, 0, 0));
    }
    public Vector3f getRightVector() {
        return this.rotation.transform(new Vector3f(1, 0, 0));
    }
    public Vector3f getUpVector() {
        return this.rotation.transform(new Vector3f(0, 1, 0));
    }
    public Vector3f getDownVector() {
        return this.rotation.transform(new Vector3f(0, -1, 0));
    }

    Matrix4f getMatrix() {
        if (this.computeRequired) {
            this.transform.translationRotateScale(this.translation, this.rotation, this.scale);
            this.computeRequired = false;
        }
        return this.transform;
    }
}