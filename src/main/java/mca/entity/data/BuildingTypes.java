package mca.entity.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mca.core.MCA;
import net.minecraft.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 建筑类型管理类
 * 对应1.21.1版本的BuildingTypes
 */
public class BuildingTypes implements Iterable<BuildingType> {
    private static final String DATA_PATH = "assets/mca/building_types/";
    private static BuildingTypes INSTANCE = new BuildingTypes();
    private final Map<String, BuildingType> buildingTypes = new HashMap<>();

    public BuildingTypes() {
        INSTANCE = this;
        loadDefaultTypes();
    }

    public static BuildingTypes getInstance() {
        return INSTANCE;
    }

    private void loadDefaultTypes() {
        // 加载默认建筑类型
        String[] defaultTypes = {
            "building", "house", "storage", "library", "inn", "graveyard", "town_center",
            "armorer", "armory", "bakery", "big_house", "blacksmith", "blocked",
            "bookkeeper", "butcher", "cartographer", "fishermans_hut", "fletcher",
            "infirmary", "leatherworker", "mason", "music_store", "prison",
            "toolsmith", "weaponsmith", "weaving_mill"
        };

        for (String type : defaultTypes) {
            try {
                InputStream stream = getClass().getClassLoader().getResourceAsStream(DATA_PATH + type + ".json");
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    JsonObject json = JsonUtils.fromJson(new Gson(), reader, JsonObject.class);
                    if (json != null) {
                        buildingTypes.put(type, new BuildingType(type, json));
                    }
                    reader.close();
                    stream.close();
                }
            } catch (IOException e) {
                MCA.getLog().error("Failed to load building type: " + type, e);
            }
        }

        // 如果没有任何类型被加载，创建默认类型
        if (buildingTypes.isEmpty()) {
            createDefaultTypes();
        }
    }

    private void createDefaultTypes() {
        // 创建基本的默认建筑类型
        MCA.getLog().info("Creating default building types...");

        // 默认建筑类型
        JsonObject buildingJson = new JsonObject();
        buildingJson.addProperty("color", "ffffffff");
        buildingJson.addProperty("visible", false);
        buildingJson.addProperty("priority", 1);
        buildingTypes.put("building", new BuildingType("building", buildingJson));

        // 住宅
        JsonObject houseJson = new JsonObject();
        houseJson.addProperty("color", "ffffffff");
        houseJson.addProperty("visible", false);
        houseJson.addProperty("priority", 1);
        JsonObject houseBlocks = new JsonObject();
        houseBlocks.addProperty("#minecraft:beds", 1);
        houseJson.add("blocks", houseBlocks);
        buildingTypes.put("house", new BuildingType("house", houseJson));

        // 仓库
        JsonObject storageJson = new JsonObject();
        storageJson.addProperty("color", "ff0000ff");
        storageJson.addProperty("visible", true);
        storageJson.addProperty("priority", 4);
        JsonObject storageBlocks = new JsonObject();
        storageBlocks.addProperty("minecraft:lectern", 1);
        storageBlocks.addProperty("#mca:chests", 4);
        storageJson.add("blocks", storageBlocks);
        storageJson.addProperty("iconU", 3);
        storageJson.addProperty("iconV", 1);
        buildingTypes.put("storage", new BuildingType("storage", storageJson));

        // 图书馆
        JsonObject libraryJson = new JsonObject();
        libraryJson.addProperty("color", "ffffff00");
        libraryJson.addProperty("visible", true);
        libraryJson.addProperty("priority", 3);
        JsonObject libraryBlocks = new JsonObject();
        libraryBlocks.addProperty("minecraft:bookshelf", 8);
        libraryJson.add("blocks", libraryBlocks);
        libraryJson.addProperty("iconU", 2);
        libraryJson.addProperty("iconV", 0);
        buildingTypes.put("library", new BuildingType("library", libraryJson));

        // 旅馆
        JsonObject innJson = new JsonObject();
        innJson.addProperty("color", "ffffa500");
        innJson.addProperty("visible", true);
        innJson.addProperty("priority", 2);
        JsonObject innBlocks = new JsonObject();
        innBlocks.addProperty("#minecraft:beds", 4);
        innJson.add("blocks", innBlocks);
        innJson.addProperty("iconU", 4);
        innJson.addProperty("iconV", 0);
        buildingTypes.put("inn", new BuildingType("inn", innJson));

        // 墓地
        JsonObject graveyardJson = new JsonObject();
        graveyardJson.addProperty("margin", 8);
        graveyardJson.addProperty("color", "ffff00ff");
        graveyardJson.addProperty("visible", true);
        graveyardJson.addProperty("priority", 0);
        JsonObject graveyardBlocks = new JsonObject();
        graveyardBlocks.addProperty("#mca:tombstones", 3);
        graveyardJson.add("blocks", graveyardBlocks);
        graveyardJson.addProperty("icon", true);
        graveyardJson.addProperty("iconU", 0);
        graveyardJson.addProperty("iconV", 0);
        graveyardJson.addProperty("grouped", true);
        graveyardJson.addProperty("mergeRange", 24);
        buildingTypes.put("graveyard", new BuildingType("graveyard", graveyardJson));

        // 市政厅
        JsonObject townCenterJson = new JsonObject();
        townCenterJson.addProperty("margin", 8);
        townCenterJson.addProperty("color", "ffff00ff");
        townCenterJson.addProperty("visible", false);
        townCenterJson.addProperty("priority", 0);
        JsonObject tcBlocks = new JsonObject();
        tcBlocks.addProperty("#mca:town_center", 1);
        townCenterJson.add("blocks", tcBlocks);
        townCenterJson.addProperty("icon", true);
        townCenterJson.addProperty("iconU", 1);
        townCenterJson.addProperty("iconV", 0);
        townCenterJson.addProperty("grouped", true);
        townCenterJson.addProperty("mergeRange", 32);
        buildingTypes.put("town_center", new BuildingType("town_center", townCenterJson));

        // 铁匠铺
        JsonObject blacksmithJson = new JsonObject();
        blacksmithJson.addProperty("color", "ff808080");
        blacksmithJson.addProperty("visible", true);
        blacksmithJson.addProperty("priority", 2);
        JsonObject bsBlocks = new JsonObject();
        bsBlocks.addProperty("minecraft:furnace", 1);
        bsBlocks.addProperty("minecraft:anvil", 1);
        blacksmithJson.add("blocks", bsBlocks);
        blacksmithJson.addProperty("iconU", 0);
        blacksmithJson.addProperty("iconV", 1);
        buildingTypes.put("blacksmith", new BuildingType("blacksmith", blacksmithJson));

        // 面包店
        JsonObject bakeryJson = new JsonObject();
        bakeryJson.addProperty("color", "ffffd700");
        bakeryJson.addProperty("visible", true);
        bakeryJson.addProperty("priority", 2);
        JsonObject bakeryBlocks = new JsonObject();
        bakeryBlocks.addProperty("minecraft:furnace", 1);
        bakeryJson.add("blocks", bakeryBlocks);
        bakeryJson.addProperty("iconU", 1);
        bakeryJson.addProperty("iconV", 1);
        buildingTypes.put("bakery", new BuildingType("bakery", bakeryJson));
    }

    public Map<String, BuildingType> getBuildingTypes() {
        return buildingTypes;
    }

    public BuildingType getBuildingType(String type) {
        return buildingTypes.getOrDefault(type, buildingTypes.get("building"));
    }

    @Override
    public Iterator<BuildingType> iterator() {
        return buildingTypes.values().iterator();
    }
}
