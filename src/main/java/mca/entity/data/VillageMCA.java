package mca.entity.data;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

/**
 * MCA村庄数据类
 * 对应1.21.1版本的Village
 */
public class VillageMCA extends WorldSavedData implements Iterable<Building> {
    public static final int PLAYER_BORDER_MARGIN = 32;
    public static final int BORDER_MARGIN = 48;
    public static final int MERGE_MARGIN = 64;
    private static final int MOVE_IN_COOLDOWN = 1200;

    private final Map<Integer, Building> buildings = new HashMap<>();
    private final int id;

    private String name = "Village";
    private Map<UUID, Map<UUID, Integer>> reputation = new HashMap<>();
    private int beds;
    private Map<UUID, String> residentNames = new HashMap<>();
    private Map<UUID, Long> residentHomes = new HashMap<>();
    private float taxes = 0;
    private float populationThreshold = 0.75f;
    private float marriageThreshold = 0.5f;
    private boolean autoScan = true;

    // 边界框
    private int boxMinX, boxMinY, boxMinZ;
    private int boxMaxX, boxMaxY, boxMaxZ;

    public VillageMCA(int id) {
        super("MCA_Village_" + id);
        this.id = id;
    }

    public VillageMCA(NBTTagCompound nbt) {
        super("MCA_Village_" + nbt.getInteger("id"));
        this.id = nbt.getInteger("id");
        readFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        name = nbt.getString("name");
        taxes = nbt.getFloat("taxesFloat");
        beds = nbt.getInteger("beds");
        populationThreshold = nbt.getFloat("populationThresholdFloat");
        marriageThreshold = nbt.getFloat("marriageThresholdFloat");
        autoScan = nbt.getBoolean("autoScan");

        boxMinX = nbt.getInteger("boxMinX");
        boxMinY = nbt.getInteger("boxMinY");
        boxMinZ = nbt.getInteger("boxMinZ");
        boxMaxX = nbt.getInteger("boxMaxX");
        boxMaxY = nbt.getInteger("boxMaxY");
        boxMaxZ = nbt.getInteger("boxMaxZ");

        // 读取建筑
        NBTTagList buildingList = nbt.getTagList("buildings", 10);
        for (int i = 0; i < buildingList.tagCount(); i++) {
            Building building = new Building(buildingList.getCompoundTagAt(i));
            buildings.put(building.getId(), building);
        }

        // 读取居民
        NBTTagCompound residentsNbt = nbt.getCompoundTag("residentNames");
        for (String key : residentsNbt.getKeySet()) {
            residentNames.put(UUID.fromString(key), residentsNbt.getString(key));
        }

        NBTTagCompound homesNbt = nbt.getCompoundTag("residentHomes");
        for (String key : homesNbt.getKeySet()) {
            residentHomes.put(UUID.fromString(key), homesNbt.getLong(key));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("id", id);
        nbt.setString("name", name);
        nbt.setFloat("taxesFloat", taxes);
        nbt.setInteger("beds", beds);
        nbt.setFloat("populationThresholdFloat", populationThreshold);
        nbt.setFloat("marriageThresholdFloat", marriageThreshold);
        nbt.setBoolean("autoScan", autoScan);

        nbt.setInteger("boxMinX", boxMinX);
        nbt.setInteger("boxMinY", boxMinY);
        nbt.setInteger("boxMinZ", boxMinZ);
        nbt.setInteger("boxMaxX", boxMaxX);
        nbt.setInteger("boxMaxY", boxMaxY);
        nbt.setInteger("boxMaxZ", boxMaxZ);

        // 保存建筑
        NBTTagList buildingList = new NBTTagList();
        for (Building building : buildings.values()) {
            buildingList.appendTag(building.toNBT());
        }
        nbt.setTag("buildings", buildingList);

        // 保存居民
        NBTTagCompound residentsNbt = new NBTTagCompound();
        for (Map.Entry<UUID, String> entry : residentNames.entrySet()) {
            residentsNbt.setString(entry.getKey().toString(), entry.getValue());
        }
        nbt.setTag("residentNames", residentsNbt);

        NBTTagCompound homesNbt = new NBTTagCompound();
        for (Map.Entry<UUID, Long> entry : residentHomes.entrySet()) {
            homesNbt.setLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.setTag("residentHomes", homesNbt);

        return nbt;
    }

    @Override
    public Iterator<Building> iterator() {
        return buildings.values().iterator();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public float getTaxes() {
        return taxes;
    }

    public void setTaxes(float taxes) {
        this.taxes = taxes;
        markDirty();
    }

    public float getPopulationThreshold() {
        return populationThreshold;
    }

    public void setPopulationThreshold(float threshold) {
        this.populationThreshold = threshold;
        markDirty();
    }

    public float getMarriageThreshold() {
        return marriageThreshold;
    }

    public void setMarriageThreshold(float threshold) {
        this.marriageThreshold = threshold;
        markDirty();
    }

    public boolean isAutoScan() {
        return autoScan;
    }

    public void setAutoScan(boolean autoScan) {
        this.autoScan = autoScan;
        markDirty();
    }

    public void toggleAutoScan() {
        setAutoScan(!isAutoScan());
    }

    public Map<Integer, Building> getBuildings() {
        return buildings;
    }

    public Optional<Building> getBuilding(int id) {
        return Optional.ofNullable(buildings.get(id));
    }

    public void removeBuilding(int id) {
        buildings.remove(id);
        calculateDimensions();
        markDirty();
    }

    public boolean hasBuilding(String type) {
        return buildings.values().stream().anyMatch(b -> b.getType().equals(type) && b.isComplete());
    }

    public java.util.List<Building> getBuildingsOfType(String type) {
        return buildings.values().stream()
                .filter(b -> b.getType().equals(type))
                .collect(java.util.stream.Collectors.toList());
    }

    public int getPopulation() {
        return residentNames.size();
    }

    public int getMaxPopulation() {
        return beds > 0 ? beds : buildings.size() * 2;
    }

    public boolean hasSpace() {
        return getPopulation() < getMaxPopulation();
    }

    public BlockPos getCenter() {
        return new BlockPos((boxMinX + boxMaxX) / 2, (boxMinY + boxMaxY) / 2, (boxMinZ + boxMaxZ) / 2);
    }

    public void calculateDimensions() {
        if (buildings.isEmpty()) return;

        boxMinX = Integer.MAX_VALUE;
        boxMinY = Integer.MAX_VALUE;
        boxMinZ = Integer.MAX_VALUE;
        boxMaxX = Integer.MIN_VALUE;
        boxMaxY = Integer.MIN_VALUE;
        boxMaxZ = Integer.MIN_VALUE;

        for (Building building : buildings.values()) {
            BlockPos pos0 = building.getPos0();
            BlockPos pos1 = building.getPos1();

            boxMinX = Math.min(boxMinX, pos0.getX());
            boxMinY = Math.min(boxMinY, pos0.getY());
            boxMinZ = Math.min(boxMinZ, pos0.getZ());
            boxMaxX = Math.max(boxMaxX, pos1.getX());
            boxMaxY = Math.max(boxMaxY, pos1.getY());
            boxMaxZ = Math.max(boxMaxZ, pos1.getZ());
        }

        markDirty();
    }

    public boolean isWithinBorder(BlockPos pos, int margin) {
        return pos.getX() >= boxMinX - margin && pos.getX() <= boxMaxX + margin
               && pos.getY() >= boxMinY - margin && pos.getY() <= boxMaxY + margin
               && pos.getZ() >= boxMinZ - margin && pos.getZ() <= boxMaxZ + margin;
    }

    public void addBuilding(Building building) {
        buildings.put(building.getId(), building);
        calculateDimensions();
        markDirty();
    }

    public void updateResident(UUID uuid, String name, BlockPos home) {
        residentNames.put(uuid, name);
        if (home != null) {
            residentHomes.put(uuid, home.toLong());
        }
        markDirty();
    }

    public void removeResident(UUID uuid) {
        residentNames.remove(uuid);
        residentHomes.remove(uuid);
        markDirty();
    }

    public Map<UUID, String> getResidentNames() {
        return residentNames;
    }

    public boolean isVillage() {
        return buildings.size() >= 3; // 默认需要3个建筑才算村庄
    }

    public void tick(World world, long time) {
        // 税收季节检查
        if (time % MCA.getConfig().taxSeason == 0 && hasBuilding("storage")) {
            collectTaxes(world);
        }
    }

    private void collectTaxes(World world) {
        // 简化版税收收集
        if (taxes <= 0) return;

        double taxAmount = taxes * getPopulation();

        // 根据税率产生不同的消息和效果
        if (taxes < 0.1f) {
            // 低税收 - 村民高兴
            MCA.getLog().info("Village " + name + ": Low taxes, villagers are happy");
        } else if (taxes > 0.7f) {
            // 高税收 - 村民不满
            MCA.getLog().info("Village " + name + ": High taxes, villagers are unhappy");
        }

        // 这里可以添加实际的物品收集逻辑
        MCA.getLog().info("Collected " + taxAmount + " taxes from " + name);
    }
}
