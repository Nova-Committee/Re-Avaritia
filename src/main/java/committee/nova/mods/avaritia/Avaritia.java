package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.data.ModDataGen;
import committee.nova.mods.avaritia.init.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
@Mod(Const.MOD_ID)
public class Avaritia {

    public Avaritia(IEventBus modEventBus, ModContainer modContainer) {
        ModConfig.register(modContainer);


        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModArmorMaterial.ARMOR_MATERIALS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        ModTileEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipeTypes.RECIPES.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModIngredients.INGREDIENT.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(ModDataGen::gatherData);
    }

    public void setup(final FMLCommonSetupEvent event) {
        //if (Static.isLoad("projecte")) ModEMCHandler.init();
//        SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
        //DispenserBlock.registerProjectileBehavior(ModItems.endest_pearl.get());
    }

}
