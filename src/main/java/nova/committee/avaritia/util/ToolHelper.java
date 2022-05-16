package nova.committee.avaritia.util;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import nova.committee.avaritia.common.item.MatterClusterItem;
import nova.committee.avaritia.init.handler.InfinityHandler;
import nova.committee.avaritia.init.registry.ModItems;

import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:50
 * Version: 1.0
 */
public class ToolHelper {
    public static Material[] materialsPick = new Material[]{Material.STONE, Material.METAL, Material.ICE, Material.GLASS, Material.PISTON, Material.HEAVY_METAL};
    public static Set<Material> materialsAxe = Sets.newHashSet(Material.LEAVES, Material.PLANT, Material.WOOD, Material.BAMBOO);


    public static void aoeBlocks(Player player, ItemStack stack, Level world, BlockPos origin, BlockPos min, BlockPos max, Block target, Set<Material> validMaterials) {

        InfinityHandler.enableItemCapture();

        for (int lx = min.getX(); lx < max.getX(); lx++) {
            for (int ly = min.getY(); ly < max.getY(); ly++) {
                for (int lz = min.getZ(); lz < max.getZ(); lz++) {
                    BlockPos pos = origin.offset(lx, ly, lz);
                    removeBlockWithDrops(player, stack, world, pos, target, validMaterials);
                }
            }
        }

        InfinityHandler.stopItemCapture();

        Set<ItemStack> drops = InfinityHandler.getCapturedDrops();

        spawnClusters(world, player, drops);


    }

    public static void spawnClusters(Level world, Player player, Set<ItemStack> drops) {
        if (!world.isClientSide) {
            List<ItemStack> clusters = MatterClusterItem.makeClusters(drops);
            for (ItemStack cluster : clusters) {
                Containers.dropItemStack(world, player.getX(), player.getY(), player.getZ(), cluster);
            }
        }
    }

    public static void putMapItem(ItemStack drop, Map<ItemStack, Integer> map) {
        ItemStack itemStack = ItemUtil.mapEquals(drop, map);
        if (!itemStack.isEmpty())
            map.put(itemStack, map.get(itemStack) + drop.getCount());
        else map.put(drop, drop.getCount());
    }

    public static void putMapDrops(Level world, BlockPos pos, Player player, ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack drop : Block.getDrops(world.getBlockState(pos), (ServerLevel) world, pos, world.getBlockEntity(pos), player, stack)) {
            putMapItem(drop, map);
        }
    }

//    public static Set<ItemStack> removeTrash(ItemStack holdingStack, Set<ItemStack> drops) {
//        Set<ItemStack> trashItems = new HashSet<>();
//        for (ItemStack drop : drops) {
//            if (isTrash(holdingStack, drop)) {
//                trashItems.add(drop);
//            }
//        }
//        drops.removeAll(trashItems);
//        return drops;
//    }

//    private static boolean isTrash(ItemStack holdingStack, ItemStack suspect) {
//        boolean isTrash = false;
//        for (ResourceLocation id : ItemTags.getAllTags().getMatchingTags(holdingStack.getItem())) {
//            for (String ore : InfinityHandler.defaultTrashOres) {
//                if (ItemTags.getAllTags().getTagOrEmpty(id).equals(ore)) {
//                    return true;
//                }
//            }
//        }
//
//        return isTrash;
//    }

    public static void removeBlockWithDrops(Player player, ItemStack stack, Level world, BlockPos pos, Block target, Set<Material> validMaterials) {
        if (!world.isLoaded(pos)) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!world.isClientSide) {
            if ((target != null && target != state.getBlock()) || state.isAir()) {
                return;
            }
            Material material = state.getMaterial();
            if (block == Blocks.GRASS && stack.getItem() == ModItems.pick_axe) {
                world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            }
            if (!block.canHarvestBlock(state, world, pos, player) || !validMaterials.contains(material)) {
                return;
            }
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
            MinecraftForge.EVENT_BUS.post(event);

            if (!event.isCanceled()) {
                if (!player.isCreative()) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    block.playerWillDestroy(world, pos, state, player);
                    if (block.onDestroyedByPlayer(state, world, pos, player, true, world.getFluidState(pos))) {
                        stack.onBlockStartBreak(pos, player);
                        block.playerDestroy(world, player, pos, state, tile, stack);
                    }
                } else {
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
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
            int fullstacks = (int) Math.floor(count / size);

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
