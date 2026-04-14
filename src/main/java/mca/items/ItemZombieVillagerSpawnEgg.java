package mca.items;

import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemZombieVillagerSpawnEgg extends Item {
    private boolean isMale;

    public ItemZombieVillagerSpawnEgg(boolean isMale) {
        this.isMale = isMale;
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        if (!world.isRemote) {
            EntityZombieVillagerMCA zombie = new EntityZombieVillagerMCA(world);
            zombie.setPosition(posX + 0.5D, posY, posZ + 0.5D);
            
            // Set gender
            zombie.setGender(isMale ? EnumGender.MALE : EnumGender.FEMALE);
            
            // Set random genetics
            NBTTagCompound genetics = new NBTTagCompound();
            genetics.setFloat("Size", world.rand.nextFloat());
            genetics.setFloat("Width", world.rand.nextFloat());
            genetics.setFloat("Melanin", world.rand.nextFloat());
            genetics.setFloat("Hemoglobin", world.rand.nextFloat());
            genetics.setFloat("Eumelanin", world.rand.nextFloat());
            genetics.setFloat("Pheomelanin", world.rand.nextFloat());
            genetics.setFloat("Skin", world.rand.nextFloat());
            genetics.setFloat("Face", world.rand.nextFloat());
            zombie.setGeneticsTag(genetics);
            
            // Set random traits
            NBTTagCompound traits = new NBTTagCompound();
            traits.setBoolean("Heterochromia", world.rand.nextFloat() < 0.01F);
            zombie.setTraitsTag(traits);
            
            zombie.onInitialSpawn(world.getDifficultyForLocation(zombie.getPosition()), null);
            world.spawnEntity(zombie);

            if (!player.capabilities.isCreativeMode) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }
        }

        return EnumActionResult.PASS;
    }
}
