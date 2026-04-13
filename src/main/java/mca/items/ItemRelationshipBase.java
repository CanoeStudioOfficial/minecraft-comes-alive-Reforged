package mca.items;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumMarriageState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import static mca.entity.EntityVillagerMCA.MARRIAGE_STATE;

public abstract class ItemRelationshipBase extends ItemSpecialCaseGift {
    public abstract int getHeartsRequired();

    @Override
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        PlayerSaveData playerData = PlayerSaveData.get(player);
        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        String response;

        if (villager.isChild()) {
            response = "interaction.relationship.fail.isbaby";
        } else if (villager.playerIsParent(player)) {
            response = "interaction.relationship.fail.isparent";
        } else if (villager.isMarriedTo(player.getUniqueID())) {
            response = "interaction.relationship.fail.marriedtogiver";
        } else if (villager.isMarried()) {
            response = "interaction.relationship.fail.married";
        } else if (villager.get(MARRIAGE_STATE) == EnumMarriageState.ENGAGED.getId() && !villager.isEngagedTo(player.getUniqueID())) {
            response = "interaction.relationship.fail.engaged";
        } else if (playerData.getMarriageState() == EnumMarriageState.MARRIED) {
            response = "interaction.relationship.fail.playermarried";
        } else if (history.getHearts() < getHeartsRequired()) {
            response = "interaction.relationship.fail.lowhearts";
        } else if (!villager.canBeAttractedTo(playerData)) {
            response = "interaction.relationship.fail.incompatible";
        } else {
            return false;
        }

        villager.say(com.google.common.base.Optional.of(player), response);
        return true;
    }
}
