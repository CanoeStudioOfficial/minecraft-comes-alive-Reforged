package mca.core.forge;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiStaffOfLife;
import mca.client.gui.GuiWhistle;
import mca.client.network.ClientMessageQueue;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import mca.entity.data.SavedVillagers;
import mca.entity.inventory.InventoryMCA;
import mca.enums.EnumGender;
import mca.enums.EnumSetupType;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.server.ServerMessageHandler;
import mca.structure.McaDestinyManager;
import mca.structure.McaBlueprintManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class NetMCA {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("mca");

    public static void registerMessages() {
        INSTANCE.registerMessage(ButtonActionHandler.class, ButtonAction.class, 0, Side.SERVER);
        INSTANCE.registerMessage(SayHandler.class, Say.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(BabyNameHandler.class, BabyName.class, 2, Side.SERVER);
        INSTANCE.registerMessage(CareerResponseHandler.class, CareerResponse.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(CareerRequestHandler.class, CareerRequest.class, 4, Side.SERVER);
        INSTANCE.registerMessage(InventoryRequestHandler.class, InventoryRequest.class, 5, Side.SERVER);
        INSTANCE.registerMessage(InventoryResponseHandler.class, InventoryResponse.class, 6, Side.CLIENT);
        INSTANCE.registerMessage(SavedVillagersRequestHandler.class, SavedVillagersRequest.class, 7, Side.SERVER);
        INSTANCE.registerMessage(SavedVillagersResponseHandler.class, SavedVillagersResponse.class, 8, Side.CLIENT);
        INSTANCE.registerMessage(ReviveVillagerHandler.class, ReviveVillager.class, 9, Side.SERVER);
        INSTANCE.registerMessage(SetNameHandler.class, SetName.class, 10, Side.SERVER);
        INSTANCE.registerMessage(SpawnParticlesHandler.class, SpawnParticles.class, 11, Side.CLIENT);
        INSTANCE.registerMessage(GetFamilyHandler.class, GetFamily.class, 12, Side.SERVER);
        INSTANCE.registerMessage(GetFamilyResponseHandler.class, GetFamilyResponse.class, 13, Side.CLIENT);
        INSTANCE.registerMessage(CallToPlayerHandler.class, CallToPlayer.class, 14, Side.SERVER);
        INSTANCE.registerMessage(SetTextureHandler.class, SetTexture.class, 15, Side.SERVER);
        INSTANCE.registerMessage(SetProfessionHandler.class, SetProfession.class, 16, Side.SERVER);
        INSTANCE.registerMessage(SetPlayerGenderHandler.class, SetPlayerGender.class, 17, Side.SERVER);
        INSTANCE.registerMessage(SetupCompleteHandler.class, SetupComplete.class, 18, Side.SERVER);
        INSTANCE.registerMessage(BlueprintScanHandler.class, BlueprintScan.class, 19, Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    private static EntityPlayer getPlayerClient() {
        return Minecraft.getMinecraft().player;
    }

    public static class ButtonAction implements IMessage {
        private String guiKey;
        private String buttonId;
        private UUID targetUUID;

        public ButtonAction() {
        }

        public ButtonAction(String guiKey, String buttonId, UUID targetUUID) {
            this.guiKey = guiKey;
            this.buttonId = buttonId;
            this.targetUUID = targetUUID;
        }

        public String getGuiKey() {
            return guiKey;
        }

        public String getButtonId() {
            return buttonId;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(targetUUID != null);
            ByteBufUtils.writeUTF8String(buf, this.guiKey);
            ByteBufUtils.writeUTF8String(buf, this.buttonId);

            if (targetUUID != null) {
                ByteBufUtils.writeUTF8String(buf, this.targetUUID.toString());
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            boolean hasTarget = buf.readBoolean();
            this.guiKey = ByteBufUtils.readUTF8String(buf);
            this.buttonId = ByteBufUtils.readUTF8String(buf);

            if (hasTarget) {
                this.targetUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            }
        }

        public boolean targetsServer() {
            return getTargetUUID() == null;
        }
    }

    public static class ButtonActionHandler implements IMessageHandler<ButtonAction, IMessage> {
        @Override
        public IMessage onMessage(ButtonAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            if (!message.targetsServer()) {
                EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.targetUUID);
                if (villager != null) player.getServerWorld().addScheduledTask(() -> villager.handleButtonClick(player, message.guiKey, message.buttonId));
            } else player.getServerWorld().addScheduledTask(() -> ServerMessageHandler.handleMessage(player, message));
            return null;
        }
    }

    public static class Say implements IMessage {
        private String phraseId;
        private int speakingEntityId;

        public Say() {
        }

        public Say(String phraseId, int speakingEntityId) {
            this.phraseId = phraseId;
            this.speakingEntityId = speakingEntityId;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, this.phraseId);
            buf.writeInt(this.speakingEntityId);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.phraseId = ByteBufUtils.readUTF8String(buf);
            this.speakingEntityId = buf.readInt();
        }
    }

    public static class SayHandler implements IMessageHandler<Say, IMessage> {

        @Override
        public IMessage onMessage(Say message, MessageContext ctx) {
            EntityPlayer player = getPlayerClient();
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getEntityWorld().getEntityByID(message.speakingEntityId);

            if (villager != null) villager.say(com.google.common.base.Optional.of(player), message.phraseId);

            return null;
        }
    }

    public static class BabyName implements IMessage {
        private String babyName;

        public BabyName() {
        }

        public BabyName(String babyName) {
            this.babyName = babyName;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, this.babyName);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.babyName = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class BabyNameHandler implements IMessageHandler<BabyName, IMessage> {

        @Override
        public IMessage onMessage(BabyName message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);

            if (stack.getItem() instanceof ItemBaby) stack.getTagCompound().setString("name", message.babyName);

            return null;
        }
    }

    public static class CareerResponse implements IMessage {
        private int careerId;
        private UUID entityUUID;

        public CareerResponse() {
        }

        public CareerResponse(int careerId, UUID entityUUID) {
            this.careerId = careerId;
            this.entityUUID = entityUUID;
        }

        public int getCareerId() {
            return careerId;
        }

        public UUID getEntityUUID() {
            return entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeVarInt(buf, careerId, 4);
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.careerId = ByteBufUtils.readVarInt(buf, 4);
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class CareerResponseHandler implements IMessageHandler<CareerResponse, IMessage> {

        @Override
        public IMessage onMessage(CareerResponse message, MessageContext ctx) {
            ClientMessageQueue.add(message);
            return null;
        }
    }

    public static class CareerRequest implements IMessage {
        private UUID entityUUID;

        public CareerRequest() {
        }

        public CareerRequest(UUID entityUUID) {
            this.entityUUID = entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class CareerRequestHandler implements IMessageHandler<CareerRequest, IMessage> {

        @Override
        public IMessage onMessage(CareerRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            int careerId = -255;

            try {
                EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.entityUUID);

                if (villager != null) careerId = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager, EntityVillagerMCA.VANILLA_CAREER_ID_FIELD_INDEX);
            } catch (ClassCastException ignored) {
                MCA.getLog().error("UUID provided in career request does not match an MCA villager!: " + message.entityUUID.toString());
                return null;
            } catch (NullPointerException ignored) {
                MCA.getLog().error("UUID provided in career request does not match a loaded MCA villager!: " + message.entityUUID.toString());
                return null;
            }

            if (careerId == -255) {
                MCA.getLog().error("Career ID wasn't assigned for UUID: " + message.entityUUID);
                return null;
            }

            return new CareerResponse(careerId, message.entityUUID);
        }
    }

    public static class InventoryRequest implements IMessage {
        private UUID entityUUID;

        public InventoryRequest() {
        }

        public InventoryRequest(UUID entityUUID) {
            this.entityUUID = entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class InventoryRequestHandler implements IMessageHandler<InventoryRequest, IMessage> {

        @Override
        public IMessage onMessage(InventoryRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.entityUUID);
            if (villager != null && villager.inventory != null) return new InventoryResponse(villager.getUniqueID(), villager.inventory);
            return null;
        }
    }

    public static class InventoryResponse implements IMessage {
        private UUID entityUUID;
        private NBTTagCompound inventoryNBT;

        public InventoryResponse() {
        }

        public InventoryResponse(UUID entityUUID, InventoryMCA inventory) {
            this.inventoryNBT = new NBTTagCompound();
            this.entityUUID = entityUUID;
            this.inventoryNBT.setTag("inventory", inventory.writeInventoryToNBT());
        }

        public UUID getEntityUUID() {
            return entityUUID;
        }

        public NBTTagCompound getInventoryNBT() {
            return inventoryNBT;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
            ByteBufUtils.writeTag(buf, inventoryNBT);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            this.inventoryNBT = ByteBufUtils.readTag(buf);
        }
    }

    public static class InventoryResponseHandler implements IMessageHandler<InventoryResponse, IMessage> {

        @Override
        public IMessage onMessage(InventoryResponse message, MessageContext ctx) {
            ClientMessageQueue.add(message);
            return null;
        }
    }

    public static class SavedVillagersRequest implements IMessage {

        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}
    }

    public static class SavedVillagersRequestHandler implements IMessageHandler<SavedVillagersRequest, IMessage> {

        @Override
        public IMessage onMessage(SavedVillagersRequest message, MessageContext ctx) {
            return new SavedVillagersResponse(ctx.getServerHandler().player);
        }
    }

    public static class SavedVillagersResponse implements IMessage {
        private Map<String, NBTTagCompound> villagers = new HashMap<>();

        public SavedVillagersResponse() {
        }

        public SavedVillagersResponse(EntityPlayer player) {
            villagers = SavedVillagers.get(player.world).getMap();
        }

        public Map<String, NBTTagCompound> getVillagers() {
            return villagers;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(villagers.size());
            villagers.forEach((k,v) -> {
                ByteBufUtils.writeUTF8String(buf, k);
                ByteBufUtils.writeTag(buf, v);
            });
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                String k = ByteBufUtils.readUTF8String(buf);
                NBTTagCompound v = ByteBufUtils.readTag(buf);
                villagers.put(k, v);
            }
        }
    }

    public static class SavedVillagersResponseHandler implements IMessageHandler<SavedVillagersResponse, IMessage> {

        @Override
        public IMessage onMessage(SavedVillagersResponse message, MessageContext ctx) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiStaffOfLife) ((GuiStaffOfLife) screen).setVillagerData(message.villagers);
            return null;
        }
    }

    public static class ReviveVillager implements IMessage {
        private UUID target;

        public ReviveVillager() {
        }

        public ReviveVillager(UUID target) {
            this.target = target;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, target.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            target = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class ReviveVillagerHandler implements IMessageHandler<ReviveVillager, IMessage> {

        @Override
        public IMessage onMessage(ReviveVillager message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            SavedVillagers villagers = SavedVillagers.get(player.world);
            NBTTagCompound nbt = SavedVillagers.get(player.world).loadByUUID(message.target);
            if (nbt != null) {
                EntityVillagerMCA villager = new EntityVillagerMCA(player.world);
                villager.setPosition(player.posX, player.posY, player.posZ);
                player.world.spawnEntity(villager);

                villager.readEntityFromNBT(nbt);
                villager.reset();

                villagers.remove(message.target);
                player.inventory.mainInventory.get(player.inventory.currentItem).damageItem(1, player);
            }

            return null;
        }
    }

    public static class SetName implements IMessage {
        private String name;
        private UUID entityUUID;

        public SetName() {
        }

        public SetName(String name, UUID entityUUID) {
            this.name = name;
            this.entityUUID = entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
            ByteBufUtils.writeUTF8String(buf, name);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            name = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class SetNameHandler implements IMessageHandler<SetName, IMessage> {

        @Override
        public IMessage onMessage(SetName message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.world;
            java.util.Optional<Entity> entity = world.loadedEntityList.stream().filter((e) -> e.getUniqueID().equals(message.entityUUID)).findFirst();
            if (!entity.isPresent()) return null;
            if (entity.get() instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity.get();
                villager.set(EntityVillagerMCA.VILLAGER_NAME, message.name);
            }
            return null;
        }
    }

    public static class SpawnParticles implements IMessage {
        private UUID entityUUID;
        private EnumParticleTypes particleType;

        public SpawnParticles() {
        }

        public SpawnParticles(UUID entityUUID, EnumParticleTypes particleType) {
            this.entityUUID = entityUUID;
            this.particleType = particleType;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
            buf.writeInt(particleType.getParticleID());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            particleType = EnumParticleTypes.getParticleFromId(buf.readInt());
        }
    }

    public static class SpawnParticlesHandler implements IMessageHandler<SpawnParticles, IMessage> {
        @Override
        public IMessage onMessage(SpawnParticles message, MessageContext ctx) {
            World world = getPlayerClient().world;
            java.util.Optional<Entity> entity = world.loadedEntityList.stream().filter((e) -> e.getUniqueID().equals(message.entityUUID)).findFirst();
            if (!entity.isPresent()) return null;
            if (entity.get() instanceof EntityVillagerMCA) {
                EntityVillagerMCA villager = (EntityVillagerMCA) entity.get();
                villager.spawnParticles(message.particleType);
            }
            return null;
        }
    }

    public static class GetFamily implements IMessage {
        @Override
        public void toBytes(ByteBuf buf) {}

        @Override
        public void fromBytes(ByteBuf buf) {}
    }

    public static class GetFamilyHandler implements IMessageHandler<GetFamily, IMessage> {
        @Override
        public IMessage onMessage(GetFamily message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            List<EntityVillagerMCA> villagers = new ArrayList<>();
            List<NBTTagCompound> familyData = new ArrayList<>();

            player.world.loadedEntityList.stream().filter(e -> e instanceof EntityVillagerMCA).forEach(e -> villagers.add((EntityVillagerMCA)e));
            villagers.stream().filter(e -> e.isMarriedTo(player.getUniqueID()) || e.playerIsParent(player)).forEach(e -> {
                NBTTagCompound nbt = new NBTTagCompound();
                e.writeEntityToNBT(nbt);
                familyData.add(nbt);
            });
            return new GetFamilyResponse(familyData);
        }
    }

    public static class GetFamilyResponse implements IMessage {
        private List<NBTTagCompound> familyData;

        public GetFamilyResponse() {
        }

        public GetFamilyResponse(List<NBTTagCompound> familyData) {
            this.familyData = familyData;
        }

        public List<NBTTagCompound> getFamilyData() {
            return familyData;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(familyData.size());
            familyData.stream().forEach(n -> ByteBufUtils.writeTag(buf, n));
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            familyData = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                familyData.add(ByteBufUtils.readTag(buf));
            }
        }
    }

    public static class GetFamilyResponseHandler implements IMessageHandler<GetFamilyResponse, IMessage> {
        @Override
        public IMessage onMessage(GetFamilyResponse message, MessageContext ctx) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiWhistle) {
                GuiWhistle whistleScreen = (GuiWhistle)screen;
                whistleScreen.setVillagerDataList(message.familyData);
            }
            return null;
        }
    }

    public static class CallToPlayer implements IMessage {
        private UUID targetUUID;

        public CallToPlayer() {
        }

        public CallToPlayer(UUID targetUUID) {
            this.targetUUID = targetUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, targetUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            targetUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class CallToPlayerHandler implements IMessageHandler<CallToPlayer, IMessage> {
        @Override
        public IMessage onMessage(CallToPlayer message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            Optional<Entity> entity = player.world.loadedEntityList.stream().filter(e -> e.getUniqueID().equals(message.targetUUID)).findFirst();
            entity.ifPresent(e -> {
                e.setPosition(player.posX, player.posY, player.posZ);
                ((EntityLiving)e).getNavigator().clearPath();
            });
            return null;
        }
    }

    public static class SetProfession implements IMessage {
        private UUID targetUUID;
        private String profession;

        public SetProfession() {
        }

        public SetProfession(UUID targetUUID, String profession) {
            this.targetUUID = targetUUID;
            this.profession = profession;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, targetUUID.toString());
            ByteBufUtils.writeUTF8String(buf, profession);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            targetUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            profession = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class SetProfessionHandler implements IMessageHandler<SetProfession, IMessage> {
        @Override
        public IMessage onMessage(SetProfession message, MessageContext ctx) {
            boolean isCareerSet = false;
            EntityPlayer player = ctx.getServerHandler().player;
            Optional<Entity> entity = player.world.loadedEntityList.stream().filter(e -> e.getUniqueID().equals(message.targetUUID)).findFirst();
            if (entity.isPresent()) {
                for (Map.Entry<ResourceLocation, VillagerRegistry.VillagerProfession> professionEntry : ProfessionsMCA.registry.getEntries()) {
                    List<VillagerRegistry.VillagerCareer> careers = ObfuscationReflectionHelper.getPrivateValue(VillagerRegistry.VillagerProfession.class, professionEntry.getValue(), 3);

                    for (int i = 0; i < careers.size(); i++) {
                        VillagerRegistry.VillagerCareer career = careers.get(i);

                        if (career.getName().equals(message.profession)) {
                            EntityVillagerMCA villager = (EntityVillagerMCA)entity.get();
                            villager.setProfession(professionEntry.getValue());
                            villager.setVanillaCareer(i);
                            villager.refreshSpecialAI();
                            player.sendMessage(new TextComponentTranslation("notify.profession.set", message.profession));
                            isCareerSet = true;
                            break;
                        }
                    }
                }
            } else {
                MCA.getLog().error("Entity not found on career set!: " + message.targetUUID.toString());
                return null;
            }

            if (!isCareerSet) {
                player.sendMessage(new TextComponentTranslation("notify.profession.notfound", message.profession));
            }
            return null;
        }
    }

    public static class SetTexture implements IMessage {
        private UUID targetUUID;
        private String texture;

        public SetTexture() {
        }

        public SetTexture(UUID targetUUID, String texture) {
            this.targetUUID = targetUUID;
            this.texture = texture;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, targetUUID.toString());
            ByteBufUtils.writeUTF8String(buf, texture);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            targetUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            texture = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class SetTextureHandler implements IMessageHandler<SetTexture, IMessage> {
        @Override
        public IMessage onMessage(SetTexture message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            Optional<Entity> entity = player.world.loadedEntityList.stream().filter(e -> e.getUniqueID().equals(message.targetUUID)).findFirst();
            entity.ifPresent(e -> ((EntityVillagerMCA)e).set(EntityVillagerMCA.TEXTURE, message.texture));
            return null;
        }
    }

    public static class SetPlayerGender implements IMessage {
        private EnumGender gender;

        public SetPlayerGender() {
        }

        public SetPlayerGender(EnumGender gender) {
            this.gender = gender;
        }

        public EnumGender getGender() {
            return gender;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(gender.getId());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.gender = EnumGender.byId(buf.readInt());
        }
    }

    public static class SetPlayerGenderHandler implements IMessageHandler<SetPlayerGender, IMessage> {
        @Override
        public IMessage onMessage(SetPlayerGender message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                PlayerSaveData data = PlayerSaveData.get(player);
                data.setGender(message.gender);
            });
            return null;
        }
    }

    public static class SetupComplete implements IMessage {
        private EnumSetupType setupType;
        private String playerName = "";
        private EnumGender genderPreference = EnumGender.UNASSIGNED;

        public SetupComplete() {
        }

        public SetupComplete(EnumSetupType setupType) {
            this.setupType = setupType;
        }

        public SetupComplete(EnumSetupType setupType, String playerName, EnumGender genderPreference) {
            this.setupType = setupType;
            this.playerName = playerName == null ? "" : playerName;
            this.genderPreference = genderPreference == null ? EnumGender.UNASSIGNED : genderPreference;
        }

        public EnumSetupType getSetupType() {
            return setupType;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(setupType.getId());
            ByteBufUtils.writeUTF8String(buf, playerName == null ? "" : playerName);
            buf.writeInt(genderPreference == null ? EnumGender.UNASSIGNED.getId() : genderPreference.getId());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.setupType = EnumSetupType.byId(buf.readInt());
            this.playerName = ByteBufUtils.readUTF8String(buf);
            this.genderPreference = EnumGender.byId(buf.readInt());
        }
    }

    public static class SetupCompleteHandler implements IMessageHandler<SetupComplete, IMessage> {
        @Override
        public IMessage onMessage(SetupComplete message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                PlayerSaveData data = PlayerSaveData.get(player);
                McaDestinyManager.choose(player, message.setupType, message.playerName, message.genderPreference);
            });
            return null;
        }
    }

    public static class BlueprintScan implements IMessage {
        @Override
        public void toBytes(ByteBuf buf) {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
        }
    }

    public static class BlueprintScanHandler implements IMessageHandler<BlueprintScan, IMessage> {
        @Override
        public IMessage onMessage(BlueprintScan message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> McaBlueprintManager.scan(player));
            return null;
        }
    }
}
