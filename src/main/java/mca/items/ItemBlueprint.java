package mca.items;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * 蓝图物品
 * 对应1.21.1版本的BlueprintItem
 */
public class ItemBlueprint extends Item {

    public ItemBlueprint() {
        setTranslationKey("blueprint");
        setRegistryName(MCA.MODID, "blueprint");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            // 服务器端打开蓝图GUI
            player.sendMessage(new TextComponentString("Blueprint GUI would open here"));
            // 实际实现需要发送网络包打开GUI
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}
