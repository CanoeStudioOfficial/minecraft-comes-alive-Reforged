package mca.items;

import com.google.common.base.Optional;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWeddingRing extends ItemRelationshipBase {
    @Override
    public int getHeartsRequired() {
        return MCA.getConfig().marriageHeartsRequirement;
    }

    @Override
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        if (!MCA.getConfig().allowPlayerMarriage) {
            player.sendMessage(new TextComponentTranslation("gui.marriage.failed"));
            return true;
        }

        if (super.handle(player, villager)) {
            return true;
        }

        PlayerSaveData playerData = PlayerSaveData.get(player);
        String response;

        response = "interaction.marry.success";
        playerData.marry(villager.getUniqueID(), villager.get(EntityVillagerMCA.VILLAGER_NAME));
        villager.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.SPOUSE);
        villager.spawnParticles(EnumParticleTypes.HEART);
        villager.marry(player);
        villager.modifyMood(15); // Positive mood boost for marriage
        playerData.updateFamilyTreeNode(player);

        villager.say(Optional.of(player), response);
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(MCA.getLocalizer().localize("item.wedding_ring.tooltip"));
    }
}
