package rendererOld.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import rendererOld.RenderCamera;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

public class Camera implements RenderCamera {
    private float fov;
    private float ratio;

    private Matrix4f viewMatrix;
    private Matrix4f camMatrix;

    private Vector3f eye;
    private Vector3f center;
    private Vector3f up;

    private Vector3f oldEye;
    private Vector3f oldCenter;
    private Vector3f oldUp;
    private Matrix4f oldCamMatrix;

    public Camera() {
        this.fov = 60.0f;
        this.ratio = 1.0f;

        this.viewMatrix = new Matrix4f();
        this.camMatrix = new Matrix4f();

        this.eye = new Vector3f();
        this.center = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);

        this.oldEye = new Vector3f();
        this.oldCenter = new Vector3f(0, 0, -1);
        this.oldUp = new Vector3f(0, 1, 0);
        this.oldCamMatrix = new Matrix4f();
    }

    @Override
    public void setEye(float x, float y, float z) {
        this.eye.set(x, y, z);
    }
    @Override
    public void setCenter(float x, float y, float z) {
        this.center.set(x, y, z);
    }
    @Override
    public void setUp(float x, float y, float z) {
        this.up.set(x, y, z);
    }
    @Override
    public void setRatio(float ration) {
        this.ratio = ration;
    }

    public boolean hasMoved() {
        return !this.eye.equals(this.oldEye, 0.001f) ||
                !this.center.equals(this.oldCenter, 0.001f) ||
                !this.up.equals(this.oldUp, 0.001f);
    }

    public void startFrame() {
        // Compute matrices
        this.viewMatrix.setLookAt(this.eye, this.center, this.up);
        this.camMatrix.setPerspective((float)Math.toRadians(this.fov), this.ratio, 0.01f, 100.0f);
        this.camMatrix.mul(this.viewMatrix);
    }

    public void endFrame() {
        // Save old camera data
        this.oldEye.set(this.eye);
        this.oldCenter.set(this.center);
        this.oldUp.set(this.up);
        this.oldCamMatrix.set(this.camMatrix);
    }

    public void pushGBufferPassUniforms() {
        // Upload data
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            this.camMatrix.get(fb);
            glUniformMatrix4fv(0, false, fb);
        }
        glUniform3f(1, this.eye.x, this.eye.y, this.eye.z);
    }

    public void pushIndirectLightPassUniforms() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            this.camMatrix.get(fb);
            glUniformMatrix4fv(5, false, fb);
        }
    }
}