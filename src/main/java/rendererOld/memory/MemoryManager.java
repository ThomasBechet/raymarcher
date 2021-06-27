package rendererOld.memory;

import org.lwjgl.BufferUtils;
import rendererOld.asset.Material;
import rendererOld.entity.Shape;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class MemoryManager {
    public final static int LAYOUT_HEADER_UBO_BINDING = 0;
    public final static int LAYOUT_HEADER_UBO_LENGTH = 16;

    public final static int MAX_MATERIAL_COUNT = 64;
    public final static int MATERIAL_UBO_LENGTH = MAX_MATERIAL_COUNT * Material.BYTE_COUNT;
    public final static int MATERIAL_UBO_BINDING = 1;

    public final static int MAX_SHAPE_COUNT = 128;
    public final static int SHAPE_UBO_LENGTH = MAX_SHAPE_COUNT * Shape.BYTE_COUNT;
    public final static int SHAPE_UBO_BINDING = 2;

    // Layout Header
    private int layoutHeaderUBO;
    private ByteBuffer layoutHeaderBuffer;

    // Material
    private int materialDataUBO;
    private ByteBuffer materialDataBuffer;

    // Shape
    private int shapeDataUBO;
    private ByteBuffer shapeDataBuffer;

    public MemoryManager() {
        // Create layout header UBO
        this.layoutHeaderUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, this.layoutHeaderUBO);
        glBufferData(GL_UNIFORM_BUFFER, LAYOUT_HEADER_UBO_LENGTH, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, LAYOUT_HEADER_UBO_BINDING, this.layoutHeaderUBO);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        this.layoutHeaderBuffer = BufferUtils.createByteBuffer(LAYOUT_HEADER_UBO_LENGTH);

        // Create shape UBO
        this.shapeDataUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, this.shapeDataUBO);
        glBufferData(GL_UNIFORM_BUFFER, SHAPE_UBO_LENGTH, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, SHAPE_UBO_BINDING, this.shapeDataUBO);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        this.shapeDataBuffer = BufferUtils.createByteBuffer(SHAPE_UBO_LENGTH);

        // Create material UBO
        this.materialDataUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, this.materialDataUBO);
        glBufferData(GL_UNIFORM_BUFFER, MATERIAL_UBO_LENGTH, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, MATERIAL_UBO_BINDING, this.materialDataUBO);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        this.materialDataBuffer = BufferUtils.createByteBuffer(MATERIAL_UBO_LENGTH);
    }

    public void cleanup() {
        // Clear layout header UBO
        glDeleteBuffers(this.layoutHeaderUBO);
        this.layoutHeaderBuffer.clear();
        // Clear shape UBO
        glDeleteBuffers(this.shapeDataUBO);
        this.shapeDataBuffer.clear();
        // Clear material UBO
        glDeleteBuffers(this.materialDataUBO);
        this.materialDataBuffer.clear();
    }

    public void setMaterialCount(int count) {
        this.layoutHeaderBuffer.putInt(0, count);
        glBindBuffer(GL_UNIFORM_BUFFER, this.layoutHeaderUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.layoutHeaderBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setShapeCount(int count) {
        this.layoutHeaderBuffer.putInt(4, count);
        glBindBuffer(GL_UNIFORM_BUFFER, this.layoutHeaderUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.layoutHeaderBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void updateMaterial(Material material) {
        material.writeBuffer(this.materialDataBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, this.materialDataUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.materialDataBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void updateShape(Shape shape) {
        shape.writeBuffer(this.shapeDataBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, this.shapeDataUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.shapeDataBuffer);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
}