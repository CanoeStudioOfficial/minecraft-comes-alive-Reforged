package mca.util.proxy;

import mca.client.render.RenderReaperFactory;
import mca.client.render.RenderVillagerFactory;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVillagerMCA.class, RenderVillagerFactory.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, RenderReaperFactory.INSTANCE);
    }

    @Override
    public void registerModelMeshers() {
        ItemsMCA.registerModelMeshers();
        BlocksMCA.registerModelMeshers();
    }
}
