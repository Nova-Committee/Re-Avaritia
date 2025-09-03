package committee.nova.mods.avaritia.util;

import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.api.common.wrapper.StrictItemStack;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/26 下午6:11
 * @Description:
 */
public class ClustersUtils {
    public static Set<String> defaultTrashOres = Sets.newHashSet("minecraft:dirt");


    public static void spawnClusters(Level world, Player player, Set<ItemStack> drops) {
        if (!world.isClientSide) {
            List<ItemStack> clusters = MatterClusterItem.makeClusters(drops);
            for (ItemStack cluster : clusters) {
                Containers.dropItemStack(world, player.getX(), player.getY() + 0.5F, player.getZ(), cluster);
            }
        }
    }

    public static void spawnClusters(Level world, Player player, Map<ItemStack, Integer> map) {
        if (!world.isClientSide) {
            HashSet<ItemStack> stacks = new HashSet<>();
            map.forEach((stack, integer) -> stacks.add(stack.copyWithCount(map.get(stack))));
            List<ItemStack> clusters = MatterClusterItem.makeClusters(stacks);
            for (ItemStack cluster : clusters) {
                Containers.dropItemStack(world, player.getX(), player.getY(), player.getZ(), cluster);
            }
        }
    }

    public static void putMapItem(ItemStack drop, Map<ItemStack, Integer> map) {
        ItemStack itemStack = ItemUtils.mapEquals(drop, map);
        if (!itemStack.isEmpty())
            map.put(itemStack, map.get(itemStack) + drop.getCount());
        else map.put(drop, drop.getCount());
    }

    public static void putMapDrops(Level world, BlockPos pos, Player player, ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack drop : Block.getDrops(world.getBlockState(pos), (ServerLevel) world, pos, world.getBlockEntity(pos), player, stack)) {
            putMapItem(drop, map);
        }
    }

    public static Set<ItemStack> removeTrash(Set<ItemStack> drops, Set<String> defaultTrashOres) {
        Set<ItemStack> trashItems = new HashSet<>();
        for (ItemStack drop : drops) {
            if (isTrash(drop, defaultTrashOres)) {
                trashItems.add(drop);
            }
        }
        drops.removeAll(trashItems);
        return drops;
    }

    private static boolean isTrash(ItemStack suspect, Set<String> defaultTrashOres) {
        boolean isTrash = false;
        for (String ore : defaultTrashOres) {
            if (suspect.is(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(ore)))) {
                return true;
            }
        }
        return isTrash;
    }

    public static List<ItemStack> collateDropList(Set<ItemStack> input) {
        return collateMatterClusterContents(collateMatterCluster(input));
    }

    public static List<ItemStack> collateMatterClusterContents(Map<StrictItemStack, Integer> input) {
        List<ItemStack> collated = new ArrayList<>();

        for (Map.Entry<StrictItemStack, Integer> e : input.entrySet()) {
            int count = e.getValue();
            StrictItemStack wrap = e.getKey();

            int size = wrap.stack.getMaxStackSize();
            int fullstacks = Mth.floor((float) count / size);

            for (int i = 0; i < fullstacks; i++) {
                count -= size;
                ItemStack stack = wrap.stack.copy();
                stack.setCount(size);
                collated.add(stack);
            }

            if (count > 0) {
                ItemStack stack = wrap.stack.copy();
                stack.setCount(count);
                collated.add(stack);
            }
        }

        return collated;
    }

    public static Map<StrictItemStack, Integer> collateMatterCluster(Set<ItemStack> input) {
        Map<StrictItemStack, Integer> counts = new HashMap<>();

        if (input != null) {
            for (ItemStack entity : input) {
                StrictItemStack wrap = new StrictItemStack(entity);
                if (!counts.containsKey(wrap)) {
                    counts.put(wrap, 0);
                }

                counts.put(wrap, counts.get(wrap) + entity.getCount());
            }
        }

        return counts;
    }
}
