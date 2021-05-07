package renderer;


import org.joml.Vector3f;

public interface RenderShapeType {
    int getID();
    String getCode();
    float getDistance(Vector3f p, RenderShapeParameters param);
    float getSafeDistance();
}