package mca.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;

public class ModelZombieVillagerMCA extends ModelVillagerMCA {
    public ModelZombieVillagerMCA() {
        super();
    }

    public ModelZombieVillagerMCA(float modelSize) {
        super(modelSize);
    }

    public ModelZombieVillagerMCA(float modelSize, boolean renderFemaleDetails) {
        super(modelSize, renderFemaleDetails);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        if (this.leftArmPose == ArmPose.BOW_AND_ARROW || this.rightArmPose == ArmPose.BOW_AND_ARROW) {
            return;
        }

        boolean armsRaised = entityIn instanceof EntityZombie && ((EntityZombie)entityIn).isArmsRaised();
        float swing = MathHelper.sin(this.swingProgress * (float)Math.PI);
        float swingEase = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
        float armAngle = -(float)Math.PI / (armsRaised ? 1.5F : 2.25F);

        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightArm.rotateAngleY = -(0.1F - swing * 0.6F);
        this.bipedLeftArm.rotateAngleY = 0.1F - swing * 0.6F;
        this.bipedRightArm.rotateAngleX = armAngle;
        this.bipedLeftArm.rotateAngleX = armAngle;
        this.bipedRightArm.rotateAngleX += swing * 1.2F - swingEase * 0.4F;
        this.bipedLeftArm.rotateAngleX += swing * 1.2F - swingEase * 0.4F;
        this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
