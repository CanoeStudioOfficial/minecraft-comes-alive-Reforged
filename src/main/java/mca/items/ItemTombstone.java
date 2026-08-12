package mca.items;

import mca.blocks.BlockTombstone;
import mca.core.minecraft.BlocksMCA;
import mca.tile.TileTombstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Places an editable MCA tombstone and opens the vanilla four-line editor. */
public class ItemTombstone extends Item {
    public ItemTombstone() {
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.UP) {
            return EnumActionResult.PASS;
        }

        BlockPos target = pos.offset(facing);
        if (!BlocksMCA.TOMBSTONE.canPlaceBlockAt(world, target)) {
            return EnumActionResult.FAIL;
        }

        int rotation = MathHelper.floor((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
        world.setBlockState(target, BlocksMCA.TOMBSTONE.getDefaultState().withProperty(BlockTombstone.ROTATION, rotation), 3);

        TileEntity tile = world.getTileEntity(target);
        if (tile instanceof TileTombstone) {
            player.openEditSign((TileTombstone) tile);
        }

        if (!player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
