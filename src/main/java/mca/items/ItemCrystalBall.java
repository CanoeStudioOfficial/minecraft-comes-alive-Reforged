package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCrystalBall extends Item
{
	public ItemCrystalBall()
	{
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		this.onItemRightClick(world, player, hand);
		return EnumActionResult.PASS;
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) 
	{
		player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST_FAR, 0.5F, 1.0F);
		player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
		
		player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) 
	{
		return true;
	}
}
