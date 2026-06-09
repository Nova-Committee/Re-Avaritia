package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.compat.curios.AvaritiaCuriosPlugin;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Avaritia — 无尽模组。
 * <p>
 * 主模组类，使用 NeoForge {@link Mod} 注解注册。
 */
@Mod(Const.MOD_ID)
public class Avaritia {
    public static IEventBus MOD_EVENT_BUS;

    @SuppressWarnings("unused")
    public Avaritia(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;
        // 注册模组配置
        ModConfig.register(modContainer);

        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.registerBlockItems();
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        ModTileEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModRecipeTypes.RECIPES.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModIngredients.INGREDIENT.register(modEventBus);

        modEventBus.addListener(this::constructMod);
        modEventBus.addListener(this::setup);
    }

    private void constructMod(final FMLConstructModEvent event)
    {
        if(ModList.get().isLoaded("curios")) {
            MOD_EVENT_BUS.addListener(AvaritiaCuriosPlugin::registerCapabilities);
        }
    }

    public void setup(final FMLCommonSetupEvent event) {
//        if (Const.isLoad("projecte")) ModEMCHandler.init();
        DispenserBlock.registerProjectileBehavior(ModItems.endest_pearl.get());
    }
}
