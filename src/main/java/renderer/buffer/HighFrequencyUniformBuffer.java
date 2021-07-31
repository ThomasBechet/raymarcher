package renderer.buffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import renderer.core.Context;
import renderer.core.MemoryManager;
import renderer.core.RenderContext;

import java.nio.ByteBuffer;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class HighFrequencyUniformBuffer {

    public final static int UNIFORM_BUFFER_SIZE = 4 * 16 + 4 * 3;

    private MemoryManager memoryManager;

    private int bufferSize;
    private int totalBufferSize;

    private long buffer;
    private ByteBuffer data;

    private int padUniformBufferSize(int originalSize, Context ctx) {
        int minUboAlignment = (int)ctx.getPhysicalDeviceProperties().limits().minUniformBufferOffsetAlignment();
        int alignedSize = originalSize;
        if (minUboAlignment > 0) {
            alignedSize = (alignedSize + minUboAlignment - 1) & ~(minUboAlignment - 1);
        }
        return alignedSize;
    }

    public HighFrequencyUniformBuffer(Context ctx, MemoryManager memoryManager, int maxInFlightFrameCount) {
        this.memoryManager = memoryManager;

        this.bufferSize = padUniformBufferSize(UNIFORM_BUFFER_SIZE, ctx);
        this.totalBufferSize = maxInFlightFrameCount * this.bufferSize;
        this.buffer = memoryManager.createBuffer(this.totalBufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);
        this.data = memByteBuffer(memoryManager.mapBuffer(this.buffer), (int)this.totalBufferSize);
    }

    public void cleanup() {
        this.memoryManager.unmapBuffer(this.buffer);
        this.memoryManager.destroyBuffer(this.buffer);
    }

    public long getBuffer() {
        return this.buffer;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public int getTotalBufferSize() {
        return this.totalBufferSize;
    }

    public void writeVPMatrix(int index, Matrix4f matrix) {
        matrix.get(this.bufferSize * index, this.data);
    }

    public void writeEye(int index, Vector3f eye) {
        eye.get(this.bufferSize * index + 4 * 16, this.data);
    }
}