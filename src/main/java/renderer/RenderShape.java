package renderer;

public interface RenderShape {
    int getIndex();
    void setType(RenderShapeType type);
    void setMaterial(RenderMaterial material);
    void setPosition(float x, float y, float z);
    void setF0(float x, float y, float z, float w);
    void setF1(float x, float y, float z, float w);
}