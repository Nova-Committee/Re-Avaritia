package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.util.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

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
                            PlayerUtils.checkedPlaceBlock(player, pos.immutable(), Blocks.GLASS.defaultBlockState());
                        }

                    });
                } else {
                    BlockPos.betweenClosedStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).forEach((currentPos) -> {
                        if (this.level().isEmptyBlock(currentPos)) {
                            PlayerUtils.checkedPlaceBlock(player, currentPos.immutable(), Blocks.FIRE.defaultBlockState());
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
                ent.setSecondsOnFire(100);
                ent.hurt(this.level().damageSources().inFire(), 50.0F);
            }
        }

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    protected void defineSynchedData() {

    }
}
