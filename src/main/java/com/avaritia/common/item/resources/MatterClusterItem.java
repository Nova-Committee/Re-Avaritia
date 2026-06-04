package com.avaritia.common.item.resources;

import com.avaritia.api.common.container.NoMenuContainer;
import com.avaritia.api.utils.ContainerUtils;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.api.utils.NBTUtils;
import com.avaritia.common.component.InfinityContainerContents;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRarities;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class MatterClusterItem extends Item {

    public static final int CAPACITY = 64 * 64;

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


    private static NoMenuContainer readClusterInventory(ItemStack stack) {
        NoMenuContainer inv = new NoMenuContainer(CAPACITY);

        InfinityContainerContents contents = NBTUtils.getClusterItems(stack);
        if (contents != null) {
            NBTUtils.readClusterItems(inv.items, contents);
        }

        return inv;
    }

    private static void writeClusterInventory(ItemStack stack, NoMenuContainer inv) {
        NBTUtils.writeClusterItems(stack, inv.items);
    }

    public static int getClusterSize(ItemStack stack) {
        if (!NBTUtils.hasClusterItems(stack)) return 0;
        return Arrays.stream(readClusterInventory(stack).items)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static List<ItemStack> makeClusters(Collection<ItemStack> input) {
        LinkedList<ItemStack> clusters = new LinkedList<>();
        LinkedList<ItemStack> stacks = new LinkedList<>(input);

        while (!stacks.isEmpty()) {
            NoMenuContainer inv = new NoMenuContainer(CAPACITY);
            int inserted = 0;

            while (!stacks.isEmpty() && inserted < CAPACITY) {
                ItemStack stack = stacks.poll();
                int remainder = ContainerUtils.insertItem(inv, stack, false);
                inserted += stack.getCount() - remainder;

                if (remainder > 0) {
                    stack.setCount(remainder);
                    stacks.addFirst(stack);
                    break;
                }
            }

            if (inserted > 0) {
                ItemStack cluster = new ItemStack(ModItems.matter_cluster.get());
                writeClusterInventory(cluster, inv);
                clusters.add(cluster);
            }
        }

        return clusters;
    }

    public static boolean mergeClusters(ItemStack from, ItemStack to) {
        NoMenuContainer toInv = readClusterInventory(to);
        int current = Arrays.stream(toInv.items).mapToInt(ItemStack::getCount).sum();
        if (current >= CAPACITY) return false;

        NoMenuContainer fromInv = readClusterInventory(from);
        int remaining = CAPACITY - current;
        boolean merged = false;

        for (ItemStack stack : fromInv.items) {
            if (stack.isEmpty() || remaining <= 0) break;

            int move = Math.min(stack.getCount(), remaining);
            ItemStack copy = stack.copy();
            copy.setCount(move);

            int rem = ContainerUtils.insertItem(toInv, copy, false);
            int actual = move - rem;

            if (actual > 0) {
                merged = true;
                stack.shrink(actual);
                remaining -= actual;
            }
        }

        writeClusterInventory(to, toInv);

        int left = Arrays.stream(fromInv.items).mapToInt(ItemStack::getCount).sum();
        if (left == 0) {
            NBTUtils.clearClusterItems(from);
            from.setCount(0);
        } else {
            writeClusterInventory(from, fromInv);
        }

        return merged;
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (!NBTUtils.hasClusterItems(stack)) return;

        int total = getClusterSize(stack);
        tooltip.accept(Component.translatable(
                "tooltip.avaritia.matter_cluster.counter",
                total, CAPACITY));

        tooltip.accept(Component.literal(""));

        if (hasShiftDown()) {
            Object2IntMap<Item> counts = new Object2IntOpenHashMap<>();

            for (ItemStack item : readClusterInventory(stack).items) {
                if (item.isEmpty()) break;
                counts.put(item.getItem(),
                        counts.getOrDefault(item.getItem(), 0) + item.getCount());
            }

            counts.forEach((item, count) ->
                    tooltip.accept(
                            Component.translatable(item.getDescriptionId())
                                    .withStyle(ChatFormatting.WHITE)
                                    .append(Component.literal(" x " + count)
                                            .withStyle(ChatFormatting.GRAY))
                    )
            );
        } else {
            tooltip.accept(Component.translatable(
                            "tooltip.avaritia.matter_cluster.desc")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.translatable(
                            "tooltip.avaritia.matter_cluster.desc2")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static boolean hasShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }


    @Override
    public @NotNull InteractionResult use(
            Level level, Player player, @NotNull InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ItemUtils.dropInventory(level, player.blockPosition(),
                    readClusterInventory(stack));
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }
}
