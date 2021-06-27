package shape;

import rendererOld.RenderContext;
import rendererOld.RenderMaterial;
import rendererOld.RenderShape;
import rendererOld.RenderShapeType;

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