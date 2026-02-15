package mca.entity.ai;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.core.minecraft.ProfessionsMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuardEnemiesSensor {
    private static final Map<ResourceLocation, Integer> DEFAULT_TARGET_PRIORITIES = new HashMap<>();
    
    static {
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:zombie"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:drowned"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:husk"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:evoker"), 3);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:vindicator"), 3);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:vex"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:spider"), 1);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:cave_spider"), 1);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:skeleton"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:stray"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:witch"), 2);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:enderman"), 1);
        DEFAULT_TARGET_PRIORITIES.put(new ResourceLocation("minecraft:creeper"), -1);
    }
    
    private final EntityVillagerMCA villager;
    private EntityLivingBase nearestEnemy;
    private int updateCooldown;
    private static final int UPDATE_INTERVAL = 20;
    
    public GuardEnemiesSensor(EntityVillagerMCA villager) {
        this.villager = villager;
    }
    
    public void tick() {
        if (updateCooldown > 0) {
            updateCooldown--;
            return;
        }
        
        updateCooldown = UPDATE_INTERVAL;
        nearestEnemy = findNearestHostile();
    }
    
    @Nullable
    public EntityLivingBase getNearestEnemy() {
        return nearestEnemy;
    }
    
    public boolean hasEnemy() {
        return nearestEnemy != null && nearestEnemy.isEntityAlive();
    }
    
    @Nullable
    private EntityLivingBase findNearestHostile() {
        if (villager.world == null || villager.world.isRemote) {
            return null;
        }
        
        if (!isGuard(villager)) {
            return null;
        }
        
        double range = MCA.getConfig().guardPatrolRadius;
        AxisAlignedBB searchBox = villager.getEntityBoundingBox().grow(range, range / 2, range);
        
        List<EntityLivingBase> entities = villager.world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        
        EntityLivingBase result = null;
        int bestPriority = -1;
        double bestDistance = Double.MAX_VALUE;
        
        for (EntityLivingBase e : entities) {
            if (e == villager || !e.isEntityAlive() || !villager.canEntityBeSeen(e)) {
                continue;
            }
            
            int priority = getPriority(e, villager);
            if (priority < 0) {
                continue;
            }
            
            double distance = e.getDistanceSq(villager);
            
            if (priority > bestPriority || (priority == bestPriority && distance < bestDistance)) {
                bestPriority = priority;
                bestDistance = distance;
                result = e;
            }
        }
        
        return result;
    }
    
    private boolean isGuard(EntityVillagerMCA villager) {
        return villager.getProfessionForge() == ProfessionsMCA.guard;
    }
    
    private boolean isHostile(EntityLivingBase entity) {
        return getPriority(entity, null) >= 0;
    }
    
    private int getPriority(EntityLivingBase entity, EntityVillagerMCA guard) {
        if (entity instanceof EntityVillagerMCA) {
            EntityVillagerMCA otherVillager = (EntityVillagerMCA) entity;
            if (otherVillager.getProfessionForge() == ProfessionsMCA.bandit) {
                return 10;
            }
            return -1;
        }
        
        if (guard != null && entity instanceof EntityMob) {
            EntityMob mob = (EntityMob) entity;
            if (mob.getAttackTarget() == guard) {
                return 9;
            }
        }
        
        ResourceLocation entityId = getEntityId(entity);
        if (entityId != null) {
            if (DEFAULT_TARGET_PRIORITIES.containsKey(entityId)) {
                return DEFAULT_TARGET_PRIORITIES.get(entityId);
            }
        }
        
        if (MCA.getConfig().guardsTargetMonsters && entity instanceof IMob) {
            return 3;
        }
        
        return -1;
    }
    
    @Nullable
    private ResourceLocation getEntityId(EntityLivingBase entity) {
        return new ResourceLocation(entity.getClass().getSimpleName().toLowerCase());
    }
    
    public static boolean isPreferredTarget(EntityVillagerMCA guard, EntityLivingBase target) {
        if (guard == null || target == null) {
            return false;
        }
        
        if (guard.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        GuardEnemiesSensor sensor = guard.getGuardSensor();
        if (sensor == null) {
            return false;
        }
        
        EntityLivingBase nearest = sensor.getNearestEnemy();
        return nearest == target;
    }
}
