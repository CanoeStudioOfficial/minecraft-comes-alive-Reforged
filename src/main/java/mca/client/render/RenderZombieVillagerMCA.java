package mca.client.render;

import mca.client.model.ModelZombieVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderZombieVillagerMCA extends RenderBiped<EntityZombieVillagerMCA> {
    public RenderZombieVillagerMCA(RenderManager manager) {
        super(manager, new ModelZombieVillagerMCA(), 0.5F);
        this.addLayer(new LayerZombieVillagerMCAArmor(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityZombieVillagerMCA entity) {
        return ZombieVillagerMCATextureCache.getZombieTexture(entity.getOriginalTextureForMCA(), entity.getGenderForMCA(), entity.getTextureResourceLocation());
    }

    @Override
    protected void preRenderCallback(EntityZombieVillagerMCA entity, float partialTickTime) {
        if (entity.getCurrentAgeStateForMCA() != mca.enums.EnumAgeState.ADULT) {
            float scale = entity.getRenderScaleForAge();
            GlStateManager.scale(scale, scale, scale);
        }
    }

    @Override
    protected void applyRotations(EntityZombieVillagerMCA entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entityLiving.isConverting()) {
            rotationYaw += (float)(Math.cos((double)entityLiving.ticksExisted * 3.25D) * Math.PI * 0.25D);
        }

        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);
    }
}
