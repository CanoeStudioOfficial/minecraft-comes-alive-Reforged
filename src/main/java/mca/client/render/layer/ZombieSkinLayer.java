package mca.client.render.layer;

import mca.client.render.RenderZombieVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ZombieSkinLayer extends ZombieVillagerLayer {
    public ZombieSkinLayer(RenderZombieVillagerMCA renderer, ModelBiped model) {
        super(renderer, model);
    }

    @Override
    protected void renderLayer(EntityZombieVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        ResourceLocation skin = getTexture(villager);
        if (skin != null && canUse(skin)) {
            int color = getColor(villager, partialTicks);
            renderer.bindTexture(skin);

            GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
            model.render(villager, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    protected ResourceLocation getTexture(EntityZombieVillagerMCA villager) {
        // Get skin from genetics
        float skin = villager.getGeneticsTag().getFloat("Skin");
        int skinIndex = (int) Math.min(4, Math.max(0, skin * 5));
        String gender = villager.getGender() == EnumGender.MALE ? "male" : "female";
        return cached("mca:skins/skin/" + gender + "/" + skinIndex + ".png");
    }

    @Override
    protected int getColor(EntityZombieVillagerMCA villager, float partialTicks) {
        // Zombie skin color - greenish tint
        float melanin = villager.getGeneticsTag().getFloat("Melanin");
        float hemoglobin = villager.getGeneticsTag().getFloat("Hemoglobin");

        // Calculate skin color based on melanin and hemoglobin
        float r = 0.6f - melanin * 0.4f + hemoglobin * 0.1f;
        float g = 0.8f - melanin * 0.3f + hemoglobin * 0.05f;
        float b = 0.4f - melanin * 0.3f;

        // Apply zombie green tint
        r = r * 0.7f;
        g = g * 0.9f;
        b = b * 0.6f;

        int red = (int) (Math.min(1.0f, Math.max(0.0f, r)) * 255);
        int green = (int) (Math.min(1.0f, Math.max(0.0f, g)) * 255);
        int blue = (int) (Math.min(1.0f, Math.max(0.0f, b)) * 255);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
