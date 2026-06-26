package committee.nova.mods.avaritia.common.entity;


import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Description:
 * @author cnlimiter,Cu6
 * Date: 2022/3/31 14:33
 * Version: 1.0
 */
public class ImmortalItemEntity extends ItemEntity {
    private static final double STOP_PULLING_DISTANCE = 1.0D;
    private static final double HOMING_SPEED_SCALE = 0.1D;
    private Player followingPlayer;
    @Nullable
    private UUID lockedHomingPlayerId;

    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.lifespan = Integer.MAX_VALUE;
        this.setUnlimitedLifetime();
    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, double x, double y, double z, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.setItem(itemStack);
            entity.setDefaultPickUpDelay();
            entity.applyImmortalLifetime();
        }
        return entity;

    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, Entity location, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level);
        if (entity != null) {
            entity.restoreFrom(location);
            entity.setItem(itemStack);
            entity.lockedHomingPlayerId = getLockedHomingPlayerId(location);
            entity.applyImmortalLifetime();
        }
        return entity;
    }

    private void applyImmortalLifetime() {
        this.lifespan = Integer.MAX_VALUE;
        this.setUnlimitedLifetime();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return source == this.damageSources().fellOutOfWorld();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            updateHomingMotion();
        }
    }

    private void updateHomingMotion() {
        if (this.pickupDelay > 0) {
            return;
        }

        UUID lockedPlayerId = this.lockedHomingPlayerId;
        if (!isValidFollowingPlayer(lockedPlayerId)) {
            this.followingPlayer = findFollowingPlayer(lockedPlayerId);
        }
        if (this.followingPlayer == null) {
            return;
        }

        double distance = this.distanceTo(this.followingPlayer);
        if (distance <= STOP_PULLING_DISTANCE) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 direction = new Vec3(
                this.followingPlayer.getX() - this.getX(),
                this.followingPlayer.getY() + this.followingPlayer.getEyeHeight() - this.getY(),
                this.followingPlayer.getZ() - this.getZ()
        ).normalize();
        double speed = Math.min(distance * HOMING_SPEED_SCALE, ModConfig.immortalItemEntitySpeed.get());

        this.setDeltaMovement(direction.scale(speed).add(0, -0.02D, 0));
    }

    @Nullable
    private static UUID getLockedHomingPlayerId(Entity location) {
        if (!(location instanceof ItemEntity itemEntity)) {
            return null;
        }

        CompoundTag itemData = new CompoundTag();
        itemEntity.addAdditionalSaveData(itemData);
        return getLockedHomingPlayerId(itemData);
    }

    @Nullable
    private static UUID getLockedHomingPlayerId(CompoundTag itemData) {
        if (itemData.hasUUID("Owner")) {
            return itemData.getUUID("Owner");
        }
        if (itemData.hasUUID("Thrower")) {
            return itemData.getUUID("Thrower");
        }
        return null;
    }

    @Nullable
    private Player findFollowingPlayer(@Nullable UUID lockedPlayerId) {
        if (lockedPlayerId == null) {
            return this.level().getNearestPlayer(this, ModConfig.immortalItemEntityRange.get());
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Player player = serverLevel.getPlayerByUUID(lockedPlayerId);
        return isPlayerInHomingRange(player) ? player : null;
    }

    private boolean isValidFollowingPlayer(@Nullable UUID lockedPlayerId) {
        return this.followingPlayer != null
                && (lockedPlayerId == null || this.followingPlayer.getUUID().equals(lockedPlayerId))
                && isPlayerInHomingRange(this.followingPlayer);
    }

    private boolean isPlayerInHomingRange(@Nullable Player player) {
        return player != null
                && player.isAlive()
                && player.level() == this.level()
                && this.distanceTo(player) <= ModConfig.immortalItemEntityRange.get();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lockedHomingPlayerId = getLockedHomingPlayerId(compound);
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        super.remove(pReason);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }
}
