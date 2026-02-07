package mca.items;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.data.PlayerSaveData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		return EnumActionResult.SUCCESS;
	}	

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) 
	{
		if (world.isRemote)
		{
			handleClientRightClick(player, world);
			
			for (int i = 0; i < 20; i++) {
				world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL, 
					player.posX + (world.rand.nextDouble() - 0.5D) * (double)player.width, 
					player.posY + world.rand.nextDouble() * (double)player.height - 0.25D, 
					player.posZ + (world.rand.nextDouble() - 0.5D) * (double)player.width, 
					(world.rand.nextDouble() - 0.5D) * 2.0D, -world.rand.nextDouble(), (world.rand.nextDouble() - 0.5D) * 2.0D);
			}
		}
		else
		{
			// Server-side data update
			PlayerSaveData data = PlayerSaveData.get(player);
			data.setHasChosenDestiny(false);
		}
		
		player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST_FAR, 0.5F, 1.0F);
		player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
		
		ItemStack stack = player.getHeldItem(hand);
		if (!player.capabilities.isCreativeMode)
		{
			stack.shrink(1);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}
	
	@SideOnly(Side.CLIENT)
	private void handleClientRightClick(EntityPlayer player, World world)
	{
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		
		if (mc.isIntegratedServerRunning())
		{
			spawnDestinyRoom(player);
		}
		else
		{
			player.openGui(MCA.getInstance(), Constants.GUI_ID_SETUP, world, (int)player.posX, (int)player.posY, (int)player.posZ);
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnDestinyRoom(EntityPlayer player)
	{
		EntityPlayerSP playerSP = (EntityPlayerSP)player;
		MCA.playPortalAnimation = true;
		playerSP.timeInPortal = 6.0F;
		playerSP.prevTimeInPortal = 0.0F;
		
		MCA.destinySpawnFlag = true; //Will hand off spawning to clientTickEvent
		MCA.destinyCenterPoint = new BlockPos(player.posX, player.posY + 1, player.posZ);
		
		player.sendMessage(new net.minecraft.util.text.TextComponentTranslation("notify.crystalball.tutorial"));
		
		player.setPositionAndRotation(player.posX, player.posY, player.posZ, 180.0F, 0.0F);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) 
	{
		return true;
	}
}
