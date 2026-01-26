package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.config.CustomEMCParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@EMCMapper
public class ModEMCHandler implements IEMCMapper<NSSItem, Long> {

    private static void registerEMC(ItemLike item, long emcValue, RegistryAccess registries) {

        if (emcValue < 0) {
            throw new IllegalArgumentException("EMC 值不能为负数: " + emcValue);
        }

        NSSItem nssItem = NSSItem.createItem(item);

        CustomEMCParser.addToFile(nssItem, emcValue);

        CustomEMCParser.flush(registries);

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

        registerEMC(ModItems.neutron_pile.get(), ModConfig.neutronPileEmc.get(), registries);
        registerEMC(ModItems.blaze_cube.get(), ModConfig.blazeCubeEmc.get(), registries);
        registerEMC(Items.TOTEM_OF_UNDYING, ModConfig.vanillaTotemEmc.get(), registries);
        registerEMC(Items.BEDROCK, ModConfig.bedrockEmc.get(), registries);

        NSSItem neutronPile = NSSItem.createItem(ModItems.neutron_pile.get());
        NSSItem blaze_cube = NSSItem.createItem(ModItems.blaze_cube.get());
        NSSItem totem = NSSItem.createItem(Items.TOTEM_OF_UNDYING);
        NSSItem bedrock = NSSItem.createItem(Items.BEDROCK);

        collector.setValueBefore(neutronPile, ModConfig.neutronPileEmc.get().longValue());
        collector.setValueBefore(blaze_cube, ModConfig.blazeCubeEmc.get().longValue());
        collector.setValueBefore(totem, ModConfig.vanillaTotemEmc.get().longValue());
        collector.setValueBefore(bedrock, ModConfig.bedrockEmc.get().longValue());
    }

}