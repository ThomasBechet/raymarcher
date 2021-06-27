package rendererOld.asset;

import rendererOld.memory.MemoryManager;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private MemoryManager memoryManager;
    private Map<Integer, Material> materials;
    private int nextShapeTypeID;

    public AssetManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
        this.materials = new HashMap<>();
        this.nextShapeTypeID = 0;
    }

    public Material createMaterial() {
        Material material = new Material(this, this.nextShapeTypeID++);
        this.materials.put(material.getIndex(), material);
        this.memoryManager.updateMaterial(material);
        this.memoryManager.setMaterialCount(this.materials.size());
        return material;
    }

    public void updateMaterial(Material material) {
        this.memoryManager.updateMaterial(material);
    }
}