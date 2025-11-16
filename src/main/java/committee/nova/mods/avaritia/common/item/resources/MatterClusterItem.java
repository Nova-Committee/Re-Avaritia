package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.utils.ContainerUtils;
import committee.nova.mods.avaritia.common.component.InfinityContainerContents;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:09
 * Version: 1.0
 */
public class MatterClusterItem extends Item {
    public static final int INV_SIZE = 512;

    public static int CAPACITY = 4096;

    public MatterClusterItem() {
        super(new Properties()
                .rarity(ModRarities.RARE)
                .stacksTo(1));
    }


    public static List<ItemStack> getClusterItems(ItemStack cluster) {
        InfinityContainerContents clusterContainer = cluster.getOrDefault(ModDataComponents.MATTER_CLUSTER.get(),
                InfinityContainerContents.EMPTY);
        return clusterContainer.getItems();
    }

    public static int getClusterSize(List<ItemStack> itemStacks) {
        int itemCount = 0;

        for (ItemStack itemStack : itemStacks) {
            if (!itemStack.isEmpty()) {
                itemCount += itemStack.getCount();
            }
        }
        return itemCount;
    }

    public static ItemStack makeClusters(Set<ItemStack> input) {
        SimpleContainer clusterInventory = new SimpleContainer(INV_SIZE);
        int count = 0;
        for (ItemStack itemStack : input) {
            if (count < CAPACITY) {
                if (clusterInventory.canAddItem(itemStack)) {
                    clusterInventory.addItem(itemStack.copy());
                    count += itemStack.getCount();
                    itemStack.setCount(0);
                }
            }
        }
        if (count > 0) {
            ItemStack cluster = new ItemStack(ModItems.matter_cluster.get());
            cluster.update(ModDataComponents.MATTER_CLUSTER.get(), InfinityContainerContents.EMPTY,
                    clusterContainer -> InfinityContainerContents.fromItems(clusterInventory.getItems()));
            return cluster;
        }
        return ItemStack.EMPTY;
    }

    public static boolean mergeClusters(ItemStack spawnCluster, ItemStack slotCluster) {
        var slotClusterInv = readClusterInventory(slotCluster);
        var slotClusterItems = slotClusterInv.getItems().toArray(ItemStack[]::new);
        SimpleContainer receivingInv = new SimpleContainer(slotClusterInv.getItems().toArray(ItemStack[]::new));
        int recipientCount = Arrays.stream(slotClusterItems).mapToInt(ItemStack::getCount).sum();
        if (recipientCount >= CAPACITY) {
            return false;
        } else {
            boolean mergedAny = false;
            SimpleContainer spawnClusterInv = readClusterInventory(spawnCluster);
            var spawnClusterItems = spawnClusterInv.getItems().toArray(ItemStack[]::new);
            for (ItemStack stack : spawnClusterInv.getItems()) {
                if (stack.isEmpty()) {
                    break;
                }

                int remainder = ContainerUtils.insertItem(receivingInv, stack, false);
                if (remainder <= stack.getCount()) {
                    mergedAny = true;
                }

                recipientCount += stack.getCount() - remainder;
                stack.setCount(remainder);
                if (recipientCount >= CAPACITY) {
                    break;
                }
            }

            writeClusterInventory(slotCluster, receivingInv);
            int spawnClusterRemaining = Arrays.stream(spawnClusterItems).mapToInt(ItemStack::getCount).sum();
            if (spawnClusterRemaining == 0) {
                spawnCluster.setCount(0);
            } else {
                writeClusterInventory(spawnCluster, spawnClusterInv);
            }

            return mergedAny;
        }
    }


    private static void writeClusterInventory(ItemStack cluster, SimpleContainer clusterContents) {
        cluster.update(ModDataComponents.MATTER_CLUSTER.get(), InfinityContainerContents.EMPTY,
                clusterContainer -> InfinityContainerContents.fromItems(clusterContents.getItems()));
    }

    private static SimpleContainer readClusterInventory(ItemStack cluster) {
        var slotClusterInv = cluster.getOrDefault(ModDataComponents.MATTER_CLUSTER.get(), InfinityContainerContents.EMPTY);
        return new SimpleContainer(slotClusterInv.getItems().toArray(ItemStack[]::new));
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (stack.has(ModDataComponents.MATTER_CLUSTER.get())) {
            List<ItemStack> itemStacks = getClusterItems(stack);
            int total = getClusterSize(itemStacks);
            if (total > 0) {
                tooltip.add(Component.translatable("tooltip.matter_cluster.counter", total, Math.max(total, CAPACITY)));
                tooltip.add(Component.literal(""));
            }
            if (Screen.hasShiftDown()) {
                Object2IntMap<Item> itemCounts = new Object2IntOpenHashMap<>();
                for (ItemStack item : readClusterInventory(stack).getItems()) {
                    if (item.isEmpty()) {
                        break;
                    }
                    itemCounts.put(item.getItem(), item.getCount() + itemCounts.getOrDefault(item.getItem(), 0));
                }

                itemCounts.forEach((itemx, count) -> {
                    tooltip.add((Component.translatable(itemx.getDescriptionId())).append((Component.literal(" x " + count)).withStyle(ChatFormatting.GRAY)));
                });
            } else {
                tooltip.add((Component.translatable("tooltip.matter_cluster.desc")).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add((Component.translatable("tooltip.matter_cluster.desc2")).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }

        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        List<ItemStack> itemStacks = getClusterItems(stack);

        if (stack.has(ModDataComponents.MATTER_CLUSTER.get()) && !itemStacks.isEmpty()) {
            if (!level.isClientSide()) {
                for (ItemStack itemStack : itemStacks) {
                    ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(),
                            itemStack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
            player.setItemInHand(hand, ItemStack.EMPTY);
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResultHolder.success(ItemStack.EMPTY);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

}
