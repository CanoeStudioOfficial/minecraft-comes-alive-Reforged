package mca.entity.data;

import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FamilyTreeNode {
    private final UUID id;
    private final boolean isPlayer;
    private String name;
    private EnumGender gender;
    private UUID father = mca.core.Constants.ZERO_UUID;
    private UUID mother = mca.core.Constants.ZERO_UUID;
    private UUID spouse = mca.core.Constants.ZERO_UUID;
    private EnumMarriageState marriageState = EnumMarriageState.NOT_MARRIED;
    private final Set<UUID> children = new HashSet<>();
    private boolean deceased = false;
    private String profession = "none";

    public FamilyTreeNode(UUID id, String name, boolean isPlayer, EnumGender gender) {
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.gender = gender;
    }

    public FamilyTreeNode(NBTTagCompound nbt) {
        this.id = nbt.getUniqueId("id");
        this.name = nbt.getString("name");
        this.isPlayer = nbt.getBoolean("isPlayer");
        this.gender = EnumGender.byId(nbt.getInteger("gender"));
        this.father = nbt.getUniqueId("father");
        this.mother = nbt.getUniqueId("mother");
        this.spouse = nbt.getUniqueId("spouse");
        this.marriageState = EnumMarriageState.byId(nbt.getInteger("marriageState"));
        this.deceased = nbt.getBoolean("deceased");
        this.profession = nbt.getString("profession");

        NBTTagList childrenList = nbt.getTagList("children", Constants.NBT.TAG_STRING);
        for (int i = 0; i < childrenList.tagCount(); i++) {
            children.add(UUID.fromString(childrenList.getStringTagAt(i)));
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("id", id);
        nbt.setString("name", name);
        nbt.setBoolean("isPlayer", isPlayer);
        nbt.setInteger("gender", gender.getId());
        nbt.setUniqueId("father", father);
        nbt.setUniqueId("mother", mother);
        nbt.setUniqueId("spouse", spouse);
        nbt.setInteger("marriageState", marriageState.getId());
        nbt.setBoolean("deceased", deceased);
        nbt.setString("profession", profession);

        NBTTagList childrenList = new NBTTagList();
        for (UUID childId : children) {
            childrenList.appendTag(new NBTTagString(childId.toString()));
        }
        nbt.setTag("children", childrenList);

        return nbt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public EnumGender getGender() {
        return gender;
    }

    public void setGender(EnumGender gender) {
        this.gender = gender;
    }

    public UUID getFather() {
        return father;
    }

    public void setFather(UUID father) {
        this.father = father;
    }

    public UUID getMother() {
        return mother;
    }

    public void setMother(UUID mother) {
        this.mother = mother;
    }

    public UUID getSpouse() {
        return spouse;
    }

    public void setSpouse(UUID spouse) {
        this.spouse = spouse;
    }

    public EnumMarriageState getMarriageState() {
        return marriageState;
    }

    public void setMarriageState(EnumMarriageState marriageState) {
        this.marriageState = marriageState;
    }

    public Set<UUID> getChildren() {
        return children;
    }

    public void addChild(UUID childId) {
        children.add(childId);
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
}
