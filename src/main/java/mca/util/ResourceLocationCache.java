package mca.util;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ResourceLocationCache {
    private static final String DEFAULT_TEXTURE = "mca:textures/entity/villager/male/brown/base.png";
    private static Map<String, ResourceLocation> cache = new HashMap<>();

    public static ResourceLocation getResourceLocationFor(String location) {
        // 如果location为空或空字符串，返回默认贴图
        if (location == null || location.isEmpty()) {
            location = DEFAULT_TEXTURE;
        }

        if (cache.containsKey(location)) {
            return cache.get(location);
        } else {
            ResourceLocation rLoc = new ResourceLocation(location);
            cache.put(location, rLoc);
            return rLoc;
        }
    }
}
