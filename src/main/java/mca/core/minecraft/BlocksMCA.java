package mca.core.minecraft;

import mca.blocks.BlockMemorial;
import mca.blocks.BlockTombstone;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

public final class BlocksMCA {
    public static final Block ROSE_GOLD_BLOCK = new BlockOre();
    public static final Block ROSE_GOLD_ORE = new BlockOre();
    public static final BlockTombstone TOMBSTONE = new BlockTombstone();
    public static final BlockVillagerSpawner VILLAGER_SPAWNER = new BlockVillagerSpawner();
    public static final BlockMemorial MEMORIAL = new BlockMemorial();

    private static final ArrayList<Block> BLOCKS = new ArrayList<>();

    public static void register(RegistryEvent.Register<Block> event) {
        ROSE_GOLD_BLOCK.setHardness(3.0F).setResistance(5.0F).setCreativeTab(MCA.creativeTab);
        ROSE_GOLD_BLOCK.setHarvestLevel("pickaxe", 2);
        ROSE_GOLD_ORE.setHardness(3.0F).setResistance(5.0F).setCreativeTab(MCA.creativeTab);
        ROSE_GOLD_ORE.setHarvestLevel("pickaxe", 2);
        VILLAGER_SPAWNER.setCreativeTab(MCA.creativeTab);
        TOMBSTONE.setCreativeTab(MCA.creativeTab);
        MEMORIAL.setCreativeTab(MCA.creativeTab);

        Block[] blocks = {
                ROSE_GOLD_BLOCK,
                ROSE_GOLD_ORE,
                TOMBSTONE,
                VILLAGER_SPAWNER,
                MEMORIAL
        };

        setBlockName(ROSE_GOLD_BLOCK, "rose_gold_block");
        setBlockName(ROSE_GOLD_ORE, "rose_gold_ore");
        setBlockName(TOMBSTONE, "tombstone");
        setBlockName(VILLAGER_SPAWNER, "villager_spawner");
        setBlockName(MEMORIAL, "memorial");

        for (Block block : blocks) {
            event.getRegistry().register(block);
            BLOCKS.add(block);
        }
    }

    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        ItemBlock[] items = {
                new ItemBlock(ROSE_GOLD_BLOCK),
                new ItemBlock(ROSE_GOLD_ORE),
                new ItemBlock(VILLAGER_SPAWNER)
        };

        for (ItemBlock item : items) {
            Block block = item.getBlock();
            ResourceLocation registryName = block.getRegistryName();
            registry.register(item.setRegistryName(registryName));
        }
    }

    private static void setBlockName(Block block, String blockName) {
        block.setRegistryName(MCA.MODID, blockName);
        block.setTranslationKey(block.getRegistryName().toString());
    }

    @SideOnly(Side.CLIENT)
    public static void registerModelMeshers() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        for (Block block : BLOCKS) {
            mesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }
}