package renderer.entity;

import renderer.memory.MemoryManager;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private MemoryManager memoryManager;
    private Map<Integer, Shape> shapes;
    private int nextShapeID;
    private Camera camera;

    public SceneManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        this.shapes = new HashMap<>();
        this.nextShapeID = 0;
        this.camera = new Camera();
    }

    public Shape createShape() {
        Shape shape = new Shape(this, this.nextShapeID++);
        this.shapes.put(shape.getIndex(), shape);
        this.memoryManager.updateShape(shape);
        this.memoryManager.setShapeCount(this.shapes.size());
        return shape;
    }

    public void updateShape(Shape shape) {
        this.memoryManager.updateShape(shape);
    }

    public Camera getActiveCamera() {
        return this.camera;
    }
}