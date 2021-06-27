package shape;

import rendererOld.RenderContext;
import rendererOld.RenderShapeType;
import org.joml.Vector3f;
import rendererOld.RenderShapeParameters;

public class BoxFrameShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 2;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "p = abs(p) - param.f1.xyz;\n";
            code += "vec3 q = abs(p + param.f0.w) - param.f0.w;\n";
            code += "return min(min(\n";
            code += "length(max(vec3(p.x, q.y, q.z), 0.0)) + min(max(p.x, max(q.y, q.z)), 0.0),\n";
            code += "length(max(vec3(q.x, p.y, q.z), 0.0)) + min(max(q.x, max(p.y, q.z)), 0.0)),\n";
            code += "length(max(vec3(q.x, q.y, p.z), 0.0)) + min(max(q.x, max(q.y, p.z)), 0.0));";
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
        type = new BoxFrameShape.Type();
        context.registerShapeType(type);
    }

    private Vector3f box;
    private float width;

    public BoxFrameShape(RenderContext context, Vector3f box, float width) {
        super(context, BoxFrameShape.type);
        this.box = box;
        this.width = width;
        this.shape.setF0(0, 0, 0, this.width);
        this.shape.setF1(this.box.x, this.box.y, this.box.z, 0);
    }
}