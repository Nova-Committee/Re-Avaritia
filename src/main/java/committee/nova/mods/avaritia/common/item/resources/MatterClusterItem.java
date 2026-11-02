package committee.nova.mods.avaritia.common.item.resources;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.api.common.container.NoMenuContainer;
import committee.nova.mods.avaritia.api.util.ContainerUtils;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.NBTUtils;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 14:09
 * Version: 1.0
 */
public class MatterClusterItem extends Item {

    public static final int CAPACITY = 64 * 64;
    private static final String ITEMS_NBT = "items";

    public MatterClusterItem() {
        super(ModItems.properties()
                .rarity(ModRarities.RARE)
                .stacksTo(1));
    }

    /** 26.3 无世界上下文时编码 ItemStack 所需的注册表访问（内建注册表实时视图）。 */
    private static HolderLookup.Provider registryProvider() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static boolean hasClusterItems(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().contains(ITEMS_NBT);
    }

    public static int getClusterSize(ItemStack cluster) {
        if (!hasClusterItems(cluster)) return 0;
        return Arrays.stream(readClusterInventory(cluster).items).mapToInt(ItemStack::getCount).sum();
    }

    /**
     * 读取物质团内全部非空物品（供 FullMatterClusterRecipe 等逻辑使用）。
     */
    public static List<ItemStack> getClusterItems(ItemStack cluster) {
        return Arrays.stream(readClusterInventory(cluster).items)
                .filter(stack -> !stack.isEmpty())
                .toList();
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
            spawnCluster.remove(DataComponents.CUSTOM_DATA);
            spawnCluster.setCount(0);
        } else {
            writeClusterInventory(spawnCluster, spawnClusterInv);
        }

        return mergedAny;
    }


    private static void writeClusterInventory(ItemStack cluster, NoMenuContainer clusterContents) {
        var items = NBTUtils.writeToTag(registryProvider(), clusterContents.items);
        CustomData.update(DataComponents.CUSTOM_DATA, cluster, tag -> tag.put(ITEMS_NBT, items));
    }

    private static NoMenuContainer readClusterInventory(ItemStack cluster) {
        NoMenuContainer clusterInventory = new NoMenuContainer(CAPACITY);
        if (hasClusterItems(cluster)) {
            CompoundTag tag = cluster.get(DataComponents.CUSTOM_DATA).copyTag();
            NBTUtils.readFromTag(registryProvider(), clusterInventory.items, tag.getListOrEmpty(ITEMS_NBT));
        }
        return clusterInventory;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        if (hasClusterItems(stack)) {
            int total = getClusterSize(stack);
            tooltip.accept(Component.translatable("tooltip.avaritia.matter_cluster.counter", total, Math.max(total, CAPACITY)));
            tooltip.accept(Component.literal(""));
            if (hasShiftDown()) {
                Object2IntMap<Item> itemCounts = new Object2IntOpenHashMap<>();
                for (ItemStack item : readClusterInventory(stack).items) {
                    if (item.isEmpty()) {
                        break;
                    }
                    itemCounts.put(item.getItem(), item.getCount() + itemCounts.getOrDefault(item.getItem(), 0));
                }

                itemCounts.forEach((itemx, count) ->
                        tooltip.accept(Component.translatable(itemx.getDescriptionId())
                                .withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(" x " + count).withStyle(ChatFormatting.GRAY))));
            } else {
                tooltip.accept(Component.translatable("tooltip.avaritia.matter_cluster.desc").withStyle(ChatFormatting.DARK_GRAY));
                tooltip.accept(Component.translatable("tooltip.avaritia.matter_cluster.desc2").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }

        }
    }

    private static boolean hasShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            ItemUtils.dropInventory(level, player.blockPosition(), readClusterInventory(stack));
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }


}
