package mca.util.proxy;

import mca.core.Localizer;

public class CommonProxy {
    public void registerEntityRenderers() {
        // Server-side, no rendering.
    }

    public void registerEventHandlers() {

    }

    public void registerModelMeshers() {

    }

    // 服务端返回null，因为服务端不需要本地化
    public Localizer getLocalizer() {
        return null;
    }

    // 服务端不需要初始化API的客户端数据
    public void initAPIClientData() {
        // 服务端空实现
    }
}