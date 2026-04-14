package mca.client.render;

import mca.client.model.ModelVillagerMCA;
import mca.client.render.layer.ZombieClothingLayer;
import mca.client.render.layer.ZombieFaceLayer;
import mca.client.render.layer.ZombieHairLayer;
import mca.client.render.layer.ZombieSkinLayer;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class RenderZombieVillagerMCA extends RenderBiped<EntityZombieVillagerMCA> {
    private Random rand = new Random();

    public RenderZombieVillagerMCA(RenderManager manager) {
        super(manager, new ModelVillagerMCA(), 0.5F);

        // Add layered skin system for zombie villagers
        ModelVillagerMCA model = (ModelVillagerMCA) this.getMainModel();
        this.addLayer(new ZombieSkinLayer(this, model));
        this.addLayer(new ZombieFaceLayer(this, model));
        this.addLayer(new ZombieHairLayer(this, model));
        this.addLayer(new ZombieClothingLayer(this, model));

        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
    }

    @Override
    protected void preRenderCallback(EntityZombieVillagerMCA entity, float partialTickTime) {
        // Get genetics from NBT
        float size = entity.getGeneticsTag().getFloat("Size");
        float width = entity.getGeneticsTag().getFloat("Width");

        float verticalScale = 0.75F + size / 2;
        float horizontalScale = 0.75F + width / 2;

        // Apply baby scale if needed
        if (entity.isChild()) {
            float babyScale = 0.5F;
            GlStateManager.scale(babyScale * horizontalScale, babyScale * verticalScale, babyScale * horizontalScale);
        } else {
            GlStateManager.scale(horizontalScale, verticalScale, horizontalScale);
        }

        // Shaking effect when converting
        if (entity.isShaking()) {
            float shake = entity.getConversionTime() / 3600.0F;
            float shakeIntensity = 0.05F * shake;
            GlStateManager.translate(
                (this.rand.nextFloat() - 0.5F) * shakeIntensity,
                0.0F,
                (this.rand.nextFloat() - 0.5F) * shakeIntensity
            );
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityZombieVillagerMCA entity) {
        // Return zombie skin texture based on gender
        String gender = entity.getGender() == EnumGender.MALE ? "male" : "female";
        return new ResourceLocation("mca", "textures/entity/zombievillager_" + gender + ".png");
    }
}
