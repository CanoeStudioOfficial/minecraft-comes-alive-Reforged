package mca.client.model;

import mca.entity.EntityZombieVillagerMCA;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelZombieVillagerMCA extends ModelBiped {

    public ModelZombieVillagerMCA() {
        super(0.0F, 0.0F, 64, 64);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        EntityZombieVillagerMCA zombie = (EntityZombieVillagerMCA) entityIn;

        // Animate zombie arms (both arms raised when attacking)
        float attackTime = zombie.getAttackTarget() != null ? 1.0F : 0.0F;
        animateZombieArms(this.bipedLeftArm, this.bipedRightArm, attackTime, ageInTicks);
    }

    private void animateZombieArms(ModelRenderer leftArm, ModelRenderer rightArm, float attackTime, float ageInTicks) {
        // Raise both arms like a zombie
        float f = MathHelper.sin(attackTime * (float) Math.PI);
        float f1 = MathHelper.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * (float) Math.PI);

        // Right arm
        rightArm.rotateAngleZ = 0.0F;
        rightArm.rotateAngleY = -(0.1F - f * 0.6F);
        rightArm.rotateAngleX = -((float) Math.PI / 2F);
        rightArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
        rightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        rightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

        // Left arm (mirror of right)
        leftArm.rotateAngleZ = 0.0F;
        leftArm.rotateAngleY = 0.1F - f * 0.6F;
        leftArm.rotateAngleX = -((float) Math.PI / 2F);
        leftArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
        leftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        leftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
