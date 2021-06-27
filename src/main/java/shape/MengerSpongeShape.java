package shape;

import org.joml.Vector3f;
import rendererOld.RenderContext;
import rendererOld.RenderShapeParameters;
import rendererOld.RenderShapeType;

public class MengerSpongeShape extends Shape {

    public static class Type implements RenderShapeType {

        @Override
        public int getID() {
            return 7;
        }

        @Override
        public String getCode() {
            String code = "";
            code += "const float scale = 30.0f;\n";
            code += "vec3 q = abs(p / scale) - vec3(0, -0.5, 0) - vec3(5, 0.6, 5);\n";
            code += "float d = length(max(q, 0)) + min(max(q.x, max(q.y, q.z)), 0);\n";
            code += "float s = 2.67;\n";
            code += "for (int m = 0; m < 6; m++) {\n";
            code += "    vec3 a = mod(q * s, 2.0) - 1.0;\n";
            code += "    s *= 3.0;\n";
            code += "    vec3 r = abs(1.0 - 3.0 * abs(a));\n";
            code += "    float da = max(r.x, r.y);\n";
            code += "    float db = max(r.y, r.z);\n";
            code += "    float dc = max(r.z, r.x);\n";
            code += "    float c = (min(da, min(db, dc)) - 1.0) / s;\n";
            code += "    d = max(d, c);\n";
            code += "}\n";
            code += "return d * scale;\n";
            return code;

//            code += "vec3 q = p;\n";
//            code += "q.xz = mod( q.xz+1.0, 2.0 ) -1.0;\n";
//            code += "vec3 w = abs(q) - vec3(1);\n";
//            code += "float d = length(max(w, 0)) + min(max(w.x, max(w.y, w.z)), 0);\n";
//            code += "float s = 1.0;\n";
//            code += "for( int m=0; m<6; m++ )\n";
//            code += "{\n";
//            code += "    float h = float(m)/6.0;\n";
//            code += "    p =  q - 0.5*sin( abs(p.y) + float(m)*3.0+vec3(0.0,3.0,1.0));\n";
//            code += "   vec3 a = mod( p*s, 2.0 )-1.0;\n";
//            code += "   s *= 3.0;\n";
//            code += "   vec3 r = abs(1.0 - 3.0*abs(a));\n";
//            code += "   float da = max(r.x,r.y);\n";
//            code += "   float db = max(r.y,r.z);\n";
//            code += "   float dc = max(r.z,r.x);\n";
//            code += "   float c = (min(da,min(db,dc))-1.0)/s;\n";
//            code += "   d = max( c, d );\n";
//            code += "}\n";
//            code += "return d;\n";
//            return code;
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
        type = new MengerSpongeShape.Type();
        context.registerShapeType(type);
    }

    public MengerSpongeShape(RenderContext context) {
        super(context, MengerSpongeShape.type);
    }
}