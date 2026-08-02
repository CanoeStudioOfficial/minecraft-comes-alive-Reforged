package mca.items;

import com.google.common.base.Optional;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSpawnEgg extends Item {
    public enum SpawnType {
        VILLAGER,
        ZOMBIE_VILLAGER,
        GRIM_REAPER
    }

    private final boolean isMale;
    private final SpawnType spawnType;

    public ItemSpawnEgg(boolean isMale) {
        this(isMale, SpawnType.VILLAGER);
    }

    public ItemSpawnEgg(boolean isMale, boolean isZombie) {
        this(isMale, isZombie ? SpawnType.ZOMBIE_VILLAGER : SpawnType.VILLAGER);
    }

    public ItemSpawnEgg(SpawnType spawnType) {
        this(false, spawnType);
    }

    private ItemSpawnEgg(boolean isMale, SpawnType spawnType) {
        this.isMale = isMale;
        this.spawnType = spawnType;
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        if (!world.isRemote) {
            EntityLiving entity = createEntity(world);
            entity.setPosition(posX + 0.5D, posY, posZ + 0.5D);
            entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), null);
            world.spawnEntity(entity);

            if (!player.capabilities.isCreativeMode) player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
        }

        return EnumActionResult.PASS;
    }

    private EntityLiving createEntity(World world) {
        switch (spawnType) {
            case ZOMBIE_VILLAGER:
                return new EntityZombieVillagerMCA(world, isMale ? EnumGender.MALE : EnumGender.FEMALE);
            case GRIM_REAPER:
                return new EntityGrimReaper(world);
            case VILLAGER:
            default:
                return new EntityVillagerMCA(world, Optional.absent(), Optional.of(isMale ? EnumGender.MALE : EnumGender.FEMALE));
        }
    }
}
