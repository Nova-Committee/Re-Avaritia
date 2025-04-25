package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.init.compat.projecte.ModEMCHandler;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.data.ModDataGen;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 10:10
 * Version: 1.0
 */
@Mod(Static.MOD_ID)
public class Avaritia {

    public Avaritia(IEventBus modEventBus, ModContainer modContainer) {
        ModConfig.register(modContainer);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(ModDataGen::gatherData);

        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        ModTileEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipeTypes.RECIPES.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModIngredients.INGREDIENT.register(modEventBus);

    }

    public void setup(final FMLCommonSetupEvent event) {
        if (Static.isLoad("projecte")) ModEMCHandler.init();
        SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
        DispenserBlock.registerProjectileBehavior(ModItems.endest_pearl.get());
    }

}
