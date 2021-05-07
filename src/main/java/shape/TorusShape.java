package shape;

import renderer.RenderContext;
import renderer.RenderShapeType;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.RenderShapeParameters;

public class TorusShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 3;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "vec2 q = vec2(length(p.xz) - param.f1.x, p.y);\n";
            code += "return length(q) - param.f1.y;";
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
        type = new TorusShape.Type();
        context.registerShapeType(type);
    }

    private Vector2f dimensions;

    public TorusShape(RenderContext context, Vector2f dimensions) {
        super(context, TorusShape.type);
        this.dimensions = dimensions;
        this.shape.setF1(this.dimensions.x, this.dimensions.y, 0, 0);
    }
}