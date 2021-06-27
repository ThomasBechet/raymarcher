package shape;

import rendererOld.RenderContext;
import rendererOld.RenderShapeType;
import org.joml.Vector3f;
import rendererOld.RenderShapeParameters;

public class RoundBoxShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 1;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "vec3 q = abs(p) - param.f1.xyz;\n";
            code += "return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0) - param.f0.w;";
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

    private static Type type;
    public static void register(RenderContext context) {
        type = new RoundBoxShape.Type();
        context.registerShapeType(type);
    }

    private Vector3f box;
    private float radius;

    public RoundBoxShape(RenderContext context, Vector3f box, float radius) {
        super(context, RoundBoxShape.type);
        this.box = box;
        this.radius = radius;
        this.shape.setF0(0, 0, 0, this.radius);
        this.shape.setF1(this.box.x, this.box.y, this.box.z, 0);
    }
}