package mca.entity.data;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerHistory {
    private int hearts;
    private int interactionFatigue;
    private boolean giftPresent;
    private int greetTimer;
    private EnumDialogueType dialogueType;

    private List<ResourceLocation> giftSaturation = new LinkedList<>();

    private UUID playerUUID;
    private EntityVillagerMCA villager;

    private PlayerHistory() {
        hearts = 0;
        interactionFatigue = 0;
        giftPresent = false;
        greetTimer = 0;
        playerUUID = Constants.ZERO_UUID;
        dialogueType = EnumDialogueType.ADULT;
    }

    public int getHearts() {
        return hearts;
    }

    public int getInteractionFatigue() {
        return interactionFatigue;
    }

    public boolean isGiftPresent() {
        return giftPresent;
    }

    public int getGreetTimer() {
        return greetTimer;
    }

    public EnumDialogueType getDialogueType() {
        return dialogueType;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void addGiftToSaturation(ItemStack stack) {
        if (stack.isEmpty()) return;

        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return;

        giftSaturation.add(id);

        while (giftSaturation.size() > MCA.getConfig().giftDesaturationQueueLength) {
            giftSaturation.remove(0);
        }
        villager.updatePlayerHistoryMap(this);
    }

    public int getGiftSaturationCount(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) return 0;

        int count = 0;
        for (ResourceLocation loc : giftSaturation) {
            if (loc.equals(id)) {
                count++;
            }
        }
        return count;
    }

    public static PlayerHistory getNew(EntityVillagerMCA villager, UUID uuid) {
        PlayerHistory history = new PlayerHistory();
        history.villager = villager;
        history.playerUUID = uuid;

        if (villager.isChild()) {
            history.setDialogueType(EnumDialogueType.CHILD);
        } else {
            history.setDialogueType(EnumDialogueType.ADULT);
        }
        return history;
    }

    public static PlayerHistory fromNBT(EntityVillagerMCA villager, UUID uuid, NBTTagCompound nbt) {
        PlayerHistory history = new PlayerHistory();
        history.villager = villager;
        history.playerUUID = uuid;

        history.hearts = nbt.getInteger("hearts");
        history.interactionFatigue = nbt.getInteger("interactionFatigue");
        history.giftPresent = nbt.getBoolean("giftPresent");
        history.greetTimer = nbt.getInteger("greetTimer");
        history.dialogueType = EnumDialogueType.byValue(nbt.getString("dialogueType"));

        if (nbt.hasKey("giftSaturation")) {
            NBTTagList list = nbt.getTagList("giftSaturation", 8);
            for (int i = 0; i < list.tagCount(); i++) {
                history.giftSaturation.add(new ResourceLocation(list.getStringTagAt(i)));
            }
        }

        return history;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setInteger("hearts", hearts);
        nbt.setInteger("interactionFatigue", interactionFatigue);
        nbt.setBoolean("giftPresent", giftPresent);
        nbt.setInteger("greetTimer", greetTimer);
        nbt.setString("dialogueType", dialogueType.getId());

        NBTTagList list = new NBTTagList();
        for (ResourceLocation loc : giftSaturation) {
            list.appendTag(new NBTTagString(loc.toString()));
        }
        nbt.setTag("giftSaturation", list);

        return nbt;
    }

    public void setHearts(int value) {
        hearts = value;
        villager.updatePlayerHistoryMap(this);
    }

    public void changeHearts(int value) {
        hearts += value;
        villager.updatePlayerHistoryMap(this);
    }

    public void changeInteractionFatigue(int value) {
        interactionFatigue += value;
        villager.updatePlayerHistoryMap(this);
    }

    public void update() {
        if (villager.ticksExisted % MCA.getConfig().interactionFatigueCooldown == 0) {
            if (interactionFatigue > 0) {
                changeInteractionFatigue(-1);
            }
        }
    }

    public void setDialogueType(EnumDialogueType type) {
        this.dialogueType = type;
        villager.updatePlayerHistoryMap(this);
    }
}
