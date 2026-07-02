package mca.client.model;

import mca.entity.EntityVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import mca.util.RangedWeaponUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.lwjgl.opengl.GL11;

public class ModelVillagerMCA extends ModelBiped {
    private ModelRenderer breasts;
    private final boolean renderFemaleDetails;

    public ModelVillagerMCA() {
        this(0.0F);
    }

    public ModelVillagerMCA(float modelSize) {
        this(modelSize, true);
    }

    protected ModelVillagerMCA(float modelSize, boolean renderFemaleDetails) {
        super(modelSize, 0.0F, 64, 64);
        this.renderFemaleDetails = renderFemaleDetails;
        breasts = new ModelRenderer(this, 18, 21);
        breasts.addBox(-3F, 0F, -1F, 6, 3, 3, modelSize);
        breasts.setRotationPoint(0F, 3.5F, -3F);
        breasts.setTextureSize(64, 64);
        breasts.mirror = true;
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
        this.rightArmPose = ArmPose.EMPTY;
        this.leftArmPose = ArmPose.EMPTY;

        if (entity instanceof EntityVillagerMCA && (entity.isHandActive() || ((EntityVillagerMCA)entity).get(EntityVillagerMCA.IS_CHARGING_RANGED_WEAPON)) && isUsingBow(entity)) {
            if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
                this.rightArmPose = ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = ArmPose.BOW_AND_ARROW;
            }
        }

        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
    }

    private boolean isUsingBow(EntityLivingBase entity) {
        return RangedWeaponUtil.isMcaRangedWeapon(entity.getActiveItemStack()) || RangedWeaponUtil.isMcaRangedWeapon(entity.getHeldItem(EnumHand.MAIN_HAND));
    }

    @Override
    public void render(Entity entity, float swing, float swingAmount, float age, float headYaw, float headPitch, float scale) {
        super.render(entity, swing, swingAmount, age, headYaw, headPitch, scale);
        if (!(entity instanceof EntityVillagerMCA)) {
            if (!(entity instanceof EntityZombieVillagerMCA)) {
                return;
            }

            EntityZombieVillagerMCA zombie = (EntityZombieVillagerMCA)entity;
            if (shouldRenderFemaleDetails() && zombie.getGenderForMCA() == EnumGender.FEMALE && !zombie.isChild() && zombie.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == ItemStack.EMPTY) {
                renderBreasts(scale);
            }
            return;
        }

        EntityVillagerMCA villager = (EntityVillagerMCA)entity;
        if (shouldRenderFemaleDetails() && EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)) == EnumGender.FEMALE && !villager.isChild() && villager.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == ItemStack.EMPTY) {
            renderBreasts(scale);
        }
    }

    protected boolean shouldRenderFemaleDetails() {
        return renderFemaleDetails;
    }

    private void renderBreasts(float scale) {
        GL11.glPushMatrix();
        GL11.glTranslated(0.005D, -0.05D, -0.28D);
        GL11.glScaled(1.15D, 1.0D, 1.0D);
        GL11.glRotatef(60.0F, 1.0F, 0.0F, 0.0F);
        breasts.render(scale);
        GL11.glPopMatrix();
    }
}
