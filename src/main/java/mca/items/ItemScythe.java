package mca.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.List;

public class ItemScythe extends ItemSword {
    private static final String HAS_SOUL = "mcaScytheHasSoul";
    private static final double ATTACK_DAMAGE = 10.0D;
    private static final double ATTACK_SPEED = -2.4D;

    public ItemScythe() {
        super(ToolMaterial.GOLD);
        this.setMaxStackSize(1);
    }

    public static void setSoul(ItemStack stack, boolean soul) {
        getTag(stack).setBoolean(HAS_SOUL, soul);
    }

    public static boolean hasSoul(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(HAS_SOUL);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (!attacker.world.isRemote && attacker.world.rand.nextInt(50) > 40) {
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1000, 1));
        }

        if (!attacker.world.isRemote && !hasSoul(stack) && target.getHealth() <= 0.0F && target instanceof EntityVillagerMCA) {
            setSoul(stack, true);
            attacker.world.playSound(null, attacker.getPosition(), SoundsMCA.reaper_scythe_out, SoundCategory.HOSTILE, 0.9F, 0.75F + attacker.world.rand.nextFloat() / 2.0F);
        } else if (!attacker.world.isRemote) {
            attacker.world.playSound(null, attacker.getPosition(), SoundsMCA.reaper_scythe_swing, SoundCategory.HOSTILE, 0.25F, 1.0F);
        }

        return super.hitEntity(stack, target, attacker);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();

        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", ATTACK_DAMAGE, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", ATTACK_SPEED, 0));
        }

        return modifiers;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(MCA.getLocalizer().localize("tooltip.scythe.desc1"));
        tooltip.add(MCA.getLocalizer().localize(hasSoul(stack) ? "tooltip.scythe.soul" : "tooltip.scythe.empty", Constants.Color.GRAY, Constants.Color.GRAY));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return hasSoul(stack);
    }

    private static NBTTagCompound getTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        return stack.getTagCompound();
    }
}
