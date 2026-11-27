package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@EMCMapper
public class ModEMCHandler implements IEMCMapper<NSSItem, Long> {

    private static void registerEMC(IMappingCollector<NSSItem, Long> collector, ItemLike item, long emcValue) {
        if (emcValue < 0) {
            throw new IllegalArgumentException("EMC 值不能为负数: " + emcValue);
        }
        collector.setValueBefore(NSSItem.createItem(item), emcValue);
    }

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
        return "Registers EMC values for Avaritia mod items (neutron pile, totem, bedrock etc.)";
    }

    @Override
    public void addMappings(IMappingCollector<NSSItem, Long> collector,
                            ReloadableServerResources serverResources,
                            RegistryAccess registries,
                            ResourceManager resourceManager) {

        registerEMC(collector, ModItems.neutron_pile.get(), ModConfig.neutronPileEmc.get());
        registerEMC(collector, ModItems.blaze_cube.get(), ModConfig.blazeCubeEmc.get());
        registerEMC(collector, Items.TOTEM_OF_UNDYING, ModConfig.vanillaTotemEmc.get());
        registerEMC(collector, ModItems.full_matter_cluster.get(), 0L);
    }

}
