package com.avaritia.common.entity.ball;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/7 下午8:26
 * @Description:
 */
public class FireBallEntity extends ThrowableProjectile {
    public FireBallEntity(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        this.discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof ServerPlayer player) {
                BlockPos pos = result.getBlockPos();
                BlockState state = this.level().getBlockState(pos);
                if (state.is(Blocks.OBSIDIAN)) {
                    this.level().setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
                } else if (state.is(Blocks.SAND)) {
                    BlockPos.betweenClosedStream(pos.offset(-2, -2, -2), pos.offset(2, 2, 2)).forEach((currentPos) -> {
                        if (this.level().getBlockState(currentPos).is(Blocks.SAND)) {
                            checkedPlaceBlock(player, currentPos.immutable(), Blocks.GLASS.defaultBlockState());
                        }

                    });
                } else {
                    BlockPos.betweenClosedStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).forEach((currentPos) -> {
                        if (this.level().isEmptyBlock(currentPos)) {
                            checkedPlaceBlock(player, currentPos.immutable(), Blocks.FIRE.defaultBlockState());
                        }

                    });
                }
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof Player) {
                Entity ent = result.getEntity();
                ent.setRemainingFireTicks(100);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    ent.hurtServer(serverLevel, this.level().damageSources().inFire(), 50.0F);
                } else {
                    ent.hurtOrSimulate(this.level().damageSources().inFire(), 50.0F);
                }
            }
        }

    }

    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    private static boolean checkedPlaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        if (Arrays.stream(Direction.values()).anyMatch(direction -> !player.mayUseItemAt(pos, direction, ItemStack.EMPTY))) {
            return false;
        }
        return player.level().setBlockAndUpdate(pos, state);
    }
}
