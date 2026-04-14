package mca.entity.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 建筑类型定义类
 * 对应1.21.1版本的BuildingType
 */
public final class BuildingType {
    private final String name;
    private final int margin;
    private final String color;
    private final int priority;
    private final boolean visible;
    private final boolean noBeds;
    private final Map<String, Integer> blocks;
    private final boolean icon;
    private final int iconU;
    private final int iconV;
    private final boolean grouped;
    private final int mergeRange;

    // 运行时计算的映射
    private transient Map<ResourceLocation, ResourceLocation> blockToGroup;
    private transient Map<ResourceLocation, Integer> groups;

    public BuildingType(String name, JsonObject value) {
        this.name = name;
        this.margin = JsonUtils.getInt(value, "margin", 0);
        this.color = JsonUtils.getString(value, "color", "ffffffff");
        this.priority = JsonUtils.getInt(value, "priority", 0);
        this.visible = JsonUtils.getBoolean(value, "visible", true);
        this.noBeds = JsonUtils.getBoolean(value, "noBeds", false);

        this.icon = JsonUtils.getBoolean(value, "icon", false);
        this.iconU = JsonUtils.getInt(value, "iconU", 0);
        this.iconV = JsonUtils.getInt(value, "iconV", 0);

        this.grouped = JsonUtils.getBoolean(value, "grouped", false);
        this.mergeRange = JsonUtils.getInt(value, "mergeRange", 0);

        this.blocks = new HashMap<>();
        if (value.has("blocks") && value.get("blocks").isJsonObject()) {
            JsonObject blocksObj = value.getAsJsonObject("blocks");
            for (Map.Entry<String, JsonElement> entry : blocksObj.entrySet()) {
                this.blocks.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }

        // 默认建筑类型需要床
        if (this.blocks.isEmpty()) {
            this.blocks.put("#minecraft:beds", 1);
        }
    }

    public BuildingType(NBTTagCompound nbt) {
        this.name = nbt.getString("name");
        this.margin = nbt.getInteger("margin");
        this.color = nbt.getString("color");
        this.priority = nbt.getInteger("priority");
        this.visible = nbt.getBoolean("visible");
        this.noBeds = nbt.getBoolean("noBeds");
        this.icon = nbt.getBoolean("icon");
        this.iconU = nbt.getInteger("iconU");
        this.iconV = nbt.getInteger("iconV");
        this.grouped = nbt.getBoolean("grouped");
        this.mergeRange = nbt.getInteger("mergeRange");

        this.blocks = new HashMap<>();
        NBTTagCompound blocksNbt = nbt.getCompoundTag("blocks");
        for (String key : blocksNbt.getKeySet()) {
            this.blocks.put(key, blocksNbt.getInteger(key));
        }
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("name", name);
        nbt.setInteger("margin", margin);
        nbt.setString("color", color);
        nbt.setInteger("priority", priority);
        nbt.setBoolean("visible", visible);
        nbt.setBoolean("noBeds", noBeds);
        nbt.setBoolean("icon", icon);
        nbt.setInteger("iconU", iconU);
        nbt.setInteger("iconV", iconV);
        nbt.setBoolean("grouped", grouped);
        nbt.setInteger("mergeRange", mergeRange);

        NBTTagCompound blocksNbt = new NBTTagCompound();
        for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
            blocksNbt.setInteger(entry.getKey(), entry.getValue());
        }
        nbt.setTag("blocks", blocksNbt);

        return nbt;
    }

    public String name() {
        return name;
    }

    public String color() {
        return color;
    }

    public int priority() {
        return priority;
    }

    public boolean visible() {
        return visible;
    }

    public int getColor() {
        return (int) Long.parseLong(color, 16);
    }

    /**
     * 获取方块到组的映射
     */
    public Map<ResourceLocation, ResourceLocation> getBlockToGroup() {
        if (blockToGroup == null) {
            blockToGroup = new HashMap<>();
            groups = new HashMap<>();
            for (Map.Entry<String, Integer> requirement : blocks.entrySet()) {
                ResourceLocation identifier;
                if (requirement.getKey().startsWith("#")) {
                    // 标签处理 - 在1.12.2中简化处理
                    identifier = new ResourceLocation(requirement.getKey().substring(1));
                    // 注意：1.12.2的标签系统与1.21.1不同，这里简化处理
                } else {
                    identifier = new ResourceLocation(requirement.getKey());
                    blockToGroup.put(identifier, identifier);
                }
                groups.put(identifier, requirement.getValue());
            }
        }
        return blockToGroup;
    }

    public Map<ResourceLocation, Integer> getGroups() {
        getBlockToGroup();
        return groups;
    }

    public boolean isIcon() {
        return icon;
    }

    public int iconU() {
        return iconU * 20;
    }

    public int iconV() {
        return iconV * 60;
    }

    public boolean grouped() {
        return grouped;
    }

    public int mergeRange() {
        return mergeRange;
    }

    public boolean noBeds() {
        return noBeds;
    }

    public int getMargin() {
        return margin;
    }

    public int getMinBlocks() {
        return blocks.values().stream().mapToInt(v -> v).sum();
    }

    public Map<String, Integer> getBlocks() {
        return blocks;
    }
}
