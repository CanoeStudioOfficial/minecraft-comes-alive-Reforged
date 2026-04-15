package mca.items;

import mca.core.MCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class ItemScythe extends ItemSword {
    private static final String NBT_HAS_SOUL = "hasSoul";
    private static final String NBT_ACTIVE = "active";

    public ItemScythe() {
        super(ToolMaterial.GOLD);
        this.setMaxStackSize(1);
    }

    public static void setSoul(ItemStack stack, boolean soul) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean(NBT_HAS_SOUL, soul);
    }

    public static boolean hasSoul(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        return stack.getTagCompound().getBoolean(NBT_HAS_SOUL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.GRAY + MCA.getLocalizer().localize("item.scythe.tooltip"));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        boolean active = stack.hasTagCompound() && stack.getTagCompound().getBoolean(NBT_ACTIVE);
        Random rand = world.rand;

        if (active != isSelected) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setBoolean(NBT_ACTIVE, isSelected);

            float baseVolume = isSelected ? 0.75F : 0.25F;
            world.playSound(null, entity.getPosition(), SoundsMCA.reaper_scythe_out, SoundCategory.PLAYERS,
                    baseVolume + rand.nextFloat() / 2F,
                    0.65F + rand.nextFloat() / 10F
            );
        }

        if (isSelected) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (living.swingProgressInt == -1) {
                world.playSound(null, entity.getPosition(), SoundsMCA.reaper_scythe_swing, SoundCategory.PLAYERS, 0.25F, 1.0F);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        Random rand = attacker.world.rand;

        // 20% chance to apply wither effect
        if (rand.nextInt(50) > 40) {
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1000, 1));
        }

        net.minecraft.util.SoundEvent sound = SoundsMCA.reaper_scythe_out;

        // Collect soul when killing a villager
        if (!hasSoul(stack) && target.getHealth() <= 0 && target instanceof EntityVillagerMCA) {
            setSoul(stack, true);
            sound = net.minecraft.init.SoundEvents.BLOCK_BELL_RESONATE;
        }

        attacker.world.playSound(null, attacker.getPosition(), sound, SoundCategory.PLAYERS,
                0.75F + rand.nextFloat() / 2F,
                0.75F + rand.nextFloat() / 2F
        );

        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return super.hasEffect(stack) || hasSoul(stack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return toRepair.getItem() == repair.getItem();
    }
}
