package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.TNTProEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 无尽远程武器共用的弹药识别与投射物创建逻辑。
 */
public final class ProjectileItemUtils {
    private ProjectileItemUtils() {
    }

    public static boolean isLaunchableProjectileItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof ProjectileItem
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.FIRE_CHARGE)
                || stack.is(Items.TNT);
    }

    public static ItemStack copySingle(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    @Nullable
    public static LaunchProjectile createLaunchProjectile(Level level, LivingEntity shooter, ItemStack sourceStack) {
        if (!isLaunchableProjectileItem(sourceStack)) {
            return null;
        }

        ItemStack ammo = copySingle(sourceStack);
        Projectile projectile;
        if (ammo.getItem() instanceof ArrowItem arrowItem) {
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, shooter, ItemStack.EMPTY);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            projectile = arrow;
        } else if (ammo.is(Items.ENDER_PEARL)) {
            projectile = new ThrownEnderpearl(level, shooter, ammo);
        } else if (ammo.is(Items.TNT)) {
            TNTProEntity tnt = new TNTProEntity(level, shooter.getX(), shooter.getEyeY(), shooter.getZ(), shooter);
            tnt.setItem(new ItemStack(Blocks.TNT));
            projectile = tnt;
        } else if (ammo.is(Items.FIRE_CHARGE)) {
            projectile = new SmallFireball(level, shooter, shooter.getLookAngle());
        } else if (ammo.getItem() instanceof ProjectileItem projectileItem) {
            Position position = new LaunchPosition(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
            Direction direction = Direction.getApproximateNearest(shooter.getLookAngle());
            projectile = projectileItem.asProjectile(level, position, ammo, direction);
            projectile.setOwner(shooter);
            if (projectile instanceof AbstractArrow arrow) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            if (projectile instanceof EndestPearlEntity endestPearl) {
                endestPearl.setShooter(shooter);
            }
        } else {
            return null;
        }

        projectile.setOwner(shooter);
        return new LaunchProjectile(projectile, soundFor(ammo), velocityFor(ammo), 1.0F, xRotOffsetFor(ammo));
    }

    private static SoundEvent soundFor(ItemStack ammo) {
        if (ammo.getItem() instanceof ArrowItem) return SoundEvents.CROSSBOW_SHOOT;
        if (ammo.getItem() instanceof FireworkRocketItem) return SoundEvents.FIREWORK_ROCKET_SHOOT;
        if (ammo.getItem() instanceof TridentItem) return SoundEvents.TRIDENT_THROW.value();
        if (ammo.is(Items.ENDER_PEARL)) return SoundEvents.ENDER_PEARL_THROW;
        if (ammo.is(Items.SNOWBALL)) return SoundEvents.SNOWBALL_THROW;
        if (ammo.is(Items.EGG)) return SoundEvents.EGG_THROW;
        if (ammo.getItem() instanceof ExperienceBottleItem) return SoundEvents.EXPERIENCE_BOTTLE_THROW;
        if (ammo.getItem() instanceof ThrowablePotionItem) {
            return ammo.is(Items.LINGERING_POTION) ? SoundEvents.LINGERING_POTION_THROW : SoundEvents.SPLASH_POTION_THROW;
        }
        if (ammo.getItem() instanceof WindChargeItem) return SoundEvents.WIND_CHARGE_THROW;
        if (ammo.is(Items.TNT)) return SoundEvents.TNT_PRIMED;
        if (ammo.is(Items.FIRE_CHARGE)) return SoundEvents.BLAZE_SHOOT;
        return SoundEvents.TRIDENT_THROW.value();
    }

    private static float velocityFor(ItemStack ammo) {
        if (ammo.getItem() instanceof ThrowablePotionItem) return 0.5F;
        if (ammo.getItem() instanceof ExperienceBottleItem) return 0.7F;
        if (ammo.is(Items.FIRE_CHARGE)) return 1.0F;
        if (ammo.is(Items.TNT)) return 1.5F;
        if (ammo.getItem() instanceof ArrowItem
                || ammo.getItem() instanceof FireworkRocketItem
                || ammo.getItem() instanceof TridentItem) {
            return 2.5F;
        }
        return 1.5F;
    }

    private static float xRotOffsetFor(ItemStack ammo) {
        return ammo.getItem() instanceof ExperienceBottleItem || ammo.getItem() instanceof ThrowablePotionItem
                ? -20.0F
                : 0.0F;
    }

    private record LaunchPosition(double x, double y, double z) implements Position {
    }

    public record LaunchProjectile(@NotNull Projectile entity, @NotNull SoundEvent sound, float velocity,
                                   float inaccuracy, float xRotOffset) {
        public void shootFromRotation(LivingEntity shooter, float yawOffset) {
            entity.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
            entity.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yawOffset,
                    xRotOffset, velocity, inaccuracy);
        }
    }
}
