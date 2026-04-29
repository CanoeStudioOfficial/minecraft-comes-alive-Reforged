package mca.core.minecraft;

import mca.core.MCA;
import mca.items.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class ItemsMCA {
    public static final ItemSpawnEgg EGG_MALE = new ItemSpawnEgg(true);
    public static final ItemSpawnEgg EGG_FEMALE = new ItemSpawnEgg(false);
    public static final ItemZombieVillagerSpawnEgg ZOMBIE_EGG_MALE = new ItemZombieVillagerSpawnEgg(true);
    public static final ItemZombieVillagerSpawnEgg ZOMBIE_EGG_FEMALE = new ItemZombieVillagerSpawnEgg(false);
    public static final Item WEDDING_RING = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item WEDDING_RING_RG = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING_RG = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item BOUQUET = new ItemBouquet().setMaxStackSize(1);
    public static final Item MATCHMAKERS_RING = new ItemMatchmakersRing().setMaxStackSize(2);
    public static final Item BABY_BOY = new ItemBaby(true);
    public static final Item BABY_GIRL = new ItemBaby(false);
    public static final Item ROSE_GOLD_INGOT = new Item().setTranslationKey("rose_gold_ingot");
    public static final Item ROSE_GOLD_DUST = new Item().setTranslationKey("rose_gold_dust");
    public static final Item VILLAGER_EDITOR = new ItemVillagerEditor();
    public static final Item STAFF_OF_LIFE = new ItemStaffOfLife();
    public static final Item WHISTLE = new ItemWhistle();
    public static final Item CRYSTAL_BALL = new ItemCrystalBall();
    public static final Item FAMILY_TREE = new ItemFamilyTree();
    public static final Item SCYTHE = new ItemScythe();

    private static final List<Item> ITEMS = new ArrayList<>();

    public static void register(RegistryEvent.Register<Item> event) {
        for (Field f : ItemsMCA.class.getFields()) {
            try {
                Object instance = f.get(null);
                if (instance instanceof Item) {
                    Item item = (Item) instance;
                    setItemName(item, f.getName().toLowerCase());
                    event.getRegistry().register(item);
                    ITEMS.add(item);
                }
            } catch (Exception e) {
                MCA.getLog().error("Error while registering items: ", e);
            }
        }
    }

    public static void assignCreativeTabs() {
        for (Item i : ITEMS) {
            i.setCreativeTab(MCA.creativeTab);
        }
    }



    @SideOnly(Side.CLIENT)
    public static void registerModelMeshers() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        for (Item item : ITEMS) mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void setItemName(Item item, String itemName) {
        item.setTranslationKey(itemName);
        item.setRegistryName(new ResourceLocation(MCA.MODID + ":" + itemName));
    }
}