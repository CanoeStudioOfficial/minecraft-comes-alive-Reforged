package mca.entity.ai;

import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.inventory.InventoryMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

import java.util.Comparator;
import java.util.Optional;

public class EntityAIGuardEquipment extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private int updateCooldown;
    private static final int UPDATE_INTERVAL = 100;
    
    private boolean lastEquipState;
    private boolean isOnDuty;
    
    public EntityAIGuardEquipment(EntityVillagerMCA villager) {
        this.villager = villager;
        this.setMutexBits(0);
    }
    
    @Override
    public boolean shouldExecute() {
        if (villager.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        if (updateCooldown > 0) {
            updateCooldown--;
            return false;
        }
        
        updateCooldown = UPDATE_INTERVAL;
        
        boolean currentDutyState = isOnDuty();
        
        if (currentDutyState != lastEquipState) {
            return true;
        }
        
        if (currentDutyState) {
            if (villager.getHeldItemMainhand().isEmpty()) {
                return true;
            }
            
            if (hasBetterEquipmentInInventory()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void startExecuting() {
        isOnDuty = isOnDuty();
        lastEquipState = isOnDuty;
        
        if (isOnDuty) {
            equipCombatGear();
        } else {
            unequipCombatGear();
        }
    }
    
    private boolean isOnDuty() {
        if (villager.getAttackTarget() != null) {
            return true;
        }
        
        GuardEnemiesSensor sensor = villager.getGuardSensor();
        if (sensor != null && sensor.hasEnemy()) {
            return true;
        }
        
        return false;
    }
    
    private void equipCombatGear() {
        equipBestWeapon();
        equipBestArmor();
    }
    
    private void unequipCombatGear() {
        villager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        villager.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
        
        if (!shouldKeepArmorOn()) {
            villager.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
            villager.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
            villager.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);
            villager.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);
        }
    }
    
    private boolean shouldKeepArmorOn() {
        return true;
    }
    
    private void equipBestWeapon() {
        InventoryMCA inventory = villager.inventory;
        if (inventory == null) return;
        
        Optional<ItemStack> bestSword = findBestSword(inventory);
        Optional<ItemStack> bestBow = findBestBow(inventory);
        
        if (bestBow.isPresent() && shouldUseRangedWeapon()) {
            villager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, bestBow.get().copy());
        } else if (bestSword.isPresent()) {
            villager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, bestSword.get().copy());
        } else {
            villager.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
    }
    
    private boolean shouldUseRangedWeapon() {
        GuardEnemiesSensor sensor = villager.getGuardSensor();
        if (sensor != null && sensor.hasEnemy()) {
            EntityLivingBase target = sensor.getNearestEnemy();
            if (target != null) {
                double distance = villager.getDistanceSq(target);
                return distance > 25.0D;
            }
        }
        return false;
    }
    
    private void equipBestArmor() {
        InventoryMCA inventory = villager.inventory;
        if (inventory == null) return;
        
        equipBestArmorForSlot(inventory, EntityEquipmentSlot.HEAD);
        equipBestArmorForSlot(inventory, EntityEquipmentSlot.CHEST);
        equipBestArmorForSlot(inventory, EntityEquipmentSlot.LEGS);
        equipBestArmorForSlot(inventory, EntityEquipmentSlot.FEET);
    }
    
    private void equipBestArmorForSlot(InventoryMCA inventory, EntityEquipmentSlot slot) {
        Optional<ItemStack> bestArmor = findBestArmor(inventory, slot);
        
        if (bestArmor.isPresent()) {
            villager.setItemStackToSlot(slot, bestArmor.get().copy());
        } else {
            Item defaultArmor = getDefaultArmor(slot);
            if (defaultArmor != null) {
                villager.setItemStackToSlot(slot, new ItemStack(defaultArmor));
            }
        }
    }
    
    private Optional<ItemStack> findBestSword(InventoryMCA inventory) {
        Optional<ItemStack> best = Optional.empty();
        float bestDamage = 0.0F;
        
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword) stack.getItem();
                float damage = sword.getAttackDamage();
                if (damage > bestDamage) {
                    bestDamage = damage;
                    best = Optional.of(stack);
                }
            }
        }
        
        return best;
    }
    
    private Optional<ItemStack> findBestBow(InventoryMCA inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBow) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }
    
    private Optional<ItemStack> findBestArmor(InventoryMCA inventory, EntityEquipmentSlot slot) {
        Optional<ItemStack> best = Optional.empty();
        int bestDamageReduction = -1;
        
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) stack.getItem();
                if (armor.getEquipmentSlot() == slot) {
                    int damageReduction = armor.getArmorMaterial().getDamageReductionAmount(slot);
                    if (damageReduction > bestDamageReduction) {
                        bestDamageReduction = damageReduction;
                        best = Optional.of(stack);
                    }
                }
            }
        }
        
        return best;
    }
    
    private Item getDefaultArmor(EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return Items.IRON_HELMET;
            case CHEST:
                return Items.IRON_CHESTPLATE;
            case LEGS:
                return Items.IRON_LEGGINGS;
            case FEET:
                return Items.IRON_BOOTS;
            default:
                return null;
        }
    }
    
    private boolean hasBetterEquipmentInInventory() {
        InventoryMCA inventory = villager.inventory;
        if (inventory == null) return false;
        
        ItemStack currentWeapon = villager.getHeldItemMainhand();
        
        if (currentWeapon.isEmpty()) {
            return true;
        }
        
        if (currentWeapon.getItem() instanceof ItemSword) {
            float currentDamage = ((ItemSword) currentWeapon.getItem()).getAttackDamage();
            
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemSword) {
                    float newDamage = ((ItemSword) stack.getItem()).getAttackDamage();
                    if (newDamage > currentDamage) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
