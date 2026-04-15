package mca.client.render.layer;

import mca.client.render.RenderZombieVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ZombieClothingLayer extends ZombieVillagerLayer {
    public ZombieClothingLayer(RenderZombieVillagerMCA renderer, ModelBase model) {
        super(renderer, model);
    }

    @Override
    protected void renderLayer(EntityZombieVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        ResourceLocation clothes = getTexture(villager);
        if (clothes != null && canUse(clothes)) {
            renderer.bindTexture(clothes);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            model.render(villager, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    protected ResourceLocation getTexture(EntityZombieVillagerMCA villager) {
        String variant = villager.isBurned() ? "burnt" : "zombie";
        String gender = villager.getGender() == EnumGender.MALE ? "male" : "female";

        // Try to find clothing based on profession and gender
        ResourceLocation clothing = cached("mca:skins/clothing/" + variant + "/" + gender + "/none/0.png");
        if (canUse(clothing)) {
            return clothing;
        }

        // Fallback to normal zombie clothing
        clothing = cached("mca:skins/clothing/zombie/" + gender + "/none/0.png");
        if (canUse(clothing)) {
            return clothing;
        }

        // Final fallback
        return cached("mca:skins/clothing/normal/" + gender + "/none/0.png");
    }
}
