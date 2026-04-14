package mca.entity;

import mca.core.MCA;
import mca.enums.EnumAgeState;
import mca.enums.EnumGender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityZombieVillagerMCA extends EntityZombie {

    public static final DataParameter<Integer> GENDER = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> AGE_STATE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);
    public static final DataParameter<String> TEXTURE = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.STRING);
    public static final DataParameter<NBTTagCompound> GENETICS = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.COMPOUND_TAG);
    public static final DataParameter<NBTTagCompound> TRAITS = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.COMPOUND_TAG);
    public static final DataParameter<Boolean> IS_CONVERTING = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> CONVERSION_TIME = EntityDataManager.createKey(EntityZombieVillagerMCA.class, DataSerializers.VARINT);

    private int burned;
    private Random random = new Random();

    public EntityZombieVillagerMCA(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(GENDER, EnumGender.MALE.getId());
        this.dataManager.register(AGE_STATE, EnumAgeState.ADULT.getId());
        this.dataManager.register(TEXTURE, "");
        this.dataManager.register(GENETICS, new NBTTagCompound());
        this.dataManager.register(TRAITS, new NBTTagCompound());
        this.dataManager.register(IS_CONVERTING, false);
        this.dataManager.register(CONVERSION_TIME, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    protected void applyEntityAI() {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityPigZombie.class));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        // Handle burned clothing effect
        burned--;
        if (isBurning()) {
            burned = 100;
        }
        if (burned > 0 && world.isRemote) {
            spawnBurntParticles();
        }

        // Handle conversion
        if (!this.world.isRemote && this.isConverting()) {
            int time = getConversionTime() - 1;
            setConversionTime(time);

            if (time <= 0) {
                convertToVillager();
            }
        }
    }

    private void spawnBurntParticles() {
        if (world.rand.nextInt(10) == 0) {
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, 
                posX + (rand.nextDouble() - 0.5D) * width, 
                posY + rand.nextDouble() * height, 
                posZ + (rand.nextDouble() - 0.5D) * width, 
                0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isBurned() {
        return burned > 0;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Conversion with golden apple and weakness effect
        if (stack.getItem() == Items.GOLDEN_APPLE && this.isPotionActive(MobEffects.WEAKNESS)) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            if (!this.world.isRemote) {
                startConverting(player);
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    private void startConverting(EntityPlayer player) {
        this.setConverting(true);
        this.setConversionTime(3600);
        this.world.setEntityState(this, (byte) 16);
    }

    private void convertToVillager() {
        EntityVillagerMCA villager = new EntityVillagerMCA(this.world);
        villager.copyLocationAndAnglesFrom(this);

        // Copy gender
        villager.set(EntityVillagerMCA.GENDER, get(GENDER));

        // Copy genetics
        villager.set(EntityVillagerMCA.GENETICS, get(GENETICS));

        // Copy traits
        villager.set(EntityVillagerMCA.TRAITS, get(TRAITS));

        // Copy age state
        villager.set(EntityVillagerMCA.AGE_STATE, get(AGE_STATE));
        if (isChild()) {
            villager.setScaleForAge(true);
        }

        // Mark as cured
        villager.set(EntityVillagerMCA.IS_INFECTED, false);
        villager.set(EntityVillagerMCA.INFECTION_PROGRESS, 0.0F);

        this.world.removeEntity(this);
        this.world.spawnEntity(villager);

        villager.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
        this.world.playEvent((EntityPlayer) null, 1027, new BlockPos(this), 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_VILLAGER_STEP;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return new ResourceLocation("minecraft", "entities/zombie");
    }

    @Override
    protected ItemStack getSkullDrop() {
        return ItemStack.EMPTY;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Gender", get(GENDER));
        compound.setInteger("AgeState", get(AGE_STATE));
        compound.setString("Texture", get(TEXTURE));
        compound.setTag("Genetics", get(GENETICS));
        compound.setTag("Traits", get(TRAITS));
        compound.setBoolean("IsConverting", get(IS_CONVERTING));
        compound.setInteger("ConversionTime", get(CONVERSION_TIME));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        set(GENDER, compound.getInteger("Gender"));
        set(AGE_STATE, compound.getInteger("AgeState"));
        set(TEXTURE, compound.getString("Texture"));
        set(GENETICS, compound.getCompoundTag("Genetics"));
        set(TRAITS, compound.getCompoundTag("Traits"));
        set(IS_CONVERTING, compound.getBoolean("IsConverting"));
        set(CONVERSION_TIME, compound.getInteger("ConversionTime"));
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        // Random gender
        setGender(EnumGender.getRandom());

        // Random genetics
        NBTTagCompound genetics = new NBTTagCompound();
        genetics.setFloat("Size", random.nextFloat());
        genetics.setFloat("Width", random.nextFloat());
        genetics.setFloat("Melanin", random.nextFloat());
        genetics.setFloat("Hemoglobin", random.nextFloat());
        genetics.setFloat("Eumelanin", random.nextFloat());
        genetics.setFloat("Pheomelanin", random.nextFloat());
        genetics.setFloat("Skin", random.nextFloat());
        genetics.setFloat("Face", random.nextFloat());
        set(GENETICS, genetics);

        // Random traits
        NBTTagCompound traits = new NBTTagCompound();
        traits.setBoolean("Heterochromia", random.nextFloat() < 0.01F);
        set(TRAITS, traits);

        // Random age state (mostly adults)
        // Baby zombie villagers cause weird bugs, so we skip that stage (like 1.21.1)
        if (random.nextFloat() < 0.05F) {
            setAgeState(EnumAgeState.CHILD);
            this.setChild(true);
        } else {
            setAgeState(EnumAgeState.ADULT);
        }

        return livingdata;
    }

    // Getters and Setters
    public EnumGender getGender() {
        return EnumGender.byId(get(GENDER));
    }

    public void setGender(EnumGender gender) {
        set(GENDER, gender.getId());
    }

    public EnumAgeState getAgeState() {
        return EnumAgeState.byId(get(AGE_STATE));
    }

    public void setAgeState(EnumAgeState ageState) {
        set(AGE_STATE, ageState.getId());
    }

    public String getTexture() {
        return get(TEXTURE);
    }

    public void setTexture(String texture) {
        set(TEXTURE, texture);
    }

    public NBTTagCompound getGeneticsTag() {
        return get(GENETICS);
    }

    public void setGeneticsTag(NBTTagCompound genetics) {
        set(GENETICS, genetics);
    }

    public NBTTagCompound getTraitsTag() {
        return get(TRAITS);
    }

    public void setTraitsTag(NBTTagCompound traits) {
        set(TRAITS, traits);
    }

    public boolean isConverting() {
        return get(IS_CONVERTING);
    }

    public void setConverting(boolean converting) {
        set(IS_CONVERTING, converting);
    }

    public int getConversionTime() {
        return get(CONVERSION_TIME);
    }

    public void setConversionTime(int time) {
        set(CONVERSION_TIME, time);
    }

    public float getInfectionProgress() {
        return 1.0F;
    }

    public boolean isShaking() {
        return isConverting();
    }

    // Helper method for data manager access
    private <T> T get(DataParameter<T> key) {
        return this.dataManager.get(key);
    }

    private <T> void set(DataParameter<T> key, T value) {
        this.dataManager.set(key, value);
    }
}
