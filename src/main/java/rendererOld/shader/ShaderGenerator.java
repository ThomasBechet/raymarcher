package rendererOld.shader;

import rendererOld.RenderShapeType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ShaderGenerator {
    // Tokens
    private final static String SHAPE_SD_FUNCTIONS_TOKEN = "#SHAPE_SD_FUNCTIONS_TOKEN";
    private final static String MAP_SD_FUNCTION_TOKEN    = "#MAP_SD_FUNCTION_TOKEN";

    // Known files
    public final static String SCENE_FILE           = "scene.glsl";
    public final static String DIRECT_LIGHTING_FILE = "direct_lighting.glsl";
    public final static String GBUFFER_FILE         = "gbuffer.comp";
    public final static String QUAD_VERTEX_FILE     = "quad.vert";
    public final static String QUAD_FRAGMENT_FILE   = "quad.frag";

    private Map<Integer, RenderShapeType> shapeTypes;
    private Map<String, String> codes;

    public ShaderGenerator() {
        this.shapeTypes = new HashMap<>();
        this.codes = new HashMap<>();

        String commonFiles[] = {
                SCENE_FILE,
                DIRECT_LIGHTING_FILE
        };
        for (String files : commonFiles) {
            readRawCode(files);
        }
    }

    public void registerShapeType(RenderShapeType shapeType) {
        this.shapeTypes.put(shapeType.getID(), shapeType);
    }

    public String getCode(String file) {
        String code = readRawCode(file);
        code = processTokens(code);
        code = processIncludes(code);
        return code;
    }

    private String readRawCode(String file) {
        if (this.codes.containsKey(file)) {
            return this.codes.get(file);
        } else {
            try {
                String code = Files.readString(Path.of(getClass().getClassLoader().getResource(file).toURI()), StandardCharsets.UTF_8);
                this.codes.put(file, code);
                return code;
            } catch(IOException | URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private String processTokens(String code) {
        if (code.contains(SHAPE_SD_FUNCTIONS_TOKEN)) {
            code = code.replace(SHAPE_SD_FUNCTIONS_TOKEN, getShapeSDFunctionsCode());
        }

        if (code.contains(MAP_SD_FUNCTION_TOKEN)) {
            code = code.replace(MAP_SD_FUNCTION_TOKEN, getMapSDFunction());
        }

        return code;
    }

    private String processIncludes(String code) {
        for (Map.Entry<String, String> entry : this.codes.entrySet()) {
            String includeToken = "#include <" + entry.getKey() + ">";
            if (code.contains(includeToken)) {
                code = code.replace(includeToken, getCode(entry.getKey()));
            }
        }
        return code;
    }

    private String getShapeSDFunctionsCode() {
        String code = "";
        for (RenderShapeType shapeType : this.shapeTypes.values()) {
            code += "float sd" + shapeType.getID() + "(in vec3 p, in Shape param) {\n";
            code += shapeType.getCode() + "\n";
            code += "}\n";
        }
        return code;
    }

    private String getMapSDFunction() {
        String code = "float sdShape(in vec3 p, in Shape param) {\n";
        int index = 0;
        for (RenderShapeType shapeType : this.shapeTypes.values()) {
            if (index == 0) {
                code += String.format("if (param.type == %d) return sd%d(p, param);\n",
                        shapeType.getID(), shapeType.getID());
            } else {
                code += String.format("else if (param.type == %d) return sd%d(p, param);\n",
                        shapeType.getID(), shapeType.getID());
            }
            index++;
        }
        code += "return 0.0;\n}\n";
        return code;
    }
}