package rendererOld.memory;

import static org.lwjgl.opengl.GL45.*;

public class ColorBuffer {

    private int width;
    private int height;

    private int texture;
    private int sampler;

    private int framebuffer;

    public ColorBuffer(int width, int height) {
        this.width = width;
        this.height = height;

        this.texture = Utility.createTexture(this.width, this.height);
        this.sampler = Utility.createSampler();

        createFramebuffer();
    }

    public void cleanup() {
        Utility.deleteTexture(this.texture);
        Utility.deleteSampler(this.sampler);
        deleteFramebuffer();
    }

    public void setViewport() {
        glViewport(0, 0, this.width, this.height);
    }

    public void bindFramebuffer() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.framebuffer);
        int buffers[] = {GL_COLOR_ATTACHMENT0};
        glDrawBuffers(buffers);
    }

    public void bindSampler(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, this.texture);
        glBindSampler(unit, this.sampler);
    }

    private void createFramebuffer() {
        this.framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        glBindTexture(GL_TEXTURE_2D, this.texture);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.texture, 0);

        assert(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    private void deleteFramebuffer() {
        glDeleteFramebuffers(this.framebuffer);
    }
}