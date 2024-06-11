package committee.nova.mods.avaritia.common.item;

import committee.nova.mods.avaritia.api.common.container.SimpleContainer;
import committee.nova.mods.avaritia.api.common.item.ItemStackWrapper;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ContainerUtils;
import committee.nova.mods.avaritia.util.ItemUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

        while(!stacks.isEmpty()) {
            SimpleContainer clusterInventory = new SimpleContainer(512);
            int totalInserted = 0;

            ItemStack cluster;
            while(!stacks.isEmpty() && totalInserted < CAPACITY) {
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

    public static boolean mergeClusters(ItemStack donor, ItemStack recipient) {
        SimpleContainer receivingInv = readClusterInventory(recipient);
        int recipientCount = Arrays.stream(receivingInv.items).mapToInt(ItemStack::getCount).sum();
        if (recipientCount >= CAPACITY) {
            return false;
        } else {
            boolean mergedAny = false;
            SimpleContainer donorInv = readClusterInventory(donor);
            for (ItemStack stack : donorInv.items) {
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

            writeClusterInventory(recipient, receivingInv);
            int donorRemaining = Arrays.stream(donorInv.items).mapToInt(ItemStack::getCount).sum();
            if (donorRemaining == 0) {
                donor.setTag(null);
                donor.setCount(0);
            } else {
                writeClusterInventory(donor, donorInv);
            }

            return mergedAny;
        }
    }


    private static void writeClusterInventory(ItemStack cluster, SimpleContainer clusterContents) {
        CompoundTag nbt = cluster.getOrCreateTag();
        nbt.put("items", writeItemStacksToTag(clusterContents.items));
    }

    private static SimpleContainer readClusterInventory(ItemStack cluster) {
        SimpleContainer clusterInventory = new SimpleContainer(512);
        if (cluster.hasTag()) {
            readItemStacksFromTag(clusterInventory.items, cluster.getOrCreateTag().getList("items", Tag.TAG_COMPOUND));
        }
        return clusterInventory;
    }


    private static ListTag writeItemStacksToTag(ItemStack[] items) {
        ListTag tagList = new ListTag();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                tagList.add(item.save(new CompoundTag()));
            }
        }
        return tagList;
    }

    private static void readItemStacksFromTag(ItemStack[] items, ListTag tagList) {
        for(int i = 0; i < tagList.size(); ++i) {
            items[i] = ItemStack.of(tagList.getCompound(i));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (stack.hasTag() || !stack.getOrCreateTag().contains("items", Tag.TAG_LIST)) {
            SimpleContainer inventory = readClusterInventory(stack);
            int total = Arrays.stream(inventory.items).mapToInt(ItemStack::getCount).sum();
            tooltip.add(Component.translatable("tooltip.matter_cluster.counter", total, Math.max(total, CAPACITY)));
            tooltip.add(Component.literal(""));
            if (Screen.hasShiftDown()) {
                Object2IntMap<Item> itemCounts = new Object2IntOpenHashMap<>();
                for (ItemStack item : inventory.items) {
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
        }

        player.setItemInHand(hand, ItemStack.EMPTY);
        return InteractionResultHolder.success(ItemStack.EMPTY);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

}
