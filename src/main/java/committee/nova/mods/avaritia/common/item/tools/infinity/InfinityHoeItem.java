package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 16:47
 * Version: 1.0
 */
public class InfinityHoeItem extends HoeItem implements IUndamageable, ISwitchable {

    public InfinityHoeItem() {
        super(ModToolTiers.INFINITY,0, ModToolTiers.INFINITY.speed(),
                ModItems.properties()
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
        );

    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, @NotNull Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "infinity_hoe_sow");
            return InteractionResult.SUCCESS;
        }
        if (pLevel instanceof ServerLevel serverLevel && isActive(stack, "infinity_hoe_sow")) {
            player.swing(hand);
            BlockPos blockPos = player.getOnPos();
            int rang = 7;
            int height = 2;
            ToolUtils.rangeHarvest(serverLevel, player, stack, blockPos, rang, height);
            ToolUtils.rangeBonemealable(serverLevel, blockPos, rang, height,3);
            player.getCooldowns().addCooldown(stack, 10);
            serverLevel.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        var world = context.getLevel();
        var blockpos = context.getClickedPos();
        var targetBlock = world.getBlockState(blockpos).getBlock();
        var player = context.getPlayer();
        var blockstate = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7);
        var soulFarmState = ModBlocks.soul_farmland.get().defaultBlockState();
        int rang = 5;
        var minPos = blockpos.offset(-rang, 0, -rang);
        var maxPos = blockpos.offset(rang, 0, rang);
//        int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
//        if (hook != 0) return hook > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockpos.above()) &&
                (targetBlock instanceof GrassBlock || targetBlock.equals(Blocks.DIRT) || targetBlock.equals(Blocks.COARSE_DIRT))) {
            if (player != null && !world.isClientSide()) {
                if (player.isCrouching() && isActive(stack, "infinity_hoe_sow")) {
                    var boxMutable = BlockPos.betweenClosed(minPos, maxPos);
                    for (BlockPos pos : boxMutable) {
                        var state = world.getBlockState(pos);
                        var block = state.getBlock();


                        if (world.isEmptyBlock(pos.above()) && (block instanceof GrassBlock || block.equals(Blocks.DIRT) || block.equals(
                                Blocks.COARSE_DIRT) || block instanceof FarmlandBlock)) {
                            world.setBlock(pos, blockstate, 11);
                        }
                        if (world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.below())) {
                            world.setBlock(pos, blockstate, 11);
                        }
                        if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer) {
                            world.setBlock(pos, blockstate, 11);
                        }
                    }

                    Iterable<BlockPos> inBoxMutable = BlockPos.betweenClosed(minPos, maxPos.offset(0, 3, 0));
                    Iterable<BlockPos> allInBoxMutable = BlockPos.betweenClosed(minPos.offset(-1, 0, -1), maxPos.offset(1, 4, 1));
                    for (BlockPos pos : allInBoxMutable) {
                        if (!hasBox(pos, inBoxMutable)) { //澶栧３鍧愭爣
                            var state = world.getBlockState(pos);
                            if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer)
                                world.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                        }
                    }
                } else {
                    world.setBlock(blockpos, blockstate, 11); //till unsneaked
                    world.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
                }
            }
        } else if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockpos.above()) &&
                (targetBlock instanceof SoulSandBlock || targetBlock.equals(Blocks.SOUL_SOIL))) {
            if (player != null && !world.isClientSide()) {
                if (player.isShiftKeyDown() && isActive(stack, "infinity_hoe_sow")) {
                    var boxMutable = BlockPos.betweenClosed(minPos, maxPos);
                    for (BlockPos pos : boxMutable) {
                        var state = world.getBlockState(pos);
                        var block = state.getBlock();

                        if (world.isEmptyBlock(pos.above()) && (block instanceof SoulSandBlock || block.equals(Blocks.SOUL_SOIL))) {
                            world.setBlock(pos, soulFarmState, 11);
                        }
                        if (world.isEmptyBlock(pos) && !world.isEmptyBlock(pos.below())) {
                            world.setBlock(pos, soulFarmState, 11);
                        }
                        if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer) {
                            world.setBlock(pos, soulFarmState, 11);
                        }
                    }

                    Iterable<BlockPos> inBoxMutable = BlockPos.betweenClosed(minPos, maxPos.offset(0, 3, 0));
                    Iterable<BlockPos> allInBoxMutable = BlockPos.betweenClosed(minPos.offset(-1, 0, -1), maxPos.offset(1, 4, 1));
                    for (BlockPos pos : allInBoxMutable) {
                        if (!hasBox(pos, inBoxMutable)) { //澶栧３鍧愭爣
                            var state = world.getBlockState(pos);
                            if (state.getMapColor(world, pos) == MapColor.WATER || state.getBlock() instanceof LiquidBlockContainer)
                                world.setBlockAndUpdate(pos, Blocks.SOUL_SOIL.defaultBlockState());
                        }
                    }
                } else {
                    world.setBlock(blockpos, soulFarmState, 11); //soul till unsneaked
                    world.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private boolean hasBox(BlockPos pos, Iterable<BlockPos> box) {
        for (BlockPos pos1 : box) {
            if (pos1.getX() == pos.getX() && pos1.getY() == pos.getY() && pos1.getZ() == pos.getZ()) return true;
        }
        return false;
    }


}
