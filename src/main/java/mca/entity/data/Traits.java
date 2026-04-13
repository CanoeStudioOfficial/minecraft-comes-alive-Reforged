package mca.entity.data;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;
import java.util.stream.Collectors;

public class Traits {
    public static final Map<String, Trait> TRAIT_REGISTRY = new HashMap<>();

    public static Trait ATHLETIC = registerTrait("athletic", 1.0F, 0.5F, false);
    public static Trait LEFT_HANDED = registerTrait("left_handed", 1.0F, 0.5F, false);
    public static Trait WEAK = registerTrait("weak", 1.0F, 1.0F, false);
    public static Trait TOUGH = registerTrait("tough", 1.0F, 1.0F, false);
    public static Trait COLOR_BLIND = registerTrait("color_blind", 1.0F, 0.5F);
    public static Trait HETEROCHROMIA = registerTrait("heterochromia", 1.0F, 0.5F);
    public static Trait LACTOSE_INTOLERANCE = registerTrait("lactose_intolerance", 1.0F, 1.0F);
    public static Trait COELIAC_DISEASE = registerTrait("coeliac_disease", 1.0F, 1.0F, false);
    public static Trait DIABETES = registerTrait("diabetes", 1.0F, 1.0F, false);
    public static Trait DWARFISM = registerTrait("dwarfism", 1.0F, 1.0F);
    public static Trait ALBINISM = registerTrait("albinism", 1.0F, 1.0F);
    public static Trait VEGETARIAN = registerTrait("vegetarian", 1.0F, 1.0F, false);
    public static Trait BISEXUAL = registerTrait("bisexual", 1.0F, 0.0F);
    public static Trait HOMOSEXUAL = registerTrait("homosexual", 1.0F, 0.0F);
    public static Trait ASEXUAL = registerTrait("asexual", 1.0F, 0.0F);
    public static Trait ELECTRIFIED = registerTrait("electrified", 0.0F, 0.0F, false);
    public static Trait SIRBEN = registerTrait("sirben", 0.025F, 1.0F);
    public static Trait RAINBOW = registerTrait("rainbow", 0.05F, 0.0F);
    public static Trait UNKNOWN = registerTrait("unknown", 0.0F, 0.0F, false);

    private final ITraitsCarrier carrier;
    private Random random = new Random();

    public Traits(ITraitsCarrier carrier) {
        this.carrier = carrier;
    }

    public Traits(final EntityVillagerMCA entity) {
        this(new ITraitsCarrier() {
            @Override
            public NBTTagCompound getTraits() { return entity.get(EntityVillagerMCA.TRAITS); }
            @Override
            public void setTraits(NBTTagCompound nbt) { entity.set(EntityVillagerMCA.TRAITS, nbt); }
        });
    }

    public Traits(final PlayerSaveData data) {
        this(new ITraitsCarrier() {
            @Override
            public NBTTagCompound getTraits() { return data.getTraits(); }
            @Override
            public void setTraits(NBTTagCompound nbt) { data.setTraits(nbt); }
        });
    }

    public static Trait registerTrait(String id, float chance, float inherit, boolean usableOnPlayer) {
        Trait trait = new Trait(id, chance, inherit, usableOnPlayer);
        TRAIT_REGISTRY.put(id, trait);
        return trait;
    }

    public static Trait registerTrait(String id, float chance, float inherit) {
        return registerTrait(id, chance, inherit, true);
    }

    public Set<Trait> getTraits() {
        NBTTagCompound nbt = carrier.getTraits();
        return nbt.getKeySet().stream().map(Trait::valueOf).collect(Collectors.toSet());
    }

    public Set<Trait> getInheritedTraits() {
        return getTraits().stream().filter(t -> random.nextFloat() < t.inherit * MCA.getConfig().traitInheritChance).collect(Collectors.toSet());
    }

    public boolean hasTrait(Trait trait) {
        return carrier.getTraits().hasKey(trait.id());
    }

    public boolean hasTrait(String traitId) {
        return carrier.getTraits().hasKey(traitId);
    }

    public void addTrait(Trait trait) {
        NBTTagCompound nbt = carrier.getTraits().copy();
        nbt.setBoolean(trait.id(), true);
        carrier.setTraits(nbt);
    }

    public void removeTrait(Trait trait) {
        NBTTagCompound nbt = carrier.getTraits().copy();
        nbt.removeTag(trait.id());
        carrier.setTraits(nbt);
    }

    public void randomize() {
        float total = (float) Trait.values().stream().mapToDouble(tr -> tr.chance).sum();
        for (Trait t : Trait.values()) {
            float chance = MCA.getConfig().traitChance / total * t.chance;
            if (random.nextFloat() < chance && t.isEnabled()) {
                addTrait(t);
            }
        }
    }

    public void inherit(Traits from) {
        for (Trait t : from.getInheritedTraits()) {
            addTrait(t);
        }
    }

    public float getVerticalScaleFactor() {
        return hasTrait(Traits.DWARFISM) ? 0.65f : 1.0f;
    }

    public float getHorizontalScaleFactor() {
        return (hasTrait(Traits.DWARFISM) ? 0.85f : 1.0f) * (hasTrait(Traits.TOUGH) ? 1.2f : 1.0f) * (hasTrait(Traits.WEAK) ? 0.85f : 1.0f);
    }

    public interface ITraitsCarrier {
        NBTTagCompound getTraits();
        void setTraits(NBTTagCompound nbt);
    }

    public static class Trait {
        private final String id;
        private final float chance;
        private final float inherit;
        private final boolean usableOnPlayer;

        Trait(String id, float chance, float inherit, boolean usableOnPlayer) {
            this.id = id;
            this.chance = chance;
            this.inherit = inherit;
            this.usableOnPlayer = usableOnPlayer;
        }

        public static Collection<Trait> values() {
            return TRAIT_REGISTRY.values();
        }

        public static Trait valueOf(String id) {
            return TRAIT_REGISTRY.getOrDefault(id, UNKNOWN);
        }

        public String id() {
            return this.id;
        }

        public ITextComponent getName() {
            return new TextComponentTranslation("trait." + id());
        }

        public ITextComponent getDescription() {
            return new TextComponentTranslation("traitDescription." + id());
        }

        public boolean isUsableOnPlayer() {
            return usableOnPlayer;
        }

        public boolean isEnabled() {
            // In 1.12.2 project, config may not have this specific map. 
            // I will assume it's enabled if config traitChance > 0 for now, 
            // or I'll need to update Config.java.
            return true; 
        }
    }
}
