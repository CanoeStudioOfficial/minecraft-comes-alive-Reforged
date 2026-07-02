package mca.entity;

import com.google.common.base.Optional;
import mca.api.API;
import mca.core.MCA;
import mca.enums.EnumAgeState;
import mca.enums.EnumGender;
import mca.util.ResourceLocationCache;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class EntityZombieVillagerMCA extends EntityZombieVillager {
    public static final DataParameter<String> VILLAGER_NAME = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<String> TEXTURE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<String> ORIGINAL_TEXTURE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<Integer> GENDER = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> AGE_STATE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> GROWTH_AMOUNT = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> STARTING_AGE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> VANILLA_CAREER = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);

    private int startingAge;
    private NBTTagCompound mcaData = new NBTTagCompound();
    private boolean updatingAgeDimensions;
    private boolean converting;

    public EntityZombieVillagerMCA(World worldIn) {
        super(worldIn);
    }

    public EntityZombieVillagerMCA(World worldIn, EnumGender gender) {
        this(worldIn);
        EnumGender assignedGender = gender == EnumGender.UNASSIGNED ? EnumGender.getRandom() : gender;
        set(GENDER, assignedGender.getId());
        set(VILLAGER_NAME, API.getRandomName(assignedGender));
        set(AGE_STATE, EnumAgeState.ADULT.getId());
        setStartingAge(0);
        set(TEXTURE, getDefaultZombieTexture());
        set(ORIGINAL_TEXTURE, "");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VILLAGER_NAME, "");
        this.dataManager.register(TEXTURE, "");
        this.dataManager.register(ORIGINAL_TEXTURE, "");
        this.dataManager.register(GENDER, EnumGender.UNASSIGNED.getId());
        this.dataManager.register(AGE_STATE, EnumAgeState.ADULT.getId());
        this.dataManager.register(GROWTH_AMOUNT, 0);
        this.dataManager.register(STARTING_AGE, 0);
        this.dataManager.register(VANILLA_CAREER, 0);
    }

    public <T> T get(DataParameter<T> key) {
        return this.dataManager.get(key);
    }

    public <T> void set(DataParameter<T> key, T value) {
        this.dataManager.set(key, value);
    }

    public void copyMCADataFrom(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
        if (gender == EnumGender.UNASSIGNED) {
            gender = EnumGender.getRandom();
        }

        set(GENDER, gender.getId());
        set(VILLAGER_NAME, villager.get(EntityVillagerMCA.VILLAGER_NAME));
        set(ORIGINAL_TEXTURE, villager.get(EntityVillagerMCA.TEXTURE));
        set(TEXTURE, getZombieTextureFor(gender));
        set(AGE_STATE, villager.get(EntityVillagerMCA.AGE_STATE));
        setStartingAge(villager.getStartingAgeForMCA());
        setGrowingAgeForMCA(villager.getGrowingAgeForMCA());
        setChild(villager.getCurrentAgeStateForMCA() != EnumAgeState.ADULT);
        setForgeProfession(villager.getProfessionForge());
        set(VANILLA_CAREER, Math.max(0, ObfuscationReflectionHelper.getPrivateValue(
                net.minecraft.entity.passive.EntityVillager.class,
                villager,
                EntityVillagerMCA.VANILLA_CAREER_ID_FIELD_INDEX)));
        this.mcaData = copyVillagerData(villager);

        if (villager.hasCustomName()) {
            setCustomNameTag(villager.getCustomNameTag());
            setAlwaysRenderNameTag(villager.getAlwaysRenderNameTag());
        }

        updateAgeStateAndDimensions();
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);

        if (EnumGender.byId(get(GENDER)) == EnumGender.UNASSIGNED) {
            EnumGender gender = EnumGender.getRandom();
            set(GENDER, gender.getId());
            set(VILLAGER_NAME, API.getRandomName(gender));
        }

        if (get(TEXTURE).isEmpty()) {
            set(TEXTURE, getDefaultZombieTexture());
        }

        if (!this.world.isRemote && get(ORIGINAL_TEXTURE).isEmpty()) {
            set(ORIGINAL_TEXTURE, getRandomOriginalTextureFor(EnumGender.byId(get(GENDER))));
        }

        if (isChild() && getCurrentAgeState() == EnumAgeState.ADULT) {
            set(AGE_STATE, EnumAgeState.CHILD.getId());
            setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
            setGrowingAgeForMCA(Math.min(getGrowingAgeForMCA(), -1));
        }

        updateAgeStateAndDimensions();
        return data;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("mcaName", get(VILLAGER_NAME));
        compound.setString("mcaTexture", get(TEXTURE));
        compound.setString("mcaOriginalTexture", get(ORIGINAL_TEXTURE));
        compound.setInteger("mcaGender", get(GENDER));
        compound.setInteger("mcaAgeState", get(AGE_STATE));
        compound.setInteger("mcaStartingAge", startingAge);
        compound.setInteger("mcaGrowthAmount", get(GROWTH_AMOUNT));
        compound.setInteger("mcaCareer", get(VANILLA_CAREER));
        compound.setTag("mcaData", mcaData.copy());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        set(VILLAGER_NAME, compound.getString("mcaName"));
        set(TEXTURE, compound.getString("mcaTexture"));
        set(ORIGINAL_TEXTURE, compound.getString("mcaOriginalTexture"));
        set(GENDER, compound.hasKey("mcaGender") ? compound.getInteger("mcaGender") : EnumGender.getRandom().getId());
        set(AGE_STATE, compound.hasKey("mcaAgeState") ? compound.getInteger("mcaAgeState") : EnumAgeState.ADULT.getId());
        this.startingAge = compound.hasKey("mcaStartingAge") ? compound.getInteger("mcaStartingAge") : getDefaultStartingAge(compound.getInteger("mcaGrowthAmount"));
        set(STARTING_AGE, this.startingAge);
        setGrowingAgeForMCA(compound.getInteger("mcaGrowthAmount"));
        set(VANILLA_CAREER, compound.hasKey("mcaCareer") ? compound.getInteger("mcaCareer") : 0);
        this.mcaData = compound.hasKey("mcaData") ? compound.getCompoundTag("mcaData").copy() : new NBTTagCompound();

        if (get(TEXTURE).isEmpty()) {
            set(TEXTURE, getDefaultZombieTexture());
        }

        if (get(ORIGINAL_TEXTURE).isEmpty() && mcaData.hasKey("texture")) {
            set(ORIGINAL_TEXTURE, mcaData.getString("texture"));
        }

        updateAgeStateAndDimensions();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (AGE_STATE.equals(key) || GROWTH_AMOUNT.equals(key) || STARTING_AGE.equals(key)) {
            updateAgeStateAndDimensions();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote && getGrowingAgeForMCA() < 0) {
            setGrowingAgeForMCA(getGrowingAgeForMCA() + 1);
        }
        updateAgeStateAndDimensions();
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause) {
        super.onDeath(cause);

        if (!this.world.isRemote) {
            dropStoredInventory();
        }
    }

    @Override
    protected void finishConversion() {
        converting = true;

        EntityVillagerMCA villager = new EntityVillagerMCA(this.world, Optional.of(getForgeProfession()), Optional.of(EnumGender.byId(get(GENDER))));
        villager.copyLocationAndAnglesFrom(this);

        NBTTagCompound storedData = mcaData;
        if (!storedData.isEmpty()) {
            villager.readEntityFromNBT(storedData.copy());
            villager.copyLocationAndAnglesFrom(this);
        } else if (!get(ORIGINAL_TEXTURE).isEmpty()) {
            villager.set(EntityVillagerMCA.TEXTURE, get(ORIGINAL_TEXTURE));
        } else {
            villager.set(EntityVillagerMCA.TEXTURE, API.getRandomSkin(villager));
        }

        villager.set(EntityVillagerMCA.GENDER, EnumGender.byId(get(GENDER)).getId());
        villager.set(EntityVillagerMCA.VILLAGER_NAME, get(VILLAGER_NAME));
        villager.set(EntityVillagerMCA.IS_INFECTED, false);
        villager.setProfession(getForgeProfession());
        villager.setVanillaCareer(get(VANILLA_CAREER));
        villager.setStartingAge(this.startingAge);
        villager.setGrowingAge(getGrowingAgeForMCA());
        villager.setNoAI(this.isAIDisabled());
        villager.refreshSpecialAI();

        if (this.hasCustomName()) {
            villager.setCustomNameTag(this.getCustomNameTag());
            villager.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
        }

        this.world.removeEntity(this);
        this.world.spawnEntity(villager);
        villager.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
        this.world.playEvent(null, 1027, this.getPosition(), 0);
    }

    @Override
    public void setChild(boolean childZombie) {
        super.setChild(childZombie);
        if (updatingAgeDimensions) {
            return;
        }

        if (childZombie && getCurrentAgeState() == EnumAgeState.ADULT) {
            set(AGE_STATE, EnumAgeState.CHILD.getId());
            setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
            setGrowingAgeForMCA(Math.min(getGrowingAgeForMCA(), -1));
        } else if (!childZombie) {
            set(AGE_STATE, EnumAgeState.ADULT.getId());
            setStartingAge(0);
            setGrowingAgeForMCA(0);
        }
        updateAgeStateAndDimensions();
    }

    @Override
    public boolean canDespawn() {
        return !converting && super.canDespawn();
    }

    @Override
    protected float getSoundPitch() {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + MCAVillagerDimensions.getSoundPitch(getCurrentAgeState(), getAgeProgressDelta());
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return new TextComponentString(MCA.getConfig().villagerChatPrefix + get(VILLAGER_NAME));
    }

    @Override
    @Nonnull
    public String getCustomNameTag() {
        return get(VILLAGER_NAME);
    }

    @Override
    public boolean hasCustomName() {
        return !get(VILLAGER_NAME).isEmpty();
    }

    public ResourceLocation getTextureResourceLocation() {
        return ResourceLocationCache.getResourceLocationFor(get(TEXTURE).isEmpty() ? getDefaultZombieTexture() : get(TEXTURE));
    }

    public String getOriginalTextureForMCA() {
        return get(ORIGINAL_TEXTURE);
    }

    public EnumGender getGenderForMCA() {
        return EnumGender.byId(get(GENDER));
    }

    public float getRenderScaleForAge() {
        return MCAVillagerDimensions.getRenderScale(getCurrentAgeState(), getAgeProgressDelta());
    }

    public int getStartingAgeForMCA() {
        return startingAge;
    }

    public int getGrowingAgeForMCA() {
        return get(GROWTH_AMOUNT);
    }

    public EnumAgeState getCurrentAgeStateForMCA() {
        return getCurrentAgeState();
    }

    public void setStartingAge(int value) {
        this.startingAge = value;
        setIfDataReady(STARTING_AGE, value);
    }

    public void setGrowingAgeForMCA(int value) {
        setIfDataReady(GROWTH_AMOUNT, Math.min(value, 0));
        updateAgeStateAndDimensions();
    }

    private void updateAgeStateAndDimensions() {
        if (!hasDataParameter(AGE_STATE) || !hasDataParameter(GROWTH_AMOUNT) || !hasDataParameter(STARTING_AGE)) {
            return;
        }

        boolean serverSide = this.world == null || !this.world.isRemote;
        int growthAmount = Math.min(get(GROWTH_AMOUNT), 0);
        if (serverSide) {
            setIfDataReady(GROWTH_AMOUNT, growthAmount);
            setIfDataReady(STARTING_AGE, startingAge);
        }

        EnumAgeState target = EnumAgeState.byCurrentAge(get(STARTING_AGE), growthAmount);
        if (serverSide && get(AGE_STATE) != target.getId()) {
            set(AGE_STATE, target.getId());
        }

        boolean child = target != EnumAgeState.ADULT;
        float childScale = child ? 0.5F : 1.0F;
        this.setSize(MCAVillagerDimensions.getCollisionWidth(target, getAgeProgressDelta()) / childScale, MCAVillagerDimensions.getCollisionHeight(target, getAgeProgressDelta()) / childScale);

        updatingAgeDimensions = true;
        super.setChild(child);
        updatingAgeDimensions = false;

        if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(
                    MCAVillagerDimensions.getMovementSpeed(target, getAgeProgressDelta()));
        }
    }

    private EnumAgeState getCurrentAgeState() {
        EnumAgeState target = EnumAgeState.byCurrentAge(get(STARTING_AGE), get(GROWTH_AMOUNT));
        EnumAgeState syncedState = EnumAgeState.byId(get(AGE_STATE));
        return target == EnumAgeState.ADULT && syncedState != EnumAgeState.ADULT && syncedState != EnumAgeState.UNASSIGNED ? syncedState : target;
    }

    private float getAgeProgressDelta() {
        return EnumAgeState.getDelta(get(STARTING_AGE), get(GROWTH_AMOUNT));
    }

    private int getDefaultStartingAge(int age) {
        return age < 0 ? MCA.getConfig().childGrowUpTime * 60 * 20 * -1 : 0;
    }

    private <T> void setIfDataReady(DataParameter<T> key, T value) {
        if (hasDataParameter(key) && !Objects.equals(get(key), value)) {
            set(key, value);
        }
    }

    private boolean hasDataParameter(DataParameter<?> key) {
        if (this.dataManager == null) {
            return false;
        }

        try {
            this.dataManager.get(key);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String getDefaultZombieTexture() {
        return getZombieTextureFor(EnumGender.byId(get(GENDER)));
    }

    private String getZombieTextureFor(EnumGender gender) {
        return String.format("mca:skins/%s/zombievillager.png", gender == EnumGender.FEMALE ? "female" : "male");
    }

    private String getRandomOriginalTextureFor(EnumGender gender) {
        EntityVillagerMCA dummy = new EntityVillagerMCA(this.world, Optional.of(getForgeProfession()), Optional.of(gender));
        dummy.set(EntityVillagerMCA.VILLAGER_NAME, get(VILLAGER_NAME));
        return API.getRandomSkin(dummy);
    }

    private NBTTagCompound copyVillagerData(EntityVillagerMCA villager) {
        NBTTagCompound nbt = new NBTTagCompound();
        villager.writeEntityToNBT(nbt);

        nbt.setBoolean("infected", false);
        nbt.setString("name", villager.get(EntityVillagerMCA.VILLAGER_NAME));
        nbt.setInteger("gender", EnumGender.byId(villager.get(EntityVillagerMCA.GENDER)).getId());
        nbt.setInteger("ageState", villager.get(EntityVillagerMCA.AGE_STATE));
        nbt.setInteger("startingAge", villager.getStartingAgeForMCA());
        nbt.setInteger("Age", villager.getGrowingAgeForMCA());
        nbt.setTag("inventory", copyInventory(villager));
        return nbt;
    }

    private NBTTagList copyInventory(EntityVillagerMCA villager) {
        return villager.inventory != null ? villager.inventory.writeInventoryToNBT().copy() : new NBTTagList();
    }

    private void dropStoredInventory() {
        if (!mcaData.hasKey("inventory")) {
            return;
        }

        NBTTagList inventory = mcaData.getTagList("inventory", 10);
        for (int i = 0; i < inventory.tagCount(); i++) {
            ItemStack stack = new ItemStack(inventory.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                entityDropItem(stack, 1.0F);
            }
        }

        mcaData.removeTag("inventory");
    }
}
