package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemFamilyTree extends Item {
    public ItemFamilyTree() {
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(MCA.getInstance(), Constants.GUI_ID_FAMILYTREE, world, 0, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (target instanceof EntityVillagerMCA) {
            if (player.world.isRemote) {
                player.openGui(MCA.getInstance(), Constants.GUI_ID_FAMILYTREE, player.world, target.getEntityId(), 0, 0);
            }
            return true;
        }
        return false;
    }
}
