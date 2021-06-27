package rendererOld.shader;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL45.*;

public class Shader {
    private final int programId;
    private List<Integer> shaders = new ArrayList<>();

    public Shader() throws Exception {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new Exception("Could not create Shader.");
        }
    }

    public void bind() {
        glUseProgram(this.programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgram() {
        return this.programId;
    }

    public void cleanup() {
        unbind();
        if (this.programId != 0) {
            glDeleteProgram(this.programId);
        }
    }

    protected void addShader(String code, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Failed to create renderer.shader.");
        }

        glShaderSource(shaderId, code);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(code);
            throw new Exception("Failed to compile renderer.shader: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(this.programId, shaderId);

        this.shaders.add(shaderId);
    }

    protected void link() throws Exception {
        glLinkProgram(this.programId);
        if (glGetProgrami(this.programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Failed to link renderer.shader: " + glGetProgramInfoLog(this.programId, 1024));
        }

        for (int shader : this.shaders) {
            glDetachShader(this.programId, shader);
        }

        glValidateProgram(this.programId);
        if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == 0) {
            throw new Exception("Warning validating renderer.shader code: " + glGetProgramInfoLog(this.programId, 1024));
        }
    }
}