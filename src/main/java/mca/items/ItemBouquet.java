package mca.items;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import mca.enums.EnumMarriageState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemBouquet extends ItemRelationshipBase {
    @Override
    public int getHeartsRequired() {
        return MCA.getConfig().bouquetHeartsRequirement;
    }

    @Override
    public boolean handle(EntityPlayer player, EntityVillagerMCA villager) {
        if (super.handle(player, villager)) {
            return true;
        }

        PlayerSaveData playerData = PlayerSaveData.get(player);
        String response;

        if (villager.getMarriageState() == EnumMarriageState.PROMISED) {
            response = "interaction.promise.fail.promised";
        } else {
            response = "interaction.promise.success";
            playerData.promise(villager.getUniqueID(), villager.get(EntityVillagerMCA.VILLAGER_NAME));
            villager.promise(player);
            villager.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.SPOUSE);
            villager.spawnParticles(EnumParticleTypes.HEART);
            playerData.updateFamilyTreeNode(player);
        }

        villager.say(Optional.of(player), response);
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(MCA.getLocalizer().localize("item.bouquet.tooltip"));
    }
}
