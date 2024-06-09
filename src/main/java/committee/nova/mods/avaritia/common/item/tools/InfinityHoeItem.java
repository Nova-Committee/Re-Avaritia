package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.handler.ItemCaptureHandler;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ItemStackUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 16:47
 * Version: 1.0
 */
public class InfinityHoeItem extends HoeItem {

    public InfinityHoeItem() {
        super(Tier.INFINITY_HOE, -5, 0f, (new Properties())
                .stacksTo(1)
                .fireResistant());

    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }


    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack heldItem = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            pPlayer.swing(InteractionHand.MAIN_HAND);
            BlockPos blockPos = pPlayer.getOnPos();
            int rang = 7;
            int height = 2;
            BlockPos minPos = blockPos.offset(-rang, -height, -rang);
            BlockPos maxPos = blockPos.offset(rang, height, rang);
            Map<ItemStack, Integer> map = new HashMap<>();
            ItemCaptureHandler.enableItemCapture(true);
            for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
                BlockState state = pLevel.getBlockState(pos);
                Block block = state.getBlock();
                //harvest
                if (block instanceof CropBlock) { //common
                    if (block instanceof BeetrootBlock ? state.getValue(BeetrootBlock.AGE) >= 3 : state.getValue(CropBlock.AGE) >= 7) {
                        ToolUtils.putMapDrops(pLevel, pos, pPlayer, new ItemStack(this), map);
                        pLevel.setBlock(pos, state.setValue(block instanceof BeetrootBlock ? BeetrootBlock.AGE : CropBlock.AGE, 0), 11);
                    }
                }
                if (block instanceof CocoaBlock) { //coca
                    if (state.getValue(CocoaBlock.AGE) >= 2) {
                        ToolUtils.putMapDrops(pLevel, pos, pPlayer, new ItemStack(this), map);
                        pLevel.setBlock(pos, state.setValue(CocoaBlock.AGE, 0), 11);
                    }
                }
                if (block instanceof StemGrownBlock) { //pumpkin
                    ToolUtils.putMapDrops(pLevel, pos, pPlayer, new ItemStack(this), map);
                    pLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                }
                if (block instanceof SweetBerryBushBlock) { //SweetBerry
                    if (state.getValue(SweetBerryBushBlock.AGE) >= 3) {
                        ToolUtils.putMapDrops(pLevel, pos, pPlayer, new ItemStack(this), map);
                        pLevel.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 0), 11);
                    }
                }
                //grow
                if (block instanceof BonemealableBlock bonemealableBlock && !(block instanceof GrassBlock) && bonemealableBlock.isValidBonemealTarget(pLevel, pos, state, true)) {
                    for (int i = 0; i < 3; i++)
                        bonemealableBlock.isBonemealSuccess(pLevel, pLevel.random, pos, state);
                }
            }
            ItemCaptureHandler.enableItemCapture(false);
            Set<ItemStack> drops = ItemCaptureHandler.getCapturedDrops();
            ToolUtils.spawnClusters(pLevel, pPlayer, map.keySet());
            pPlayer.getCooldowns().addCooldown(heldItem.getItem(), 20);
        }
        pLevel.playSound(pPlayer, pPlayer.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResultHolder.pass(heldItem);

    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var world = context.getLevel();
        var blockpos = context.getClickedPos();
        var block1 = world.getBlockState(blockpos).getBlock();
//        int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
//        if (hook != 0) return hook > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockpos.above()) && (block1 instanceof GrassBlock
                || block1.equals(Blocks.DIRT) || block1.equals(Blocks.COARSE_DIRT))) {
            var blockstate = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
            var playerentity = context.getPlayer();
            world.playSound(playerentity, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide && playerentity != null) {
                int rang = 5;
                var minPos = blockpos.offset(-rang, 0, -rang);
                var maxPos = blockpos.offset(rang, 0, rang);
                if (playerentity.isCrouching()) {
                    ItemCaptureHandler.enableItemCapture(true);
                    Map<ItemStack, Integer> map = new HashMap<>();
                    var boxMutable = BlockPos.betweenClosed(minPos, maxPos);
                    for (BlockPos pos : boxMutable) {
                        var state = world.getBlockState(pos);
                        var block = state.getBlock();
                        if (!world.isEmptyBlock(pos.above())) {
                            for (int i = 1; i <= 3; i++) {
                                harvest(world, pos.above(i), playerentity, map);
                            }
                        }

                        if (world.isEmptyBlock(pos.above()) && (block instanceof GrassBlock || block.equals(Blocks.DIRT) || block.equals(
                                Blocks.COARSE_DIRT) || block instanceof FarmBlock)) {
                            world.setBlock(pos, blockstate, 11);
                        }
                        if (world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.below())) {
                            world.setBlock(pos, blockstate, 11);
                        }
                        if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer) {
                            world.setBlock(pos, blockstate, 11);
                        }
                    }
                    ItemCaptureHandler.enableItemCapture(false);
                    Set<ItemStack> drops = ItemCaptureHandler.getCapturedDrops();
                    ToolUtils.spawnClusters(world, playerentity, map.keySet());

                    Iterable<BlockPos> inBoxMutable = BlockPos.betweenClosed(minPos, maxPos.offset(0, 3, 0));
                    Iterable<BlockPos> allInBoxMutable = BlockPos.betweenClosed(minPos.offset(-1, 0, -1), maxPos.offset(1, 4, 1));
                    for (BlockPos pos : allInBoxMutable) {
                        if (!hasBox(pos, inBoxMutable)) { //外壳坐标
                            var state = world.getBlockState(pos);
                            if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer)
                                world.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                        }
                    }
                } else world.setBlock(blockpos, blockstate, 11); //未潜行耕种一个方块
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private boolean hasBox(BlockPos pos, Iterable<BlockPos> box) {
        for (BlockPos pos1 : box) {
            if (pos1.getX() == pos.getX() && pos1.getY() == pos.getY() && pos1.getZ() == pos.getZ()) return true;
        }
        return false;
    }

    private void harvest(Level world, BlockPos pos, Player player, Map<ItemStack, Integer> map) {
        var state = world.getBlockState(pos);
        var block = state.getBlock();
        if (world.isEmptyBlock(pos) || block instanceof CropBlock || block instanceof StemBlock
                || block instanceof CocoaBlock || block instanceof SweetBerryBushBlock) {
            return;
        }
        if (block.equals(Blocks.BEDROCK)) {
            var stack1 = new ItemStack(Blocks.BEDROCK);
            ItemStack itemStack = ItemStackUtils.mapEquals(stack1, map);
            if (!itemStack.isEmpty())
                map.put(itemStack, map.get(itemStack) + stack1.getCount());
            else map.put(stack1, stack1.getCount());
        } else ToolUtils.putMapDrops(world, pos, player, new ItemStack(this), map);
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
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
