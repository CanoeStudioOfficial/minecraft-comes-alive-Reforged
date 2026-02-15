package mca.core.forge;

import mca.client.network.ClientMessageQueue;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.MCAServer;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.core.minecraft.WorldEventListenerMCA;
import mca.core.minecraft.SoundsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.items.ItemBaby;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventHooks {
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    public Map<UUID, ItemStack> limbo = new HashMap<>();

    private int reaperSummonTicks = 0;
    private BlockPos reaperSpawnPos = BlockPos.ORIGIN;
    private World reaperSpawnWorld = null;

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        ItemsMCA.register(event);
        BlocksMCA.registerItemBlocks(event);

    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        BlocksMCA.register(event);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) event.getWorld().addEventListener(new WorldEventListenerMCA());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        ClientMessageQueue.processScheduledMessages();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        MCAServer.get().tick();

        if (reaperSummonTicks > 0) {
            reaperSummonTicks--;

            // Spawn particles around the summon point.
            if (reaperSpawnWorld instanceof WorldServer) {
                WorldServer worldServer = (WorldServer) reaperSpawnWorld;
                worldServer.spawnParticle(EnumParticleTypes.PORTAL, reaperSpawnPos.getX() + 0.5D, reaperSpawnPos.getY(), reaperSpawnPos.getZ() + 0.5D, 20, 0.5D, 0.5D, 0.5D, 0.1D);
            }

            // Lightning will strike periodically.
            if (reaperSummonTicks % 40 == 0) { // Every 2 seconds
                double dX = reaperSpawnPos.getX() + (reaperSpawnWorld.rand.nextInt(6) * (reaperSpawnWorld.rand.nextBoolean() ? 1 : -1));
                double dZ = reaperSpawnPos.getZ() + (reaperSpawnWorld.rand.nextInt(6) * (reaperSpawnWorld.rand.nextBoolean() ? 1 : -1));
                double y = reaperSpawnWorld.getHeight(new BlockPos(dX, 0, dZ)).getY();

                EntityLightningBolt lightning = new EntityLightningBolt(reaperSpawnWorld, dX, y, dZ, false);
                reaperSpawnWorld.spawnEntity(lightning);

                // On the first lightning bolt (or at 80 ticks), send the summon sound.
                if (reaperSummonTicks == 80) {
                    reaperSpawnWorld.playSound(null, reaperSpawnPos, SoundsMCA.reaper_summon, SoundCategory.HOSTILE, 1.0F, 1.0F);
                }
            }

            if (reaperSummonTicks == 0) {
                EntityGrimReaper reaper = new EntityGrimReaper(reaperSpawnWorld);
                reaper.setPosition(reaperSpawnPos.getX() + 0.5D, reaperSpawnPos.getY(), reaperSpawnPos.getZ() + 0.5D);
                reaperSpawnWorld.spawnEntity(reaper);

                reaperSpawnWorld = null;
                reaperSpawnPos = BlockPos.ORIGIN;
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        Entity entity = event.getEntity();

        if (world.isRemote) return;

        if (entity.getClass().equals(EntityVillager.class)) {
            EntityVillager originalVillager = (EntityVillager) entity;

            if (MCA.getConfig().overwriteOriginalVillagers) {
                EntityVillagerMCA newVillager = new EntityVillagerMCA(world,
                        com.google.common.base.Optional.of(originalVillager.getProfessionForge()),
                        com.google.common.base.Optional.absent());
                newVillager.setPosition(originalVillager.posX, originalVillager.posY, originalVillager.posZ);
                newVillager.finalizeMobSpawn(world.getDifficultyForLocation(newVillager.getPos()), null, false);
                newVillager.forcePositionAsHome();
                world.spawnEntity(newVillager);
                event.setCanceled(true);
            } else if (MCA.getConfig().villagerCoexistence) {
                if (world.rand.nextDouble() < MCA.getConfig().mcaVillagerSpawnRatio) {
                    EntityVillagerMCA newVillager = new EntityVillagerMCA(world,
                            com.google.common.base.Optional.of(originalVillager.getProfessionForge()),
                            com.google.common.base.Optional.absent());
                    newVillager.setPosition(originalVillager.posX, originalVillager.posY, originalVillager.posZ);
                    newVillager.finalizeMobSpawn(world.getDifficultyForLocation(newVillager.getPos()), null, false);
                    newVillager.forcePositionAsHome();
                    world.spawnEntity(newVillager);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof EntityVillagerMCA && event.getEntityPlayer() != null) {
            EntityPlayer player = event.getEntityPlayer();
            EntityVillagerMCA villager = (EntityVillagerMCA)event.getTarget();

            if (villager.getProfessionForge() == ProfessionsMCA.bandit) {
                event.setResult(Event.Result.DENY);
            } else if (player.getHeldItemMainhand().getItem() == ItemsMCA.VILLAGER_EDITOR) {
                player.openGui(MCA.getInstance(), Constants.GUI_ID_VILLAGEREDITOR, player.world, villager.getEntityId(), 0, 0);
                event.setResult(Event.Result.ALLOW);
            } else {
                player.addStat(StatList.TALKED_TO_VILLAGER);
                player.openGui(MCA.getInstance(), Constants.GUI_ID_INTERACT, player.world, villager.getEntityId(), 0, 0);
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public void onEntityDamaged(LivingDamageEvent event) {
        if (event.getEntity() instanceof EntityVillagerMCA) {
            EntityVillagerMCA villager = (EntityVillagerMCA)event.getEntity();
            Entity source = event.getSource() != null ? event.getSource().getTrueSource() : null;
            
            if (source instanceof EntityLivingBase && villager.getProfessionForge() != ProfessionsMCA.bandit) {
                villager.world.loadedEntityList.stream().filter(e ->
                        e instanceof EntityVillagerMCA &&
                        e.getDistance(villager) <= 10.0D &&
                        ((EntityVillagerMCA)e).getProfessionForge() == ProfessionsMCA.guard)
                .forEach(e -> ((EntityVillagerMCA) e).setAttackTarget((EntityLivingBase)source));
            }
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (stack.getItem() instanceof ItemBaby) {
            event.getPlayer().addItemStackToInventory(stack);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaceEvent(BlockEvent.PlaceEvent event) {
        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();
        Block placedBlock = event.getPlacedBlock().getBlock();

        if (placedBlock == Blocks.FIRE && event.getWorld().getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.EMERALD_BLOCK) {
            int totemsFound = 0;

            // Check on +/- X and Z for at least 3 totems on fire.
            for (int i = 0; i < 4; i++) {
                int dX = 0;
                int dZ = 0;

                if (i == 0 || i == 2) dX = -3;
                else dZ = 3;

                // Scan upwards to ensure it's obsidian, and on fire.
                for (int j = -1; j < 2; j++) {
                    Block block = event.getWorld().getBlockState(new BlockPos(x + dX, y + j, z + dZ)).getBlock();
                    if (block != Blocks.OBSIDIAN && block != Blocks.FIRE) break;

                    // If we made it up to 1 without breaking, make sure the block is fire so that it's a lit totem.
                    if (j == 1 && block == Blocks.FIRE) totemsFound++;
                }
            }

            if (totemsFound >= 3 && !event.getWorld().isDaytime()) {
                reaperSpawnWorld = event.getWorld();
                reaperSpawnPos = new BlockPos(x + 1, y + 10, z + 1);
                reaperSummonTicks = 120; // 6 seconds

                for (int i = 0; i < 2; i++) event.getWorld().setBlockToAir(new BlockPos(x, y - i, z));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // When players respawn check to see if their baby was saved in limbo. Add it back to their inventory.
        if (limbo.containsKey(event.player.getUniqueID())) {
            event.player.inventory.addItemStackToInventory(limbo.get(event.player.getUniqueID()));
            limbo.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // If a player dies while holding a baby, remember it until they respawn.
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            Optional<ItemStack> babyStack = player.inventory.mainInventory.stream().filter(s -> s.getItem() instanceof ItemBaby).findFirst();
            babyStack.ifPresent(s -> limbo.put(player.getUniqueID(), babyStack.get()));
        }
    }

    @SubscribeEvent
    public void onLivingSetTarget(LivingSetAttackTargetEvent event) {
        // Mobs shouldn't attack infected villagers. Account for this when they attempt to set their target.
        if (event.getEntityLiving() instanceof EntityMob && event.getTarget() instanceof EntityVillagerMCA) {
            EntityMob mob = (EntityMob) event.getEntityLiving();
            EntityVillagerMCA target = (EntityVillagerMCA) event.getTarget();

            if (target.get(EntityVillagerMCA.IS_INFECTED)) {
                mob.setAttackTarget(null);
            }
        }
    }

    @SubscribeEvent
    public void onPlaySoundAtEntityEvent(PlaySoundAtEntityEvent event) {
        // Cancel all villager sounds. We unfortunately cannot control on a per entity basis as getEntity() always returns null.
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT) return;
        try {
            event.setCanceled(event.getSound().getSoundName().toString().contains("villager"));
        } catch (NullPointerException e) {
            // throw out potential NPEs due to bad event data. some of these have been reported
        }
    }
}
