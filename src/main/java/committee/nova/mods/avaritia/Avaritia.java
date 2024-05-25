package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.*;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.fabricmc.api.ModInitializer;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

public class Avaritia implements ModInitializer {
	@Override
	public void onInitialize() {
		ModConfig.register();
		this.setup();

		ModBlocks.init();
		ModItems.init();
		ModEntities.init();
		ModTileEntities.init();
		ModMenus.init();
		ModRecipeSerializers.init();
		ModRecipeTypes.init();
		ModCreativeModeTabs.init();


		RecipeUtil.init();
		AbilityHandler.init();
		InfinityHandler.init();
		DynamicRecipeHandler.init();
		ItemCaptureHandler.init();
		ResourceReloadHandler.init();
		SingularityRegistryHandler.init();
	}

	public void setup() {
		NetworkHandler.registerPackets();
		SingularityRegistryHandler.getInstance().writeDefaultSingularityFiles();
		DispenserBlock.registerBehavior(ModItems.endest_pearl.get(), new AbstractProjectileDispenseBehavior() {
			protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position position, @NotNull ItemStack stack) {
				return Util.make(new EndestPearlEntity(level, position.x(), position.y(), position.z()), (entity) -> entity.setItem(stack));
			}
		});
	}
}