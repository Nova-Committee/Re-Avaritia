package committee.nova.mods.avaritia.init.mixin;

import committee.nova.mods.avaritia.api.init.event.ModEventFactory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PlayerMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午2:48
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow @Final private Inventory inventory;

    @Unique
    Player player = (Player) (Object) this;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "hasCorrectToolForDrops",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void placeNewPlayer1(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ModEventFactory.doPlayerHarvestCheck(player, blockState, !blockState.requiresCorrectToolForDrops() || this.inventory.getSelected().isCorrectToolForDrops(blockState)));
        cir.cancel();
    }
}
