package committee.nova.mods.avaritia.api.common.item.iface;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/21 01:45
 * @Description:
 */
@FunctionalInterface
public interface IItemCapability {
    void attachCapabilities(RegisterCapabilitiesEvent event);
}
