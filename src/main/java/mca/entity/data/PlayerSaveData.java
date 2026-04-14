package mca.entity.data;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerSaveData extends WorldSavedData {
    private static final String PREFIX = "MCA-Player-V1-";

    private UUID spouseUUID = Constants.ZERO_UUID;
    private EnumMarriageState marriageState = EnumMarriageState.NOT_MARRIED;
    private String spouseName = "";
    private boolean babyPresent = false;
    private EnumGender gender = EnumGender.MALE;
    private boolean hasChosenDestiny = false;
    private NBTTagCompound genetics = new NBTTagCompound();
    private NBTTagCompound traits = new NBTTagCompound();

    // 信件/邮件系统
    private final List<Letter> inbox = new LinkedList<>();
    private boolean mailNotificationSent = false;

    public UUID getSpouseUUID() {
        return spouseUUID;
    }

    public EnumMarriageState getMarriageState() {
        return marriageState;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public boolean isBabyPresent() {
        return babyPresent;
    }

    public EnumGender getGender() {
        return gender;
    }

    public boolean isHasChosenDestiny() {
        return hasChosenDestiny;
    }

    public NBTTagCompound getGenetics() {
        return genetics;
    }

    public void setGenetics(NBTTagCompound genetics) {
        this.genetics = genetics;
        markDirty();
    }

    public NBTTagCompound getTraits() {
        return traits;
    }

    public void setTraits(NBTTagCompound traits) {
        this.traits = traits;
        markDirty();
    }

    public PlayerSaveData(String id) {
        super(id);
    }

    public static PlayerSaveData get(EntityPlayer player) {
        String dataId = PREFIX + player.getUniqueID().toString();
        PlayerSaveData data = (PlayerSaveData) player.world.loadData(PlayerSaveData.class, dataId);

        if (data == null) {
            data = new PlayerSaveData(dataId);
            player.world.setData(dataId, data);
        }

        return data;
    }

    public static PlayerSaveData getExisting(World world, UUID uuid) {
        return (PlayerSaveData) world.loadData(PlayerSaveData.class, PREFIX + uuid.toString());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setUniqueId("spouseUUID", spouseUUID);
        nbt.setInteger("marriageState", marriageState.getId());
        nbt.setString("spouseName", spouseName);
        nbt.setBoolean("babyPresent", babyPresent);
        nbt.setInteger("gender", gender.getId());
        nbt.setBoolean("hasChosenDestiny", hasChosenDestiny);
        nbt.setTag("genetics", genetics);
        nbt.setTag("traits", traits);

        // 保存信件数据
        NBTTagList inboxList = new NBTTagList();
        for (Letter letter : inbox) {
            inboxList.appendTag(letter.toNBT());
        }
        nbt.setTag("inbox", inboxList);
        nbt.setBoolean("mailNotificationSent", mailNotificationSent);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        spouseUUID = nbt.getUniqueId("spouseUUID");
        marriageState = EnumMarriageState.byId(nbt.getInteger("marriageState"));
        spouseName = nbt.getString("spouseName");
        babyPresent = nbt.getBoolean("babyPresent");
        gender = EnumGender.byId(nbt.getInteger("gender"));
        hasChosenDestiny = nbt.getBoolean("hasChosenDestiny");
        genetics = nbt.getCompoundTag("genetics");
        traits = nbt.getCompoundTag("traits");

        // 读取信件数据
        inbox.clear();
        if (nbt.hasKey("inbox")) {
            NBTTagList inboxList = nbt.getTagList("inbox", 10);
            for (int i = 0; i < inboxList.tagCount(); i++) {
                inbox.add(new Letter(inboxList.getCompoundTagAt(i)));
            }
        }
        mailNotificationSent = nbt.getBoolean("mailNotificationSent");
    }

    public void setGender(EnumGender gender) {
        this.gender = gender;
        markDirty();
    }

    public void setHasChosenDestiny(boolean value) {
        this.hasChosenDestiny = value;
        markDirty();
    }

    public boolean isMarriedOrEngaged() {
        return marriageState != EnumMarriageState.NOT_MARRIED;
    }

    public void marry(UUID uuid, String name) {
        spouseUUID = uuid;
        marriageState = EnumMarriageState.MARRIED;
        spouseName = name;
        markDirty();
    }

    public void engage(UUID uuid, String name) {
        spouseUUID = uuid;
        marriageState = EnumMarriageState.ENGAGED;
        spouseName = name;
        markDirty();
    }

    public void promise(UUID uuid, String name) {
        spouseUUID = uuid;
        marriageState = EnumMarriageState.PROMISED;
        spouseName = name;
        markDirty();
    }

    public void endMarriage() {
        spouseUUID = Constants.ZERO_UUID;
        spouseName = "";
        marriageState = EnumMarriageState.NOT_MARRIED;
        markDirty();
    }

    public void setBabyPresent(boolean value) {
        this.babyPresent = value;
        markDirty();
    }

    public void reset() {
        endMarriage();
        setBabyPresent(false);
        markDirty();
    }

    public FamilyTreeNode updateFamilyTreeNode(EntityPlayer player) {
        if (player.world.isRemote) return null;

        FamilyTree tree = FamilyTree.get(player.world);
        FamilyTreeNode node = tree.getOrCreateNode(player.getUniqueID(), player.getName(), true, gender);

        node.setName(player.getName());
        node.setGender(gender);
        node.setProfession("player");
        node.setMarriageState(marriageState);
        node.setSpouse(spouseUUID);
        node.setDeceased(!player.isEntityAlive());

        tree.markDirty();
        return node;
    }

    public List<Field> getDataFields() {
        return Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> !Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
    }

    public void dump(EntityPlayer player) {
        for (Field f : getDataFields()) {
            try {
                player.sendMessage(new TextComponentString(f.getName() + " = " + f.get(this).toString()));
            } catch (Exception e) {
                MCA.getLog().error("Error dumping player data!");
                MCA.getLog().error(e);
            }
        }
    }

    // ==================== 信件/邮件系统 ====================

    /**
     * 发送邮件（信件）到玩家的收件箱
     * @param letter 信件对象
     */
    public void sendMail(Letter letter) {
        if (MCA.getConfig().enableVillagerMailingPlayers) {
            inbox.add(letter);
            mailNotificationSent = false;
            markDirty();
        }
    }

    /**
     * 检查玩家是否有邮件
     * @return 是否有邮件
     */
    public boolean hasMail() {
        return !inbox.isEmpty();
    }

    /**
     * 获取一封邮件（并从收件箱中移除）
     * @return 信件物品堆，如果没有邮件则返回null
     */
    public ItemStack getMail() {
        if (hasMail()) {
            Letter letter = inbox.remove(0);
            markDirty();
            return letter.toItemStack();
        }
        return null;
    }

    /**
     * 获取所有邮件（不清除收件箱）
     * @return 信件列表
     */
    public List<Letter> getAllMail() {
        return new ArrayList<>(inbox);
    }

    /**
     * 发送一封信件（使用字符串内容）
     * @param lines 信件内容行
     */
    public void sendLetter(String... lines) {
        List<ITextComponent> pages = new ArrayList<>();
        for (String line : lines) {
            pages.add(new TextComponentString(line));
        }
        sendMail(new Letter("", pages));

        // 通知在线玩家
        World world = getWorld();
        if (world != null) {
            EntityPlayer player = world.getPlayerEntityByUUID(getUUIDFromDataId());
            if (player instanceof EntityPlayerMP) {
                showMailNotification((EntityPlayerMP) player);
            }
        }
    }

    /**
     * 使用本地化键发送信件
     * @param translationKey 本地化键
     * @param params 参数
     */
    public void sendLetterTranslated(String translationKey, Object... params) {
        sendLetter(new TextComponentTranslation(translationKey, params).getFormattedText());
    }

    /**
     * 发送慰问信（当村民死亡时）
     * @param villagerName 死亡的村民名字
     * @param villageName 村庄名字
     */
    public void sendLetterOfCondolence(String villagerName, String villageName) {
        sendLetterTranslated("mca.letter.condolence", getFamilyEntry().getName(), villagerName, villageName);
    }

    /**
     * 显示邮件通知
     * @param player 玩家
     */
    public static void showMailNotification(EntityPlayerMP player) {
        player.sendMessage(new TextComponentTranslation("mca.notification.mail.title").appendText("\n").appendSibling(new TextComponentTranslation("mca.notification.mail.description")));
    }

    /**
     * 检查并显示邮件通知（玩家登录时调用）
     * @param player 玩家
     */
    public void checkAndShowMailNotification(EntityPlayerMP player) {
        if (hasMail() && !mailNotificationSent) {
            showMailNotification(player);
            mailNotificationSent = true;
            markDirty();
        }
    }

    private UUID getUUIDFromDataId() {
        // 从数据ID中提取UUID
        String id = this.mapName;
        if (id.startsWith(PREFIX)) {
            return UUID.fromString(id.substring(PREFIX.length()));
        }
        return Constants.ZERO_UUID;
    }

    private World getWorld() {
        // 尝试从已加载的世界获取
        for (net.minecraft.world.WorldServer world : net.minecraftforge.common.DimensionManager.getWorlds()) {
            PlayerSaveData data = getExisting(world, getUUIDFromDataId());
            if (data == this) {
                return world;
            }
        }
        return null;
    }

    private FamilyTreeNode getFamilyEntry() {
        World world = getWorld();
        if (world == null) return null;
        UUID uuid = getUUIDFromDataId();
        FamilyTree tree = FamilyTree.get(world);
        return tree.getOrCreateNode(uuid, "Unknown", true, gender);
    }

    /**
     * 信件数据类
     */
    public static class Letter {
        private final String title;
        private final List<ITextComponent> pages;

        public Letter(String title, List<ITextComponent> pages) {
            this.title = title;
            this.pages = pages;
        }

        public Letter(NBTTagCompound nbt) {
            this.title = nbt.getString("title");
            this.pages = new ArrayList<>();

            NBTTagList pagesList = nbt.getTagList("pages", 8);
            for (int i = 0; i < pagesList.tagCount(); i++) {
                String json = pagesList.getStringTagAt(i);
                try {
                    ITextComponent component = ITextComponent.Serializer.jsonToComponent(json);
                    if (component != null) {
                        pages.add(component);
                    }
                } catch (Exception e) {
                    // 如果解析失败，直接使用字符串
                    pages.add(new TextComponentString(json));
                }
            }
        }

        public NBTTagCompound toNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("title", title);

            NBTTagList pagesList = new NBTTagList();
            for (ITextComponent page : pages) {
                pagesList.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(page)));
            }
            nbt.setTag("pages", pagesList);

            return nbt;
        }

        public ItemStack toItemStack() {
            return mca.items.ItemLetter.createLetterFromComponents(mca.core.minecraft.ItemsMCA.LETTER, title, pages);
        }

        public String getTitle() {
            return title;
        }

        public List<ITextComponent> getPages() {
            return pages;
        }
    }
}
