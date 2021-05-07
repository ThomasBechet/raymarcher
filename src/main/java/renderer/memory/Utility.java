package renderer.memory;

import static org.lwjgl.opengl.GL45.*;

public class Utility {

    public static int createTexture(int width, int height) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA32F, width, height);
        int[] clearColor = {0, 0, 0, 0};
        glClearTexImage(texture, 0, GL_RGBA, GL_UNSIGNED_BYTE, clearColor);
        glBindTexture(GL_TEXTURE_2D, 0);
        return texture;
    }
    public static void deleteTexture(int texture) {
        glDeleteTextures(texture);
    }

    public static int createSampler() {
        int sampler = glGenSamplers();
        glSamplerParameteri(sampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameteri(sampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(sampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glSamplerParameteri(sampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        return sampler;
    }
    public static void deleteSampler(int sampler) {
        glDeleteSamplers(sampler);
    }
}