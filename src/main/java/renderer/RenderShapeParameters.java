package renderer;

import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class RenderShapeParameters {
    public final static int BYTE_COUNT = 32;

    public Vector4f f0;
    public Vector4f f1;

    public RenderShapeParameters() {
        this.f0 = new Vector4f();
        this.f1 = new Vector4f();
    }

    public void writeBuffer(int start, ByteBuffer buffer) {
        // F0
        this.f0.get(start, buffer);
        // F1
        this.f1.get(start + 16, buffer);
    }
}