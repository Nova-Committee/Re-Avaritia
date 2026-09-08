package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.*;
import committee.nova.mods.avaritia.common.net.channel.*;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestActionPacket;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPacket;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePacket;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Avaritia 主网络通道注册入口。
 * <p>
 * 使用 NeoForge 26.1.2 原生 {@link RegisterPayloadHandlersEvent} + {@link PayloadRegistrar}
 * 替代已移除的旧版 SimpleChannel API。所有 PLAY 阶段自定义 payload 在此统一注册。
 * <p>
 * 共注册 <b>13</b> 个网络包：
 * <ul>
 *   <li>3 个 S2C（服务端→客户端）：SideConfigSync, Totem, Singularities</li>
 *   <li>8 个 C2S（客户端→服务端）：CompressorEject, CompressorLock, SetTime, SideConfig,
 *       ElytraSpeedUp, ItemFilter, Rename, OpenRing</li>
 *   <li>1 个双向：NbtData</li>
 * </ul>
 * <p>
 * 压缩箱子包编解码枚举 {@code committee.nova.mods.avaritia.network.chest.ChannelState} 和
 * {@code committee.nova.mods.avaritia.network.chest.ChannelAction} 作为独立 {@link net.minecraft.network.codec.StreamCodec}
 * 提供方存在，供 chest 相关 payload 组合使用。
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class NetworkHandler {
    /** 主网络通道名：avaritia:main。 */
    public static final Identifier CHANNEL_NAME = Const.rl( "main");
    /** NeoForge payload 协议版本。 */
    public static final String PROTOCOL_VERSION = "1";

    private NetworkHandler() {
    }

    /**
     * 注册 PLAY 阶段所有迁移后的自定义网络包。
     * <p>
     * <b>已注册的 chest 子包编解码枚举（非 payload，无需在此注册）：</b>
     * <ul>
     *   <li>{@code committee.nova.mods.avaritia.network.chest.ChannelState} — 压缩箱频道状态枚举</li>
     *   <li>{@code committee.nova.mods.avaritia.network.chest.ChannelAction} — 压缩箱频道操作枚举</li>
     * </ul>
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // === 服务端 → 客户端 (S2C) ===
        registrar.playToClient(S2CSideConfigSyncPacket.TYPE, S2CSideConfigSyncPacket.STREAM_CODEC,
                new S2CSideConfigSyncPacket.Handler());
        registrar.playToClient(S2CTotemPacket.TYPE, S2CTotemPacket.STREAM_CODEC,
                new S2CTotemPacket.Handler());
        registrar.playToClient(S2CSingularitiesPacket.TYPE, S2CSingularitiesPacket.STREAM_CODEC,
                new S2CSingularitiesPacket.Handler());
        registrar.playToClient(S2CChannelActionPack.TYPE, S2CChannelActionPack.STREAM_CODEC,
                new S2CChannelActionPack.Handler());
        registrar.playToClient(S2CChannelListPack.TYPE, S2CChannelListPack.STREAM_CODEC,
                new S2CChannelListPack.Handler());
        registrar.playToClient(S2CChannelStatePack.TYPE, S2CChannelStatePack.STREAM_CODEC,
                new S2CChannelStatePack.Handler());
        registrar.playToClient(S2CInfinityChestStatePacket.TYPE, S2CInfinityChestStatePacket.STREAM_CODEC,
                new S2CInfinityChestStatePacket.Handler());
        registrar.playToClient(S2CInfinityRingOpenPack.TYPE, S2CInfinityRingOpenPack.STREAM_CODEC,
                new S2CInfinityRingOpenPack.Handler());
        registrar.playToClient(S2CUpdateDimensionsPack.TYPE, S2CUpdateDimensionsPack.STREAM_CODEC,
                new S2CUpdateDimensionsPack.Handler());
        registrar.playToClient(S2CNeutronRingOpenPack.TYPE, S2CNeutronRingOpenPack.STREAM_CODEC,
                new S2CNeutronRingOpenPack.Handler());
        registrar.playToClient(S2CNeutronRingPreviewPack.TYPE, S2CNeutronRingPreviewPack.STREAM_CODEC,
                new S2CNeutronRingPreviewPack.Handler());


        // === 客户端 → 服务端 (C2S) ===
        registrar.playToServer(C2SCompressorEjectPacket.TYPE, C2SCompressorEjectPacket.STREAM_CODEC,
                new C2SCompressorEjectPacket.Handler());
        registrar.playToServer(C2SCompressorLockPacket.TYPE, C2SCompressorLockPacket.STREAM_CODEC,
                new C2SCompressorLockPacket.Handler());
        registrar.playToServer(C2SSetTimePacket.TYPE, C2SSetTimePacket.STREAM_CODEC,
                new C2SSetTimePacket.Handler());
        registrar.playToServer(C2SSideConfigPacket.TYPE, C2SSideConfigPacket.STREAM_CODEC,
                new C2SSideConfigPacket.Handler());
        registrar.playToServer(C2SElytraSpeedUpPacket.TYPE, C2SElytraSpeedUpPacket.STREAM_CODEC,
                new C2SElytraSpeedUpPacket.Handler());
        registrar.playToServer(C2SItemFilterPacket.TYPE, C2SItemFilterPacket.STREAM_CODEC,
                new C2SItemFilterPacket.Handler());
        registrar.playToServer(C2SRenamePacket.TYPE, C2SRenamePacket.STREAM_CODEC,
                new C2SRenamePacket.Handler());
        registrar.playToServer(C2SOpenRingPacket.TYPE, C2SOpenRingPacket.STREAM_CODEC,
                new C2SOpenRingPacket.Handler());
        registrar.playToServer(C2SAddChannelPack.TYPE, C2SAddChannelPack.STREAM_CODEC,
                new C2SAddChannelPack.Handler());
        registrar.playToServer(C2SSetChannelPack.TYPE, C2SSetChannelPack.STREAM_CODEC,
                new C2SSetChannelPack.Handler());
        registrar.playToServer(C2SRemoveChannelPack.TYPE, C2SRemoveChannelPack.STREAM_CODEC,
                new C2SRemoveChannelPack.Handler());
        registrar.playToServer(C2SRenameChannelPack.TYPE, C2SRenameChannelPack.STREAM_CODEC,
                new C2SRenameChannelPack.Handler());
        registrar.playToServer(C2SChannelFilterPack.TYPE, C2SChannelFilterPack.STREAM_CODEC,
                new C2SChannelFilterPack.Handler());
        registrar.playToServer(C2SChannelViewPack.TYPE, C2SChannelViewPack.STREAM_CODEC,
                new C2SChannelViewPack.Handler());
        registrar.playToServer(C2SChannelActionPack.TYPE, C2SChannelActionPack.STREAM_CODEC,
                new C2SChannelActionPack.Handler());
        registrar.playToServer(C2SInfinityChestActionPacket.TYPE, C2SInfinityChestActionPacket.STREAM_CODEC,
                new C2SInfinityChestActionPacket.Handler());
        registrar.playToServer(C2SInfinityChestFilterPacket.TYPE, C2SInfinityChestFilterPacket.STREAM_CODEC,
                new C2SInfinityChestFilterPacket.Handler());
        registrar.playToServer(C2SInfinityRingPack.TYPE, C2SInfinityRingPack.STREAM_CODEC,
                new C2SInfinityRingPack.Handler());
        registrar.playToServer(C2SNeutronRingPack.TYPE, C2SNeutronRingPack.STREAM_CODEC,
                new C2SNeutronRingPack.Handler());


        // === 双向 ===
        registrar.playBidirectional(NbtDataPacket.TYPE, NbtDataPacket.STREAM_CODEC,
                new NbtDataPacket.Handler(), new NbtDataPacket.Handler());
    }

    public static void sendNbtDataToServer(CompoundTag tag) {
        sendToServer(new NbtDataPacket(tag));
    }

    public static void sendCompressorLockPacket(BlockPos pos, boolean locked) {
        sendToServer(new C2SCompressorLockPacket(pos, locked));
    }

    public static void sendCompressorEjectPacket(BlockPos pos) {
        sendToServer(new C2SCompressorEjectPacket(pos));
    }

    public static void sendSideConfigUpdate(BlockPos blockPos, SideConfiguration sideConfig) {
        sendToServer(new C2SSideConfigPacket(blockPos, sideConfig));
    }

    public static void sendSideConfigSync(BlockPos pos, SideConfiguration sideConfig) {
        PacketDistributor.sendToAllPlayers(new S2CSideConfigSyncPacket(pos, sideConfig));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
