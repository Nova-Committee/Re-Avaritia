package committee.nova.mods.avaritia.common.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    private static final int RETURN_PICKUP_DELAY = 5;
    private static final int INFINITE_PICKUP_DELAY = 32767;
    @Nullable
    private UUID lockedReturnPlayerId;

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
            entity.setPickUpDelay(RETURN_PICKUP_DELAY);
            entity.applyImmortalLifetime();
        }
        return entity;

    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, Entity location, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level);
        if (entity != null) {
            entity.restoreFrom(location);
            entity.setItem(itemStack);
            entity.lockedReturnPlayerId = getLockedReturnPlayerId(location);
            entity.lockToReturnPlayer();
            entity.shortenPickupDelay();
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
            returnToOwner();
        }
    }

    private void returnToOwner() {
        if (this.pickupDelay > 0) {
            return;
        }

        Player player = findReturnPlayer();
        if (player == null) {
            return;
        }

        ItemStack remaining = this.getItem().copy();
        if (remaining.isEmpty()) {
            return;
        }

        player.getInventory().add(remaining);
        if (remaining.isEmpty()) {
            this.discard();
            return;
        }

        this.setItem(remaining);
        this.teleportTo(player.getX(), player.getY() + 0.25D, player.getZ());
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.setPickUpDelay(RETURN_PICKUP_DELAY);
        this.lockToReturnPlayer();
    }

    private void shortenPickupDelay() {
        if (this.pickupDelay != INFINITE_PICKUP_DELAY) {
            this.pickupDelay = Math.min(this.pickupDelay, RETURN_PICKUP_DELAY);
        }
    }

    @Nullable
    private static UUID getLockedReturnPlayerId(Entity location) {
        if (!(location instanceof ItemEntity itemEntity)) {
            return null;
        }

        CompoundTag itemData = new CompoundTag();
        itemEntity.addAdditionalSaveData(itemData);
        return getLockedReturnPlayerId(itemData);
    }

    @Nullable
    private static UUID getLockedReturnPlayerId(CompoundTag itemData) {
        if (itemData.hasUUID("Owner")) {
            return itemData.getUUID("Owner");
        }
        if (itemData.hasUUID("Thrower")) {
            return itemData.getUUID("Thrower");
        }
        return null;
    }

    @Nullable
    private Player findReturnPlayer() {
        if (this.lockedReturnPlayerId == null) {
            return null;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Player player = serverLevel.getPlayerByUUID(this.lockedReturnPlayerId);
        return isValidReturnPlayer(player) ? player : null;
    }

    private boolean isValidReturnPlayer(@Nullable Player player) {
        return player != null
                && player.isAlive()
                && player.level() == this.level();
    }

    private void lockToReturnPlayer() {
        if (this.lockedReturnPlayerId != null) {
            this.setTarget(this.lockedReturnPlayerId);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lockedReturnPlayerId = getLockedReturnPlayerId(compound);
        this.lockToReturnPlayer();
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
