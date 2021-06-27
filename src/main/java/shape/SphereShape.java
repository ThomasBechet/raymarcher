package shape;

import org.joml.Vector3f;
import rendererOld.RenderContext;
import rendererOld.RenderShapeType;
import rendererOld.RenderShapeParameters;

public class SphereShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 0;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "return length(p) - param.f0.w;";
            return code;
        }

        @Override
        public float getDistance(Vector3f p, RenderShapeParameters param) {
            return p.length() - param.f0.w;
        }

        @Override
        public float getSafeDistance() {
            return 0;
        }
    }

    private static RenderShapeType type;
    public static void register(RenderContext context) {
        type = new SphereShape.Type();
        context.registerShapeType(type);
    }

    private float radius;

    public SphereShape(RenderContext context, float radius) {
        super(context, SphereShape.type);
        this.shape.setType(type);
        this.radius = radius;
        this.shape.setF0(0, 0, 0, this.radius);
    }
}