package mca.core.minecraft;

import mca.blocks.BlockTombstone;
import mca.blocks.BlockVillagerSpawner;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;

public final class BlocksMCA {
    public static final Block ROSE_GOLD_BLOCK = new BlockOre();
    public static final BlockVillagerSpawner VILLAGER_SPAWNER = new BlockVillagerSpawner();

    // Tombstones - 10 different styles
    public static Block GRAVELLING_HEADSTONE;
    public static Block UPRIGHT_HEADSTONE;
    public static Block SLANTED_HEADSTONE;
    public static Block CROSS_HEADSTONE;
    public static Block WALL_HEADSTONE;
    public static Block COBBLESTONE_UPRIGHT_HEADSTONE;
    public static Block COBBLESTONE_SLANTED_HEADSTONE;
    public static Block WOODEN_UPRIGHT_HEADSTONE;
    public static Block WOODEN_SLANTED_HEADSTONE;
    public static Block GOLDEN_UPRIGHT_HEADSTONE;
    public static Block GOLDEN_SLANTED_HEADSTONE;
    public static Block DEEPSLATE_UPRIGHT_HEADSTONE;
    public static Block DEEPSLATE_SLANTED_HEADSTONE;

    private static final ArrayList<Block> BLOCKS = new ArrayList<>();

    public static void register(RegistryEvent.Register<Block> event) {
        ROSE_GOLD_BLOCK.setHardness(3.0F).setResistance(5.0F).setCreativeTab(MCA.creativeTab);
        ROSE_GOLD_BLOCK.setHarvestLevel("pickaxe", 2);
        VILLAGER_SPAWNER.setCreativeTab(MCA.creativeTab);

        // Initialize tombstones
        GRAVELLING_HEADSTONE = new BlockTombstone(Material.ROCK, 100, 50, new Vec3d(0, -25, 40), -90.0f, true, BlockTombstone.GRAVELLING_SHAPE);
        UPRIGHT_HEADSTONE = new BlockTombstone(Material.ROCK, 70, 30, new Vec3d(0, -30, -8), 0.0f, true, BlockTombstone.UPRIGHT_SHAPE);
        SLANTED_HEADSTONE = new BlockTombstone(Material.ROCK, 90, 15, new Vec3d(0, -12, 22), -72.5f, true, BlockTombstone.SLANTED_SHAPE);
        CROSS_HEADSTONE = new BlockTombstone(Material.ROCK, 80, 15, new Vec3d(0, -13, 15), -45.0f, true, BlockTombstone.CROSS_SHAPE);
        WALL_HEADSTONE = new BlockTombstone(Material.ROCK, 100, 15, new Vec3d(0, -25, 40), 0.0f, false, BlockTombstone.WALL_SHAPE);
        COBBLESTONE_UPRIGHT_HEADSTONE = new BlockTombstone(Material.ROCK, 70, 30, new Vec3d(0, -30, -8), 0.0f, true, BlockTombstone.UPRIGHT_SHAPE);
        COBBLESTONE_SLANTED_HEADSTONE = new BlockTombstone(Material.ROCK, 90, 15, new Vec3d(0, -12, 22), -72.5f, true, BlockTombstone.SLANTED_SHAPE);
        WOODEN_UPRIGHT_HEADSTONE = new BlockTombstone(Material.WOOD, 70, 30, new Vec3d(0, -30, -8), 0.0f, true, BlockTombstone.UPRIGHT_SHAPE);
        WOODEN_SLANTED_HEADSTONE = new BlockTombstone(Material.WOOD, 90, 15, new Vec3d(0, -12, 22), -72.5f, true, BlockTombstone.SLANTED_SHAPE);
        GOLDEN_UPRIGHT_HEADSTONE = new BlockTombstone(Material.ROCK, 70, 30, new Vec3d(0, -30, -8), 0.0f, true, BlockTombstone.UPRIGHT_SHAPE);
        GOLDEN_SLANTED_HEADSTONE = new BlockTombstone(Material.ROCK, 90, 15, new Vec3d(0, -12, 22), -72.5f, true, BlockTombstone.SLANTED_SHAPE);
        DEEPSLATE_UPRIGHT_HEADSTONE = new BlockTombstone(Material.ROCK, 70, 30, new Vec3d(0, -30, -8), 0.0f, true, BlockTombstone.UPRIGHT_SHAPE);
        DEEPSLATE_SLANTED_HEADSTONE = new BlockTombstone(Material.ROCK, 90, 15, new Vec3d(0, -12, 22), -72.5f, true, BlockTombstone.SLANTED_SHAPE);

        Block[] blocks = {
                ROSE_GOLD_BLOCK,
                VILLAGER_SPAWNER,
                GRAVELLING_HEADSTONE,
                UPRIGHT_HEADSTONE,
                SLANTED_HEADSTONE,
                CROSS_HEADSTONE,
                WALL_HEADSTONE,
                COBBLESTONE_UPRIGHT_HEADSTONE,
                COBBLESTONE_SLANTED_HEADSTONE,
                WOODEN_UPRIGHT_HEADSTONE,
                WOODEN_SLANTED_HEADSTONE,
                GOLDEN_UPRIGHT_HEADSTONE,
                GOLDEN_SLANTED_HEADSTONE,
                DEEPSLATE_UPRIGHT_HEADSTONE,
                DEEPSLATE_SLANTED_HEADSTONE
        };

        setBlockName(ROSE_GOLD_BLOCK, "rose_gold_block");
        setBlockName(VILLAGER_SPAWNER, "villager_spawner");
        setBlockName(GRAVELLING_HEADSTONE, "gravelling_headstone");
        setBlockName(UPRIGHT_HEADSTONE, "upright_headstone");
        setBlockName(SLANTED_HEADSTONE, "slanted_headstone");
        setBlockName(CROSS_HEADSTONE, "cross_headstone");
        setBlockName(WALL_HEADSTONE, "wall_headstone");
        setBlockName(COBBLESTONE_UPRIGHT_HEADSTONE, "cobblestone_upright_headstone");
        setBlockName(COBBLESTONE_SLANTED_HEADSTONE, "cobblestone_slanted_headstone");
        setBlockName(WOODEN_UPRIGHT_HEADSTONE, "wooden_upright_headstone");
        setBlockName(WOODEN_SLANTED_HEADSTONE, "wooden_slanted_headstone");
        setBlockName(GOLDEN_UPRIGHT_HEADSTONE, "golden_upright_headstone");
        setBlockName(GOLDEN_SLANTED_HEADSTONE, "golden_slanted_headstone");
        setBlockName(DEEPSLATE_UPRIGHT_HEADSTONE, "deepslate_upright_headstone");
        setBlockName(DEEPSLATE_SLANTED_HEADSTONE, "deepslate_slanted_headstone");

        for (Block block : blocks) {
            event.getRegistry().register(block);
            BLOCKS.add(block);
        }
    }

    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        ItemBlock[] items = {
                new ItemBlock(ROSE_GOLD_BLOCK),
                new ItemBlock(VILLAGER_SPAWNER),
                new ItemBlock(GRAVELLING_HEADSTONE),
                new ItemBlock(UPRIGHT_HEADSTONE),
                new ItemBlock(SLANTED_HEADSTONE),
                new ItemBlock(CROSS_HEADSTONE),
                new ItemBlock(WALL_HEADSTONE),
                new ItemBlock(COBBLESTONE_UPRIGHT_HEADSTONE),
                new ItemBlock(COBBLESTONE_SLANTED_HEADSTONE),
                new ItemBlock(WOODEN_UPRIGHT_HEADSTONE),
                new ItemBlock(WOODEN_SLANTED_HEADSTONE),
                new ItemBlock(GOLDEN_UPRIGHT_HEADSTONE),
                new ItemBlock(GOLDEN_SLANTED_HEADSTONE),
                new ItemBlock(DEEPSLATE_UPRIGHT_HEADSTONE),
                new ItemBlock(DEEPSLATE_SLANTED_HEADSTONE)
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