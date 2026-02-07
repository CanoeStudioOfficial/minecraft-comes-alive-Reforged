package mca.util;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.io.IOException;

public class SchematicDump {
    public static void dump(String path) {
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("mca", "schematic/destiny-test.schematic")).getInputStream();
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
            System.out.println("Schematic NBT: " + nbt.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
