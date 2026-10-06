package committee.nova.mods.avaritia.common.entity;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ImmortalItemEntity extends ItemEntity {
    private static final int RETURN_PICKUP_DELAY = 5;

    @Nullable
    private UUID lockedReturnPlayerId;

    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.applyImmortalLifetime();
    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, double x, double y, double z, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level, EntitySpawnReason.EVENT);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.setItem(itemStack);
            entity.setPickUpDelay(RETURN_PICKUP_DELAY);
            entity.applyImmortalLifetime();
        }
        return entity;
    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, Entity location, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level, EntitySpawnReason.EVENT);
        if (entity != null) {
            entity.restoreFrom(location);
            entity.setItem(itemStack);
            entity.lockedReturnPlayerId = getLockedReturnPlayerId(location);
            entity.lockToReturnPlayer();
            entity.setPickUpDelay(RETURN_PICKUP_DELAY);
            entity.applyImmortalLifetime();
        }
        return entity;
    }

    private void applyImmortalLifetime() {
        this.lifespan = Integer.MAX_VALUE;
        this.setUnlimitedLifetime();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.returnToOwner();
        }
        this.applyImmortalLifetime();
    }

    private void returnToOwner() {
        if (this.hasPickUpDelay()) {
            return;
        }

        Player player = this.findReturnPlayer();
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

    @Nullable
    private static UUID getLockedReturnPlayerId(Entity location) {
        if (!(location instanceof ItemEntity itemEntity)) {
            return null;
        }

        UUID target = itemEntity.getTarget();
        if (target != null) {
            return target;
        }
        Entity owner = itemEntity.getOwner();
        return owner == null ? null : owner.getUUID();
    }

    @Nullable
    private Player findReturnPlayer() {
        if (this.lockedReturnPlayerId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Player player = serverLevel.getPlayerByUUID(this.lockedReturnPlayerId);
        return player != null && player.isAlive() && player.level() == this.level() ? player : null;
    }

    private void lockToReturnPlayer() {
        if (this.lockedReturnPlayerId != null) {
            this.setTarget(this.lockedReturnPlayerId);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lockedReturnPlayerId = input.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.lockToReturnPlayer();
        this.applyImmortalLifetime();
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        return true;
    }

    @Override
    public boolean canTeleport(@NotNull Level oldLevel, @NotNull Level newLevel) {
        return false;
    }

}
