package committee.nova.mods.avaritia.util;

import com.google.common.collect.Sets;
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

public class ClustersUtils {

    public static final Set<String> defaultTrashOres =
            Sets.newHashSet("minecraft:dirt");


    public static void spawnClusters(Level world, Player player, Set<ItemStack> drops) {
        if (world.isClientSide) return;

        List<ItemStack> clusters = MatterClusterItem.makeClusters(drops);
        for (ItemStack cluster : clusters) {
            Containers.dropItemStack(
                    world,
                    player.getX(),
                    player.getY() + 0.5F,
                    player.getZ(),
                    cluster
            );
        }
    }

    public static void spawnClusters(Level world, Player player, Map<ItemStack, Integer> map) {
        if (world.isClientSide) return;

        Set<ItemStack> stacks = new HashSet<>();
        map.forEach((stack, count) -> {
            ItemStack copy = stack.copy();
            copy.setCount(count);
            stacks.add(copy);
        });

        List<ItemStack> clusters = MatterClusterItem.makeClusters(stacks);
        for (ItemStack cluster : clusters) {
            Containers.dropItemStack(
                    world,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    cluster
            );
        }
    }


    public static void putMapItem(ItemStack drop, Map<ItemStack, Integer> map) {
        ItemStack existed = ItemUtils.mapEquals(drop, map);
        if (!existed.isEmpty()) {
            map.put(existed, map.get(existed) + drop.getCount());
        } else {
            map.put(drop.copy(), drop.getCount());
        }
    }

    public static void putMapDrops(
            Level world,
            BlockPos pos,
            Player player,
            ItemStack tool,
            Map<ItemStack, Integer> map
    ) {
        for (ItemStack drop : Block.getDrops(
                world.getBlockState(pos),
                (ServerLevel) world,
                pos,
                world.getBlockEntity(pos),
                player,
                tool
        )) {
            putMapItem(drop, map);
        }
    }


    public static Set<ItemStack> removeTrash(Set<ItemStack> drops, Set<String> trashList) {
        drops.removeIf(drop -> isTrash(drop, trashList));
        return drops;
    }

    private static boolean isTrash(ItemStack stack, Set<String> trashList) {
        for (String id : trashList) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null && stack.is(BuiltInRegistries.ITEM.get(rl))) {
                return true;
            }
        }
        return false;
    }


    public static List<ItemStack> collateDropList(Set<ItemStack> input) {
        return collateMatterClusterContents(collateMatterCluster(input));
    }

    public static List<ItemStack> collateMatterClusterContents(
            Map<ItemStack, Integer> input
    ) {
        List<ItemStack> result = new ArrayList<>();

        for (Map.Entry<ItemStack, Integer> e : input.entrySet()) {
            int count = e.getValue();
            ItemStack base = e.getKey();

            int max = base.getMaxStackSize();
            int full = Mth.floor((float) count / max);

            for (int i = 0; i < full; i++) {
                ItemStack stack = base.copy();
                stack.setCount(max);
                result.add(stack);
                count -= max;
            }

            if (count > 0) {
                ItemStack stack = base.copy();
                stack.setCount(count);
                result.add(stack);
            }
        }

        return result;
    }

    public static Map<ItemStack, Integer> collateMatterCluster(Set<ItemStack> input) {
        Map<ItemStack, Integer> counts = new HashMap<>();

        if (input == null) return counts;

        for (ItemStack stack : input) {
            ItemStack key = stack.copy();
            key.setCount(1);

            counts.put(
                    key,
                    counts.getOrDefault(key, 0) + stack.getCount()
            );
        }

        return counts;
    }
}
