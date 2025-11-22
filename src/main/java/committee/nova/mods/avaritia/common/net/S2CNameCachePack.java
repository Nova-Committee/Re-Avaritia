package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * C2SRenamePack
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record S2CNameCachePack(CompoundTag name) implements CustomPacketPayload {
    public static final Type<S2CNameCachePack> TYPE = new Type<>(Const.rl("s2c_name_cache"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CNameCachePack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            S2CNameCachePack::name,
            S2CNameCachePack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CNameCachePack> {
        @Override
        public void handle(@NotNull S2CNameCachePack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientChestManager.getInstance().setUserCache(packet.name);
            });
        }
    }
}
