package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.util.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 中子压缩器弹出材料数据包
 * Description: 用于弹出配方材料的数据包
 * Author: cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class C2SCompressorEjectPacket {
    private BlockPos pos;

    public C2SCompressorEjectPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public C2SCompressorEjectPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(this.pos);

            if (tile instanceof NeutronCompressorTile compressor) {
                if (compressor.isRecipeLocked()) {
                    player.sendSystemMessage(Component.literal("§c[中子压缩器] §f请先解锁配方"));
                    return;
                }
                if (compressor.getMaterialCount() > 0) {
                    ItemStack materialStack = compressor.getMaterialStack();
                    if (!materialStack.isEmpty()) {
                        // 创建弹出物品堆
                        ItemStack ejectStack = materialStack.copy();
                        ejectStack.setCount(compressor.getMaterialCount());

                        Inventory playerInventory = player.getInventory();
                        boolean addedToInventory = false;

                        // 尝试添加到玩家物品栏
                        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                            ItemStack slotStack = playerInventory.getItem(i);
                            if (slotStack.isEmpty()) {
                                // 空槽位，直接放入
                                playerInventory.setItem(i, ejectStack);
                                addedToInventory = true;
                                break;
                            } else if (ItemUtils.areStacksSameType(slotStack, ejectStack) &&
                                      slotStack.getCount() < slotStack.getMaxStackSize()) {
                                // 相同物品且有空间
                                int canAdd = Math.min(ejectStack.getCount(),
                                                     slotStack.getMaxStackSize() - slotStack.getCount());
                                if (canAdd > 0) {
                                    slotStack.grow(canAdd);
                                    ejectStack.shrink(canAdd);
                                    if (ejectStack.isEmpty()) {
                                        addedToInventory = true;
                                        break;
                                    }
                                }
                            }
                        }

                        // 如果物品栏装不下，弹出到地上
                        if (!addedToInventory) {
                            player.drop(ejectStack, false);
                        }

                        // 清空压缩器中的材料
                        compressor.clearMaterials();

                        // 发送成功消息
                        player.sendSystemMessage(Component.literal("§a[中子压缩器] §f已弹出材料: " +
                            ejectStack.getCount() + "x " + ejectStack.getDisplayName().getString()));
                    }
                } else {
                    // 没有材料可弹出
                    player.sendSystemMessage(Component.literal("§c[中子压缩器] §f没有可弹出的材料"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}