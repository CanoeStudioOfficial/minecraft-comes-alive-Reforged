package mca.util;

import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 动态约束管理器 - 支持从配置文件动态加载职业约束
 */
public class ConstraintManager {
    
    private static final Map<String, DynamicConstraint> DYNAMIC_CONSTRAINTS = new HashMap<>();
    
    /**
     * 动态约束类
     */
    public static class DynamicConstraint {
        private final String id;
        private final BiPredicate<EntityVillagerMCA, EntityPlayer> predicate;
        
        public DynamicConstraint(String id, BiPredicate<EntityVillagerMCA, EntityPlayer> predicate) {
            this.id = id.toLowerCase();
            this.predicate = predicate;
        }
        
        public String getId() {
            return id;
        }
        
        public boolean test(EntityVillagerMCA villager, EntityPlayer player) {
            return predicate.test(villager, player);
        }
    }
    
    /**
     * 注册或获取职业约束
     * @param professionName 职业名称（可以是完整ID如"minecraft:farmer"或仅路径如"farmer"）
     * @return 动态约束对象
     */
    public static DynamicConstraint getOrCreateProfessionConstraint(String professionName) {
        String key = professionName.toLowerCase();
        
        if (DYNAMIC_CONSTRAINTS.containsKey(key)) {
            return DYNAMIC_CONSTRAINTS.get(key);
        }
        
        DynamicConstraint constraint = new DynamicConstraint(key, (villager, player) -> {
            VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
            if (profession == null) return false;
            ResourceLocation registryName = profession.getRegistryName();
            if (registryName == null) return false;
            
            String profKey = key;
            // 支持带命名空间的完整ID匹配
            if (profKey.contains(":")) {
                return registryName.toString().equalsIgnoreCase(profKey);
            }
            // 仅路径匹配
            return registryName.getPath().equalsIgnoreCase(profKey);
        });
        
        DYNAMIC_CONSTRAINTS.put(key, constraint);
        return constraint;
    }
    
    /**
     * 注册或获取反向职业约束
     * @param professionName 职业名称
     * @return 动态约束对象
     */
    public static DynamicConstraint getOrCreateNotProfessionConstraint(String professionName) {
        String key = "!" + professionName.toLowerCase();
        
        if (DYNAMIC_CONSTRAINTS.containsKey(key)) {
            return DYNAMIC_CONSTRAINTS.get(key);
        }
        
        DynamicConstraint constraint = new DynamicConstraint(key, (villager, player) -> {
            VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
            if (profession == null) return true;
            ResourceLocation registryName = profession.getRegistryName();
            if (registryName == null) return true;
            
            String profKey = professionName.toLowerCase();
            if (profKey.contains(":")) {
                return !registryName.toString().equalsIgnoreCase(profKey);
            }
            return !registryName.getPath().equalsIgnoreCase(profKey);
        });
        
        DYNAMIC_CONSTRAINTS.put(key, constraint);
        return constraint;
    }
    
    /**
     * 从字符串获取约束（支持动态职业约束）
     * @param value 约束字符串
     * @return 动态约束对象，如果不存在则返回null
     */
    public static DynamicConstraint getConstraint(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        String key = value.toLowerCase();
        
        // 检查缓存
        if (DYNAMIC_CONSTRAINTS.containsKey(key)) {
            return DYNAMIC_CONSTRAINTS.get(key);
        }
        
        // 创建新的职业约束
        if (key.startsWith("!")) {
            return getOrCreateNotProfessionConstraint(key.substring(1));
        } else {
            return getOrCreateProfessionConstraint(key);
        }
    }
    
    /**
     * 清除所有动态约束缓存
     */
    public static void clearConstraints() {
        DYNAMIC_CONSTRAINTS.clear();
    }
    
    /**
     * 获取所有已注册的动态约束
     */
    public static Map<String, DynamicConstraint> getAllConstraints() {
        return new HashMap<>(DYNAMIC_CONSTRAINTS);
    }
}
