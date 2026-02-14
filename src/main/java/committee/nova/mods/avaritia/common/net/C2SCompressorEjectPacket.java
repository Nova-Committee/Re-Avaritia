package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 中子压缩器弹出材料数据包
 * Description: 用于弹出配方材料的数据包
 * @author cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class C2SCompressorEjectPacket {
    private final BlockPos pos;

    public C2SCompressorEjectPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public C2SCompressorEjectPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(this.pos);

            if (tile instanceof NeutronCompressorTile compressor) {
                if (compressor.isRecipeLocked()) {
                    player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_eject.message_1"));
                    return;
                }
                if (compressor.getMaterialCount() > 0) {
                    ItemStack materialStack = compressor.getMaterialStack();
                    if (!materialStack.isEmpty()) {
                        int materialCount = compressor.getMaterialCount();

                        // 创建一个包含所有材料的集合，用于打包成物质团
                        Set<ItemStack> drops = new HashSet<>();
                        ItemStack fullMaterialStack = materialStack.copy();
                        fullMaterialStack.setCount(materialCount);
                        drops.add(fullMaterialStack);

                        // 使用ClustersUtils创建物质团
                        List<ItemStack> clusters = MatterClusterItem.makeClusters(drops);

                        // 将生成的物质团添加到玩家物品栏或丢弃到地面
                        for (ItemStack cluster : clusters) {
                            boolean addedToInventory = false;
                            Inventory playerInventory = player.getInventory();

                            // 尝试添加到玩家物品栏
                            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                                ItemStack slotStack = playerInventory.getItem(i);
                                if (slotStack.isEmpty()) {
                                    // 空槽位，直接放入
                                    playerInventory.setItem(i, cluster.copy());
                                    addedToInventory = true;
                                    break;
                                } else if (ItemUtils.areStacksSameType(slotStack, cluster) &&
                                        slotStack.getCount() < slotStack.getMaxStackSize()) {
                                    // 相同物品且有空间
                                    int canAdd = Math.min(cluster.getCount(),
                                            slotStack.getMaxStackSize() - slotStack.getCount());
                                    if (canAdd > 0) {
                                        slotStack.grow(canAdd);
                                        cluster.shrink(canAdd);
                                        if (cluster.isEmpty()) {
                                            addedToInventory = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            // 如果物品栏装不下，弹出到地上
                            if (!addedToInventory && !cluster.isEmpty()) {
                                player.drop(cluster, false);
                            }
                        }

                        // 清空压缩器中的材料
                        compressor.clearMaterials();

                        // 发送成功消息
                        player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_eject.message_2"));
                    }
                } else {
                    // 没有材料可弹出
                    player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_eject.message_3"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}