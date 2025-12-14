package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
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
                                player.sendSystemMessage(Component.literal("§c[中子压缩器] §f请先解锁配方"));
                                return;
                            }
                            if (compressor.getMaterialCount() > 0) {
                                ItemStack materialStack = compressor.getMaterialStack();
                                if (!materialStack.isEmpty()) {
                                    int materialCount = compressor.getMaterialCount();
                                    int maxStackSize = materialStack.getMaxStackSize();
                                    int successfullyEjected = 0;

                                    Inventory playerInventory = player.getInventory();

                                    // 按堆叠上限分批处理弹出的材料
                                    while (materialCount > 0) {
                                        // 计算当前批次数量（不超过堆叠上限）
                                        int currentBatchCount = Math.min(materialCount, maxStackSize);

                                        // 创建弹出物品堆
                                        ItemStack ejectStack = materialStack.copy();
                                        ejectStack.setCount(currentBatchCount);

                                        // 尝试添加到玩家物品栏
                                        boolean addedToInventory = false;
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

                                        // 更新已成功弹出的数量
                                        successfullyEjected += currentBatchCount;

                                        // 减少剩余需要弹出的材料数量
                                        materialCount -= currentBatchCount;
                                    }

                                    // 清空压缩器中的材料
                                    compressor.clearMaterials();

                                    // 发送成功消息
                                    player.sendSystemMessage(Component.literal("§a[中子压缩器] §f已弹出材料: " +
                                            successfullyEjected + "x " + materialStack.getDisplayName().getString()));
                                }
                            } else {
                                // 没有材料可弹出
                                player.sendSystemMessage(Component.literal("§c[中子压缩器] §f没有可弹出的材料"));
                            }
                        }
                    }
                }

            });
        }
    }



}
