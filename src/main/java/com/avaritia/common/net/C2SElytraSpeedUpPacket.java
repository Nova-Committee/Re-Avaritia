package com.avaritia.common.net;

import com.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class C2SElytraSpeedUpPacket {
    private final boolean customFlying;
    private final boolean boosting;

    public C2SElytraSpeedUpPacket(FriendlyByteBuf buf) {
        this.customFlying = buf.readBoolean();
        this.boosting = buf.readBoolean();
    }

    public C2SElytraSpeedUpPacket(boolean customFlying, boolean boosting) {
        this.customFlying = customFlying;
        this.boosting = boosting;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(customFlying);
        buf.writeBoolean(boosting);
    }

    public void run(Supplier<IPayloadContext> ctx) {
        ctx.get().enqueueWork(() -> {
            // 从 context 中获取玩家
            if (!(ctx.get().player() instanceof ServerPlayer player)) return;

            // 检查玩家是否装备了无尽鞘翅
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) {
                if (customFlying) {
                    // 启用自定义飞行模式
                    // 强制玩家进入鞘翅飞行状态
                    if (!player.isFallFlying()) {
                        // 给玩家一个向下的初始速度来触发飞行
                        player.push(0, -0.5, 0);
                    }

                    // 如果正在推进，增加速度
                    if (boosting) {
                        applyBoost(player);
                    }
                }
                // 如果customFlying为false，则不执行任何操作，让玩家自然结束飞行
            }
        }).exceptionally(e -> {
            // 处理异常
            ctx.get().disconnect(Component.literal("Error processing packet: " + e.getMessage()));
            return null;
        });
    }

    private void applyBoost(ServerPlayer player) {
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
                    player.getY() + player.getBbHeight() / 2 + (player.level().random.nextDouble() - 0.5) * 0.5,
                    player.getZ() - lookVec.z * 0.5,
                    -lookVec.x * 0.5 + (player.level().random.nextDouble() - 0.5) * 0.2,
                    -0.3 + player.level().random.nextDouble() * 0.3,
                    -lookVec.z * 0.5 + (player.level().random.nextDouble() - 0.5) * 0.2
            );
        }
    }
}
