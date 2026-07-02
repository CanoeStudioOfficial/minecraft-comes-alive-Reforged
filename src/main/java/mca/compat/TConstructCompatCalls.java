package mca.compat;

import com.google.common.collect.Lists;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.events.ProjectileEvent;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tools.ProjectileLauncherNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerMaterials;
import slimeknights.tconstruct.tools.ranged.TinkerRangedWeapons;
import slimeknights.tconstruct.tools.ranged.item.CrossBow;

import java.util.List;

final class TConstructCompatCalls {
    private TConstructCompatCalls() {
    }

    static ItemStack getPillagerCrossbow(EntityLivingBase entity) {
        if (TinkerRangedWeapons.crossBow == null) {
            return ItemStack.EMPTY;
        }

        List<Material> parts = Lists.newArrayList(
                TinkerMaterials.wood,
                entity.getRNG().nextInt(2) == 0 ? TinkerMaterials.iron : TinkerMaterials.wood,
                entity.getRNG().nextInt(2) == 0 ? TinkerMaterials.iron : TinkerMaterials.wood,
                TinkerMaterials.string);
        return TinkerRangedWeapons.crossBow.buildItem(parts);
    }

    static boolean isCrossbow(ItemStack stack) {
        return stack.getItem() instanceof CrossBow;
    }

    static void setCrossbowLoaded(ItemStack stack, boolean loaded) {
        ((CrossBow)stack.getItem()).setLoaded(stack, loaded);
    }

    static float getChargeAmount(ItemStack stack, EntityLivingBase entity) {
        return ((CrossBow)stack.getItem()).getDrawbackProgress(stack, entity);
    }

    static void shootCrossbow(EntityVillagerMCA entity, ItemStack stack, EntityLivingBase target) {
        World world = entity.world;
        float power = ItemBow.getArrowVelocity(20) * 7.0F * ProjectileLauncherNBT.from(stack).range;
        EntityArrow projectile = ((ItemArrow)Items.ARROW).createArrow(world, new ItemStack(Items.ARROW), entity);
        double targetX = target.posX - entity.posX;
        double targetY = target.getEntityBoundingBox().minY + (double)(target.height * 0.4F) - projectile.posY;
        double targetZ = target.posZ - entity.posZ;

        projectile.shoot(targetX, targetY, targetZ, power, 0.0F);
        projectile.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
        if (ProjectileEvent.OnLaunch.fireEvent(projectile, stack, entity)) {
            ToolHelper.damageTool(stack, 1, entity);
            world.spawnEntity(projectile);
        }

        setCrossbowLoaded(stack, false);
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ARROW_SHOOT, entity.getSoundCategory(), 1.0F, 1.0F / (entity.getRNG().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }
}
