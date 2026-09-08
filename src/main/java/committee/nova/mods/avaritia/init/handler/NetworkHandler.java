package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.*;
import committee.nova.mods.avaritia.common.net.channel.*;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestActionPack;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPack;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 13:07
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Const.rl("main"), () -> {
        return "1.0";
    }, (s) -> {
        return true;
    }, (s) -> {
        return true;
    });
    public static int id = 0;
    ;

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(id++, NbtDataPack.class, NbtDataPack::write, NbtDataPack::new, NbtDataPack::run);
        CHANNEL.registerMessage(id++, S2CTotemPack.class, S2CTotemPack::write, S2CTotemPack::new, S2CTotemPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CSingularitiesPack.class, S2CSingularitiesPack::write, S2CSingularitiesPack::new, S2CSingularitiesPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SItemFilterPack.class, C2SItemFilterPack::write, C2SItemFilterPack::new, C2SItemFilterPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SRenamePack.class, C2SRenamePack::write, C2SRenamePack::new, C2SRenamePack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SElytraSpeedUpPacket.class, C2SElytraSpeedUpPacket::write, C2SElytraSpeedUpPacket::new, C2SElytraSpeedUpPacket::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SChannelActionPack.class, C2SChannelActionPack::write, C2SChannelActionPack::new, C2SChannelActionPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, S2CChannelActionPack.class, S2CChannelActionPack::write, S2CChannelActionPack::new, S2CChannelActionPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CChannelListPack.class, S2CChannelListPack::write, S2CChannelListPack::new, S2CChannelListPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CChannelStatePack.class, S2CChannelStatePack::write, S2CChannelStatePack::new, S2CChannelStatePack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SChannelFilterPack.class, C2SChannelFilterPack::write, C2SChannelFilterPack::new, C2SChannelFilterPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SSetChannelPack.class, C2SSetChannelPack::write, C2SSetChannelPack::new, C2SSetChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SAddChannelPack.class, C2SAddChannelPack::write, C2SAddChannelPack::new, C2SAddChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SRenameChannelPack.class, C2SRenameChannelPack::write, C2SRenameChannelPack::new, C2SRenameChannelPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SOpenRingPack.class, C2SOpenRingPack::write, C2SOpenRingPack::new, C2SOpenRingPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SSetTimePacket.class, C2SSetTimePacket::write, C2SSetTimePacket::new, C2SSetTimePacket::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // 中子压缩器新增功能包
        CHANNEL.registerMessage(id++, C2SCompressorLockPacket.class, C2SCompressorLockPacket::write, C2SCompressorLockPacket::new, C2SCompressorLockPacket::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SCompressorEjectPacket.class, C2SCompressorEjectPacket::write, C2SCompressorEjectPacket::new, C2SCompressorEjectPacket::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // 方块配置功能包
        CHANNEL.registerMessage(id++, C2SSideConfigPacket.class, C2SSideConfigPacket::write, C2SSideConfigPacket::new, C2SSideConfigPacket::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, S2CSideConfigSyncPacket.class, S2CSideConfigSyncPacket::write, S2CSideConfigSyncPacket::new, S2CSideConfigSyncPacket::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++, S2CInfinityChestStatePack.class, S2CInfinityChestStatePack::write, S2CInfinityChestStatePack::new, S2CInfinityChestStatePack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SInfinityChestActionPack.class, C2SInfinityChestActionPack::write, C2SInfinityChestActionPack::new, C2SInfinityChestActionPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, C2SInfinityChestFilterPack.class, C2SInfinityChestFilterPack::write, C2SInfinityChestFilterPack::new, C2SInfinityChestFilterPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++, C2SInfinityRingPack.class, C2SInfinityRingPack::write, C2SInfinityRingPack::new, C2SInfinityRingPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, S2CInfinityRingOpenPack.class, S2CInfinityRingOpenPack::write, S2CInfinityRingOpenPack::new, S2CInfinityRingOpenPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CUpdateDimensionsPack.class, S2CUpdateDimensionsPack::write, S2CUpdateDimensionsPack::new, S2CUpdateDimensionsPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SNeutronRingPack.class, C2SNeutronRingPack::write, C2SNeutronRingPack::new, C2SNeutronRingPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, S2CNeutronRingOpenPack.class, S2CNeutronRingOpenPack::write, S2CNeutronRingOpenPack::new, S2CNeutronRingOpenPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, S2CNeutronRingPreviewPack.class, S2CNeutronRingPreviewPack::write, S2CNeutronRingPreviewPack::new, S2CNeutronRingPreviewPack::run, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, C2SInfinityBucketActionPack.class, C2SInfinityBucketActionPack::write, C2SInfinityBucketActionPack::new, C2SInfinityBucketActionPack::run, Optional.of(NetworkDirection.PLAY_TO_SERVER));


    }

    public static void sendNbtDataToServer(CompoundTag tag) {
        CHANNEL.sendToServer(new NbtDataPack(tag));
    }

    public static void sendNbtDataTo(ServerPlayer pl, CompoundTag tag) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> pl), new NbtDataPack(tag));
    }

    // 便捷方法：发送压缩器锁定包
    public static void sendCompressorLockPacket(BlockPos pos, boolean locked) {
        CHANNEL.sendToServer(new C2SCompressorLockPacket(pos, locked));
    }

    // 便捷方法：发送压缩器弹出包
    public static void sendCompressorEjectPacket(BlockPos pos) {
        CHANNEL.sendToServer(new C2SCompressorEjectPacket(pos));
    }

    // 便捷方法：发送方块配置更新包
    public static void sendSideConfigUpdate(BlockPos blockPos, SideConfiguration sideConfig) {
        // 解析字符串格式的位置: BlockPos{x=123, y=456, z=789}
        CHANNEL.sendToServer(new C2SSideConfigPacket(blockPos, sideConfig));
    }

    // 便捷方法：发送方块配置同步包给附近玩家
    public static void sendSideConfigSync(Level level, BlockPos pos, SideConfiguration sideConfig) {
        // 暂时使用空参数，后续可以改进为指定位置的广播
        CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSideConfigSyncPacket(pos, sideConfig));
    }
}
