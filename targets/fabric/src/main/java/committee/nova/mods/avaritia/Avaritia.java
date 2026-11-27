package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

/** Loader-only common bootstrap; gameplay and lifecycle callbacks are native mixins. */
public final class Avaritia implements ModInitializer {
    @Override
    public void onInitialize() {
        NetworkHandler.init();
        TrinketsIntegration.initialize();
        DispenserBlock.registerBehavior(ModItems.endest_pearl.get(), new AbstractProjectileDispenseBehavior() {
            @Override
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position position, @NotNull ItemStack stack) {
                return Util.make(new EndestPearlEntity(level, position.x(), position.y(), position.z()), entity -> entity.setItem(stack));
            }
        });
    }
}
