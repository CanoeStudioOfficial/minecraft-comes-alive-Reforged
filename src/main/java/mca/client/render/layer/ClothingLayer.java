package mca.client.render.layer;

import mca.client.render.RenderVillagerMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ClothingLayer extends VillagerLayer {
    private final String variant;

    public ClothingLayer(RenderVillagerMCA renderer, ModelBiped model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    protected void renderLayer(EntityVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        ResourceLocation clothes = getTexture(villager);
        if (clothes != null && canUse(clothes)) {
            renderer.bindTexture(clothes);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            model.render(villager, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    protected ResourceLocation getTexture(EntityVillagerMCA villager) {
        String v = villager.isBurned() ? "burnt" : variant;
        String clothesId = villager.getClothes();

        if (clothesId == null || clothesId.isEmpty()) {
            // Use default clothing based on profession
            return getDefaultClothing(villager, v);
        }

        // Try variant first, then fallback to default
        ResourceLocation id = cached(clothesId);
        ResourceLocation idNew = cached(clothesId.replace("normal", v));

        if (canUse(idNew)) {
            return idNew;
        }
        return id;
    }

    private ResourceLocation getDefaultClothing(EntityVillagerMCA villager, String variant) {
        String profession = villager.getProfessionForge().getRegistryName().toString().replace(":", "/");
        String gender = mca.enums.EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)).getStrName();

        // Try to find clothing based on profession and gender
        ResourceLocation clothing = cached("mca:skins/clothing/" + variant + "/" + gender + "/" + profession + "/0.png");
        if (canUse(clothing)) {
            return clothing;
        }

        // Fallback to none profession
        clothing = cached("mca:skins/clothing/" + variant + "/" + gender + "/none/0.png");
        if (canUse(clothing)) {
            return clothing;
        }

        // Final fallback
        return cached("mca:skins/clothing/normal/" + gender + "/none/0.png");
    }
}
