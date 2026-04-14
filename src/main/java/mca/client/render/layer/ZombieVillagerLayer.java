package mca.client.render.layer;

import mca.client.render.RenderZombieVillagerMCA;
import mca.entity.EntityZombieVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public abstract class ZombieVillagerLayer implements LayerRenderer<EntityZombieVillagerMCA> {
    protected static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();
    protected final RenderZombieVillagerMCA renderer;
    protected final ModelBiped model;

    public ZombieVillagerLayer(RenderZombieVillagerMCA renderer, ModelBiped model) {
        this.renderer = renderer;
        this.model = model;
    }

    @Override
    public void doRenderLayer(EntityZombieVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean visible = !villager.isInvisible();
        boolean glowing = villager.isGlowing();

        // Copy animation from parent model
        renderer.getMainModel().setModelAttributes(model);

        renderLayer(villager, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, visible, glowing);
    }

    protected abstract void renderLayer(EntityZombieVillagerMCA villager, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean visible, boolean glowing);

    protected ResourceLocation getTexture(EntityZombieVillagerMCA villager) {
        return null;
    }

    protected int getColor(EntityZombieVillagerMCA villager, float partialTicks) {
        return 0xFFFFFFFF;
    }

    protected ResourceLocation cached(String path) {
        return TEXTURE_CACHE.computeIfAbsent(path, ResourceLocation::new);
    }

    protected boolean canUse(ResourceLocation texture) {
        if (texture == null) return false;
        try {
            return Minecraft.getMinecraft().getResourceManager().getResource(texture) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
