package mca.entity;

import com.google.common.base.Optional;
import mca.api.API;
import mca.core.minecraft.ProfessionsMCA;
import mca.enums.EnumGender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityBanditMCA extends EntityVillagerMCA {
    public EntityBanditMCA(World worldIn) {
        super(worldIn, Optional.of(ProfessionsMCA.bandit), Optional.absent());
        ensureBanditIdentity();
        if (worldIn != null && !worldIn.isRemote) {
            setBanditPillagerCareer();
            refreshSpecialAI();
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        ensureBanditIdentity();
    }

    public void ensureBanditIdentity() {
        if (world == null || world.isRemote) {
            return;
        }

        EnumGender gender = EnumGender.byId(get(GENDER));
        if (gender == EnumGender.UNASSIGNED) {
            gender = EnumGender.getRandom();
            set(GENDER, gender.getId());
        }

        if (get(VILLAGER_NAME).isEmpty()) {
            set(VILLAGER_NAME, API.getRandomName(gender));
        }

        setProfession(ProfessionsMCA.bandit);
        if (!hasBanditCareer()) {
            setBanditPillagerCareer();
        }

        if (get(TEXTURE).isEmpty()) {
            set(TEXTURE, API.getRandomSkin(this));
        }
    }
}
