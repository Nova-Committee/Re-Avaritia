package committee.nova.mods.avaritia.init.compat.equivox;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import com.yaskulsky.equivox.api.mapper.EMCMapper;
import com.yaskulsky.equivox.api.mapper.IEMCMapper;
import com.yaskulsky.equivox.api.mapper.collector.IMappingCollector;
import com.yaskulsky.equivox.api.nss.NormalizedSimpleStack;
import com.yaskulsky.equivox.api.nss.NSSItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;

/** Discovered by Equivox's annotation scanner, not by Avaritia's entrypoint. */
@EMCMapper(requiredMods = "equivox")
public final class ModEMCHandler implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return Const.MOD_ID + "_emc_mapper";
    }

    @Override
    public String getTranslationKey() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Registers Avaritia's configured base EMC values.";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> collector,
                            ReloadableServerResources resources, RegistryAccess registries,
                            ResourceManager resourceManager) {
        collector.setValueBefore(NSSItem.createItem(ModItems.neutron_pile.get()), (long) ModConfig.neutronPileEmc.get());
        collector.setValueBefore(NSSItem.createItem(ModItems.blaze_cube.get()), (long) ModConfig.blazeCubeEmc.get());
        collector.setValueBefore(NSSItem.createItem(Items.TOTEM_OF_UNDYING), (long) ModConfig.vanillaTotemEmc.get());
        collector.setValueBefore(NSSItem.createItem(ModItems.full_matter_cluster.get()), 0L);
    }
}
