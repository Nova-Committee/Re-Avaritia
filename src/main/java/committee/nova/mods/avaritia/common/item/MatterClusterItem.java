package committee.nova.mods.avaritia.common.item;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolHelper;
import committee.nova.mods.avaritia.util.item.ItemStackWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:09
 * Version: 1.0
 */
public class MatterClusterItem extends Item {

    public static final String MAINTAG = "clusteritems";
    public static final String LISTTAG = "items";
    public static final String ITEMTAG = "item";
    public static final String COUNTTAG = "count";
    public static final String MAINCOUNTTAG = "total";

    public static int CAPACITY = 64 * 64;

    public MatterClusterItem() {
        super(new Properties()
                .stacksTo(1));
    }

    public static List<ItemStack> makeClusters(Set<ItemStack> input) {
        Map<ItemStackWrapper, Integer> items = ToolHelper.collateMatterCluster(input);
        List<ItemStack> clusters = new ArrayList<>();
        List<Map.Entry<ItemStackWrapper, Integer>> itemlist = new ArrayList<>(items.entrySet());

        int currentTotal = 0;
        Map<ItemStackWrapper, Integer> currentItems = new HashMap<>();

        while (!itemlist.isEmpty()) {
            Map.Entry<ItemStackWrapper, Integer> e = itemlist.get(0);
            ItemStackWrapper wrap = e.getKey();
            int wrapcount = e.getValue();

            int count = Math.min(CAPACITY - currentTotal, wrapcount);

            if (!currentItems.containsKey(e.getKey())) {
                currentItems.put(wrap, count);
            } else {
                currentItems.put(wrap, currentItems.get(wrap) + count);
            }
            currentTotal += count;

            e.setValue(wrapcount - count);
            if (e.getValue() == 0) {
                itemlist.remove(0);
            }

            if (currentTotal == CAPACITY) {
                ItemStack cluster = makeCluster(currentItems);

                clusters.add(cluster);

                currentTotal = 0;
                currentItems = new HashMap<>();
            }
        }

        if (currentTotal > 0) {
            ItemStack cluster = makeCluster(currentItems);

            clusters.add(cluster);
        }

        return clusters;
    }

    public static ItemStack makeCluster(Map<ItemStackWrapper, Integer> input) {
        ItemStack cluster = new ItemStack(ModItems.matter_cluster);
        int total = 0;
        for (int num : input.values()) {
            total += num;
        }
        setClusterData(cluster, input, total);
        return cluster;
    }

    public static Map<ItemStackWrapper, Integer> getClusterData(ItemStack cluster) {
        if (!cluster.hasTag() || !cluster.getOrCreateTag().contains(MAINTAG)) {
            return null;
        }
        CompoundTag tag = cluster.getOrCreateTag().getCompound(MAINTAG);
        ListTag list = tag.getList(LISTTAG, 10);
        Map<ItemStackWrapper, Integer> data = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStackWrapper wrap = new ItemStackWrapper(ItemStack.of(entry.getCompound(ITEMTAG)));
            int count = entry.getInt(COUNTTAG);
            data.put(wrap, count);
        }
        return data;
    }

    public static int getClusterSize(ItemStack cluster) {
        if (!cluster.hasTag() || !cluster.getOrCreateTag().contains(MAINTAG)) {
            return 0;
        }
        return cluster.getOrCreateTag().getCompound(MAINTAG).getInt(MAINCOUNTTAG);
    }

    public static void setClusterData(ItemStack stack, Map<ItemStackWrapper, Integer> data, int count) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }

        CompoundTag clustertag = new CompoundTag();
        ListTag list = new ListTag();

        for (Map.Entry<ItemStackWrapper, Integer> entry : data.entrySet()) {
            CompoundTag itemtag = new CompoundTag();
            itemtag.put(ITEMTAG, entry.getKey().stack.save(new CompoundTag()));
            itemtag.putInt(COUNTTAG, entry.getValue());
            list.add(itemtag);
        }
        clustertag.put(LISTTAG, list);
        clustertag.putInt(MAINCOUNTTAG, count);
        stack.getOrCreateTag().put(MAINTAG, clustertag);
    }

    public static void mergeClusters(ItemStack donor, ItemStack recipient) {
        int donorcount = getClusterSize(donor);
        int recipientcount = getClusterSize(recipient);

        if (donorcount == 0 || donorcount == CAPACITY || recipientcount == CAPACITY) {
            return;
        }

        Map<ItemStackWrapper, Integer> donordata = getClusterData(donor);
        Map<ItemStackWrapper, Integer> recipientdata = getClusterData(recipient);
        List<Map.Entry<ItemStackWrapper, Integer>> datalist = new ArrayList<>();
        datalist.addAll(donordata.entrySet());

        while (recipientcount < CAPACITY && donorcount > 0) {
            Map.Entry<ItemStackWrapper, Integer> e = datalist.get(0);
            ItemStackWrapper wrap = e.getKey();
            int wrapcount = e.getValue();

            int count = Math.min(CAPACITY - recipientcount, wrapcount);

            if (!recipientdata.containsKey(wrap)) {
                recipientdata.put(wrap, count);
            } else {
                recipientdata.put(wrap, recipientdata.get(wrap) + count);
            }

            donorcount -= count;
            recipientcount += count;

            if (wrapcount - count > 0) {
                e.setValue(wrapcount - count);
            } else {
                donordata.remove(wrap);
                datalist.remove(0);
            }
        }
        setClusterData(recipient, recipientdata, recipientcount);

        if (donorcount > 0) {
            setClusterData(donor, donordata, donorcount);
        } else {
            donor.setTag(null);
            donor.setCount(0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (!stack.hasTag() || !stack.getOrCreateTag().contains(MAINTAG)) {
            return;
        }
        CompoundTag clustertag = stack.getOrCreateTag().getCompound(MAINTAG);

        tooltip.add(Component.literal(clustertag.getInt(MAINCOUNTTAG) + "/" + CAPACITY + " " + I18n.get("tooltip.matter_cluster.counter")));
        tooltip.add(Component.literal(""));

        if (Screen.hasShiftDown()) {
            ListTag list = clustertag.getList(LISTTAG, 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                ItemStack countstack = ItemStack.of(tag.getCompound(ITEMTAG));
                int count = tag.getInt(COUNTTAG);

                tooltip.add(Component.literal(countstack.getItem().getRarity(countstack).color + countstack.getDisplayName().getString() + ChatFormatting.GRAY + " x " + count));
            }
        } else {
            tooltip.add(Component.literal(ChatFormatting.DARK_GRAY + I18n.get("tooltip.matter_cluster.desc")));
            tooltip.add(Component.literal(ChatFormatting.DARK_GRAY.toString() + ChatFormatting.ITALIC + I18n.get("tooltip.matter_cluster.desc2")));
        }

    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 pos = player.position();
        if (!world.isClientSide) {
            List<ItemStack> drops = ToolHelper.collateMatterClusterContents(Objects.requireNonNull(MatterClusterItem.getClusterData(stack)));

            for (ItemStack drop : drops) {
                Containers.dropItemStack(world, pos.x, pos.y, pos.z, drop);
            }
        }

        stack.setCount(0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

}
