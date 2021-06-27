package rendererOld;

public interface RenderCamera {
    void setEye(float x, float y, float z);
    void setCenter(float x, float y, float z);
    void setUp(float x, float y, float z);
    void setRatio(float ration);
}