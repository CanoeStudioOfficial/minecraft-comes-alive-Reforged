package mca.entity.data;

import mca.core.Constants;
import mca.core.MCA;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerSaveData extends WorldSavedData {
    private static final String PREFIX = "MCA-Player-V1-";

    private UUID spouseUUID = Constants.ZERO_UUID;
    private EnumMarriageState marriageState = EnumMarriageState.NOT_MARRIED;
    private String spouseName = "";
    private boolean babyPresent = false;
    private int lastProcreation = 0;
    private EnumGender gender = EnumGender.MALE;
    private EnumGender genderPreference = EnumGender.UNASSIGNED;
    private String playerName = "";
    private boolean hasChosenDestiny = false;
    private boolean destinyInProgress = false;

    public UUID getSpouseUUID() {
        return spouseUUID;
    }

    public EnumMarriageState getMarriageState() {
        return marriageState;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public boolean isBabyPresent() {
        return babyPresent;
    }

    public boolean mayProcreateAgain(long worldTime) {
        int intTime = (int) worldTime;
        int delta = intTime - lastProcreation;
        return lastProcreation == 0 || delta < 0 || delta > MCA.getConfig().procreationCooldown;
    }

    public EnumGender getGender() {
        return gender;
    }

    public boolean isHasChosenDestiny() {
        return hasChosenDestiny;
    }

    public boolean isDestinyInProgress() {
        return destinyInProgress;
    }

    public EnumGender getGenderPreference() {
        return genderPreference;
    }

    public String getPlayerName(EntityPlayer player) {
        return playerName.isEmpty() ? player.getName() : playerName;
    }

    public PlayerSaveData(String id) {
        super(id);
    }

    public static PlayerSaveData get(EntityPlayer player) {
        String dataId = PREFIX + player.getUniqueID().toString();
        PlayerSaveData data = (PlayerSaveData) player.world.loadData(PlayerSaveData.class, dataId);

        if (data == null) {
            data = new PlayerSaveData(dataId);
            player.world.setData(dataId, data);
        }

        return data;
    }

    public static PlayerSaveData getExisting(World world, UUID uuid) {
        return (PlayerSaveData) world.loadData(PlayerSaveData.class, PREFIX + uuid.toString());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setUniqueId("spouseUUID", spouseUUID);
        nbt.setInteger("marriageState", marriageState.getId());
        nbt.setString("spouseName", spouseName);
        nbt.setBoolean("babyPresent", babyPresent);
        nbt.setInteger("lastProcreation", lastProcreation);
        nbt.setInteger("gender", gender.getId());
        nbt.setInteger("genderPreference", genderPreference.getId());
        nbt.setString("playerName", playerName);
        nbt.setBoolean("hasChosenDestiny", hasChosenDestiny);
        nbt.setBoolean("destinyInProgress", false);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        spouseUUID = nbt.hasUniqueId("spouseUUID") ? nbt.getUniqueId("spouseUUID") : Constants.ZERO_UUID;
        marriageState = EnumMarriageState.byId(nbt.getInteger("marriageState"));
        spouseName = nbt.getString("spouseName");
        babyPresent = nbt.getBoolean("babyPresent");
        lastProcreation = nbt.getInteger("lastProcreation");
        gender = EnumGender.byId(nbt.getInteger("gender"));
        genderPreference = EnumGender.byId(nbt.getInteger("genderPreference"));
        playerName = nbt.getString("playerName");
        hasChosenDestiny = nbt.getBoolean("hasChosenDestiny");
        destinyInProgress = false;

        // A relationship without a partner cannot be restored safely. This also
        // repairs saves written by older builds that only stored one side.
        if (Constants.ZERO_UUID.equals(spouseUUID)) {
            marriageState = EnumMarriageState.NOT_MARRIED;
            spouseName = "";
        }
    }

    public void setGender(EnumGender gender) {
        this.gender = gender;
        markDirty();
    }

    public void setHasChosenDestiny(boolean value) {
        this.hasChosenDestiny = value;
        markDirty();
    }

    public void setDestinyInProgress(boolean value) {
        this.destinyInProgress = value;
        markDirty();
    }

    public void setGenderPreference(EnumGender genderPreference) {
        this.genderPreference = genderPreference == null ? EnumGender.UNASSIGNED : genderPreference;
        markDirty();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName == null ? "" : playerName.trim();
        markDirty();
    }

    public boolean isMarriedOrEngaged() {
        return isMarried() || isEngaged();
    }

    public boolean isMarried() {
        return marriageState == EnumMarriageState.MARRIED && !Constants.ZERO_UUID.equals(spouseUUID);
    }

    public boolean isEngaged() {
        return marriageState == EnumMarriageState.ENGAGED && !Constants.ZERO_UUID.equals(spouseUUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return isMarried() && spouseUUID.equals(uuid);
    }

    public boolean isEngagedTo(UUID uuid) {
        return isEngaged() && spouseUUID.equals(uuid);
    }

    public void engage(UUID uuid, String name) {
        spouseUUID = uuid;
        marriageState = EnumMarriageState.ENGAGED;
        spouseName = name;
        markDirty();
    }

    public void marry(UUID uuid, String name) {
        spouseUUID = uuid;
        marriageState = EnumMarriageState.MARRIED;
        spouseName = name;
        markDirty();
    }

    public void endMarriage() {
        spouseUUID = Constants.ZERO_UUID;
        spouseName = "";
        marriageState = EnumMarriageState.NOT_MARRIED;
        markDirty();
    }

    public void setBabyPresent(boolean value) {
        this.babyPresent = value;
        markDirty();
    }

    public void markProcreated(long worldTime) {
        this.lastProcreation = (int) worldTime;
        markDirty();
    }

    public void reset() {
        endMarriage();
        setBabyPresent(false);
        lastProcreation = 0;
        markDirty();
    }

    public List<Field> getDataFields() {
        return Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> !Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
    }

    public void dump(EntityPlayer player) {
        for (Field f : getDataFields()) {
            try {
                player.sendMessage(new TextComponentString(f.getName() + " = " + f.get(this).toString()));
            } catch (Exception e) {
                MCA.getLog().error("Error dumping player data!");
                MCA.getLog().error(e);
            }
        }
    }
}
