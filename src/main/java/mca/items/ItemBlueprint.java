package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/** The high-version style blueprint catalog. It never places structures. */
public class ItemBlueprint extends Item {
    public ItemBlueprint() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(MCA.getInstance(), Constants.GUI_ID_BLUEPRINT, world, 0, 0, 0);
        }
        return new ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
