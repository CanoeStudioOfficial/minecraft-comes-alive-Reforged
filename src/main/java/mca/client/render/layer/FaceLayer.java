package mca.client.render.layer;

import mca.client.render.RenderVillagerMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Genetics;
import mca.entity.data.Traits;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class FaceLayer extends VillagerLayer {
    private static final int FACE_COUNT = 22;
    private final String variant;

    public FaceLayer(RenderVillagerMCA renderer, ModelBiped model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    protected void renderLayer(EntityVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        // Only render head for face
        model.bipedLeftLeg.showModel = false;
        model.bipedRightLeg.showModel = false;
        model.bipedLeftArm.showModel = false;
        model.bipedRightArm.showModel = false;
        model.bipedBody.showModel = false;
        model.bipedHead.showModel = true;
        model.bipedHeadwear.showModel = false;

        ResourceLocation face = getTexture(villager);
        if (face != null && canUse(face)) {
            renderer.bindTexture(face);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            model.render(villager, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disableBlend();
        }

        // Reset visibility
        model.bipedLeftLeg.showModel = true;
        model.bipedRightLeg.showModel = true;
        model.bipedLeftArm.showModel = true;
        model.bipedRightArm.showModel = true;
        model.bipedBody.showModel = true;
        model.bipedHeadwear.showModel = true;
    }

    @Override
    protected ResourceLocation getTexture(EntityVillagerMCA villager) {
        Genetics genetics = new Genetics(villager);
        Traits traits = new Traits(villager);

        int index = (int) Math.min(FACE_COUNT - 1, Math.max(0, genetics.getGene(Genetics.FACE) * FACE_COUNT));
        int time = villager.ticksExisted / 2 + (int) (genetics.getGene(Genetics.HEMOGLOBIN) * 65536);
        boolean blink = time % 50 == 1 || time % 57 == 1 || villager.isSleeping() || !villager.isEntityAlive();
        boolean hasHeterochromia = variant.equals("normal") && traits.hasTrait(Traits.HETEROCHROMIA);
        String gender = genetics.getGender().getStrName();
        String blinkTexture = blink ? "_blink" : (hasHeterochromia ? "_hetero" : "");

        return cached("mca:skins/face/" + variant + "/" + gender + "/" + index + blinkTexture + ".png");
    }
}
