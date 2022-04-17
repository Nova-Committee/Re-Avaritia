package nova.committee.avaritia.init.proxy;

import nova.committee.avaritia.init.registry.ModMenus;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 12:41
 * Version: 1.0
 */
public class ClientProxy implements IProxy {
    @Override
    public void init() {
        ModMenus.onClientSetup();
    }
}
