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

import mca.enums.EnumMemorialType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

public final class ItemsMCA {
    public static final ItemSpawnEgg EGG_MALE = new ItemSpawnEgg(true);
    public static final ItemSpawnEgg EGG_FEMALE = new ItemSpawnEgg(false);
    public static final Item WEDDING_RING = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item WEDDING_RING_RG = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING_RG = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item MATCHMAKERS_RING = new ItemMatchmakersRing().setMaxStackSize(2);
    public static final Item BABY_BOY = new ItemBaby(true);
    public static final Item BABY_GIRL = new ItemBaby(false);
    public static final Item ROSE_GOLD_INGOT = new Item().setTranslationKey("rose_gold_ingot");
    public static final Item ROSE_GOLD_DUST = new Item().setTranslationKey("rose_gold_dust");
    public static final Item VILLAGER_EDITOR = new ItemVillagerEditor();
    public static final Item STAFF_OF_LIFE = new ItemStaffOfLife();
    public static final Item WHISTLE = new ItemWhistle();
    public static final Item DIVORCE_PAPERS = new ItemTooltipAppender().setTooltip("Gift to your spouse to end your marriage.").setMaxStackSize(1);
    public static final Item EGG_GRIM_REAPER = new ItemSpawnGrimReaper();
    public static final Item CRYSTAL_BALL = new ItemCrystalBall();
    public static final Item GOLD_DUST = new Item();
    public static final Item NEW_OUTFIT = new ItemNewOutfit();
    public static final Item NEEDLE_AND_STRING = new ItemTooltipAppender().setTooltip("Use with some wool to create cloth.").setMaxDamage(16).setMaxStackSize(1);
    public static final Item CLOTH = new ItemTooltipAppender().setTooltip("This can be used to craft new clothes for your villagers.");
    public static final Item TOMBSTONE = new ItemTombstone();
    public static final Item BROKEN_RING = new ItemMemorial(EnumMemorialType.BROKEN_RING);
    public static final Item CHILDS_DOLL = new ItemMemorial(EnumMemorialType.DOLL);
    public static final Item TOY_TRAIN = new ItemMemorial(EnumMemorialType.TRAIN);
    public static final Item BOOK_DEATH = new ItemGuideBook();
    public static final Item BOOK_ROMANCE = new ItemGuideBook();
    public static final Item BOOK_FAMILY = new ItemGuideBook();
    public static final Item BOOK_ROSE_GOLD = new ItemGuideBook();
    public static final Item BOOK_INFECTION = new ItemGuideBook();

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
        ITEMS.stream().forEach(i -> i.setCreativeTab(MCA.creativeTab));
    }



    @SideOnly(Side.CLIENT)
    public static void registerModelMeshers() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        for (Item item : ITEMS) mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    public static void setItemName(Item item, String itemName) {
        item.setTranslationKey(itemName);
        item.setRegistryName(new ResourceLocation(MCA.MODID + ":" + itemName));
    }

    public static void setBookNBT(ItemStack stack) {
        Item book = stack.getItem();
        NBTTagCompound nbt = new NBTTagCompound();

        if (book == BOOK_DEATH) {
            nbt.setString("title", "Death, and How to Cure It!");
            nbt.setString("author", "Ozzie the Warrior");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();
            pages.appendTag(new NBTTagString(""
                    + "I couldn't count how many times my family has been blown to pieces by creepers.\n\nHow are they still around, you may ask?"
                    + "\n\nEasy! I, dear reader, have discovered a CURE for death itself! And through this book, I can share it with you."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Memorials\n\n" + TextFormatting.RESET
                    + "When a family member dies, they will drop a chest - and inside will be an " + TextFormatting.BOLD + "item" + TextFormatting.RESET + " that was important to them.\n\n"
                    + "This is the key to reviving someone, don't lose it! Only your spouse and children will drop these items."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Staff Of Life\n\n" + TextFormatting.RESET
                    + "The Staff is a powerful item that can revive up to 5 people. Place a memorial item on the ground and wave the staff over it. Within moments, your loved one will be fully revived!\n\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Summoning Grim\n\n" + TextFormatting.RESET
                    + "Unfortunately, you must obtain the Staff from the Grim Reaper himself.\n\n"
                    + "To summon him, you must build an altar consisting of 3 obsidian columns that are at least 2 blocks high. They may be higher if you like."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Summoning Grim pt. 2\n\n" + TextFormatting.RESET
                    + "     # # # X # # #\n"
                    + "     # # # # # # #\n"
                    + "     # # # # # # #\n"
                    + "     X # # E # # X\n\n"
                    + "X = Column\n"
                    + "E = Emerald\n"
                    + "# = Empty"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Summoning Grim pt. 3\n\n" + TextFormatting.RESET
                    + "After building the altar, wait until night and light all 3 columns.\n\n"
                    + "When you're ready to fight, light the emerald block and run!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Fighting Grim\n\n" + TextFormatting.RESET
                    + "Grim is tough. Use full diamond armor, lots of potions, and lots of enchantments.\n\n"
                    + "He can:\n"
                    + "- Fly\n"
                    + "- Block attacks\n"
                    + "- Blind you\n"
                    + "- Move your items\n"
                    + "- Teleport\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Fighting Grim pt. 2\n\n" + TextFormatting.RESET
                    + "If you hit Grim while he's blocking, he will teleport behind you and strike.\n\n"
                    + "Do not try to use arrows or poison, he is immune!\n\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Fighting Grim pt. 3\n\n" + TextFormatting.RESET
                    + "When Grim is at " + TextFormatting.BOLD + "half health" + TextFormatting.RESET + " he will teleport into the air and begin healing.\n\n"
                    + "While healing, he will summon his minions from the underworld to fight you."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Fighting Grim pt. 4\n\n" + TextFormatting.RESET
                    + "When Grim is healed, he will continue attacking you, but he won't be able to heal again for 3 minutes and 30 seconds.\n\n"
                    + "Each time Grim heals, he will not be able to restore as much health has he did previously."));

            nbt.setTag("pages", pages);
        } else if (book == BOOK_ROMANCE) {
            nbt.setString("title", "Relationships and You");
            nbt.setString("author", "Gerry the Librarian");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Introduction\n\n" + TextFormatting.RESET
                    + "Interaction is key to building relationships and finding the love of your life.\n\n"
                    + "I've happily written this book in order to share my knowledge of interaction, love, and, unfortunately, divorce, to anyone who may need a little push in the right direction."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Personalities\n\n" + TextFormatting.RESET
                    + "When speaking to any villager, you'll notice they have a personality.\n\n"
                    + "Pay close attention to this. I have outlined each personality's quirks here. Each personality has a particular category they fall into (1-3)."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Personalities pt. 2\n" + TextFormatting.RESET
                    + TextFormatting.BOLD + "Athletic (2): " + TextFormatting.RESET + "Runs faster\n"
                    + TextFormatting.BOLD + "Confident (3): " + TextFormatting.RESET + "Hits harder\n"
                    + TextFormatting.BOLD + "Strong (3): " + TextFormatting.RESET + "Doubled attack damage\n"
                    + TextFormatting.BOLD + "Friendly (1): " + TextFormatting.RESET + "Gains hearts faster\n"
                    + TextFormatting.BOLD + "Curious (2): " + TextFormatting.RESET + "Finds more when working\n"
                    + TextFormatting.BOLD + "Peaceful (1): " + TextFormatting.RESET + "Will not fight\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Personalities pt. 3\n" + TextFormatting.RESET
                    + TextFormatting.BOLD + "Flirty (2): " + TextFormatting.RESET + "Bonus to all interactions\n"
                    + TextFormatting.BOLD + "Witty (2): " + TextFormatting.RESET + "Appreciates jokes\n"
                    + TextFormatting.BOLD + "Sensitive (1): " + TextFormatting.RESET + "Easily offended\n"
                    + TextFormatting.BOLD + "Greedy (3): " + TextFormatting.RESET + "Finds less when working\n"
                    + TextFormatting.BOLD + "Stubborn (3): " + TextFormatting.RESET + "Harder to gain hearts\n"
                    + TextFormatting.BOLD + "Odd (2): " + TextFormatting.RESET + "N/A\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Personalities pt. 4\n" + TextFormatting.RESET
                    + "The category shows what a person likes more than others:\n"
                    + TextFormatting.BOLD + "1:" + TextFormatting.RESET + "Chatting, Stories\n"
                    + TextFormatting.BOLD + "2:" + TextFormatting.RESET + "Joking, Romance\n"
                    + TextFormatting.BOLD + "3:" + TextFormatting.RESET + "Chatting, Shake Hand, Stories\n\n"
                    + "In the case of personality type 3, it is not recommended to attempt jokes or romantic interactions."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Moods\n" + TextFormatting.RESET
                    + "Every person has a mood, which is always apparent when speaking to them.\n\n"
                    + "Moods can change throughout the day, and determine how likely a villager is to like your interaction, and how many hearts you'll gain."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Moods pt. 2\n" + TextFormatting.RESET
                    + "Villagers in a good mood may have a certain 'glow' about them, and will be easier to interact with.\n\n"
                    + "Villagers in a bad mood may cry, or be visibly angry and be more difficult to interact with."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Moods pt. 3\n" + TextFormatting.RESET
                    + "The death of a villager seems to put those nearby in bad moods.\n\n"
                    + "Taxing also gradually decreases the moods of everyone nearby.\n\n"
                    + "Gifts and successful interactions are known to boost moods."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Interacting\n" + TextFormatting.RESET
                    + "Choose wisely when interacting with a villager, based on their mood and personality!\n\n"
                    + "If choosing a romantic interaction, be sure that the villager you are talking to likes you a lot."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Interacting pt. 2\n" + TextFormatting.RESET
                    + "Don't be annoying! Talking to someone for too long will bore them, and your interactions may stop succeeding.\n\n"
                    + "If this happens, simply wait a few minutes before trying to talk to them again."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Marriage\n" + TextFormatting.RESET
                    + "To get married, simply gift a villager a wedding ring once you feel you have reached the highest relationship level.\n\n"
                    + "If you have a lot of friends in the village, you may want to get engaged first!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Engagement\n" + TextFormatting.RESET
                    + "Gift an engagement ring before gifting a wedding ring.\n\n"
                    + "All nearby villagers will give you gifts when you get married, but only if you're engaged first!\n\n"
                    + "Villagers that like you more will give you better gifts."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Divorce\n" + TextFormatting.RESET
                    + "Unfortunately, sometimes it is best to split from your spouse and move on.\n\n"
                    + "To do this, you may craft divorce papers with paper, a feather, and ink.\n\n"
                    + "Gift them to your spouse and the marriage will end. They will not be happy!"));

            nbt.setTag("pages", pages);
        } else if (book == BOOK_FAMILY) {
            nbt.setString("title", "Managing Your Family Vol. XI");
            nbt.setString("author", "Leanne the Cleric");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();
            pages.appendTag(new NBTTagString(""
                    + "Children are our future! Make sure to have as many as you possibly can.\n\nNot only do you get to experience the joy of"
                    + " raising a child, but once they are past the baby stage, put them to work!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Babies\n\n" + TextFormatting.RESET
                    + "When you are married, simply approach your spouse and offer to 'Procreate'.\n\n"
                    + "After a short dance, you'll be the proud owner of a new baby boy or girl (or maybe even both)!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Growth\n\n" + TextFormatting.RESET
                    + "Babies take time to grow, make sure to hold them until they are ready, or give them to your spouse to take care of.\n\n"
                    + "Once a baby is ready to grow, you may place it on the ground and it will grow into a child!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Adolescence\n\n" + TextFormatting.RESET
                    + "Children will grow slowly from age 4 to 18.\n\nHowever, the magical properties of Golden Apples are said to accelerate "
                    + "any child's growth. I have yet to try this myself."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Chores\n\n" + TextFormatting.RESET
                    + "Any child can farm, cut wood, mine, hunt, and fish. You'll need to provide them with the tools they need to do so.\n\n"
                    + "If a tool breaks and the child doesn't have another, they will have no choice but to stop working."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Greed\n\n" + TextFormatting.RESET
                    + "If you notice your child has a Greedy personality, be careful!\n\n"
                    + "Greedy children are known to steal away items that they may pick up while doing chores."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Mining\n\n" + TextFormatting.RESET
                    + "A special note on Mining here, most children have a peculiar ability to search for and locate ores underground.\n\n"
                    + "This activity damages any pickaxe they may have in their inventory.\n\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Adulthood\n\n" + TextFormatting.RESET
                    + "As sad as it may be, children will eventually grow into adults. Once they are adults, they will no longer work for you.\n\n"
                    + "Adults can be married off by using Matchmaker's Rings, or they will eventually get married on their own."));

            nbt.setTag("pages", pages);
        } else if (book == BOOK_ROSE_GOLD) {
            nbt.setString("title", "On Rose Gold");
            nbt.setString("author", "William the Miner");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Warning!\nTOP SECRET\n\n" + TextFormatting.RESET
                    + "This manual is the property of William Mining Co.\n\nIf you are not a William Mining Co. employee, please refrain from "
                    + "reading this manual and return promptly to William the Miner."));

            pages.appendTag(new NBTTagString(""
                    + "Ah, rose gold - a lovely combination of silver, copper, and gold that smelts into a pinkish orange metal.\n\n"
                    + "Most use it as an alternative to gold for crafting rings as it is much less expensive.\n\n"
                    + "However, it has some interesting qualities that are easy to miss."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Creating Dust\n\n" + TextFormatting.RESET
                    + "Rose gold, once smelted, can be crushed into a fine dust.\n\n"
                    + "Look closely at rose gold dust in bright light, and you'll see shiny flecks of pure gold!\n\n"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Washing Dust\n\n" + TextFormatting.RESET
                    + "With a little work, we can actually extract the gold from the dust and create pure gold ingots. Simply mix dust with a bucket of water.\n\n"
                    + "The lighter silver and copper components will wash away, leaving you with about 6 smaller piles of gold dust."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Extracting Gold\n\n" + TextFormatting.RESET
                    + "Arrange 9 piles of dust on your crafting table, and if you're lucky, you'll find a gold nugget in one of them!\n\n"
                    + "And of course, once you have 9 gold nuggets, you'll be able to craft them into a solid gold ingot."));

            nbt.setTag("pages", pages);
        } else if (book == BOOK_INFECTION) {
            nbt.setString("title", "Beware the Infection!");
            nbt.setString("author", "Richard the Zombie");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();

            pages.appendTag(new NBTTagString(""
                    + "Good day, readers! I've written this book so that you may not end up suffering the same fate as I.\n\n"
                    + "Although I caught the infection, I was luckily able to keep all of my mental faculties."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "What is the Infection?\n\n" + TextFormatting.RESET
                    + "I discovered long ago that the zombies that appear at night are actually villagers in the late stages of infection!\n\n"
                    + "Newly infected villagers turn green, are unable to speak, and occasionally try to bite!"));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Curing\n\n" + TextFormatting.RESET
                    + "Contrary to popular belief, any infected villager can be cured.\n\n"
                    + "You must first weaken the villager or zombie with a potion.\n\n"
                    + "Then, immediately feed them a golden apple."));

            pages.appendTag(new NBTTagString(TextFormatting.BOLD + "Curing pt. 2\n\n" + TextFormatting.RESET
                    + "Zombies that can be cured often have enlarged heads and noses.\n\n"
                    + "Any other zombies you see unfortunately are too far gone, and cannot be cured."));

            nbt.setTag("pages", pages);
        }

        stack.setTagCompound(nbt);
    }

    private static void setItemName(Item item, String itemName) {
        item.setTranslationKey(itemName);
        item.setRegistryName(new ResourceLocation(MCA.MODID + ":" + itemName));
    }
}