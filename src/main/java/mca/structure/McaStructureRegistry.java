package mca.structure;

import com.google.gson.Gson;
import mca.core.MCA;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads MCA's independent building definitions from assets/mca/structures. */
public final class McaStructureRegistry {
    private static final String[] STRUCTURE_IDS = {
            "bachelor",
            "destiny-test",
            "family",
            "melon1",
            "mine1",
            "player-family-house",
            "sugarcane1",
            "village1",
            "wheat1"
    };

    private static final Map<String, McaStructure> STRUCTURES = new LinkedHashMap<>();
    private static boolean loaded;

    private McaStructureRegistry() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        Gson gson = new Gson();
        for (String id : STRUCTURE_IDS) {
            String resource = "/assets/mca/structures/" + id + ".json";
            try (InputStream stream = MCA.class.getResourceAsStream(resource)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing MCA structure resource: " + resource);
                }

                McaStructure structure = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), McaStructure.class);
                if (structure == null || structure.getId() == null || structure.getResolvedBlocks().isEmpty()) {
                    throw new IllegalStateException("Empty or invalid MCA structure resource: " + resource);
                }
                STRUCTURES.put(structure.getId(), structure);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load MCA structure: " + resource, e);
            }
        }
        loaded = true;
    }

    public static McaStructure get(String id) {
        load();
        return STRUCTURES.get(id);
    }

    public static Collection<McaStructure> all() {
        load();
        return Collections.unmodifiableCollection(new ArrayList<>(STRUCTURES.values()));
    }
}
