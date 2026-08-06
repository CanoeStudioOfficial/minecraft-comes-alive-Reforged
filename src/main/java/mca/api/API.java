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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    private static final String DEFAULT_MALE_SKIN = "mca:skins/generated/male/minecraft_farmer/minecraft_farmer_00.png";
    private static final String DEFAULT_FEMALE_SKIN = "mca:skins/generated/female/minecraft_farmer/minecraft_farmer_00.png";
    private static final String[] SKIN_MANIFESTS = {
            "api/skins.json"
    };

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
        for (String manifest : SKIN_MANIFESTS) {
            loadSkinGroups(manifest);
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
     *
     * @param villager The villager who will be assigned the random skin.
     * @return String location of the random skin
     */
    public static String getRandomSkin(EntityVillagerMCA villager) {
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
        String professionName = profession == null || profession.getRegistryName() == null ? null : profession.getRegistryName().toString();

        List<String> professionSkins = collectSkinPaths(gender, professionName);
        if (!professionSkins.isEmpty()) {
            return professionSkins.get(rng.nextInt(professionSkins.size()));
        }

        List<String> genderSkins = collectSkinPaths(gender, null);
        if (!genderSkins.isEmpty()) {
            return genderSkins.get(rng.nextInt(genderSkins.size()));
        }

        MCA.getLog().error("No MCA villager skins are available for gender " + gender + " and profession " + professionName);
        return getFallbackSkin(gender);
    }

    /**
     * Returns a valid high-version fallback texture for the requested gender.
     */
    public static String getFallbackSkin(EnumGender gender) {
        return gender == EnumGender.FEMALE ? DEFAULT_FEMALE_SKIN : DEFAULT_MALE_SKIN;
    }

    /**
     * Identifies empty or pre-high-version MCA skin paths that need migration.
     */
    public static boolean isLegacySkinPath(String texture) {
        if (texture == null || texture.trim().isEmpty()) {
            return true;
        }

        return texture.startsWith("mca:skins/")
                && !texture.startsWith("mca:skins/generated/")
                && !texture.startsWith("mca:skins/layers/");
    }

    private static void loadSkinGroups(String manifest) {
        try {
            SkinsGroup[] skins = Util.readResourceAsJSON(manifest, SkinsGroup[].class);
            if (skins != null) {
                Collections.addAll(skinGroups, skins);
            }
        } catch (RuntimeException e) {
            MCA.getLog().warn("Could not load MCA skin manifest: " + manifest, e);
        }
    }

    private static List<String> collectSkinPaths(EnumGender gender, @Nullable String professionName) {
        List<String> paths = new ArrayList<>();
        for (SkinsGroup group : skinGroups) {
            if (group.getGender() != gender) {
                continue;
            }
            if (professionName != null && !professionName.equals(group.getProfession())) {
                continue;
            }

            String[] groupPaths = group.getPaths();
            if (groupPaths == null || groupPaths.length == 0) {
                continue;
            }
            Collections.addAll(paths, groupPaths);
        }
        return paths;
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
        return giftMap.containsKey(name) ? giftMap.get(name).getValue() : -5;
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
                MCA.getLog().error("maleNames is empty; unable to generate a random name.");
                return "Steve";
            }
            return maleNames.get(rng.nextInt(maleNames.size()));
        } else if (gender == EnumGender.FEMALE) {
            if (femaleNames.isEmpty()) {
                MCA.getLog().error("femaleNames is empty; unable to generate a random name.");
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
