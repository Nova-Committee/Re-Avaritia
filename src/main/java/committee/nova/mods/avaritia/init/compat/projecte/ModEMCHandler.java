package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModItems;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/15 下午11:58
 * @Description:
 */
public class ModEMCHandler {
    private static void registerEMC(ItemLike item, int emc) {
        APICustomEMCMapper.INSTANCE.registerCustomEMC(Static.MOD_ID, new CustomEMCRegistration(NSSItem.createItem(item), emc));
    }
    public static void init() {
        registerEMC(ModItems.neutron_pile.get(), 100);
        registerEMC(Items.TOTEM_OF_UNDYING, 1000);
    }
}
