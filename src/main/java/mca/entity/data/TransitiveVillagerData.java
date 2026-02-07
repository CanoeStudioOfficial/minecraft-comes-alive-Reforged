package mca.entity.data;

import java.io.Serializable;
import java.util.UUID;

import mca.core.Constants;
import mca.enums.EnumAgeState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumPersonality;
import net.minecraft.nbt.NBTTagCompound;

/*
 * Used to carry around villager attributes without using the data manager, ex. in memorial objects.
 */
public class TransitiveVillagerData implements Serializable
{
	private final UUID uuid;
	private final String name;
	private final String headTexture;
	private final String clothesTexture;
	private final Integer profession;
	private final Integer personality;
	private final Integer gender;
	private final String spouseName;
	private final UUID spouseUUID;
	private final Integer spouseGender;
	private final String motherName;
	private final UUID motherUUID;
	private final Integer motherGender;
	private final String fatherName;
	private final UUID fatherUUID;
	private final Integer fatherGender;
	private final Integer babyState;
	private final Integer movementState;
	private final Boolean isChild;
	private final Integer age;
	private final Float scaleHeight;
	private final Float scaleWidth;
	private final Boolean doDisplay;
	private final Boolean isSwinging;
	private final Integer heldItemSlot;
	private final Boolean isInfected;
	private final Boolean doOpenInventory;
	private final Integer marriageState;
	

	public TransitiveVillagerData(NBTTagCompound nbt)
	{
		this.uuid = nbt.getUniqueId("uuid");
		this.name = nbt.getString("name");
		this.headTexture = nbt.getString("headTexture");
		this.clothesTexture = nbt.getString("clothesTexture");
		this.profession = nbt.getInteger("profession");
		this.personality = nbt.getInteger("personality");
		this.gender = nbt.getInteger("gender");
		this.spouseUUID = nbt.getUniqueId("spouseUUID");
		this.spouseGender = nbt.getInteger("spouseGender");
		this.spouseName = nbt.getString("spouseName");
		this.motherUUID = nbt.getUniqueId("motherUUID");
		this.motherGender = nbt.getInteger("motherGender");
		this.motherName = nbt.getString("motherName");
		this.fatherUUID = nbt.getUniqueId("fatherUUID");
		this.fatherGender = nbt.getInteger("fatherGender");
		this.fatherName = nbt.getString("fatherName");
		this.babyState = nbt.getInteger("babyState");
		this.movementState = nbt.getInteger("movementState");
		this.isChild = nbt.getBoolean("isChild");
		this.age = nbt.getInteger("age");
		this.scaleHeight = nbt.getFloat("scaleHeight");
		this.scaleWidth = nbt.getFloat("scaleWidth");
		this.doDisplay = nbt.getBoolean("doDisplay");
		this.isSwinging = nbt.getBoolean("isSwinging");
		this.heldItemSlot = nbt.getInteger("heldItemSlot");
		this.isInfected = nbt.getBoolean("isInfected");
		this.doOpenInventory = nbt.getBoolean("doOpenInventory");
		this.marriageState = nbt.getInteger("marriageState");
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
	
	public String getName() 
	{
		return name;
	}

	public String getHeadTexture() 
	{
		return headTexture;
	}

	public String getClothesTexture() 
	{
		return clothesTexture;
	}

	public EnumPersonality getPersonality() 
	{
		return EnumPersonality.getById(personality);
	}

	public EnumGender getGender() 
	{
		return EnumGender.byId(gender);
	}

	public String getSpouseName()
	{
		return spouseName;
	}
	
	public UUID getSpouseUUID() 
	{
		return spouseUUID;
	}

	public EnumGender getSpouseGender() 
	{
		return EnumGender.byId(spouseGender);
	}

	public String getMotherName() 
	{
		return motherName;
	}

	public UUID getMotherUUID() 
	{
		return motherUUID;
	}

	public EnumGender getMotherGender() 
	{
		return EnumGender.byId(motherGender);
	}

	public String getFatherName() 
	{
		return fatherName;
	}

	public UUID getFatherUUID() 
	{
		return fatherUUID;
	}

	public EnumGender getFatherGender() 
	{
		return EnumGender.byId(fatherGender);
	}

	public Boolean getIsChild() 
	{
		return isChild;
	}

	public Integer getAge() 
	{
		return age;
	}

	public Float getScaleHeight() 
	{
		return scaleHeight;
	}

	public Float getScaleWidth() 
	{
		return scaleWidth;
	}

	public Boolean getDoDisplay() 
	{
		return doDisplay;
	}

	public Boolean getIsSwinging() 
	{
		return isSwinging;
	}

	public Integer getHeldItemSlot() 
	{
		return heldItemSlot;
	}

	public Boolean getIsInfected() 
	{
		return isInfected;
	}

	public Boolean getDoOpenInventory() 
	{
		return doOpenInventory;
	}

	public EnumMarriageState getMarriageState() 
	{
		return EnumMarriageState.byId(marriageState);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setUniqueId("uuid", uuid);
		nbt.setString("name", name);
		nbt.setString("headTexture", headTexture);
		nbt.setString("clothesTexture", clothesTexture);
		nbt.setInteger("profession", profession);
		nbt.setInteger("personality", personality);
		nbt.setInteger("gender", gender);
		nbt.setUniqueId("spouseUUID", spouseUUID);
		nbt.setInteger("spouseGender", spouseGender);
		nbt.setString("spouseName", spouseName);
		nbt.setUniqueId("motherUUID", motherUUID);
		nbt.setInteger("motherGender", motherGender);
		nbt.setString("motherName", motherName);
		nbt.setUniqueId("fatherUUID", fatherUUID);
		nbt.setInteger("fatherGender", fatherGender);
		nbt.setString("fatherName", fatherName);
		nbt.setInteger("babyState", babyState);
		nbt.setInteger("movementState", movementState);
		nbt.setBoolean("isChild", isChild);
		nbt.setInteger("age", age);
		nbt.setFloat("scaleHeight", scaleHeight);
		nbt.setFloat("scaleWidth", scaleWidth);
		nbt.setBoolean("doDisplay", doDisplay);
		nbt.setBoolean("isSwinging", isSwinging);
		nbt.setInteger("heldItemSlot", heldItemSlot);
		nbt.setBoolean("isInfected", isInfected);
		nbt.setBoolean("doOpenInventory", doOpenInventory);
		nbt.setInteger("marriageState", marriageState);
	}
}
