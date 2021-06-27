package rendererOld.shader;

import static org.lwjgl.opengl.GL45.*;

public class ComputeShader extends Shader {
    public ComputeShader(String computeCode) throws Exception {
        addShader(computeCode, GL_COMPUTE_SHADER);
        link();
    }

    public void dispatch(int groupsX, int groupsY, int groupsZ) {
        glDispatchCompute(groupsX, groupsY, groupsZ);
    }
}