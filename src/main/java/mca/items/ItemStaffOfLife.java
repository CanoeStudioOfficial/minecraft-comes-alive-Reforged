package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.data.PlayerSaveData;
import mca.entity.data.TransitiveVillagerData;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMemorialType;
import mca.tile.TileMemorial;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStaffOfLife extends Item {
    public ItemStaffOfLife() {
        super();
        maxStackSize = 1;
        setTranslationKey("staff_of_life");
        setMaxDamage(4);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);

            if (tile instanceof TileMemorial) {
                TileMemorial memorial = (TileMemorial) tile;
                TransitiveVillagerData data = memorial.getTransitiveVillagerData();
                //Make sure the owner is the one reviving them.
                if (!memorial.getOwnerUUID().equals(playerIn.getUniqueID())) {
                    playerIn.sendMessage(new TextComponentString(Constants.Color.RED + "You cannot revive " + data.getName() + " because they are not related to you."));
                    return EnumActionResult.FAIL;
                }

                //For rings, they belonged to a spouse. Check for remarriage and forbid.
                PlayerSaveData psd = PlayerSaveData.get(playerIn);
                if (memorial.getType() == EnumMemorialType.BROKEN_RING && psd.isMarriedOrEngaged()) {
                    playerIn.sendMessage(new TextComponentString(Constants.Color.RED + "You cannot revive " + data.getName() + " because you are already married."));
                    return EnumActionResult.FAIL;
                }

                //Once everything is okay, set the tile's revival ticks to begin the revival process.
                memorial.setPlayer(playerIn);
                memorial.setRevivalTicks(20 * 5); // 5 seconds
                stack.damageItem(1, playerIn);
                worldIn.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.AMBIENT, 3.0F, 1.0F);

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!MCA.getConfig().enableRevivals)
            playerIn.sendMessage(new TextComponentString(MCA.getLocalizer().localize("notify.revival.disabled")));

        playerIn.openGui(MCA.getInstance(), Constants.GUI_ID_STAFFOFLIFE, playerIn.world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Uses left: " + (itemStack.getMaxDamage() - itemStack.getItemDamage() + 1));
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add("Use on an item owned by a dead");
            tooltip.add("villager to revive them. Item");
            tooltip.add("must be placed in the world.");
        } else tooltip.add("Hold " + Constants.Color.YELLOW + "SHIFT" + Constants.Color.GRAY + " for info.");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return true;
    }
}
