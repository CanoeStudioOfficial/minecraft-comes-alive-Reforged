package mca.structure;

import com.google.common.base.Optional;
import mca.api.API;
import mca.core.Config;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumSetupType;
import mca.items.ItemCrystalBall;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Random;

/** Owns the one-time crystal-ball initialization flow. */
public final class McaDestinyManager {
    private McaDestinyManager() {
    }

    public static void choose(EntityPlayerMP player, EnumSetupType type, String playerName, EnumGender preference) {
        if (player == null || type == null || player.world.isRemote) {
            return;
        }

        PlayerSaveData data = PlayerSaveData.get(player);
        if (data.isHasChosenDestiny() || data.isDestinyInProgress()) {
            return;
        }

        ItemStack crystalBall = findCrystalBall(player);
        if (crystalBall.isEmpty()) {
            return;
        }

        if (preference != null) {
            data.setGenderPreference(preference);
        }
        if (playerName != null && !playerName.trim().isEmpty()) {
            data.setPlayerName(playerName);
        }

        if (type == EnumSetupType.NONE) {
            complete(player, data, type, crystalBall);
            return;
        }

        Config config = MCA.getConfig();
        boolean dedicatedServer = FMLCommonHandler.instance().getMinecraftServerInstance() != null
                && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer();
        if ((!dedicatedServer && !config.enableStructureSpawning) || (dedicatedServer && !config.serverEnableStructureSpawning)) {
            player.sendMessage(new TextComponentTranslation("notify.setup.structures_disabled"));
            return;
        }

        McaStructure structure = McaStructureRegistry.get(getStructureId(type));
        if (structure == null) {
            MCA.getLog().error("Missing destiny structure for setup type " + type);
            return;
        }

        data.setDestinyInProgress(true);
        BlockPos anchor = player.getPosition();
        boolean queued = McaStructurePlacer.enqueue(player.world, structure, anchor, success -> {
            if (!success) {
                data.setDestinyInProgress(false);
                return;
            }

            if (type == EnumSetupType.FAMILY) {
                createFamily((WorldServer) player.world, player, data);
            } else if (type == EnumSetupType.VILLAGE) {
                createVillage((WorldServer) player.world, anchor, structure);
            }
            complete(player, data, type, crystalBall);
        });

        if (!queued) {
            data.setDestinyInProgress(false);
            player.sendMessage(new TextComponentTranslation("notify.setup.structures_unavailable"));
        }
    }

    private static String getStructureId(EnumSetupType type) {
        switch (type) {
            case ALONE:
                return "bachelor";
            case FAMILY:
                return "family";
            case VILLAGE:
                return "village1";
            default:
                return "";
        }
    }

    private static void complete(EntityPlayerMP player, PlayerSaveData data, EnumSetupType type, ItemStack crystalBall) {
        data.setDestinyInProgress(false);
        data.setHasChosenDestiny(true);
        consumeCrystalBall(player, crystalBall);
        player.sendMessage(new TextComponentTranslation("notify.setup.destiny_chose", new TextComponentTranslation("gui.button." + type.getName())));
        if (player.world instanceof WorldServer) {
            ((WorldServer) player.world).spawnParticle(EnumParticleTypes.PORTAL, player.posX, player.posY + 1.0D, player.posZ, 20, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    private static ItemStack findCrystalBall(EntityPlayerMP player) {
        if (player.getHeldItemMainhand().getItem() instanceof ItemCrystalBall) {
            return player.getHeldItemMainhand();
        }
        if (player.getHeldItemOffhand().getItem() instanceof ItemCrystalBall) {
            return player.getHeldItemOffhand();
        }
        return ItemStack.EMPTY;
    }

    private static void consumeCrystalBall(EntityPlayerMP player, ItemStack stack) {
        if (player.isCreative() || stack.isEmpty() || !(stack.getItem() instanceof ItemCrystalBall)) {
            return;
        }

        if (player.getHeldItemMainhand() == stack || player.getHeldItemOffhand() == stack) {
            stack.shrink(1);
            return;
        }

    }

    private static void createFamily(WorldServer world, EntityPlayerMP player, PlayerSaveData data) {
        Random random = world.rand;
        boolean spouseMale = data.getGenderPreference() == EnumGender.MALE
                || (data.getGenderPreference() == EnumGender.UNASSIGNED && random.nextBoolean());
        if (data.getGenderPreference() == EnumGender.FEMALE) {
            spouseMale = false;
        }

        EntityVillagerMCA spouse = createVillager(world, spouseMale ? EnumGender.MALE : EnumGender.FEMALE, player.posX - 2.0D, player.posY, player.posZ);
        spouse.marry(player);
        data.marry(spouse.getUniqueID(), spouse.get(EntityVillagerMCA.VILLAGER_NAME));
        PlayerHistory spouseHistory = spouse.getPlayerHistoryFor(player.getUniqueID());
        spouseHistory.setHearts(100);
        spouseHistory.setDialogueType(EnumDialogueType.SPOUSE);

        int children = random.nextInt(3);
        for (int i = 0; i < children; i++) {
            EntityVillagerMCA child = createVillager(world, random.nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE,
                    player.posX + 1.0D + random.nextInt(3), player.posY, player.posZ);
            child.setProfession(ProfessionsMCA.child);
            child.setVanillaCareer(0);
            child.set(EntityVillagerMCA.AGE_STATE, mca.enums.EnumAgeState.CHILD.getId());
            child.setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
            child.set(EntityVillagerMCA.PARENTS, mca.entity.data.ParentData.create(player.getUniqueID(), spouse.getUniqueID(), player.getName(), spouse.get(EntityVillagerMCA.VILLAGER_NAME)).toNBT());
            child.refreshSpecialAI();
            child.getPlayerHistoryFor(player.getUniqueID()).setHearts(100);
            child.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.CHILDP);
        }
    }

    private static EntityVillagerMCA createVillager(WorldServer world, EnumGender gender, double x, double y, double z) {
        EntityVillagerMCA villager = new EntityVillagerMCA(world, Optional.absent(), Optional.of(gender));
        villager.setPosition(x, y, z);
        villager.set(EntityVillagerMCA.TEXTURE, API.getRandomSkin(villager));
        world.spawnEntity(villager);
        return villager;
    }

    private static void createVillage(WorldServer world, BlockPos anchor, McaStructure structure) {
        for (McaStructure.ResolvedBlock block : structure.getResolvedBlocks()) {
            BlockPos pos = getTargetPosition(structure, anchor, block);
            if (block.getState().getBlock() == Blocks.MOB_SPAWNER) {
                world.setBlockToAir(pos);
                createVillager(world, EnumGender.getRandom(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            } else if (block.getState().getBlock() == Blocks.BEDROCK) {
                world.setBlockState(pos, BlocksMCA.TOMBSTONE.getDefaultState(), 3);
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof mca.tile.TileTombstone) {
                    mca.tile.TileTombstone tombstone = (mca.tile.TileTombstone) tile;
                    EnumGender gender = world.rand.nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE;
                    tombstone.signText[0] = new net.minecraft.util.text.TextComponentString("");
                    tombstone.signText[1] = new net.minecraft.util.text.TextComponentString(API.getRandomName(gender));
                    tombstone.signText[2] = new net.minecraft.util.text.TextComponentString("RIP");
                    tombstone.signText[3] = new net.minecraft.util.text.TextComponentString("");
                    tombstone.markDirty();
                    world.notifyBlockUpdate(pos, BlocksMCA.TOMBSTONE.getDefaultState(), BlocksMCA.TOMBSTONE.getDefaultState(), 3);
                }
            }
        }
    }

    private static BlockPos getTargetPosition(McaStructure structure, BlockPos anchor, McaStructure.ResolvedBlock block) {
        return new BlockPos(anchor.getX() - structure.getWidth() / 2 + block.getX(), anchor.getY() - 1 + block.getY(), anchor.getZ() - structure.getLength() / 2 + block.getZ());
    }
}
