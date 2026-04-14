package mca.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 建筑数据类
 * 对应1.21.1版本的Building
 */
public class Building {
    public static final long SCAN_COOLDOWN = 4800;

    private final Map<String, java.util.List<BlockPos>> blocks = new HashMap<>();

    private String type = "building";
    private boolean isTypeForced = false;

    private int size;
    private int pos0X, pos0Y, pos0Z;
    private int pos1X, pos1Y, pos1Z;
    private int posX, posY, posZ;
    private int id;
    private boolean strictScan;
    private long lastScan;

    public Building() {
    }

    public Building(BlockPos pos) {
        this(pos, false);
    }

    public Building(BlockPos pos, boolean strictScan) {
        pos0X = pos.getX();
        pos0Y = pos.getY();
        pos0Z = pos.getZ();

        pos1X = pos0X;
        pos1Y = pos0Y;
        pos1Z = pos0Z;

        posX = pos0X;
        posY = pos0Y;
        posZ = pos0Z;

        this.strictScan = strictScan;
    }

    public Building(NBTTagCompound nbt) {
        id = nbt.getInteger("id");
        size = nbt.getInteger("size");
        pos0X = nbt.getInteger("pos0X");
        pos0Y = nbt.getInteger("pos0Y");
        pos0Z = nbt.getInteger("pos0Z");
        pos1X = nbt.getInteger("pos1X");
        pos1Y = nbt.getInteger("pos1Y");
        pos1Z = nbt.getInteger("pos1Z");
        posX = nbt.getInteger("posX");
        posY = nbt.getInteger("posY");
        posZ = nbt.getInteger("posZ");

        isTypeForced = nbt.getBoolean("isTypeForced");
        type = nbt.getString("type");
        strictScan = nbt.getBoolean("strictScan");

        // 读取方块数据
        NBTTagCompound blocksNbt = nbt.getCompoundTag("blocks");
        for (String key : blocksNbt.getKeySet()) {
            NBTTagCompound posList = blocksNbt.getCompoundTag(key);
            java.util.List<BlockPos> positions = new java.util.ArrayList<>();
            for (String posKey : posList.getKeySet()) {
                NBTTagCompound posNbt = posList.getCompoundTag(posKey);
                positions.add(new BlockPos(posNbt.getInteger("x"), posNbt.getInteger("y"), posNbt.getInteger("z")));
            }
            blocks.put(key, positions);
        }
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("id", id);
        nbt.setInteger("size", size);
        nbt.setInteger("pos0X", pos0X);
        nbt.setInteger("pos0Y", pos0Y);
        nbt.setInteger("pos0Z", pos0Z);
        nbt.setInteger("pos1X", pos1X);
        nbt.setInteger("pos1Y", pos1Y);
        nbt.setInteger("pos1Z", pos1Z);
        nbt.setInteger("posX", posX);
        nbt.setInteger("posY", posY);
        nbt.setInteger("posZ", posZ);
        nbt.setBoolean("isTypeForced", isTypeForced);
        nbt.setString("type", type);
        nbt.setBoolean("strictScan", strictScan);

        // 保存方块数据
        NBTTagCompound blocksNbt = new NBTTagCompound();
        for (Map.Entry<String, java.util.List<BlockPos>> entry : blocks.entrySet()) {
            NBTTagCompound posList = new NBTTagCompound();
            int i = 0;
            for (BlockPos pos : entry.getValue()) {
                NBTTagCompound posNbt = new NBTTagCompound();
                posNbt.setInteger("x", pos.getX());
                posNbt.setInteger("y", pos.getY());
                posNbt.setInteger("z", pos.getZ());
                posList.setTag(String.valueOf(i++), posNbt);
            }
            blocksNbt.setTag(entry.getKey(), posList);
        }
        nbt.setTag("blocks", blocksNbt);

        return nbt;
    }

    public BlockPos getPos0() {
        int margin = getBuildingType().getMargin();
        return new BlockPos(pos0X - margin, pos0Y - margin, pos0Z - margin);
    }

    public BlockPos getPos1() {
        int margin = getBuildingType().getMargin();
        return new BlockPos(pos1X + margin, pos1Y + margin, pos1Z + margin);
    }

    public BlockPos getCenter() {
        return new BlockPos((pos0X + pos1X) / 2, (pos0Y + pos1Y) / 2, (pos0Z + pos1Z) / 2);
    }

    public BlockPos getSourceBlock() {
        return new BlockPos(posX, posY, posZ);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTypeForced() {
        return isTypeForced;
    }

    public void setTypeForced(boolean forced) {
        this.isTypeForced = forced;
    }

    public BuildingType getBuildingType() {
        return BuildingTypes.getInstance().getBuildingType(type);
    }

    public Map<String, java.util.List<BlockPos>> getBlocks() {
        return blocks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean overlaps(Building b) {
        return pos1X > b.pos0X && pos0X < b.pos1X && pos1Y > b.pos0Y && pos0Y < b.pos1Y && pos1Z > b.pos0Z && pos0Z < b.pos1Z;
    }

    public boolean containsPos(net.minecraft.util.math.Vec3i pos) {
        if (getBuildingType().grouped()) {
            return pos.distanceSq(getCenter()) <= getBuildingType().mergeRange() * getBuildingType().mergeRange();
        }
        return pos.getX() >= pos0X && pos.getX() <= pos1X
               && pos.getY() >= pos0Y && pos.getY() <= pos1Y
               && pos.getZ() >= pos0Z && pos.getZ() <= pos1Z;
    }

    public boolean isIdentical(Building b) {
        return pos0X == b.pos0X && pos1X == b.pos1X && pos0Y == b.pos0Y && pos1Y == b.pos1Y && pos0Z == b.pos0Z && pos1Z == b.pos1Z;
    }

    public int getSize() {
        return size;
    }

    public long getLastScan() {
        return lastScan;
    }

    public void setLastScan(long lastScan) {
        this.lastScan = lastScan;
    }

    public boolean isStrictScan() {
        return strictScan;
    }

    public boolean isComplete() {
        BuildingType bt = getBuildingType();
        int minBlocks = bt.getMinBlocks();
        return minBlocks == 0 || getBlockCount() >= minBlocks;
    }

    public int getBlockCount() {
        return blocks.values().stream().mapToInt(java.util.List::size).sum();
    }

    public enum ValidationResult {
        OVERLAP,
        BLOCK_LIMIT,
        SIZE_LIMIT,
        NO_DOOR,
        TOO_SMALL,
        IDENTICAL,
        SUCCESS,
        INVALID_TYPE
    }
}
