package shape;

import renderer.RenderContext;
import renderer.RenderShapeType;
import org.joml.Vector3f;
import renderer.RenderShapeParameters;

public class PlaneShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 4;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "return dot(p, param.f1.xyz) - param.f0.w;";
            return code;
        }

        @Override
        public float getDistance(Vector3f p, RenderShapeParameters param) {
            return 0;
        }

        @Override
        public float getSafeDistance() {
            return 0;
        }
    }

    private static RenderShapeType type;
    public static void register(RenderContext context) {
        type = new PlaneShape.Type();
        context.registerShapeType(type);
    }

    private Vector3f normal;
    private float height;

    public PlaneShape(RenderContext context, Vector3f normal, float height) {
        super(context, PlaneShape.type);
        this.normal = normal;
        this.height = height;
        this.shape.setF0(0, 0, 0, this.height);
        this.shape.setF1(this.normal.x, this.normal.y, this.normal.z, 0);
    }
}