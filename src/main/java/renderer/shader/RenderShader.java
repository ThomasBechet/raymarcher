package renderer.shader;

import static org.lwjgl.opengl.GL45.*;

public class RenderShader extends Shader {
    public RenderShader(String vertexCode, String fragmentCode) throws Exception {
        addShader(vertexCode, GL_VERTEX_SHADER);
        addShader(fragmentCode, GL_FRAGMENT_SHADER);
        link();
    }
}