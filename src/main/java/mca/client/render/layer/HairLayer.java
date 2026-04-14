package mca.client.render.layer;

import mca.client.render.RenderVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Genetics;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class HairLayer extends VillagerLayer {
    public HairLayer(RenderVillagerMCA renderer, ModelBiped model) {
        super(renderer, model);
    }

    @Override
    protected void renderLayer(EntityVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        // Only render head and hat parts for hair
        model.bipedLeftLeg.showModel = false;
        model.bipedRightLeg.showModel = false;
        model.bipedLeftArm.showModel = false;
        model.bipedRightArm.showModel = false;
        model.bipedBody.showModel = false;
        model.bipedHead.showModel = true;
        model.bipedHeadwear.showModel = true;

        ResourceLocation hair = getTexture(villager);
        if (hair != null && canUse(hair)) {
            int color = getColor(villager, partialTicks);
            renderer.bindTexture(hair);

            GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
            model.render(villager, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // Reset visibility
        model.bipedLeftLeg.showModel = true;
        model.bipedRightLeg.showModel = true;
        model.bipedLeftArm.showModel = true;
        model.bipedRightArm.showModel = true;
        model.bipedBody.showModel = true;
    }

    @Override
    protected ResourceLocation getTexture(EntityVillagerMCA villager) {
        String hairId = villager.getHair();
        if (hairId == null || hairId.isEmpty()) {
            // Default hair based on genetics
            Genetics genetics = new Genetics(villager);
            int hair = (int) Math.min(39, Math.max(0, genetics.getGene(Genetics.SKIN) * 40));
            String gender = genetics.getGender().getDataName();
            return cached("mca:skins/hair/" + gender + "/" + hair + ".png");
        }
        return cached(hairId);
    }

    @Override
    protected int getColor(EntityVillagerMCA villager, float partialTicks) {
        Genetics genetics = new Genetics(villager);

        float eumelanin = genetics.getGene(Genetics.EUMELANIN);
        float pheomelanin = genetics.getGene(Genetics.PHEOMELANIN);

        // Calculate hair color based on melanin types
        // Eumelanin = black/brown, Pheomelanin = red/blonde
        float r = 0.1f + eumelanin * 0.4f + pheomelanin * 0.8f;
        float g = 0.05f + eumelanin * 0.3f + pheomelanin * 0.4f;
        float b = 0.02f + eumelanin * 0.2f;

        int red = (int) (Math.min(1.0f, Math.max(0.0f, r)) * 255);
        int green = (int) (Math.min(1.0f, Math.max(0.0f, g)) * 255);
        int blue = (int) (Math.min(1.0f, Math.max(0.0f, b)) * 255);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
