package committee.nova.mods.avaritia.util;

import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.api.common.item.ItemStackWrapper;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/26 下午6:11
 * @Description:
 */
public class ClustersUtils {
    public static Set<String> defaultTrashOres = Sets.newHashSet("minecraft:dirt");//todo, set trash block in gui

    public static void spawnClusters(Level world, Player player, Set<ItemStack> drops) {
        if (!world.isClientSide) {
            List<ItemStack> clusters = MatterClusterItem.makeClusters(drops);
                for (ItemStack slot : player.getInventory().items) {
                    if (slot.is(ModItems.matter_cluster.get())) {
                        for (ItemStack cluster : clusters) {
                            MatterClusterItem.mergeClusters(cluster, slot);
                    }
                }
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

    public static Set<ItemStack> removeTrash(Set<ItemStack> drops) {
        Set<ItemStack> trashItems = new HashSet<>();
        for (ItemStack drop : drops) {
            if (isTrash(drop)) {
                trashItems.add(drop);
            }
        }
        drops.removeAll(trashItems);
        return drops;
    }

    private static boolean isTrash(ItemStack suspect) {
        boolean isTrash = false;
            for (String ore : defaultTrashOres) {
                if (suspect.is(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ore)))) {
                    return true;
                }
            }
        return isTrash;
    }

    public static List<ItemStack> collateDropList(Set<ItemStack> input) {
        return collateMatterClusterContents(collateMatterCluster(input));
    }

    public static List<ItemStack> collateMatterClusterContents(Map<ItemStackWrapper, Integer> input) {
        List<ItemStack> collated = new ArrayList<>();

        for (Map.Entry<ItemStackWrapper, Integer> e : input.entrySet()) {
            int count = e.getValue();
            ItemStackWrapper wrap = e.getKey();

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

    public static Map<ItemStackWrapper, Integer> collateMatterCluster(Set<ItemStack> input) {
        Map<ItemStackWrapper, Integer> counts = new HashMap<>();

        if (input != null) {
            for (ItemStack entity : input) {
                ItemStackWrapper wrap = new ItemStackWrapper(entity);
                if (!counts.containsKey(wrap)) {
                    counts.put(wrap, 0);
                }

                counts.put(wrap, counts.get(wrap) + entity.getCount());
            }
        }

        return counts;
    }
}
