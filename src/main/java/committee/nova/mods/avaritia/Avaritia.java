package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.init.compat.curios.AvaritiaCuriosPlugin;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.data.ModDataGen;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
@Mod(Const.MOD_ID)
public class Avaritia {

    public static IEventBus MOD_EVENT_BUS;

    public Avaritia(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;

        ModConfig.register(modContainer);


        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModArmorMaterial.ARMOR_MATERIALS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);
        ModTileEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModRecipeTypes.RECIPES.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModIngredients.INGREDIENT.register(modEventBus);

        modEventBus.addListener(this::constructMod);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(ModDataGen::gatherData);
    }

    private void constructMod(final FMLConstructModEvent event)
    {
        if(ModList.get().isLoaded("curios")) {
            MOD_EVENT_BUS.addListener(AvaritiaCuriosPlugin::registerCapabilities);
        }
    }

    public void setup(final FMLCommonSetupEvent event) {
        //if (Const.isLoad("projecte")) ModEMCHandler.init();
        SingularityDataManager.onCommonSetup();
        DispenserBlock.registerProjectileBehavior(ModItems.endest_pearl.get());
    }

}
