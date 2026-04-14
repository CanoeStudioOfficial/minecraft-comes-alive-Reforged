package mca.client.render.layer;

import mca.client.render.RenderZombieVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ZombieFaceLayer extends ZombieVillagerLayer {
    private static final int FACE_COUNT = 22;

    public ZombieFaceLayer(RenderZombieVillagerMCA renderer, ModelBiped model) {
        super(renderer, model);
    }

    @Override
    protected void renderLayer(EntityZombieVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing) {
        // Only render head and hat parts for face
        model.bipedLeftLeg.showModel = false;
        model.bipedRightLeg.showModel = false;
        model.bipedLeftArm.showModel = false;
        model.bipedRightArm.showModel = false;
        model.bipedBody.showModel = false;
        model.bipedHead.showModel = true;
        model.bipedHeadwear.showModel = true;

        ResourceLocation face = getTexture(villager);
        if (face != null) {
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
    }

    @Override
    protected ResourceLocation getTexture(EntityZombieVillagerMCA villager) {
        float faceGene = villager.getGeneticsTag().getFloat("Face");
        int index = (int) Math.min(FACE_COUNT - 1, Math.max(0, faceGene * FACE_COUNT));
        String gender = villager.getGender() == EnumGender.MALE ? "male" : "female";

        // Zombie face variant
        return cached("mca:skins/face/zombie/" + gender + "/" + index + ".png");
    }
}
