package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.common.container.NoMenuContainer;
import committee.nova.mods.avaritia.api.utils.ContainerUtils;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.NBTUtils;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:09
 * Version: 1.0
 */
public class MatterClusterItem extends Item {

    public static int CAPACITY = 64 * 64;

    public MatterClusterItem() {
        super(new Properties()
                .rarity(ModRarities.RARE)
                .stacksTo(1));
    }

    public static int getClusterSize(ItemStack cluster) {
        if (cluster.hasTag() || !cluster.getOrCreateTag().contains("items", Tag.TAG_LIST)) {
            return Arrays.stream(readClusterInventory(cluster).items).mapToInt(ItemStack::getCount).sum();
        }
        return 0;
    }

    public static List<ItemStack> makeClusters(Collection<ItemStack> input) {
        LinkedList<ItemStack> clusters = new LinkedList<>();
        LinkedList<ItemStack> stacks = new LinkedList<>(input);

        while (!stacks.isEmpty()) {
            NoMenuContainer clusterInventory = new NoMenuContainer(CAPACITY);
            int totalInserted = 0;

            ItemStack cluster;
            while (!stacks.isEmpty() && totalInserted < CAPACITY) {
                cluster = stacks.poll();
                int remainder = ContainerUtils.insertItem(clusterInventory, cluster, false);
                totalInserted += cluster.getCount() - remainder;
                if (remainder > 0) {
                    cluster.setCount(remainder);
                    stacks.add(cluster);
                    break;
                }
            }

            if (totalInserted > 0) {
                cluster = new ItemStack(ModItems.matter_cluster.get());
                writeClusterInventory(cluster, clusterInventory);
                clusters.add(cluster);
            }
        }

        return clusters;
    }

    public static boolean mergeClusters(ItemStack spawnCluster, ItemStack slotCluster) {
        NoMenuContainer receivingInv = readClusterInventory(slotCluster);
        int recipientCount = Arrays.stream(receivingInv.items).mapToInt(ItemStack::getCount).sum();

        if (recipientCount >= CAPACITY) {
            return false;
        }

        boolean mergedAny = false;
        NoMenuContainer spawnClusterInv = readClusterInventory(spawnCluster);
        int remainingCapacity = CAPACITY - recipientCount; // 计算目标物质团的剩余容量

        for (ItemStack stack : spawnClusterInv.items) {
            if (stack.isEmpty() || remainingCapacity <= 0) {
                break;
            }

            int insertCount = Math.min(stack.getCount(), remainingCapacity);
            ItemStack insertStack = stack.copy();
            insertStack.setCount(insertCount);


            int remainder = ContainerUtils.insertItem(receivingInv, insertStack, false);
            int actualInserted = insertCount - remainder;

            if (actualInserted > 0) {
                mergedAny = true;
                recipientCount += actualInserted;
                remainingCapacity -= actualInserted; // 更新剩余容量
                stack.setCount(stack.getCount() - actualInserted); // 更新源物品的剩余数量
            }
        }


        writeClusterInventory(slotCluster, receivingInv);
        int spawnClusterRemaining = Arrays.stream(spawnClusterInv.items).mapToInt(ItemStack::getCount).sum();

        if (spawnClusterRemaining == 0) {
            spawnCluster.setTag(null);
            spawnCluster.setCount(0);
        } else {
            writeClusterInventory(spawnCluster, spawnClusterInv);
        }

        return mergedAny;
    }


    private static void writeClusterInventory(ItemStack cluster, NoMenuContainer clusterContents) {
        CompoundTag nbt = cluster.getOrCreateTag();
        nbt.put("items", NBTUtils.writeToTag(clusterContents.items));
    }

    private static NoMenuContainer readClusterInventory(ItemStack cluster) {
        NoMenuContainer clusterInventory = new NoMenuContainer(CAPACITY);
        if (cluster.hasTag()) {
            NBTUtils.readFromTag(clusterInventory.items, cluster.getOrCreateTag().getList("items", Tag.TAG_COMPOUND));
        }
        return clusterInventory;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (stack.hasTag() || !stack.getOrCreateTag().contains("items", Tag.TAG_LIST)) {
            int total = getClusterSize(stack);
            tooltip.add(Component.translatable("tooltip.matter_cluster.counter", total, Math.max(total, CAPACITY)));
            tooltip.add(Component.literal(""));
            if (Screen.hasShiftDown()) {
                Object2IntMap<Item> itemCounts = new Object2IntOpenHashMap<>();
                for (ItemStack item : readClusterInventory(stack).items) {
                    if (item.isEmpty()) {
                        break;
                    }
                    itemCounts.put(item.getItem(), item.getCount() + itemCounts.getOrDefault(item.getItem(), 0));
                }

                itemCounts.forEach((itemx, count) -> {
                    tooltip.add((Component.translatable(itemx.getDescriptionId())).withStyle(itemx.getRarity(new ItemStack(itemx)).getStyleModifier()).append((Component.literal(" x " + count)).withStyle(ChatFormatting.GRAY)));
                });
            } else {
                tooltip.add((Component.translatable("tooltip.matter_cluster.desc")).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add((Component.translatable("tooltip.matter_cluster.desc2")).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }

        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemUtils.dropInventory(level, player.blockPosition(), readClusterInventory(stack));
            //if (player.isCrouching()) player.openMenu(new SimpleMenuProvider((id, playerInventory, playerx) -> ))
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResultHolder.success(ItemStack.EMPTY);
    }


}
