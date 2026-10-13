package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

/** Loader-only common bootstrap; gameplay and lifecycle callbacks are native mixins. */
public final class Avaritia implements ModInitializer {
    @Override
    public void onInitialize() {
        ModRegistries.initialize();
        NetworkHandler.init();
        TrinketsIntegration.initialize();
        // 26.3 移除 AbstractProjectileDispenseBehavior，改为 DefaultDispenseItemBehavior + spawnProjectileUsingShoot
        DispenserBlock.registerBehavior(ModItems.endest_pearl.get(), new DefaultDispenseItemBehavior() {
            @Override
            public @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
                ServerLevel level = source.level();
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                Position position = DispenserBlock.getDispensePosition(source);
                Projectile.spawnProjectileUsingShoot(
                        Util.make(new EndestPearlEntity(level, position.x(), position.y(), position.z()), entity -> entity.setItem(stack)),
                        level,
                        stack,
                        direction.getStepX(),
                        direction.getStepY(),
                        direction.getStepZ(),
                        1.1F,
                        6.0F
                );
                stack.shrink(1);
                return stack;
            }
        });
    }
}
