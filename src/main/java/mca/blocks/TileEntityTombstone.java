package mca.blocks;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class TileEntityTombstone extends TileEntity implements ITickable {
    private Optional<EntityData> entityData = Optional.empty();
    private int resurrectionProgress = 0;
    private boolean cure = false;

    public TileEntityTombstone() {
    }

    public boolean hasEntity() {
        return entityData.isPresent();
    }

    public Optional<String> getEntityName() {
        return entityData.map(e -> e.name);
    }

    public EnumGender getGender() {
        return entityData.map(e -> e.gender).orElse(EnumGender.MALE);
    }

    public boolean isResurrecting() {
        return resurrectionProgress > 0;
    }

    public void startResurrecting(boolean cure) {
        this.resurrectionProgress = 1;
        this.cure = cure;
        markDirty();
        sync();
    }

    public void setEntity(@Nullable Entity entity) {
        if (entity != null) {
            entityData = Optional.of(new EntityData(
                    writeEntityToNbt(entity),
                    entity.getName(),
                    getEntityGender(entity)
            ));
        } else {
            entityData = Optional.empty();
        }
        markDirty();
        sync();
    }

    private EnumGender getEntityGender(Entity entity) {
        return EnumGender.getRandom();
    }

    private NBTTagCompound writeEntityToNbt(Entity entity) {
        NBTTagCompound nbt = new NBTTagCompound();
        entity.writeToNBT(nbt);
        nbt.setString("id", EntityList.getKey(entity).toString());
        return nbt;
    }

    public Optional<Entity> createEntity(World world, boolean remove) {
        try {
            return entityData.flatMap(data -> {
                Entity entity = EntityList.createEntityFromNBT(data.nbt, world);
                return Optional.ofNullable(entity);
            });
        } finally {
            if (remove) {
                setEntity(null);
            }
        }
    }

    @Override
    public void update() {
        if (hasEntity() && resurrectionProgress > 0) {
            resurrectionProgress++;
            markDirty();
            sync();

            if (!world.isRemote) {
                // 每30tick播放音效和产生粒子效果
                if (resurrectionProgress % 30 == 0) {
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            cure ? SoundEvents.BLOCK_BELL_USE : SoundEvents.ENTITY_POLAR_BEAR_AMBIENT,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    // 产生破坏方块粒子效果
                    world.playEvent(2001, pos, net.minecraft.block.Block.getIdFromBlock(world.getBlockState(pos).getBlock()));
                }

                // 随机产生闪电
                if (world.rand.nextInt(10) > 5 && resurrectionProgress % 20 == 0) {
                    generateLightning();
                }
            } else {
                // 客户端粒子效果
                for (int i = 0; i < world.rand.nextInt(8) + 1; ++i) {
                    world.spawnParticle(world.rand.nextBoolean() ? net.minecraft.util.EnumParticleTypes.LARGE_SMOKE : net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                            pos.getX() + world.rand.nextFloat(),
                            pos.getY() + world.rand.nextFloat(),
                            pos.getZ() + world.rand.nextFloat(),
                            (world.rand.nextFloat() - 0.5) / 10F,
                            0,
                            (world.rand.nextFloat() - 0.5) / 10F
                    );
                }
            }

            // 复活完成
            if (resurrectionProgress > 500) {
                resurrectionProgress = 0;

                if (!world.isRemote) {
                    createEntity(world, true).ifPresent(entity -> {
                        generateLightning();
                        entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        entity.clearActivePotions();
                        entity.extinguish();
                        entity.fallDistance = 0.0f;

                        if (entity instanceof EntityLivingBase) {
                            EntityLivingBase living = (EntityLivingBase) entity;
                            living.setHealth(living.getMaxHealth());
                            living.deathTime = 0;
                        }

                        // 治愈僵尸村民
                        if (cure && entity instanceof EntityZombieVillager) {
                            EntityZombieVillager zombie = (EntityZombieVillager) entity;
                            // 转换为普通村民
                            EntityVillager villager = new EntityVillager(world);
                            villager.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            villager.setHealth(villager.getMaxHealth());
                            world.spawnEntity(villager);
                        } else if (cure && entity instanceof EntityZombieVillagerMCA) {
                            // 如果是MCA僵尸村民，转换为MCA村民
                            EntityZombieVillagerMCA zombieMCA = (EntityZombieVillagerMCA) entity;
                            EntityVillagerMCA villagerMCA = new EntityVillagerMCA(world);
                            villagerMCA.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            villagerMCA.setHealth(villagerMCA.getMaxHealth());
                            // 复制基因数据
                            world.spawnEntity(villagerMCA);
                        } else {
                            world.spawnEntity(entity);
                        }
                    });
                }
            }
        }
    }

    private void generateLightning() {
        if (!world.isRemote) {
            world.setSkyFlashTime(10);
            EntityLightningBolt bolt = new EntityLightningBolt(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, true);
            world.addWeatherEffect(bolt);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("EntityData")) {
            entityData = Optional.of(new EntityData(compound.getCompoundTag("EntityData")));
        } else {
            entityData = Optional.empty();
        }
        resurrectionProgress = compound.getInteger("ResurrectionProgress");
        cure = compound.getBoolean("Cure");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        entityData.ifPresent(data -> {
            NBTTagCompound dataTag = new NBTTagCompound();
            data.writeNbt(dataTag);
            compound.setTag("EntityData", dataTag);
        });
        compound.setInteger("ResurrectionProgress", resurrectionProgress);
        compound.setBoolean("Cure", cure);
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public void readFromStack(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("EntityData")) {
            entityData = Optional.of(new EntityData(stack.getTagCompound().getCompoundTag("EntityData")));
            markDirty();
        }
    }

    private void sync() {
        markDirty();
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    static final class EntityData {
        private final NBTTagCompound nbt;
        private final String name;
        private final EnumGender gender;

        public EntityData(NBTTagCompound nbt, String name, EnumGender gender) {
            this.nbt = nbt;
            this.name = name;
            this.gender = gender;
        }

        EntityData(NBTTagCompound nbt) {
            this(
                    nbt.getCompoundTag("EntityNBT"),
                    nbt.getString("EntityName"),
                    EnumGender.byId(nbt.getInteger("EntityGender"))
            );
        }

        void writeNbt(NBTTagCompound nbt) {
            nbt.setTag("EntityNBT", this.nbt);
            nbt.setString("EntityName", name);
            nbt.setInteger("EntityGender", gender.getId());
        }
    }
}
