package nova.committee.avaritia.init.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import nova.committee.avaritia.Static;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModTab {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Static.MOD_ID);
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = REGISTRY.register("creative_tab", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.pick_axe.getDefaultInstance())

            .build());

    public static CreativeModeTab TAB = CREATIVE_TAB.get();


}
