package mca.entity.data;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class Genetics {
    public static final String SIZE = "Size";
    public static final String WIDTH = "Width";
    public static final String BREAST = "Breast";
    public static final String MELANIN = "Melanin";
    public static final String HEMOGLOBIN = "Hemoglobin";
    public static final String EUMELANIN = "Eumelanin";
    public static final String PHEOMELANIN = "Pheomelanin";
    public static final String SKIN = "Skin";
    public static final String FACE = "Face";
    public static final String VOICE = "Voice";
    public static final String VOICE_TONE = "VoiceTone";

    private static final List<String> ALL_GENES = Arrays.asList(
            SIZE, WIDTH, BREAST, MELANIN, HEMOGLOBIN, EUMELANIN, PHEOMELANIN, SKIN, FACE, VOICE, VOICE_TONE
    );

    private final IGeneticsCarrier carrier;
    private Random random = new Random();

    public Genetics(IGeneticsCarrier carrier) {
        this.carrier = carrier;
    }

    public Genetics(final EntityVillagerMCA entity) {
        this(new IGeneticsCarrier() {
            @Override
            public NBTTagCompound getGenetics() { return entity.get(EntityVillagerMCA.GENETICS); }
            @Override
            public void setGenetics(NBTTagCompound nbt) { entity.set(EntityVillagerMCA.GENETICS, nbt); }
            @Override
            public EnumGender getGender() { return EnumGender.byId(entity.get(EntityVillagerMCA.GENDER)); }
            @Override
            public World getWorld() { return entity.world; }
            @Override
            public BlockPos getPosition() { return entity.getPosition(); }
        });
    }

    public Genetics(final PlayerSaveData data, final EntityPlayer player) {
        this(new IGeneticsCarrier() {
            @Override
            public NBTTagCompound getGenetics() { return data.getGenetics(); }
            @Override
            public void setGenetics(NBTTagCompound nbt) { data.setGenetics(nbt); }
            @Override
            public EnumGender getGender() { return data.getGender(); }
            @Override
            public World getWorld() { return player.world; }
            @Override
            public BlockPos getPosition() { return player.getPosition(); }
        });
    }

    public float getVerticalScaleFactor() {
        return 0.75F + getGene(SIZE) / 2;
    }

    public float getHorizontalScaleFactor() {
        return 0.75F + getGene(WIDTH) / 2;
    }

    public EnumGender getGender() {
        return carrier.getGender();
    }

    public float getBreastSize() {
        return getGender() == EnumGender.FEMALE ? getGene(BREAST) : 0;
    }

    public void setGene(String type, float value) {
        NBTTagCompound nbt = carrier.getGenetics().copy();
        nbt.setFloat(type, value);
        carrier.setGenetics(nbt);
    }

    public float getGene(String type) {
        NBTTagCompound nbt = carrier.getGenetics();
        return nbt.hasKey(type) ? nbt.getFloat(type) : 0.5f;
    }

    public void randomize() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (String type : ALL_GENES) {
            nbt.setFloat(type, random.nextFloat());
        }

        // size and width are more centered
        nbt.setFloat(SIZE, centeredRandom());
        nbt.setFloat(WIDTH, centeredRandom());

        // temperature based randomization
        World world = carrier.getWorld();
        BlockPos pos = carrier.getPosition();
        float temp = world != null && pos != null ? world.getBiome(pos).getTemperature(pos) : 0.5f;

        // immigrants chance from config
        if (random.nextFloat() < MCA.getConfig().geneticImmigrantChance) {
            temp = random.nextFloat() * 2 - 0.5F;
        }

        float height = pos != null ? (float) pos.getY() : 64;
        height -= world != null ? world.getSeaLevel() : 64;
        height /= 128;

        nbt.setFloat(MELANIN, MathHelper.clamp(temperatureBaseRandom(temp) - height * 0.2f, 0, 1));
        nbt.setFloat(HEMOGLOBIN, MathHelper.clamp(temperatureBaseRandom(temp) * 0.5f + height * 0.5f, 0, 1));

        carrier.setGenetics(nbt);
    }

    private float centeredRandom() {
        return Math.min(1, Math.max(0, (random.nextFloat() - 0.5F) * (random.nextFloat() - 0.5F) + 0.5F));
    }

    private float temperatureBaseRandom(float temp) {
        return (random.nextFloat() - 0.5F) * 0.35F + temp * 0.4F + 0.1F;
    }

    public void combine(Genetics mother, Genetics father) {
        NBTTagCompound nbt = new NBTTagCompound();
        for (String type : ALL_GENES) {
            float m = mother.getGene(type);
            float f = father.getGene(type);
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;
            nbt.setFloat(type, MathHelper.clamp(g, 0, 1));
        }
        carrier.setGenetics(nbt);
    }

    public interface IGeneticsCarrier {
        NBTTagCompound getGenetics();
        void setGenetics(NBTTagCompound nbt);
        EnumGender getGender();
        World getWorld();
        BlockPos getPosition();
    }
}
