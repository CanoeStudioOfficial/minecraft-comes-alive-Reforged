package mca.client.render;

import mca.entity.EntityZombieVillagerMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderZombieVillagerFactory implements IRenderFactory<EntityZombieVillagerMCA> {
    public static final RenderZombieVillagerFactory INSTANCE = new RenderZombieVillagerFactory();

    @Override
    public Render<? super EntityZombieVillagerMCA> createRenderFor(RenderManager manager) {
        return new RenderZombieVillagerMCA(manager);
    }
}
