package shape;

import renderer.RenderContext;
import renderer.RenderMaterial;
import renderer.RenderShape;
import renderer.RenderShapeType;

public abstract class Shape {

    protected RenderShape shape;

    public Shape(RenderContext context, RenderShapeType type) {
        this.shape = context.createShape();
        this.shape.setType(type);
    }

    public RenderShape getShape() {
        return this.shape;
    }

    public void setPosition(float x, float y, float z) {
        this.shape.setPosition(x, y, z);
    }

    public void setMaterial(RenderMaterial material) {
        this.shape.setMaterial(material);
    }
}