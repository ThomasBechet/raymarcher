package rendererOld.asset;

import org.joml.Vector3f;
import rendererOld.RenderMaterial;

import java.nio.ByteBuffer;

public class Material implements RenderMaterial {
    public final static int BYTE_COUNT = 32;

    public AssetManager manager;
    public int index;

    public Vector3f albedo;
    public float metallic;
    public float roughness;

    public Material(AssetManager manager, int index) {
        this.manager = manager;
        this.index = index;
        this.albedo = new Vector3f(1.0f, 1.0f, 1.0f);
        this.metallic = 0.5f;
        this.roughness = 0.5f;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setAlbedo(float r, float g, float b) {
        this.albedo.x = r;
        this.albedo.y = g;
        this.albedo.z = b;
        this.manager.updateMaterial(this);
    }

    @Override
    public void setMetallic(float metallic) {
        this.metallic = metallic;
        this.manager.updateMaterial(this);
    }

    @Override
    public void setRoughness(float roughness) {
        this.roughness = roughness;
        this.manager.updateMaterial(this);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void writeBuffer(ByteBuffer buffer) {
        int bufferStart = this.index * BYTE_COUNT;
        this.albedo.get(bufferStart, buffer);
        buffer.putFloat(bufferStart + 12, this.roughness);
        buffer.putFloat(bufferStart + 16, this.metallic);
    }
}