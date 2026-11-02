package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.handler.NeutronHarnessHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 中子战魂铠的绑定按键动作。
 *
 * @param action 0 = 召唤善良恶魂承载，1 = 展开立体防御护盾
 */
public record C2SHarnessActionPacket(int action) implements CustomPacketPayload {
    public static final int ACTION_SUMMON = 0;
    public static final int ACTION_SHIELD = 1;

    public static final CustomPacketPayload.Type<C2SHarnessActionPacket> TYPE =
            new CustomPacketPayload.Type<>(Const.rl("c2s_harness_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SHarnessActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            C2SHarnessActionPacket::action,
            C2SHarnessActionPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SHarnessActionPacket> {
        @Override
        public void handle(@NotNull C2SHarnessActionPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) {
                    return;
                }
                switch (packet.action()) {
                    case ACTION_SUMMON -> NeutronHarnessHandler.summonMount(player);
                    case ACTION_SHIELD -> NeutronHarnessHandler.openShield(player);
                    default -> {
                    }
                }
            });
        }
    }
}
