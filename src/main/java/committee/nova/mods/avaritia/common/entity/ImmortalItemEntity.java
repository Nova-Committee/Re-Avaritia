package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ImmortalItemEntity extends ItemEntity {
    private Player followingPlayer;

    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.lifespan = Integer.MAX_VALUE;
        this.setPickUpDelay(0);
        this.setUnlimitedLifetime();
    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, double x, double y, double z, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.setItem(itemStack);
            entity.setPickUpDelay(0);
        }
        return entity;
    }

    @Override
    public void tick() {
        super.tick();


        if (this.followingPlayer == null || !this.followingPlayer.isAlive()) {
            this.findClosestPlayer();
        }


        if (this.followingPlayer != null && this.followingPlayer.isAlive()) {
            this.moveToPlayer();
        }


        this.lifespan = Integer.MAX_VALUE;
        this.setPickUpDelay(0);
    }


    private void findClosestPlayer() {
        Player closestPlayer = null;
        double closestDistance = ModConfig.immortalItemEntityRange.get();

        for (Player player : this.level().players()) {
            double distance = this.distanceToSqr(player);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        this.followingPlayer = closestPlayer;
    }

    private void moveToPlayer() {
        Vec3 playerPos = new Vec3(
                this.followingPlayer.getX(),
                this.followingPlayer.getY() + this.followingPlayer.getEyeHeight(),
                this.followingPlayer.getZ()
        );

        Vec3 itemPos = new Vec3(this.getX(), this.getY(), this.getZ());
        Vec3 direction = playerPos.subtract(itemPos).normalize();

        double distance = this.distanceTo(this.followingPlayer);
        double speed = Math.min(distance * 0.1D, ModConfig.immortalItemEntitySpeed.get());

        this.setDeltaMovement(direction.scale(speed));

        if (distance < 1.0D) {
            this.setPos(playerPos.x, playerPos.y, playerPos.z);

            if (!this.level().isClientSide) {
                this.followingPlayer.getInventory().add(this.getItem());
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return source == this.damageSources().fellOutOfWorld();
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
    public boolean canChangeDimensions(@NotNull Level oldLevel, @NotNull Level newLevel) {
        return false;
    }

}
