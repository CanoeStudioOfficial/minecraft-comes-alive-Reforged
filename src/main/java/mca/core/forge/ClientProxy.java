package mca.core.forge;

import mca.client.render.RenderReaperFactory;
import mca.client.render.RenderVillagerFactory;
import mca.client.render.TileEntityTombstoneRenderer;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import mca.tile.TileTombstone;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy {
    @Override
    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVillagerMCA.class, RenderVillagerFactory.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, RenderReaperFactory.INSTANCE);
    }

    @Override
    public void registerTileEntityRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new TileEntityTombstoneRenderer());
    }

    @Override
    public void registerModelMeshers() {
        ItemsMCA.registerModelMeshers();
        BlocksMCA.registerModelMeshers();
    }
}
