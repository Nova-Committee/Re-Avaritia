package committee.nova.mods.avaritia.util;

import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.api.common.item.ItemStackWrapper;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.InfinityHandler;
import committee.nova.mods.avaritia.init.handler.ItemCaptureHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.math.RayTracer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:50
 * Version: 1.0
 */
public class ToolUtil {
    public static final Set<TagKey<Block>> materialsPick = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_PICKAXE,
            Tags.Blocks.STONE, Tags.Blocks.STORAGE_BLOCKS,
            Tags.Blocks.GLASS, Tags.Blocks.ORES,
            BlockTags.SCULK_REPLACEABLE_WORLD_GEN
    );

    public static final Set<TagKey<Block>> materialsAxe = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_AXE,
            BlockTags.FALL_DAMAGE_RESETTING,
            BlockTags.LEAVES
    );

    public static final Set<TagKey<Block>> materialsShovel = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_SHOVEL
    );

    public static boolean canUseTool(BlockState state, Set<TagKey<Block>> keySets){
        return state.getTags().collect(Collectors.toSet()).retainAll(keySets);
    }

    public static Set<String> defaultTrashOres = new HashSet<>();//todo, set trash block in gui

    public static void breakRangeBlocks(Player player, ItemStack stack, BlockPos pos, int range, Set<TagKey<Block>> keySets) {
        BlockHitResult traceResult = RayTracer.retrace(player, range);
        var world = player.level();
        var state = world.getBlockState(pos);
        if (!ToolUtil.canUseTool(state, keySets)) {
            return;
        }

        if (state.isAir()) {
            return;
        }

        var doY = traceResult.getDirection().getAxis() != Direction.Axis.Y;

        var minOffset = new BlockPos(-range, doY ? -1 : -range, -range);
        var maxOffset = new BlockPos(range, doY ? range * 2 - 2 : range, range);

        ToolUtil.breakBlocks(world, player, stack, pos, minOffset, maxOffset, keySets, false);
    }



    private static void breakBlocks(Level world, Player player, ItemStack stack, BlockPos origin, BlockPos min, BlockPos max, Set<TagKey<Block>> validMaterials, boolean filterTrash) {

        ItemCaptureHandler.enableItemCapture(true);//开启凋落物收集

        for (int lx = min.getX(); lx < max.getX(); lx++) {
            for (int ly = min.getY(); ly < max.getY(); ly++) {
                for (int lz = min.getZ(); lz < max.getZ(); lz++) {
                    BlockPos pos = origin.offset(lx, ly, lz);
                    removeBlockWithDrops(world, player, pos, stack, validMaterials);
                }
            }
        }

        ItemCaptureHandler.enableItemCapture(false);//关闭凋落物收集

        Set<ItemStack> drops = ItemCaptureHandler.getCapturedDrops();

        if (filterTrash) {//是否是黑名单
            removeTrash(drops);
        }

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
        ItemStack itemStack = ItemStackUtil.mapEquals(drop, map);
        if (!itemStack.isEmpty())
            map.put(itemStack, map.get(itemStack) + drop.getCount());
        else map.put(drop, drop.getCount());
    }

    public static void putMapDrops(Level world, BlockPos pos, Player player, ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack drop : Block.getDrops(world.getBlockState(pos), (ServerLevel) world, pos, world.getBlockEntity(pos), player, stack)) {
            putMapItem(drop, map);
        }
    }

    public static void removeTrash(Set<ItemStack> drops) {
        Set<ItemStack> trashItems = new HashSet<>();
        for (ItemStack drop : drops) {
            if (isTrash(drop)) {
                trashItems.add(drop);
            }
        }
        drops.removeAll(trashItems);
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

    public static void removeBlockWithDrops(Level world, Player player, BlockPos pos, ItemStack stack, Set<TagKey<Block>> validMaterials) {
        if (!world.isLoaded(pos)) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (world.isClientSide) return;

        if (state.is(Blocks.GRASS) && stack.is(ModItems.infinity_pickaxe.get())) {
            world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }

        //if material contains
        if (!block.canHarvestBlock(state, world, pos, player) || !ToolUtil.canUseTool(state, validMaterials)) {
            return;
        }

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            if (!player.isCreative()) {//not creative
                BlockEntity tile = world.getBlockEntity(pos);
                block.playerWillDestroy(world, pos, state, player);
                if (block.onDestroyedByPlayer(state, world, pos, player, true, world.getFluidState(pos))) {
                    block.playerDestroy(world, player, pos, state, tile, stack);
                }
            } else {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
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

    public static boolean isInfiniteChest(LivingEntity player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        return !stack.isEmpty() && stack.getItem() instanceof ArmorInfinityItem;
    }
}
