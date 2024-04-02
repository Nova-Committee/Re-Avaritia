package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.data.ModDataGen;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.*;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 10:10
 * Version: 1.0
 */
@Mod(Static.MOD_ID)
public class Avaritia {

    public Avaritia() {
        ModConfig.register();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::onClientSetup);
        bus.addListener(ModDataGen::gatherData);

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModTileEntities.BLOCK_ENTITIES.register(bus);
        ModMenus.MENUS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModRecipeTypes.RECIPES.register(bus);
        ModRecipeSerializers.SERIALIZERS.register(bus);
        ModCreativeModeTabs.TABS.register(bus);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AvaritiaShaders::init);
    }

    public void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
        DispenserBlock.registerBehavior(ModItems.endest_pearl.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position position, @NotNull ItemStack stack) {
                return Util.make(new EndestPearlEntity(level, position.x(), position.y(), position.z()), (entity) -> entity.setItem(stack));
            }
        });
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ModEntities.onClientSetup();
        ModMenus.onClientSetup();
        ModTileEntities.onClientSetup();
    }






}
