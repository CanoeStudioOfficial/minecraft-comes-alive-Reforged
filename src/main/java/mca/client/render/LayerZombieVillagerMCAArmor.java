package mca.client.render;

import mca.client.model.ModelZombieVillagerMCA;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;

public class LayerZombieVillagerMCAArmor extends LayerBipedArmor {
    public LayerZombieVillagerMCAArmor(RenderLivingBase<?> rendererIn) {
        super(rendererIn);
    }

    @Override
    protected void initArmor() {
        this.modelLeggings = new ModelZombieVillagerMCA(0.5F, false);
        this.modelArmor = new ModelZombieVillagerMCA(1.0F, false);
    }
}
