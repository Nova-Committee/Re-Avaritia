package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class AvaritiaRegistriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap);

    private AvaritiaRegistriesProvider() {
    }

    public static void register(GatherDataEvent.Client event) {
        // DamageType 属于数据包动态注册表，交给 NeoForge 生成内置 datapack 条目。
        event.createDatapackRegistryObjects(BUILDER);
    }
}
