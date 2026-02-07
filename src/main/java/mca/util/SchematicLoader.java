package mca.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.io.IOException;

public class SchematicLoader {
    public static void spawnStructure(String resourcePath, BlockPos pos, World world) {
        try {
            // Remove leading slash if present for ResourceLocation
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            
            // ResourcePath is usually "assets/mca/schematic/..."
            // We need to convert it to "mca:schematic/..."
            String mcaPath = resourcePath.replace("assets/mca/", "");
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("mca", mcaPath)).getInputStream();
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
            
            short width = nbt.getShort("Width");
            short height = nbt.getShort("Height");
            short length = nbt.getShort("Length");
            byte[] blocks = nbt.getByteArray("Blocks");
            byte[] data = nbt.getByteArray("Data");
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        int index = x + (y * length + z) * width;
                        int blockID = blocks[index] & 0xFF;
                        int meta = data[index] & 0xFF;
                        
                        Block block = Block.getBlockById(blockID);
                        if (block != null) {
                            IBlockState state = block.getStateFromMeta(meta);
                            world.setBlockState(pos.add(x, y, z), state, 2);
                        }
                    }
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearStructure(String resourcePath, BlockPos pos, World world) {
        try {
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            String mcaPath = resourcePath.replace("assets/mca/", "");
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("mca", mcaPath)).getInputStream();
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
            
            short width = nbt.getShort("Width");
            short height = nbt.getShort("Height");
            short length = nbt.getShort("Length");
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        world.setBlockState(pos.add(x, y, z), Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
