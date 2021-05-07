package renderer.memory;

import static org.lwjgl.opengl.GL45.*;

public class GBuffer {

    private int width;
    private int height;

    private int positionDepthTexture;
    private int normalMaterialTexture;

    private int framebuffer;
    private int sampler;

    public GBuffer(int width, int height) {
        this.width = width;
        this.height = height;

        this.positionDepthTexture = Utility.createTexture(this.width, this.height);
        this.normalMaterialTexture = Utility.createTexture(this.width, this.height);

        createFramebuffer();
        this.sampler = Utility.createSampler();
    }

    public void cleanup() {
        Utility.deleteTexture(this.positionDepthTexture);
        Utility.deleteTexture(this.normalMaterialTexture);

        deleteFramebuffer();
        Utility.deleteSampler(this.sampler);
    }

    public void setViewport() {
        glViewport(0, 0, this.width, this.height);
    }

    public void bindFramebuffer() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.framebuffer);
        int buffers[] = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};
        glDrawBuffers(buffers);
    }

    public void bindAllSamplers(int startUnit) {
        glActiveTexture(GL_TEXTURE0 + startUnit + 0);
        glBindTexture(GL_TEXTURE_2D, this.positionDepthTexture);
        glBindSampler(startUnit + 0, this.sampler);

        glActiveTexture(GL_TEXTURE0 + startUnit + 1);
        glBindTexture(GL_TEXTURE_2D, this.normalMaterialTexture);
        glBindSampler(startUnit + 1, this.sampler);
    }

    private void createFramebuffer() {
        this.framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        glBindTexture(GL_TEXTURE_2D, this.positionDepthTexture);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.positionDepthTexture, 0);

        glBindTexture(GL_TEXTURE_2D, this.normalMaterialTexture);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, this.normalMaterialTexture, 0);

        assert(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    private void deleteFramebuffer() {
        glDeleteFramebuffers(this.framebuffer);
    }
}