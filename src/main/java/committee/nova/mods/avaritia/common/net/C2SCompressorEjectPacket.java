package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record C2SCompressorEjectPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<C2SCompressorEjectPacket> TYPE = new Type<>(Const.rl("c2s_compressor_eject"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCompressorEjectPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            C2SCompressorEjectPacket::pos,
            C2SCompressorEjectPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SCompressorEjectPacket> {
        @Override
        public void handle(@NotNull C2SCompressorEjectPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player)  {
                    if (player.level() instanceof ServerLevel level) {
                        BlockEntity tile = level.getBlockEntity(packet.pos);

                        if (tile instanceof NeutronCompressorTile compressor) {
                            if (compressor.isRecipeLocked()) {
                                player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_eject.message_1"));
                                return;
                            }
                            if (compressor.getMaterialCount() > 0) {
                                ItemStack materialStack = compressor.getMaterialStack();
                                if (!materialStack.isEmpty()) {
                                    int materialCount = compressor.getMaterialCount();

                                    // 使用Set来收集要打包成物质团的物品
                                    Set<ItemStack> drops = new HashSet<>();
                                    ItemStack materialCopy = materialStack.copy();
                                    materialCopy.setCount(materialCount);
                                    drops.add(materialCopy);

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
                    }
                }
            });
        }
    }




}
