package mca.server;

import com.google.common.base.Optional;
import mca.core.forge.NetMCA;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.core.minecraft.VillageHelper;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumAgeState;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.util.SchematicLoader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

public class ServerMessageHandler {

    public static void handleMessage(EntityPlayerMP player, NetMCA.ButtonAction message) {
        switch (message.getButtonId()) {
            case "gui.button.debug.startraid":
                startRaid(player);
                break;
            case "gui.button.debug.spawnguards":
                spawnGuards(player);
                break;
            case "gui.button.debug.rebuildvillage":
                rebuildVillage(player);
                break;
        }
    }

    public static void handleDestinyChoice(EntityPlayerMP player, int choiceId) {
        EnumDestinyChoice choice = EnumDestinyChoice.fromId(choiceId);
        PlayerSaveData data = PlayerSaveData.get(player);

        if (choice == EnumDestinyChoice.NONE || choice == EnumDestinyChoice.CANCEL) {
            if (choice == EnumDestinyChoice.CANCEL) {
                player.inventory.addItemStackToInventory(new ItemStack(ItemsMCA.CRYSTAL_BALL));
            }
        } else {
            if (!MCA.getConfig().serverEnableStructureSpawning) {
                return;
            }

            BlockPos spawnPos = player.getPosition();

            if (choice == EnumDestinyChoice.FAMILY) {
                SchematicLoader.spawnStructure("assets/mca/schematic/family.schematic", spawnPos, player.world);
                
                // Spawn spouse
                EnumGender spouseGender = data.getGenderPreference();
                if (spouseGender == EnumGender.UNASSIGNED) {
                    spouseGender = EnumGender.getRandom();
                }
                
                EntityVillagerMCA spouse = new EntityVillagerMCA(player.world, Optional.absent(), Optional.of(spouseGender));
                spouse.setPosition(spawnPos.getX() + 5, spawnPos.getY() + 1, spawnPos.getZ() + 5);
                spouse.finalizeMobSpawn(player.world.getDifficultyForLocation(spouse.getPosition()), null, false);
                
                // Link spouse
                spouse.set(EntityVillagerMCA.SPOUSE_UUID, Optional.of(player.getUniqueID()));
                spouse.set(EntityVillagerMCA.SPOUSE_NAME, data.getMcaName());
                spouse.set(EntityVillagerMCA.MARRIAGE_STATE, EnumMarriageState.MARRIED.getId());
                player.world.spawnEntity(spouse);
                
                data.marry(spouse.getUniqueID(), spouse.get(EntityVillagerMCA.VILLAGER_NAME));

                // Spawn children
                int numChildren = player.world.rand.nextInt(2) + 1;
                for (int i = 0; i < numChildren; i++) {
                    EntityVillagerMCA child = new EntityVillagerMCA(player.world, Optional.absent(), Optional.of(EnumGender.getRandom()));
                    child.setPosition(spawnPos.getX() + 5 + i, spawnPos.getY() + 1, spawnPos.getZ() + 4);
                    child.set(EntityVillagerMCA.AGE_STATE, EnumAgeState.CHILD.getId());
                    child.setGrowingAge(-24000); // Set to child age
                    child.finalizeMobSpawn(player.world.getDifficultyForLocation(child.getPosition()), null, false);
                    
                    // Set parents
                    mca.entity.data.ParentData parents = new mca.entity.data.ParentData(player.getUniqueID(), spouse.getUniqueID());
                    child.set(EntityVillagerMCA.PARENTS, parents.toNBT());
                    
                    player.world.spawnEntity(child);
                }
                
            } else if (choice == EnumDestinyChoice.ALONE) {
                SchematicLoader.spawnStructure("assets/mca/schematic/bachelor.schematic", spawnPos, player.world);
            } else if (choice == EnumDestinyChoice.VILLAGE) {
                SchematicLoader.spawnStructure("assets/mca/schematic/village1.schematic", spawnPos, player.world);
                
                // Spawn some villagers
                for (int i = 0; i < 5; i++) {
                    EntityVillagerMCA villager = new EntityVillagerMCA(player.world, Optional.absent(), Optional.of(EnumGender.getRandom()));
                    villager.setPosition(spawnPos.getX() + player.world.rand.nextInt(10) - 5, spawnPos.getY() + 1, spawnPos.getZ() + player.world.rand.nextInt(10) - 5);
                    villager.finalizeMobSpawn(player.world.getDifficultyForLocation(villager.getPosition()), null, false);
                    player.world.spawnEntity(villager);
                }
            }
            
            data.setHasChosenDestiny(true);
        }
    }

    private static void startRaid(EntityPlayerMP player) {
        player.sendMessage(new TextComponentString("Starting raid on village..."));
        VillageHelper.forceRaid(player);
    }

    private static void spawnGuards(EntityPlayerMP player) {
        player.sendMessage(new TextComponentString("Spawning village guards..."));
        VillageHelper.tick(player.world);
    }

    private static void rebuildVillage(EntityPlayerMP player) {
        player.sendMessage(new TextComponentString("Rebuilding annihilated village..."));
    }
}
