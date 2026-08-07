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

public class ItemWeddingRing extends ItemSpecialCaseGift {
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {

        if (!MCA.getConfig().allowPlayerMarriage) {
            player.sendMessage(new TextComponentTranslation("gui.marriage.failed"));
            return false;
        }

        PlayerSaveData playerData = PlayerSaveData.get(player);
        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        String response;

        if (villager.isMarriedTo(player.getUniqueID()))
            response = "interaction.marry.fail.marriedtogiver";
        else if (villager.isMarried())
            response = "interaction.marry.fail.marriedtoother";
        else if (villager.isEngaged() && !villager.isEngagedTo(player.getUniqueID()))
            response = "interaction.engage.fail.engagedtoother";
        else if (playerData.isMarriedOrEngaged() && !playerData.isEngagedTo(villager.getUniqueID()))
            response = "interaction.marry.fail.playermarried";
        else if (!playerData.isEngagedTo(villager.getUniqueID())
                && history.getHearts() < MCA.getConfig().marriageHeartsRequirement)
            response = "interaction.marry.fail.lowhearts";
        else {
            response = "interaction.marry.success";
            playerData.marry(villager.getUniqueID(), villager.get(EntityVillagerMCA.VILLAGER_NAME));
            villager.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.SPOUSE);
            villager.spawnParticles(EnumParticleTypes.HEART);
            villager.marry(player);
        }

        villager.say(Optional.of(player), response);
        return response.equals("interaction.marry.success");
    }

    protected boolean handleEngagement(EntityPlayer player, EntityVillagerMCA villager) {
        if (!MCA.getConfig().allowPlayerMarriage) {
            player.sendMessage(new TextComponentTranslation("gui.marriage.failed"));
            return false;
        }

        PlayerSaveData playerData = PlayerSaveData.get(player);
        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        String response;

        if (villager.isMarriedTo(player.getUniqueID()) || villager.isEngagedTo(player.getUniqueID()))
            response = "interaction.marry.fail.marriedtogiver";
        else if (villager.isMarried())
            response = "interaction.marry.fail.marriedtoother";
        else if (villager.isEngaged())
            response = "interaction.engage.fail.engagedtoother";
        else if (playerData.isMarriedOrEngaged())
            response = "interaction.marry.fail.playermarried";
        else if (history.getHearts() < Math.max(1, MCA.getConfig().marriageHeartsRequirement / 2))
            response = "interaction.marry.fail.lowhearts";
        else {
            response = "interaction.engage.success";
            playerData.engage(villager.getUniqueID(), villager.get(EntityVillagerMCA.VILLAGER_NAME));
            villager.engage(player);
            villager.spawnParticles(EnumParticleTypes.HEART);
        }

        villager.say(Optional.of(player), response);
        return response.equals("interaction.engage.success");
    }
}
