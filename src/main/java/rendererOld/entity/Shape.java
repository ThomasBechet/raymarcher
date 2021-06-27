package rendererOld.entity;

import org.joml.Vector3f;
import rendererOld.RenderMaterial;
import rendererOld.RenderShape;
import rendererOld.asset.Material;
import rendererOld.RenderShapeType;
import rendererOld.RenderShapeParameters;

import java.nio.ByteBuffer;

public class Shape implements RenderShape {
    public final static int BYTE_COUNT = 32 + RenderShapeParameters.BYTE_COUNT;

    private SceneManager manager;
    private int index;

    private Material material;
    private RenderShapeType type;
    private Vector3f position;

    private RenderShapeParameters parameters;

    public Shape(SceneManager manager, int index) {
        this.manager = manager;
        this.index = index;

        this.position = new Vector3f();
        this.type = null;
        this.material = null;

        this.parameters = new RenderShapeParameters();
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setType(RenderShapeType type) {
        this.type = type;
        this.manager.updateShape(this);
    }

    @Override
    public void setMaterial(RenderMaterial material) {
        this.material = (Material)material;
        this.manager.updateShape(this);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.manager.updateShape(this);
    }

    @Override
    public void setF0(float x, float y, float z, float w) {
        this.parameters.f0.x = x;
        this.parameters.f0.y = y;
        this.parameters.f0.z = z;
        this.parameters.f0.w = w;
        this.manager.updateShape(this);
    }

    @Override
    public void setF1(float x, float y, float z, float w) {
        this.parameters.f1.x = x;
        this.parameters.f1.y = y;
        this.parameters.f1.z = z;
        this.parameters.f1.w = w;
        this.manager.updateShape(this);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void writeBuffer(ByteBuffer buffer) {
        int bufferStart = this.index * BYTE_COUNT;
        // Type
        buffer.putInt(bufferStart + 0, this.type == null ? 0 : this.type.getID());
        // Material
        buffer.putInt(bufferStart + 4, this.material == null ? 0 : this.material.getIndex());
        // Position
        this.position.get(bufferStart + 16, buffer);
        // Parameters
        this.parameters.writeBuffer(bufferStart + 32, buffer);
    }
}