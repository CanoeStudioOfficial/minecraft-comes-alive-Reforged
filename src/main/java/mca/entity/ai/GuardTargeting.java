package mca.entity.ai;

import mca.compat.RaidsBackportCompat;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;

public final class GuardTargeting {
    private GuardTargeting() {
    }

    public static boolean isGuardEnemy(EntityVillagerMCA guard, EntityLivingBase target) {
        if (target == null || target == guard || !target.isEntityAlive()) {
            return false;
        }

        if (target instanceof EntityVillagerMCA) {
            return ((EntityVillagerMCA) target).getProfessionForge() == ProfessionsMCA.bandit;
        }

        if (RaidsBackportCompat.isRaidEnemy(target)) {
            return true;
        }

        if (target instanceof EntityZombieVillagerMCA) {
            return true;
        }

        if (guard != null && target instanceof EntityLiving && ((EntityLiving) target).getAttackTarget() == guard) {
            return true;
        }

        return target instanceof IMob;
    }
}
