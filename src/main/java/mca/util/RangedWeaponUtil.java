package mca.util;

import mca.compat.TConstructCompat;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;

public final class RangedWeaponUtil {
    private RangedWeaponUtil() {
    }

    public static boolean isMcaRangedWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemBow || TConstructCompat.isCrossbow(stack));
    }
}
