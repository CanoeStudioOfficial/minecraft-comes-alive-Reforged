package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemCrystalBall extends Item {
    public ItemCrystalBall() {
        super();
        maxStackSize = 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(MCA.getInstance(), Constants.GUI_ID_SETUP, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
