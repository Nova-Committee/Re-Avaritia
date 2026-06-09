package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SElytraSpeedUpPacket(boolean customFlying, boolean boosting) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SElytraSpeedUpPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_elytra_speed_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SElytraSpeedUpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            C2SElytraSpeedUpPacket::customFlying,
            ByteBufCodecs.BOOL,
            C2SElytraSpeedUpPacket::boosting,
            C2SElytraSpeedUpPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SElytraSpeedUpPacket> {
        @Override
        public void handle(@NotNull C2SElytraSpeedUpPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                // 从 context 中获取玩家
                if (!(context.player() instanceof ServerPlayer player)) return;

                // 检查玩家是否装备了无尽鞘翅
                if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) {
                    if (packet.customFlying) {
                        // 启用自定义飞行模式
                        // 强制玩家进入鞘翅飞行状态
                        if (!player.isFallFlying()) {
                            // 给玩家一个向下的初始速度来触发飞行
                            player.push(0, -0.5, 0);
                        }

                        // 如果正在推进，增加速度
                        if (packet.boosting) {
                            applyBoost(player);
                        }
                    }
                    // 如果customFlying为false，则不执行任何操作，让玩家自然结束飞行
                }
            }).exceptionally(e -> {
                // 处理异常
                context.disconnect(Component.literal("Error processing packet: " + e.getMessage()));
                return null;
            });
        }
    }

    private static void applyBoost(ServerPlayer player) {
        // 获取玩家的视线方向
        Vec3 lookVec = player.getLookAngle();
        // 增加推进力
        double boostStrength = 0.7; // 推进强度

        // 直接增加玩家的速度
        player.push(
                lookVec.x * boostStrength,
                lookVec.y * boostStrength * 0.8, // 略微减少Y轴推进力以保持更好的控制
                lookVec.z * boostStrength
        );

        // 添加粒子效果
        for (int i = 0; i < 15; i++) {
            player.level().addParticle(
                    ParticleTypes.FLAME,
                    player.getX() - lookVec.x * 0.5,
                    player.getY() + player.getBbHeight() / 2 + (player.level().getRandom().nextDouble() - 0.5) * 0.5,
                    player.getZ() - lookVec.z * 0.5,
                    -lookVec.x * 0.5 + (player.level().getRandom().nextDouble() - 0.5) * 0.2,
                    -0.3 + player.level().getRandom().nextDouble() * 0.3,
                    -lookVec.z * 0.5 + (player.level().getRandom().nextDouble() - 0.5) * 0.2
            );
        }
    }
}
