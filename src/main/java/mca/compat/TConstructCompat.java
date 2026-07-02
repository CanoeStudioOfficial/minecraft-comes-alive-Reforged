package mca.compat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import mca.entity.EntityVillagerMCA;

public final class TConstructCompat {
    private static final String MODID = "tconstruct";

    private TConstructCompat() {
    }

    public static boolean canUseWithRaids() {
        return RaidsBackportCompat.isLoaded() && Loader.isModLoaded(MODID);
    }

    public static ItemStack getPillagerCrossbow(EntityLivingBase entity) {
        if (!canUseWithRaids()) {
            return ItemStack.EMPTY;
        }

        try {
            return TConstructCompatCalls.getPillagerCrossbow(entity);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean isCrossbow(ItemStack stack) {
        if (stack.isEmpty() || !Loader.isModLoaded(MODID)) {
            return false;
        }

        try {
            return TConstructCompatCalls.isCrossbow(stack);
        } catch (Throwable t) {
            return false;
        }
    }

    public static float getChargeAmount(ItemStack stack, EntityLivingBase entity) {
        if (!isCrossbow(stack)) {
            return 0.0F;
        }

        try {
            return TConstructCompatCalls.getChargeAmount(stack, entity);
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    public static boolean shootCrossbow(EntityVillagerMCA entity, ItemStack stack, EntityLivingBase target) {
        if (!isCrossbow(stack)) {
            return false;
        }

        try {
            TConstructCompatCalls.shootCrossbow(entity, stack, target);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void setCrossbowLoaded(ItemStack stack, boolean loaded) {
        if (!isCrossbow(stack)) {
            return;
        }

        try {
            TConstructCompatCalls.setCrossbowLoaded(stack, loaded);
        } catch (Throwable ignored) {
        }
    }
}
