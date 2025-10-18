package mca.util.proxy;

import mca.api.API;
import mca.client.render.RenderReaperFactory;
import mca.client.render.RenderVillagerFactory;
import mca.core.Localizer;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityVillagerMCA;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    private Localizer localizer;

    @Override
    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVillagerMCA.class, RenderVillagerFactory.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrimReaper.class, RenderReaperFactory.INSTANCE);

        // 在客户端初始化Localizer
        localizer = new Localizer();

        // 在客户端初始化API的客户端数据
        API.initClientData();
    }

    @Override
    public void registerModelMeshers() {
        ItemsMCA.registerModelMeshers();
        BlocksMCA.registerModelMeshers();
    }

    @Override
    public Localizer getLocalizer() {
        return localizer;
    }
}