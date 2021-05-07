package renderer;

public interface RenderMaterial {
    int getIndex();
    void setAlbedo(float r, float g, float b);
    void setMetallic(float metallic);
    void setRoughness(float roughness);
}