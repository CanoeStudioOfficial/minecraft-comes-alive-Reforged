package mca.entity;

import com.google.common.base.Optional;
import mca.enums.EnumGender;
import net.minecraft.world.World;

public class EntityVillagerMCAMale extends EntityVillagerMCA {
    public EntityVillagerMCAMale(World world) {
        super(world, Optional.absent(), Optional.of(EnumGender.MALE));
    }
}
