package mca.entity;

import com.google.common.base.Optional;
import mca.enums.EnumGender;
import net.minecraft.world.World;

public class EntityVillagerMCAFemale extends EntityVillagerMCA {
    public EntityVillagerMCAFemale(World world) {
        super(world, Optional.absent(), Optional.of(EnumGender.FEMALE));
    }
}
