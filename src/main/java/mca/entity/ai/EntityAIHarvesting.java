package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the complete farming loop used by the high-version farmer task:
 * plant empty farmland, harvest mature crops, and optionally use bone meal.
 */
public class EntityAIHarvesting extends AbstractEntityAIChore {
    private static final int SCAN_INTERVAL = 1200;
    private static final int WORK_TICKS = 40;
    private static final double WORK_DISTANCE_SQ = 6.0D;

    private final List<BlockPos> plantable = new ArrayList<>();
    private final List<BlockPos> harvestable = new ArrayList<>();
    private final List<BlockPos> bonemealable = new ArrayList<>();

    private int lastLandScan = Integer.MIN_VALUE;
    private int lastCropScan = Integer.MIN_VALUE;
    private int retryCooldown;
    private int workingTicks;
    private boolean taskActive;
    private BlockPos target;

    public EntityAIHarvesting(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (retryCooldown > 0) {
            retryCooldown--;
            return false;
        }

        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }

        return canDoChore() && isAssignedChore(EnumChore.HARVEST);
    }

    @Override
    public void startExecuting() {
        taskActive = true;
        workingTicks = 0;
        target = null;

        if (!hasAssigningPlayer()) {
            villager.stopChore();
            return;
        }

        if (!villager.inventory.contains(ItemHoe.class)) {
            villager.say(getAssigningPlayer(), "chore.harvesting.nohoe");
            villager.stopChore();
            return;
        }

        scanTargets(true);
        chooseTarget();
    }

    @Override
    public void updateTask() {
        super.updateTask();
        if (!taskActive || !hasAssigningPlayer()) {
            return;
        }

        if (!villager.inventory.contains(ItemHoe.class)) {
            villager.say(getAssigningPlayer(), "chore.harvesting.nohoe");
            villager.stopChore();
            return;
        }

        if (target == null) {
            scanTargets(false);
            chooseTarget();
            if (target == null) {
                retryCooldown = 100;
                return;
            }
        }

        if (!isCurrentTargetValid()) {
            removeTargetFromLists();
            target = null;
            workingTicks = 0;
            return;
        }

        if (villager.getDistanceSq(target) > WORK_DISTANCE_SQ) {
            workingTicks = 0;
            if (!villager.moveTowardsBlock(target, 0.5D)) {
                removeTargetFromLists();
                target = null;
                retryCooldown = 40;
            }
            return;
        }

        villager.getNavigator().clearPath();
        workingTicks++;
        if (workingTicks % 5 == 0) {
            villager.swingArm(EnumHand.MAIN_HAND);
        }

        if (workingTicks >= WORK_TICKS) {
            performWork();
            workingTicks = 0;
            target = null;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return taskActive
                && canDoChore()
                && isAssignedChore(EnumChore.HARVEST)
                && retryCooldown == 0;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        taskActive = false;
        target = null;
        workingTicks = 0;
        plantable.clear();
        harvestable.clear();
        bonemealable.clear();
    }

    private void scanTargets(boolean force) {
        int now = villager.ticksExisted;

        if (force || plantable.isEmpty() && now - lastLandScan >= SCAN_INTERVAL) {
            plantable.clear();
            addValidFarmland(Util.getNearbyBlocks(villager.getPos(), villager.world,
                    BlockFarmland.class, 32, 8));
            lastLandScan = now;
        }

        if (force || harvestable.isEmpty() && bonemealable.isEmpty()
                && now - lastCropScan >= SCAN_INTERVAL) {
            harvestable.clear();
            bonemealable.clear();
            for (BlockPos pos : Util.getNearbyBlocks(villager.getPos(), villager.world,
                    null, 32, 8)) {
                Block block = villager.world.getBlockState(pos).getBlock();
                if (block instanceof BlockCrops || block instanceof BlockStem) {
                    if (isMatureCrop(pos)) {
                        addUnique(harvestable, pos);
                    } else if (hasBoneMeal() && isImmatureCrop(pos)) {
                        addUnique(bonemealable, pos);
                    }
                }
            }
            lastCropScan = now;
        }

        removeInvalidTargets();
    }

    private void addValidFarmland(List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (isValidFarmland(pos)) {
                addUnique(plantable, pos);
            }
        }
    }

    private void chooseTarget() {
        removeInvalidTargets();

        if (hasPlantableSeed(null)) {
            target = Util.getNearestPoint(villager.getPos(), plantable);
        }
        if (target == null) {
            target = Util.getNearestPoint(villager.getPos(), harvestable);
        }
        if (target == null && hasBoneMeal()) {
            target = Util.getNearestPoint(villager.getPos(), bonemealable);
        }
    }

    private void removeInvalidTargets() {
        plantable.removeIf(pos -> !isValidFarmland(pos));
        harvestable.removeIf(pos -> !isMatureCrop(pos));
        bonemealable.removeIf(pos -> !isImmatureCrop(pos) || !hasBoneMeal());
    }

    private void removeTargetFromLists() {
        if (target == null) {
            return;
        }
        plantable.remove(target);
        harvestable.remove(target);
        bonemealable.remove(target);
    }

    private boolean isCurrentTargetValid() {
        return isValidFarmland(target) || isMatureCrop(target) || isImmatureCrop(target);
    }

    private boolean isValidFarmland(BlockPos pos) {
        return villager.world.getBlockState(pos).getBlock() instanceof BlockFarmland
                && villager.world.isAirBlock(pos.up());
    }

    private boolean isMatureCrop(BlockPos pos) {
        IBlockState state = villager.world.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof BlockStem
                || block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(state);
    }

    private boolean isImmatureCrop(BlockPos pos) {
        IBlockState state = villager.world.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof BlockCrops && !((BlockCrops) block).isMaxAge(state);
    }

    private boolean hasBoneMeal() {
        return findInventorySlot(stack -> stack.getItem() == Items.DYE && stack.getMetadata() == 15) >= 0;
    }

    private boolean hasPlantableSeed(@Nullable Block preferredCrop) {
        return findPlantableSlot(BlockPos.ORIGIN, preferredCrop) >= 0;
    }

    private int findPlantableSlot(BlockPos targetPos, @Nullable Block preferredCrop) {
        int fallback = -1;
        for (int slot = 0; slot < villager.inventory.getSizeInventory(); slot++) {
            ItemStack stack = villager.inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof IPlantable)) {
                continue;
            }

            IBlockState plantState = getPlantState((IPlantable) stack.getItem(), targetPos);
            if (!isSupportedPlant(plantState)) {
                continue;
            }
            if (preferredCrop != null && plantState.getBlock() == preferredCrop) {
                return slot;
            }
            if (fallback < 0) {
                fallback = slot;
            }
        }
        return fallback;
    }

    private int findInventorySlot(ItemMatcher matcher) {
        for (int slot = 0; slot < villager.inventory.getSizeInventory(); slot++) {
            ItemStack stack = villager.inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && matcher.matches(stack)) {
                return slot;
            }
        }
        return -1;
    }

    private IBlockState getPlantState(IPlantable plantable, BlockPos pos) {
        try {
            return plantable.getPlant(villager.world, pos);
        } catch (RuntimeException ignored) {
            return Blocks.AIR.getDefaultState();
        }
    }

    private boolean isSupportedPlant(@Nullable IBlockState state) {
        return state != null && (state.getBlock() instanceof BlockCrops || state.getBlock() instanceof BlockStem);
    }

    private void performWork() {
        if (isValidFarmland(target)) {
            plantSeeds(target.up(), null);
        } else if (isMatureCrop(target)) {
            IBlockState state = villager.world.getBlockState(target);
            Block crop = state.getBlock();
            harvestCrop(target);
            if (!(crop instanceof BlockStem)) {
                plantSeeds(target, crop);
            }
        } else if (isImmatureCrop(target)) {
            useBoneMeal(target);
        }

        removeTargetFromLists();
        damageHoe();
    }

    private void plantSeeds(BlockPos cropPos, @Nullable Block preferredCrop) {
        int slot = findPlantableSlot(cropPos, preferredCrop);
        if (slot < 0) {
            if (hasAssigningPlayer()) {
                villager.say(getAssigningPlayer(), "chore.harvesting.noseed");
            }
            return;
        }

        ItemStack stack = villager.inventory.getStackInSlot(slot);
        IBlockState state = getPlantState((IPlantable) stack.getItem(), cropPos);
        if (!isSupportedPlant(state) || !state.getBlock().canPlaceBlockAt(villager.world, cropPos)) {
            return;
        }

        if (villager.world.setBlockState(cropPos, state, 3)) {
            stack.shrink(1);
            villager.swingArm(EnumHand.MAIN_HAND);
            bonemealable.remove(cropPos);
        }
    }

    private void harvestCrop(BlockPos pos) {
        IBlockState state = villager.world.getBlockState(pos);
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, villager.world, pos, state, 0);
        for (ItemStack drop : drops) {
            villager.inventory.addItem(drop);
        }
        villager.world.setBlockToAir(pos);
        villager.swingArm(EnumHand.MAIN_HAND);
    }

    private void useBoneMeal(BlockPos pos) {
        int slot = findInventorySlot(stack -> stack.getItem() == Items.DYE
                && stack.getMetadata() == 15);
        if (slot < 0) {
            return;
        }

        ItemStack boneMeal = villager.inventory.getStackInSlot(slot);
        if (ItemDye.applyBonemeal(boneMeal, villager.world, pos)) {
            villager.swingArm(EnumHand.MAIN_HAND);
        }
    }

    private void damageHoe() {
        ItemStack hoe = villager.inventory.getBestItemOfType(ItemHoe.class);
        if (!hoe.isEmpty()) {
            hoe.damageItem(1, villager);
        }
    }

    private void addUnique(List<BlockPos> positions, BlockPos position) {
        if (!positions.contains(position)) {
            positions.add(position);
        }
    }

    private interface ItemMatcher {
        boolean matches(ItemStack stack);
    }
}
