package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.imc.IMCMethods;
import moze_intel.projecte.api.nss.NSSItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.InterModComms;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/7/15 下午11:58
 * @Description:
 */
public final class ModEMCHandler {
    private ModEMCHandler() {
    }

    public static void enqueue() {
        register(ModItems.neutron_pile.get(), ModConfig.neutronPileEmc.get());
        register(ModItems.blaze_cube.get(), ModConfig.blazeCubeEmc.get());
        register(Items.TOTEM_OF_UNDYING, ModConfig.vanillaTotemEmc.get());
        register(ModItems.full_matter_cluster.get(), 0L);
    }

    private static void register(ItemLike item, long emc) {
        InterModComms.sendTo(
                ProjectEAPI.PROJECTE_MODID,
                IMCMethods.REGISTER_CUSTOM_EMC,
                () -> new CustomEMCRegistration(NSSItem.createItem(item), emc)
        );
    }
}
