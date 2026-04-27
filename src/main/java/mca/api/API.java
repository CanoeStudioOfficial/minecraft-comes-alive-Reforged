package mca.api;

import com.google.common.base.Charsets;
import mca.api.types.APIButton;
import mca.api.types.Gift;
import mca.api.types.SkinsGroup;
import mca.client.gui.component.GuiButtonEx;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumConstraint;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;

/**
 * Class API handles interaction with MCA's configurable options via JSON in the resources folder
 */
public class API {
    private static Map<String, Gift> giftMap = new HashMap<>();
    private static Map<String, APIButton[]> buttonMap = new HashMap<>();
    private static List<String> maleNames = new ArrayList<>();
    private static List<String> femaleNames = new ArrayList<>();
    private static List<SkinsGroup> skinGroups = new ArrayList<>();
    private static Random rng = new Random();

    /**
     * Performs initialization of the API
     */
    public static void init() {
        rng = new Random();

        // Load skins
        skinGroups.clear();
        SkinsGroup[] skins = Util.readResourceAsJSON("api/skins.json", SkinsGroup[].class);
        if (skins != null) {
            Collections.addAll(skinGroups, skins);
        }

        // Load names
        maleNames.clear();
        femaleNames.clear();
        String langCode = "en_us";
        try {
            if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient()) {
                langCode = net.minecraft.client.Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
            }
        } catch (Throwable t) {
            langCode = "en_us";
        }

        String langFilePath = String.format("/assets/mca/lang/%s.lang", langCode);
        InputStream namesStream = MCA.class.getResourceAsStream(langFilePath);
        
        if (namesStream == null && !langCode.equals("en_us")) {
            langFilePath = "/assets/mca/lang/en_us.lang";
            namesStream = MCA.class.getResourceAsStream(langFilePath);
        }

        if (namesStream != null) {
            try {
                // read in all names and process into the correct list
                List<String> lines = IOUtils.readLines(namesStream, Charsets.UTF_8);
                for (String line : lines) {
                    if (line.contains("name.male")) {
                        maleNames.add(line.split("=")[1].trim());
                    } else if (line.contains("name.female")) {
                        femaleNames.add(line.split("=")[1].trim());
                    }
                }
            } catch (Exception e) {
                MCA.getLog().fatal("Failed to load all NPC names from file: " + langFilePath, e);
            } finally {
                IOUtils.closeQuietly(namesStream);
            }
        } else {
            MCA.getLog().error("Could not find names lang file: " + langFilePath);
        }

        // Read in buttons
        buttonMap.put("main", Util.readResourceAsJSON("api/gui/main.json", APIButton[].class));
        buttonMap.put("interact", Util.readResourceAsJSON("api/gui/interact.json", APIButton[].class));
        buttonMap.put("debug", Util.readResourceAsJSON("api/gui/debug.json", APIButton[].class));
        buttonMap.put("editor", Util.readResourceAsJSON("api/gui/editor.json", APIButton[].class));
        buttonMap.put("work", Util.readResourceAsJSON("api/gui/work.json", APIButton[].class));
        buttonMap.put("location", Util.readResourceAsJSON("api/gui/location.json", APIButton[].class));

        // Load gifts and assign to the appropriate map with a key value pair and print warnings on potential issues
        Gift[] gifts = Util.readResourceAsJSON("api/gifts.json", Gift[].class);
        if (gifts != null) {
            for (Gift gift : gifts) {
                if (!gift.exists()) {
                    MCA.getLog().warn("Could not find gift item or block in registry: " + gift.getName());
                } else {
                    giftMap.put(gift.getName(), gift);
                }
            }
        }
    }

    /**
     * Returns a random skin based on the profession and gender provided.
     * 根据黑色素值选择合适的肤色皮肤。
     *
     * @param villager The villager who will be assigned the random skin.
     * @return String location of the random skin
     */
    public static String getRandomSkin(EntityVillagerMCA villager) {
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
        String name = villager.get(EntityVillagerMCA.VILLAGER_NAME);

        //Special-case skins
        if (gender == EnumGender.MALE) {
            switch (name.toLowerCase()) {
                case "pewdiepie": return "mca:skins/male/special/pewdiepie_boy.png";
                case "sven": return "mca:skins/male/special/sven.png";
                case "noob":
                case "noober":
                case "neeber": return "mca:skins/male/special/noob.png";
                case "shepard": return "mca:skins/male/special/shepard.png";
                case "minsc": return "mca:skins/male/special/minsc.png";
            }
        } else if (gender == EnumGender.FEMALE) {
            switch (name.toLowerCase()) {
                case "pewdiepie": return "mca:skins/female/special/pewdiepie_girl.png";
            }
        }

        // 获取黑色素值 (0.0 - 1.0)，用于选择合适的肤色
        mca.entity.data.Genetics genetics = new mca.entity.data.Genetics(villager);
        float melanin = genetics.getGene(mca.entity.data.Genetics.MELANIN);

        //Default skin behavior
        Optional<SkinsGroup> group = skinGroups.stream()
                .filter(g -> g.getGender() == gender && profession.getRegistryName() != null && g.getProfession().equals(profession.getRegistryName().toString()))
                .findFirst();

        // group 存在且 paths 不为空
        if (group.isPresent()) {
            String[] paths = group.get().getPaths();
            if (paths == null || paths.length == 0) {
                MCA.getLog().error("SkinsGroup paths 为空，无法分配皮肤！");
                return "mca:skins/default.png";
            }
            // 根据黑色素值选择合适的皮肤
            return selectSkinByMelanin(paths, melanin);
        }

        // skinGroups 为空
        if (skinGroups.isEmpty()) {
            MCA.getLog().error("skinGroups 为空，无法分配皮肤！");
            return "mca:skins/default.png";
        }

        // 随机选取同性别的 SkinsGroup
        List<SkinsGroup> genderGroups = new ArrayList<>();
        for (SkinsGroup g : skinGroups) {
            if (g.getGender() == gender) {
                genderGroups.add(g);
            }
        }
        if (genderGroups.isEmpty()) {
            MCA.getLog().error("没有找到对应性别的 SkinsGroup，无法分配皮肤！");
            return "mca:skins/default.png";
        }
        SkinsGroup randomGroup = genderGroups.get(rng.nextInt(genderGroups.size()));
        String[] paths = randomGroup.getPaths();
        if (paths == null || paths.length == 0) {
            MCA.getLog().error("随机选中的 SkinsGroup paths 为空，无法分配皮肤！");
            return "mca:skins/default.png";
        }
        // 根据黑色素值选择合适的皮肤
        return selectSkinByMelanin(paths, melanin);
    }

    /**
     * 根据黑色素值选择合适的皮肤
     * 皮肤编号 0-4 对应不同肤色深度：0=最浅, 4=最深
     *
     * @param paths 可用皮肤路径数组
     * @param melanin 黑色素值 (0.0 - 1.0)
     * @return 选中的皮肤路径
     */
    private static String selectSkinByMelanin(String[] paths, float melanin) {
        if (paths == null || paths.length == 0) {
            return "mca:skins/default.png";
        }

        // 如果只有一张皮肤，直接返回
        if (paths.length == 1) {
            return paths[0];
        }

        // 根据黑色素值计算皮肤索引
        // melanin 0.0-0.2 -> 索引 0 (最浅)
        // melanin 0.2-0.4 -> 索引 1
        // melanin 0.4-0.6 -> 索引 2
        // melanin 0.6-0.8 -> 索引 3
        // melanin 0.8-1.0 -> 索引 4 (最深)
        int skinIndex = Math.min(4, (int) (melanin * 5));

        // 尝试找到对应编号的皮肤
        for (String path : paths) {
            // 提取文件名中的数字 (如 "0.png" 中的 0)
            if (path.matches(".*[/\\\\]" + skinIndex + "\\.png$")) {
                return path;
            }
        }

        // 如果没有找到对应编号的皮肤，返回随机皮肤
        return paths[rng.nextInt(paths.length)];
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    @Nullable
    public static APIButton getButtonById(String key, String id) {
        APIButton[] buttons = buttonMap.get(key);
        if (buttons == null) return null;

        for (APIButton b : buttons) {
            if (b.getIdentifier().equals(id)) return b;
        }
        return null;
    }

    /**
     * Returns the value of a gift from an ItemStack
     *
     * @param stack ItemStack containing the gift item
     * @return int value determining the gift value of a stack
     */
    public static int getGiftValueFromStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) return 0;

        String name = stack.getItem().getRegistryName().toString();
        // 如果物品在礼物列表中，返回对应价值；否则返回0（不增加也不减少好感度）
        return giftMap.containsKey(name) ? giftMap.get(name).getValue() : 0;
    }

    /**
     * Returns the proper response type based on a gift provided
     *
     * @param stack ItemStack containing the gift item
     * @return String value of the appropriate response type
     */
    public static String getResponseForGift(ItemStack stack) {
        int value = getGiftValueFromStack(stack);
        return "gift." + (value <= 0 ? "fail" : value <= 5 ? "good" : value <= 10 ? "better" : "best");
    }

    /**
     * Gets a random name based on the gender provided.
     *
     * @param gender The gender the name should be appropriate for.
     * @return A gender appropriate name based on the provided gender.
     */
    public static String getRandomName(@Nonnull EnumGender gender) {
        if (gender == EnumGender.MALE) {
            if (maleNames.isEmpty()) {
                MCA.getLog().error("maleNames 列表为空，无法生成随机名字！");
                return "Steve";
            }
            return maleNames.get(rng.nextInt(maleNames.size()));
        } else if (gender == EnumGender.FEMALE) {
            if (femaleNames.isEmpty()) {
                MCA.getLog().error("femaleNames 列表为空，无法生成随机名字！");
                return "Alex";
            }
            return femaleNames.get(rng.nextInt(femaleNames.size()));
        }
        return "";
    }

    /**
     * Adds API buttons to the GUI screen provided.
     *
     * @param guiKey   String key for the GUI's buttons
     * @param villager Optional EntityVillagerMCA the GuiScreen has been opened on
     * @param player   EntityPlayer who has opened the GUI
     * @param screen   GuiScreen instance the buttons should be added to
     */
    @SideOnly(Side.CLIENT)
    public static void addButtons(String guiKey, @Nullable EntityVillagerMCA villager, EntityPlayer player, GuiScreen screen) {
        List<GuiButton> buttonList = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, screen, Constants.GUI_SCREEN_BUTTON_LIST_FIELD_INDEX);
        for (APIButton b : buttonMap.get(guiKey)) {
            GuiButtonEx guiButton = new GuiButtonEx(screen, b);
            buttonList.add(guiButton);

            // Ensure that if a constraint is attached to the button
            if (villager == null && b.getConstraints().size() > 0) {
                MCA.getLog().error("No villager provided for list of buttons with constraints! Button ID:" + b.getIdentifier());
                continue;
            }

            // Remove the button if we specify it should not be present on constraint failure
            // Otherwise we just mark the button as disabled.
            boolean isValid = b.isValidForConstraint(villager, player);
            if (!isValid && b.getConstraints().contains(EnumConstraint.HIDE_ON_FAIL)) buttonList.remove(guiButton);
            else if (!isValid) guiButton.enabled = false;
        }
    }

    /**
     * Returns an instance of the button linked to the given ID on the provided GuiScreen
     *
     * @param id     String id of the button desired
     * @param screen GuiScreen containing the button
     * @return GuiButtonEx matching the provided id
     */
    @SideOnly(Side.CLIENT)
    public static Optional<GuiButtonEx> getButton(String id, GuiScreen screen) {
        List<GuiButton> buttonList = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, screen, Constants.GUI_SCREEN_BUTTON_LIST_FIELD_INDEX);
        Optional<GuiButton> button = buttonList.stream().filter(
                (b) -> b instanceof GuiButtonEx && ((GuiButtonEx) b).getApiButton().getIdentifier().equals(id)).findFirst();

        return button.map(guiButton -> (GuiButtonEx) guiButton);
    }
}
