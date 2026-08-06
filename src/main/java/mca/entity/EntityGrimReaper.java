package mca.entity;


import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.enums.EnumReaperAttackState;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EntityGrimReaper extends EntityMob {
    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.<Integer>createKey(EntityGrimReaper.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STATE_TRANSITION_COOLDOWN = EntityDataManager.<Integer>createKey(EntityGrimReaper.class, DataSerializers.VARINT);
    private static final int MELEE_COOLDOWN = 150;
    private static final int REST_COOLDOWN = 1000;
    private static final int REST_MAX_COUNT = 5;
    private static final int REST_TIME = 400;

    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);

    private float floatingTicks;

    public EntityGrimReaper(World world) {
        super(world);
        setSize(1.0F, 2.6F);
        setNoGravity(true);
        this.experienceValue = 100;

        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        this.tasks.addTask(3, new GrimReaperRestAI(this));
        this.tasks.addTask(4, new GrimReaperMeleeAI(this));
        this.tasks.addTask(5, new GrimReaperIdleAI(this, 1.0D));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new GrimReaperTargetAI(this));
    }

    @Override
    protected final void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30F);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0F);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0F);
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int lootingLvl) {
        dropItem(ItemsMCA.SCYTHE, 1);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_STATE, 0);
        this.dataManager.register(STATE_TRANSITION_COOLDOWN, 0);
    }

    public EnumReaperAttackState getAttackState() {
        return EnumReaperAttackState.fromId(this.dataManager.get(ATTACK_STATE));
    }

    public void setAttackState(EnumReaperAttackState state) {
        // Only update if needed so that sounds only play once.
        if (this.dataManager.get(ATTACK_STATE) != state.getId()) {
            this.dataManager.set(ATTACK_STATE, state.getId());

            switch (state) {
                case PRE:
                    this.playSound(SoundsMCA.reaper_scythe_out, 1.0F, 1.0F);
                    break;
                case POST:
                    this.playSound(SoundsMCA.reaper_scythe_swing, 1.0F, 1.0F);
                    break;
            }
        }
    }

    public boolean hasEntityToAttack() {
        return this.getAttackTarget() != null;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt entity) {
        return;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        // Ignore wall damage and fire damage.
        if (source == DamageSource.IN_WALL || source == DamageSource.ON_FIRE || source.isExplosion() || source == DamageSource.IN_FIRE) {
            // Teleport out of any walls we may end up in.
            if (source == DamageSource.IN_WALL) {
                teleportTo(this.posX, this.posY + 3, this.posZ);
            }

            return false;
        }

        Entity attacker = source.getTrueSource();
        Entity direct = source.getImmediateSource();

        // Ignore damage when blocking.
        if (!world.isRemote && this.getAttackState() == EnumReaperAttackState.BLOCK && attacker != null) {
            this.playSound(SoundsMCA.reaper_block, 1.0F, 1.0F);
            return false;
        }

        // Teleport next to the player who fired a projectile and ignore its damage.
        if (!world.isRemote && direct instanceof EntityArrow && getAttackState() != EnumReaperAttackState.REST && attacker != null && rand.nextBoolean()) {
            double newX = attacker.posX + (rand.nextFloat() >= 0.50F ? 4 : -4);
            double newZ = attacker.posZ + (rand.nextFloat() >= 0.50F ? 4 : -4);
            teleportTo(newX, attacker.posY, newZ);
            direct.setDead();
            return false;
        }

        // Randomly portal behind the attacker.
        if (!world.isRemote && attacker != null && rand.nextFloat() >= 0.30F) {
            double deltaX = this.posX - attacker.posX;
            double deltaZ = this.posZ - attacker.posZ;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            if (distance > 0.001D) {
                double length = Math.max(5.0D, distance) / distance * 0.95D;
                teleportTo(attacker.posX - deltaX * length, attacker.posY + 1.5D, attacker.posZ - deltaZ * length);
            }
        }

        // Still take damage when healing, but reduced by a third.
        if (this.getAttackState() == EnumReaperAttackState.REST) {
            damage *= 0.25F;
        }

        return super.attackEntityFrom(source, damage);
    }

    @Override
    public int getTalkInterval() {
        return 300;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsMCA.reaper_idle;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsMCA.reaper_death;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WITHER_HURT;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        extinguish(); // No fire.
        bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        setNoGravity(true);

        if (!MCA.getConfig().allowGrimReaper) {
            setDead();
        }

        EntityLivingBase entityToAttack = this.getAttackTarget();

        // Increment floating ticks on the client when resting.
        if (world.isRemote && getAttackState() == EnumReaperAttackState.REST) {
            floatingTicks += 0.1F;
        }

        // Prevent flying off into oblivion on death...
        if (this.getHealth() <= 0.0F) {
            motionX = 0;
            motionY = 0;
            motionZ = 0;
            return;
        }

        // Stop at our current position if resting
        if (getAttackState() == EnumReaperAttackState.REST) {
            motionX = 0;
            motionY = 0;
            motionZ = 0;
        }

        // Logic for flying.
        fallDistance = 0.0F;

        if (motionY > 0) {
            motionY = motionY * 1.04F;
        } else {
            double yMod = Math.sqrt((motionX * motionX) + (motionZ * motionZ));
            motionY = motionY * 0.6F + yMod * 0.3F;
        }

        // Tick down cooldowns.
        if (getStateTransitionCooldown() > 0) {
            setStateTransitionCooldown(getStateTransitionCooldown() - 1);
        }

        // See if our entity to attack has died at any point.
        if (entityToAttack != null && !entityToAttack.isEntityAlive()) {
            this.setAttackTarget(null);
            setAttackState(EnumReaperAttackState.IDLE);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
    }

    @Override
    public String getName() {
        return "Grim Reaper";
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    public int getStateTransitionCooldown() {
        return this.dataManager.get(STATE_TRANSITION_COOLDOWN);
    }

    public void setStateTransitionCooldown(int value) {
        this.dataManager.set(STATE_TRANSITION_COOLDOWN, value);
    }

    public float getFloatingTicks() {
        return floatingTicks;
    }

    private void teleportTo(double x, double y, double z) {
        if (!world.isRemote) {
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 2.0F, 1.0F);
            this.setPosition(x, y, z);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 2.0F, 1.0F);
        }
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    private static class GrimReaperTargetAI extends EntityAIBase {
        private final EntityGrimReaper reaper;
        private int nextScanTick = 20;

        private GrimReaperTargetAI(EntityGrimReaper reaper) {
            this.reaper = reaper;
        }

        @Override
        public boolean shouldExecute() {
            if (nextScanTick-- > 0) {
                return false;
            }

            nextScanTick = 20;

            if (reaper.world.isRemote) {
                return false;
            }

            List<EntityPlayer> players = reaper.world.playerEntities.stream()
                    .filter(player -> player.isEntityAlive() && !player.isSpectator() && !player.capabilities.disableDamage)
                    .filter(player -> reaper.getDistanceSq(player) <= 48.0D * 48.0D)
                    .sorted(Comparator.comparingDouble((EntityPlayer player) -> player.posY).reversed())
                    .collect(Collectors.toList());

            for (EntityPlayer player : players) {
                if (reaper.canEntityBeSeen(player)) {
                    reaper.setAttackTarget(player);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            EntityLivingBase target = reaper.getAttackTarget();
            return target != null && target.isEntityAlive();
        }
    }

    private static class GrimReaperMeleeAI extends EntityAIBase {
        private final EntityGrimReaper reaper;

        private int blockDuration;
        private int attackDuration;
        private int retreatDuration;
        private int lastAttack;

        private GrimReaperMeleeAI(EntityGrimReaper reaper) {
            this.reaper = reaper;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase target = reaper.getAttackTarget();
            return target != null
                    && target.isEntityAlive()
                    && reaper.getDistanceSq(target) <= 144.0D
                    && reaper.ticksExisted > lastAttack + MELEE_COOLDOWN
                    && reaper.getAttackState() != EnumReaperAttackState.REST;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return retreatDuration > 0 && reaper.getAttackState() != EnumReaperAttackState.REST;
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }

        @Override
        public void startExecuting() {
            blockDuration = 50;
            attackDuration = 100;
            retreatDuration = 20;
            lastAttack = reaper.ticksExisted;
        }

        @Override
        public void resetTask() {
            reaper.setAttackState(EnumReaperAttackState.IDLE);
        }

        @Override
        public void updateTask() {
            if (reaper.getAttackState() == EnumReaperAttackState.REST) {
                return;
            }

            EntityLivingBase target = reaper.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
                retreatDuration = 0;
                return;
            }

            if (blockDuration > 0) {
                blockDuration--;
                reaper.setAttackState(EnumReaperAttackState.BLOCK);

                if (blockDuration == 0) {
                    curseBlockingPlayer(target);
                }

                if (reaper.getDistanceSq(target) <= 4.0D) {
                    int rX = reaper.getRNG().nextInt(10);
                    int rY = reaper.getRNG().nextInt(6);
                    int rZ = reaper.getRNG().nextInt(10);
                    reaper.teleportTo(reaper.posX - 5 + rX, reaper.posY + rY, reaper.posZ - 5 + rZ);
                    reaper.getNavigator().clearPath();
                }

                reaper.motionY = 0.05D;
            } else if (attackDuration > 0) {
                attackDuration--;
                reaper.setAttackState(EnumReaperAttackState.PRE);

                Vec3d dir = new Vec3d(target.posX - reaper.posX, target.posY - reaper.posY, target.posZ - reaper.posZ).normalize().scale(0.15D);
                reaper.addVelocity(dir.x, dir.y, dir.z);

                if (reaper.getDistanceSq(target) <= 1.0D) {
                    reaper.swingArm(EnumHand.MAIN_HAND);
                    attackDuration = 0;
                    target.attackEntityFrom(DamageSource.causeMobDamage(reaper), (float) reaper.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 200, 0));
                }
            } else {
                retreatDuration--;
                reaper.setAttackState(EnumReaperAttackState.POST);

                Vec3d dir = new Vec3d(target.posX - reaper.posX, target.posY - reaper.posY, target.posZ - reaper.posZ).normalize().scale(-0.1D);
                reaper.motionX = dir.x;
                reaper.motionY = dir.y;
                reaper.motionZ = dir.z;
            }
        }

        private void curseBlockingPlayer(EntityLivingBase target) {
            if (!(target instanceof EntityPlayer)) {
                return;
            }

            EntityPlayer player = (EntityPlayer) target;
            if (!player.isActiveItemStackBlocking()) {
                return;
            }

            double dX = reaper.posX - player.posX;
            double dZ = reaper.posZ - player.posZ;
            reaper.teleportTo(player.posX - (dX * 2), player.posY + 2, reaper.posZ - (dZ * 2));

            if (!reaper.world.isRemote && reaper.getRNG().nextFloat() >= 0.20F) {
                int currentItem = player.inventory.currentItem;
                int randomItem = reaper.getRNG().nextInt(InventoryPlayer.getHotbarSize());
                ItemStack currentItemStack = player.inventory.mainInventory.get(currentItem);
                ItemStack randomItemStack = player.inventory.mainInventory.get(randomItem);

                player.inventory.mainInventory.set(currentItem, randomItemStack);
                player.inventory.mainInventory.set(randomItem, currentItemStack);
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200, 0));
            }
        }
    }

    private static class GrimReaperRestAI extends EntityAIBase {
        private final EntityGrimReaper reaper;
        private int lastHeal = -REST_COOLDOWN;
        private int healingCount;
        private int healingTime;

        private GrimReaperRestAI(EntityGrimReaper reaper) {
            this.reaper = reaper;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return reaper.ticksExisted > lastHeal + REST_COOLDOWN
                    && reaper.getHealth() <= reaper.getMaxHealth() * (1.0F - (healingCount + 1.0F) / (float) REST_MAX_COUNT);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return healingTime > 0;
        }

        @Override
        public boolean isInterruptible() {
            return false;
        }

        @Override
        public void startExecuting() {
            reaper.teleportTo(reaper.posX, reaper.posY + 8, reaper.posZ);
            healingTime = REST_TIME;
            lastHeal = reaper.ticksExisted;
            healingCount++;
        }

        @Override
        public void resetTask() {
            reaper.setAttackState(EnumReaperAttackState.IDLE);
        }

        @Override
        public void updateTask() {
            healingTime--;
            reaper.setAttackState(EnumReaperAttackState.REST);
            reaper.motionX = 0.0D;
            reaper.motionY = 0.0D;
            reaper.motionZ = 0.0D;

            if (!reaper.world.isRemote && healingTime % (10 + healingCount * 5) == 0) {
                reaper.setHealth(Math.min(reaper.getMaxHealth(), reaper.getHealth() + 1.0F));
            }

            if (!reaper.world.isRemote && healingTime % 50 == 0) {
                int dX = reaper.getRNG().nextInt(16) - 8;
                int dZ = reaper.getRNG().nextInt(16) - 8;
                int x = (int) reaper.posX + dX;
                int z = (int) reaper.posZ + dZ;
                int y = Util.getSpawnSafeTopLevel(reaper.world, x, 256, z);

                reaper.world.addWeatherEffect(new EntityLightningBolt(reaper.world, x, y, z, false));

                if (healingTime % 100 == 0) {
                    EntityMob mob = reaper.getRNG().nextFloat() < 0.5F ? new EntityZombie(reaper.world) : new EntitySkeleton(reaper.world);
                    mob.setPosition(x + 0.5D, y, z + 0.5D);

                    if (mob instanceof EntitySkeleton) {
                        mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    } else {
                        mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    }

                    mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                    mob.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                    reaper.world.spawnEntity(mob);
                }
            }
        }
    }

    private static class GrimReaperIdleAI extends EntityAIBase {
        private final EntityGrimReaper reaper;
        private final double speed;
        private final int interval;
        private double wantedX;
        private double wantedY;
        private double wantedZ;

        private GrimReaperIdleAI(EntityGrimReaper reaper, double speed) {
            this(reaper, speed, 120);
        }

        private GrimReaperIdleAI(EntityGrimReaper reaper, double speed, int interval) {
            this.reaper = reaper;
            this.speed = speed;
            this.interval = interval;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (reaper.getRNG().nextInt(interval) != 0) {
                return false;
            }

            Vec3d target = getPosition();
            if (target == null) {
                return false;
            }

            wantedX = target.x;
            wantedY = target.y;
            wantedZ = target.z;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return reaper.getAttackState() != EnumReaperAttackState.REST
                    && reaper.getDistanceSq(wantedX, wantedY, wantedZ) > 2.0D;
        }

        @Override
        public void startExecuting() {
            reaper.getMoveHelper().setMoveTo(wantedX, wantedY, wantedZ, speed);
        }

        @Override
        public void resetTask() {
            reaper.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            reaper.getMoveHelper().setMoveTo(wantedX, wantedY, wantedZ, speed);
        }

        private Vec3d getPosition() {
            EntityLivingBase target = reaper.getAttackTarget();
            if (target != null) {
                return new Vec3d(target.posX, target.posY, target.posZ);
            }

            return RandomPositionGenerator.findRandomTarget(reaper, 8, 6);
        }
    }

    /**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
     * order to view its associated boss bar.
     */
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
     * more information on tracking.
     */
    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

}
