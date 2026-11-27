package committee.nova.mods.avaritia.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerGameModeAccessor {
    @Accessor("isDestroyingBlock")
    boolean avaritia$isDestroyingBlock();

    @Accessor("destroyPos")
    BlockPos avaritia$destroyPos();

    @Accessor("hasDelayedDestroy")
    boolean avaritia$hasDelayedDestroy();

    @Accessor("delayedDestroyPos")
    BlockPos avaritia$delayedDestroyPos();
}
