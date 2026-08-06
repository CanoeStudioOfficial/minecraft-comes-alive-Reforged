package mca.entity.ai;

import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMoveState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;

public class EntityAIMoveState extends EntityAIBase {
    private final EntityVillagerMCA villager;

    public EntityAIMoveState(EntityVillagerMCA entityIn) {
        this.villager = entityIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getAttackTarget() != null) {
            return false;
        }

        EnumMoveState state = EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE));
        if (state == EnumMoveState.STAY) {
            return true;
        }

        if (state == EnumMoveState.FOLLOW
                && villager.playerToFollowUUID != null
                && !villager.playerToFollowUUID.equals(Constants.ZERO_UUID)
                && villager.world.getPlayerEntityByUUID(villager.playerToFollowUUID) != null) {
            return true;
        }

        if (state == EnumMoveState.FOLLOW) {
            villager.playerToFollowUUID = Constants.ZERO_UUID;
            villager.set(EntityVillagerMCA.MOVE_STATE, EnumMoveState.MOVE.getId());
            villager.getNavigator().clearPath();
        }

        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    @Override
    public void resetTask() {
        villager.getNavigator().clearPath();
    }

    public void updateTask() {
        PathNavigate nav = villager.getNavigator();
        switch (EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE))) {
            case FOLLOW:
                EntityPlayer playerToFollow = villager.playerToFollowUUID == null
                        ? null
                        : villager.world.getPlayerEntityByUUID(villager.playerToFollowUUID);
                double distance = playerToFollow != null ? villager.getDistance(playerToFollow) : -1.0D;
                if (playerToFollow == null) {
                    villager.playerToFollowUUID = Constants.ZERO_UUID;
                    villager.set(EntityVillagerMCA.MOVE_STATE, EnumMoveState.MOVE.getId());
                    nav.clearPath();
                    break;
                } else if (distance >= 3.0D && distance <= 10.0D) {
                    nav.setPath(nav.getPathToEntityLiving(playerToFollow), villager.isRiding() ? 1.7D : 0.8D);
                } else if (distance > 10.0D) {
                    if (villager.attemptTeleport(playerToFollow.posX, playerToFollow.posY, playerToFollow.posZ)) {
                        nav.clearPath();
                    } else {
                        nav.tryMoveToEntityLiving(playerToFollow, villager.isRiding() ? 1.7D : 0.8D);
                    }
                } else { // close enough to avoid crowding the player
                    nav.clearPath();
                }
                break;
            case STAY:
                villager.playerToFollowUUID = Constants.ZERO_UUID;
                nav.clearPath();
                break;
        }
    }
}
