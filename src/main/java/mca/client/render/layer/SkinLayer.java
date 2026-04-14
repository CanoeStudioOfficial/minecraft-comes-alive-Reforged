package mca.client.render.layer;

import mca.client.render.RenderVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Genetics;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class SkinLayer extends VillagerLayer {
    public SkinLayer(RenderVillagerMCA renderer, ModelBiped model) {
        super(renderer, model);
    }

    @Override
    protected void renderLayer(EntityVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
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
    protected ResourceLocation getTexture(EntityVillagerMCA villager) {
        Genetics genetics = new Genetics(villager);
        int skin = (int) Math.min(4, Math.max(0, genetics.getGene(Genetics.SKIN) * 5));
        String gender = genetics.getGender().getDataName();
        return cached("mca:skins/skin/" + gender + "/" + skin + ".png");
    }

    @Override
    protected int getColor(EntityVillagerMCA villager, float partialTicks) {
        Genetics genetics = new Genetics(villager);

        float melanin = genetics.getGene(Genetics.MELANIN);
        float hemoglobin = genetics.getGene(Genetics.HEMOGLOBIN);
        float infection = villager.getInfectionProgress();

        // Calculate skin color based on melanin and hemoglobin
        float r = 1.0f - melanin * 0.6f + hemoglobin * 0.1f;
        float g = 0.8f - melanin * 0.5f + hemoglobin * 0.05f;
        float b = 0.6f - melanin * 0.4f;

        // Apply infection effect (greenish tint)
        r = r * (1.0f - infection * 0.5f);
        g = g * (1.0f - infection * 0.2f) + infection * 0.3f;
        b = b * (1.0f - infection * 0.5f);

        int red = (int) (Math.min(1.0f, Math.max(0.0f, r)) * 255);
        int green = (int) (Math.min(1.0f, Math.max(0.0f, g)) * 255);
        int blue = (int) (Math.min(1.0f, Math.max(0.0f, b)) * 255);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
