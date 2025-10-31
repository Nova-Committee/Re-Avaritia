package committee.nova.mods.avaritia.common.net;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 中子压缩器锁定配方切换数据包
 * Description: 用于切换锁定配方状态的数据包
 * Author: cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class C2SCompressorLockPacket {
    private BlockPos pos;
    private boolean lockState;

    public C2SCompressorLockPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.lockState = buf.readBoolean();
    }

    public C2SCompressorLockPacket(BlockPos pos, boolean lockState) {
        this.pos = pos;
        this.lockState = lockState;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.lockState);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(this.pos);

            if (tile instanceof NeutronCompressorTile compressor) {
                // 如果是锁定操作且当前有有效配方，则锁定
                if (this.lockState && compressor.hasRecipe() && compressor.hasMaterialStack()) {
                    compressor.setRecipeLock(true, compressor.getActiveRecipe());
                    player.sendSystemMessage(Component.literal("§a[中子压缩器] §f配方已锁定"));
                }
                // 如果是解锁操作，则解锁
                else if (!this.lockState) {
                    compressor.setRecipeLock(false, null);
                    player.sendSystemMessage(Component.literal("§e[中子压缩器] §f配方已解锁"));
                } else if (!compressor.hasMaterialStack()){
                    // 如果没有有效配方，则发送错误消息
                    player.sendSystemMessage(Component.literal("§c[中子压缩器] §f请先放入一个配方原料"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}