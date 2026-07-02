package mca.client.render;

import mca.core.MCA;
import mca.enums.EnumGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class ZombieVillagerMCATextureCache {
    private static final int TEXTURE_SIZE = 64;
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();
    private static boolean reloadListenerRegistered;

    private ZombieVillagerMCATextureCache() {
    }

    public static ResourceLocation getZombieTexture(String originalTexture, EnumGender gender, ResourceLocation fallback) {
        ensureReloadListener();

        if (originalTexture == null || originalTexture.trim().isEmpty() || originalTexture.endsWith("/zombievillager.png")) {
            return fallback;
        }

        String cacheKey = originalTexture + "|" + gender.getId();
        ResourceLocation cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            ResourceLocation originalLocation = new ResourceLocation(originalTexture);
            BufferedImage original = readTexture(originalLocation);
            BufferedImage zombie = createZombieTexture(original, cacheKey);
            ResourceLocation generated = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("mca_zombie_villager", new DynamicTexture(zombie));
            CACHE.put(cacheKey, generated);
            return generated;
        } catch (IOException | RuntimeException e) {
            MCA.getLog().warn("Could not create MCA zombie villager texture from " + originalTexture + ". Falling back to " + fallback, e);
            CACHE.put(cacheKey, fallback);
            return fallback;
        }
    }

    private static BufferedImage readTexture(ResourceLocation location) throws IOException {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        try (IResource resource = resourceManager.getResource(location)) {
            BufferedImage image = ImageIO.read(resource.getInputStream());
            if (image == null) {
                throw new IOException("Texture is not a readable image: " + location);
            }
            return image;
        }
    }

    private static BufferedImage createZombieTexture(BufferedImage original, String seedText) {
        BufferedImage image = new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(original, 0, 0, TEXTURE_SIZE, Math.min(TEXTURE_SIZE, original.getHeight()), null);
        graphics.dispose();

        int seed = seedText.hashCode();
        for (int y = 0; y < TEXTURE_SIZE; y++) {
            for (int x = 0; x < TEXTURE_SIZE; x++) {
                int argb = image.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha < 8) {
                    continue;
                }

                int red = (argb >> 16) & 255;
                int green = (argb >> 8) & 255;
                int blue = argb & 255;
                int transformed = isSkinLike(red, green, blue)
                        ? zombieSkinPixel(red, green, blue, x, y, seed)
                        : decayedClothingPixel(red, green, blue, x, y, seed);

                image.setRGB(x, y, (alpha << 24) | transformed);
            }
        }

        return image;
    }

    private static int zombieSkinPixel(int red, int green, int blue, int x, int y, int seed) {
        int noise = noise(x, y, seed);
        float moss = (noise & 255) / 255.0F;
        float targetRed = 72.0F + moss * 34.0F;
        float targetGreen = 118.0F + moss * 48.0F;
        float targetBlue = 70.0F + moss * 24.0F;
        float mix = 0.58F;

        int r = (int)(red * (1.0F - mix) + targetRed * mix);
        int g = (int)(green * (1.0F - mix) + targetGreen * mix);
        int b = (int)(blue * (1.0F - mix) + targetBlue * mix);
        return addRotDamage(r, g, b, x, y, seed);
    }

    private static int decayedClothingPixel(int red, int green, int blue, int x, int y, int seed) {
        int noise = noise(x, y, seed ^ 0x5f3759df);
        float gray = (red * 0.30F + green * 0.59F + blue * 0.11F);
        float desaturate = 0.24F;
        float moss = ((noise >>> 8) & 255) / 255.0F;
        float mossMix = moss > 0.67F ? 0.18F : 0.06F;

        int r = (int)((red * (1.0F - desaturate) + gray * desaturate) * 0.78F);
        int g = (int)((green * (1.0F - desaturate) + gray * desaturate) * 0.78F);
        int b = (int)((blue * (1.0F - desaturate) + gray * desaturate) * 0.78F);

        r = (int)(r * (1.0F - mossMix) + 64.0F * mossMix);
        g = (int)(g * (1.0F - mossMix) + 102.0F * mossMix);
        b = (int)(b * (1.0F - mossMix) + 58.0F * mossMix);
        return addRotDamage(r, g, b, x, y, seed);
    }

    private static int addRotDamage(int red, int green, int blue, int x, int y, int seed) {
        int crack = noise(x / 2, y / 2, seed ^ 0x1b873593) & 255;
        float shade = crack > 214 ? 0.55F : 0.88F + ((noise(x, y, seed ^ 0x85ebca6b) & 31) / 255.0F);
        int r = clamp((int)(red * shade));
        int g = clamp((int)(green * shade));
        int b = clamp((int)(blue * shade));
        return (r << 16) | (g << 8) | b;
    }

    private static boolean isSkinLike(int red, int green, int blue) {
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        boolean warmHue = hsb[0] < 0.15F || hsb[0] > 0.93F;
        return warmHue && hsb[1] > 0.12F && hsb[1] < 0.72F && hsb[2] > 0.22F && red >= green && green >= blue * 0.72F;
    }

    private static int noise(int x, int y, int seed) {
        int value = seed;
        value ^= x * 0x27d4eb2d;
        value ^= y * 0x165667b1;
        value ^= value >>> 15;
        value *= 0x85ebca6b;
        value ^= value >>> 13;
        value *= 0xc2b2ae35;
        return value ^ (value >>> 16);
    }

    private static int clamp(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    private static void ensureReloadListener() {
        if (reloadListenerRegistered) {
            return;
        }

        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager)resourceManager).registerReloadListener(new IResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(IResourceManager manager) {
                    CACHE.clear();
                }
            });
        }

        reloadListenerRegistered = true;
    }
}
