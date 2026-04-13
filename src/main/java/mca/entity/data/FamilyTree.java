package mca.entity.data;

import mca.enums.EnumGender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FamilyTree extends WorldSavedData {
    private static final String DATA_ID = "MCA-FamilyTree-V1";
    private final Map<UUID, FamilyTreeNode> nodes = new HashMap<>();

    public FamilyTree(String id) {
        super(id);
    }

    public static FamilyTree get(World world) {
        FamilyTree data = (FamilyTree) world.loadData(FamilyTree.class, DATA_ID);
        if (data == null) {
            data = new FamilyTree(DATA_ID);
            world.setData(DATA_ID, data);
        }
        return data;
    }

    public FamilyTreeNode getOrCreateNode(UUID id, String name, boolean isPlayer, EnumGender gender) {
        FamilyTreeNode node = nodes.get(id);
        if (node == null) {
            node = new FamilyTreeNode(id, name, isPlayer, gender);
            nodes.put(id, node);
            markDirty();
        }
        return node;
    }

    public FamilyTreeNode getNode(UUID id) {
        return nodes.get(id);
    }

    public void removeNode(UUID id) {
        nodes.remove(id);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nodes.clear();
        for (String key : nbt.getKeySet()) {
            nodes.put(UUID.fromString(key), new FamilyTreeNode(nbt.getCompoundTag(key)));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for (Map.Entry<UUID, FamilyTreeNode> entry : nodes.entrySet()) {
            nbt.setTag(entry.getKey().toString(), entry.getValue().writeToNBT());
        }
        return nbt;
    }

    public Map<UUID, FamilyTreeNode> getNodes() {
        return nodes;
    }
}
