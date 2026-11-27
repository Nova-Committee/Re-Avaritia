package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class GameplayGameModeMixin {
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;
    @Unique private BlockState avaritia$brokenState;
    @Inject(method = "handleBlockBreakAction", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyProgressStart:I", opcode = 181), cancellable = true)
    private void avaritia$bedrock(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int height, int sequence, CallbackInfo ci) {
        if (InfinityHandler.onPlayerMine(player, pos)) {
            player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
            ci.cancel();
        }
    }
    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void avaritia$rememberState(BlockPos pos, CallbackInfoReturnable<Boolean> cir) { avaritia$brokenState = level.getBlockState(pos); }
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"), cancellable = true)
    private void avaritia$smelt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!player.isCreative() && InfinityHandler.toolEnchant(player, pos)) cir.setReturnValue(true);
    }
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void avaritia$specialDrops(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && avaritia$brokenState != null) InfinityHandler.onBlockBroken(player, pos, avaritia$brokenState);
    }
}
