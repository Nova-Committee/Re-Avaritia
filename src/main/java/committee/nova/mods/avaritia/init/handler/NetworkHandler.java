package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.handler.NetBaseHandler;
import committee.nova.mods.avaritia.common.net.*;
import committee.nova.mods.avaritia.common.net.channel.*;
import committee.nova.mods.avaritia.common.net.chest.*;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public final class NetworkHandler {
    public static final NetBaseHandler CHANNEL = new NetBaseHandler(Const.rl("main"));
    private static final ThreadLocal<Consumer<FriendlyByteBuf>> OPENING_DATA = new ThreadLocal<>();
    private static boolean initialized;
    private NetworkHandler() {}
    public static void init() {
        if (initialized) return;
        initialized = true;
        CHANNEL.register(NbtDataPack.class, NbtDataPack::write, NbtDataPack::new, NbtDataPack::run, NetBaseHandler.Direction.BOTH);
        CHANNEL.register(S2CTotemPack.class, S2CTotemPack::write, S2CTotemPack::new, S2CTotemPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CSingularitiesPack.class, S2CSingularitiesPack::write, S2CSingularitiesPack::new, S2CSingularitiesPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(C2SItemFilterPack.class, C2SItemFilterPack::write, C2SItemFilterPack::new, C2SItemFilterPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SRenamePack.class, C2SRenamePack::write, C2SRenamePack::new, C2SRenamePack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SElytraSpeedUpPacket.class, C2SElytraSpeedUpPacket::write, C2SElytraSpeedUpPacket::new, C2SElytraSpeedUpPacket::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SChannelActionPack.class, C2SChannelActionPack::write, C2SChannelActionPack::new, C2SChannelActionPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(S2CChannelActionPack.class, S2CChannelActionPack::write, S2CChannelActionPack::new, S2CChannelActionPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CChannelListPack.class, S2CChannelListPack::write, S2CChannelListPack::new, S2CChannelListPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CChannelStatePack.class, S2CChannelStatePack::write, S2CChannelStatePack::new, S2CChannelStatePack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(C2SChannelFilterPack.class, C2SChannelFilterPack::write, C2SChannelFilterPack::new, C2SChannelFilterPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SSetChannelPack.class, C2SSetChannelPack::write, C2SSetChannelPack::new, C2SSetChannelPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SAddChannelPack.class, C2SAddChannelPack::write, C2SAddChannelPack::new, C2SAddChannelPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SRenameChannelPack.class, C2SRenameChannelPack::write, C2SRenameChannelPack::new, C2SRenameChannelPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SOpenRingPack.class, C2SOpenRingPack::write, C2SOpenRingPack::new, C2SOpenRingPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SSetTimePacket.class, C2SSetTimePacket::write, C2SSetTimePacket::new, C2SSetTimePacket::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SCompressorLockPacket.class, C2SCompressorLockPacket::write, C2SCompressorLockPacket::new, C2SCompressorLockPacket::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SCompressorEjectPacket.class, C2SCompressorEjectPacket::write, C2SCompressorEjectPacket::new, C2SCompressorEjectPacket::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SSideConfigPacket.class, C2SSideConfigPacket::write, C2SSideConfigPacket::new, C2SSideConfigPacket::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(S2CSideConfigSyncPacket.class, S2CSideConfigSyncPacket::write, S2CSideConfigSyncPacket::new, S2CSideConfigSyncPacket::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CInfinityChestStatePack.class, S2CInfinityChestStatePack::write, S2CInfinityChestStatePack::new, S2CInfinityChestStatePack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(C2SInfinityChestActionPack.class, C2SInfinityChestActionPack::write, C2SInfinityChestActionPack::new, C2SInfinityChestActionPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SInfinityChestFilterPack.class, C2SInfinityChestFilterPack::write, C2SInfinityChestFilterPack::new, C2SInfinityChestFilterPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(C2SInfinityRingPack.class, C2SInfinityRingPack::write, C2SInfinityRingPack::new, C2SInfinityRingPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(S2CInfinityRingOpenPack.class, S2CInfinityRingOpenPack::write, S2CInfinityRingOpenPack::new, S2CInfinityRingOpenPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CUpdateDimensionsPack.class, S2CUpdateDimensionsPack::write, S2CUpdateDimensionsPack::new, S2CUpdateDimensionsPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(C2SNeutronRingPack.class, C2SNeutronRingPack::write, C2SNeutronRingPack::new, C2SNeutronRingPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(S2CNeutronRingOpenPack.class, S2CNeutronRingOpenPack::write, S2CNeutronRingOpenPack::new, S2CNeutronRingOpenPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(S2CNeutronRingPreviewPack.class, S2CNeutronRingPreviewPack::write, S2CNeutronRingPreviewPack::new, S2CNeutronRingPreviewPack::run, NetBaseHandler.Direction.CLIENTBOUND);
        CHANNEL.register(C2SInfinityBucketActionPack.class, C2SInfinityBucketActionPack::write, C2SInfinityBucketActionPack::new, C2SInfinityBucketActionPack::run, NetBaseHandler.Direction.SERVERBOUND);
        CHANNEL.register(S2CMenuDataPack.class, S2CMenuDataPack::write, S2CMenuDataPack::new, S2CMenuDataPack::run, NetBaseHandler.Direction.CLIENTBOUND);
    }
    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        openScreen(player, provider, buffer -> buffer.writeBlockPos(pos));
    }
    public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        if (!player.server.isSameThread()) throw new IllegalStateException("Menus must open on server thread");
        if (OPENING_DATA.get() != null) throw new IllegalStateException("Nested extended menu opening");
        OPENING_DATA.set(extraData);
        try { player.openMenu(provider); }
        finally { OPENING_DATA.remove(); }
    }
    /** Invoked before the vanilla open-screen packet by NetworkServerPlayerMixin. */
    public static void sendOpeningData(ServerPlayer player, int containerId, net.minecraft.world.inventory.MenuType<?> type) {
        Consumer<FriendlyByteBuf> writer = OPENING_DATA.get();
        if (writer == null && !(type instanceof committee.nova.mods.avaritia.api.init.registry.DataMenuType<?>)) return;
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            if (writer != null) writer.accept(data);
            byte[] bytes = new byte[data.readableBytes()];
            data.readBytes(bytes);
            CHANNEL.sendTo(player, new S2CMenuDataPack(containerId, bytes));
        } finally { data.release(); }
    }
    public static void sendNbtDataToServer(CompoundTag tag) { CHANNEL.sendToServer(new NbtDataPack(tag)); }
    public static void sendNbtDataTo(ServerPlayer player, CompoundTag tag) { CHANNEL.sendTo(player, new NbtDataPack(tag)); }
    public static void sendCompressorLockPacket(BlockPos pos, boolean locked) { CHANNEL.sendToServer(new C2SCompressorLockPacket(pos, locked)); }
    public static void sendCompressorEjectPacket(BlockPos pos) { CHANNEL.sendToServer(new C2SCompressorEjectPacket(pos)); }
    public static void sendSideConfigUpdate(BlockPos pos, SideConfiguration configuration) { CHANNEL.sendToServer(new C2SSideConfigPacket(pos, configuration)); }
    public static void sendSideConfigSync(Level level, BlockPos pos, SideConfiguration configuration) { CHANNEL.sendToNear(level, pos, 64, new S2CSideConfigSyncPacket(pos, configuration)); }
}
