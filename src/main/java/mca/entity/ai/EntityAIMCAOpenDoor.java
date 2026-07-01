package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class EntityAIMCAOpenDoor extends EntityAIBase {
    private static final int CLOSE_DELAY_TICKS = 40;
    private static final double DOOR_SCAN_DISTANCE_SQ = 4.0D;

    private final EntityVillagerMCA villager;
    private final boolean closeDoor;
    private BlockPos doorPosition = BlockPos.ORIGIN;
    private BlockDoor doorBlock;
    private int closeDoorTicks;

    public EntityAIMCAOpenDoor(EntityVillagerMCA villager, boolean closeDoor) {
        this.villager = villager;
        this.closeDoor = closeDoor;
    }

    @Override
    public boolean shouldExecute() {
        if (!(this.villager.getNavigator() instanceof PathNavigateGround)) {
            return false;
        }

        PathNavigateGround navigator = (PathNavigateGround) this.villager.getNavigator();
        Path path = navigator.getPath();
        if (path == null || path.isFinished() || !navigator.getEnterDoors()) {
            return this.villager.collidedHorizontally && this.findDoorNearVillager();
        }

        int lastIndex = Math.min(path.getCurrentPathIndex() + 3, path.getCurrentPathLength());
        for (int i = path.getCurrentPathIndex(); i < lastIndex; i++) {
            PathPoint point = path.getPathPointFromIndex(i);
            if (this.tryUseDoor(new BlockPos(point.x, point.y, point.z))
                    || this.tryUseDoor(new BlockPos(point.x, point.y + 1, point.z))) {
                return true;
            }
        }

        return this.findDoorNearVillager();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.closeDoor && (this.closeDoorTicks > 0 || this.shouldKeepDoorOpen());
    }

    @Override
    public void startExecuting() {
        this.closeDoorTicks = CLOSE_DELAY_TICKS;
        this.doorBlock.toggleDoor(this.villager.world, this.doorPosition, true);
    }

    @Override
    public void resetTask() {
        if (this.closeDoor && this.doorBlock != null) {
            this.doorBlock.toggleDoor(this.villager.world, this.doorPosition, false);
        }

        this.doorBlock = null;
        this.doorPosition = BlockPos.ORIGIN;
    }

    @Override
    public void updateTask() {
        if (this.closeDoorTicks > 0) {
            this.closeDoorTicks--;
        }
    }

    private boolean findDoorNearVillager() {
        BlockPos base = new BlockPos(this.villager);
        return this.tryUseDoor(base)
                || this.tryUseDoor(base.up())
                || this.tryUseDoor(base.north())
                || this.tryUseDoor(base.south())
                || this.tryUseDoor(base.east())
                || this.tryUseDoor(base.west());
    }

    private boolean tryUseDoor(BlockPos pos) {
        IBlockState state = this.villager.world.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof BlockDoor) || state.getMaterial() != Material.WOOD || BlockDoor.isOpen(this.villager.world, pos)) {
            return false;
        }

        if (this.villager.getDistanceSq((double) pos.getX(), this.villager.posY, (double) pos.getZ()) > DOOR_SCAN_DISTANCE_SQ) {
            return false;
        }

        this.doorPosition = getLowerDoorPosition(pos, state);
        this.doorBlock = (BlockDoor) block;
        return true;
    }

    private BlockPos getLowerDoorPosition(BlockPos pos, IBlockState state) {
        return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
    }

    private boolean shouldKeepDoorOpen() {
        AxisAlignedBB aroundDoor = new AxisAlignedBB(this.doorPosition).grow(2.0D, 1.0D, 2.0D);
        return this.villager.world.getEntitiesWithinAABB(EntityVillagerMCA.class, aroundDoor)
                .stream()
                .anyMatch(other -> other != this.villager && other.isEntityAlive());
    }
}
