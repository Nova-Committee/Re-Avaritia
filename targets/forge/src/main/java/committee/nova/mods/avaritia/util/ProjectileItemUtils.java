package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.TNTProEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ProjectileItemUtils {
    private static final Map<Item, EntityType<?>> THROWABLE_PROJECTILE_TYPES = new IdentityHashMap<>();
    private static final Set<Item> NON_THROWABLE_PROJECTILE_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());

    private ProjectileItemUtils() {
    }

    public static boolean hasMatchingThrowableItemProjectile(Level level, ItemStack stack) {
        return findThrowableProjectileType(level, stack) != null;
    }

    public static boolean isLaunchableProjectileItem(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(Items.ARROW)
                || stack.is(Items.FIRE_CHARGE)
                || stack.is(Items.SPECTRAL_ARROW)
                || stack.is(Items.TIPPED_ARROW)
                || stack.is(Items.FIREWORK_ROCKET)
                || stack.getItem() instanceof TridentItem
                || stack.is(Items.TNT)
                || hasMatchingThrowableItemProjectile(level, stack);
    }

    public static ItemStack copySingle(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    @Nullable
    public static LaunchProjectile createLaunchProjectile(Level level, LivingEntity shooter, ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            return null;
        }

        ItemStack ammo = copySingle(sourceStack);
        if (ammo.getItem() instanceof ArrowItem arrowItem) {
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, shooter);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            return new LaunchProjectile(arrow, SoundEvents.TRIDENT_THROW, 2.5F, 1.0F, 0.0F);
        }
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            FireworkRocketEntity firework = new FireworkRocketEntity(
                    level, ammo, shooter,
                    shooter.getX(), shooter.getEyeY(), shooter.getZ(),
                    true
            );
            return new LaunchProjectile(firework, SoundEvents.FIREWORK_ROCKET_SHOOT, 2.5F, 1.0F, 0.0F);
        }
        if (ammo.getItem() instanceof TridentItem) {
            ThrownTrident trident = new ThrownTrident(level, shooter, ammo);
            trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            return new LaunchProjectile(trident, SoundEvents.TRIDENT_THROW, 2.5F, 1.0F, 0.0F);
        }
        if (ammo.is(Items.TNT)) {
            TNTProEntity tnt = new TNTProEntity(level, shooter.getX(), shooter.getEyeY(), shooter.getZ(), shooter);
            tnt.setItem(new ItemStack(Blocks.TNT));
            return new LaunchProjectile(tnt, SoundEvents.TNT_PRIMED, 1.5F, 1.0F, 0.0F);
        }
        if (ammo.is(Items.FIRE_CHARGE)) {
            SmallFireball fireball = new SmallFireball(level, shooter,
                    shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z);
            fireball.setItem(ammo);
            return new LaunchProjectile(fireball, SoundEvents.BLAZE_SHOOT, 1.5F, 1.0F, 0.0F);
        }
        return createThrowableLaunchProjectile(level, shooter, ammo);
    }

    @Nullable
    private static LaunchProjectile createThrowableLaunchProjectile(Level level, LivingEntity shooter, ItemStack ammo) {
        EntityType<?> type = findThrowableProjectileType(level, ammo);
        if (type == null) {
            return null;
        }

        Entity entity = createEntitySafely(type, level);
        if (!(entity instanceof ThrowableItemProjectile projectile)) {
            if (entity != null) {
                entity.discard();
            }
            return null;
        }

        projectile.setOwner(shooter);
        projectile.setItem(ammo);
        if (projectile instanceof EndestPearlEntity endestPearl) {
            endestPearl.setShooter(shooter);
        }
        return new LaunchProjectile(projectile, getThrowableProjectileSound(ammo),
                getThrowableProjectileVelocity(ammo), 1.0F, getThrowableProjectileXRotOffset(ammo));
    }

    @Nullable
    public static EntityType<?> findThrowableProjectileType(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Item item = stack.getItem();
        EntityType<?> cachedType = THROWABLE_PROJECTILE_TYPES.get(item);
        if (cachedType != null) {
            return cachedType;
        }
        if (NON_THROWABLE_PROJECTILE_ITEMS.contains(item)) {
            return null;
        }

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            Entity entity = createEntitySafely(type, level);
            if (entity instanceof ThrowableItemProjectile projectile && hasDefaultItem(projectile, item)) {
                entity.discard();
                THROWABLE_PROJECTILE_TYPES.put(item, type);
                return type;
            }
            if (entity != null) {
                entity.discard();
            }
        }

        EntityType<?> knownType = findKnownThrowableProjectileType(stack);
        if (knownType != null) {
            THROWABLE_PROJECTILE_TYPES.put(item, knownType);
            return knownType;
        }

        NON_THROWABLE_PROJECTILE_ITEMS.add(item);
        return null;
    }

    @Nullable
    public static Entity createEntitySafely(EntityType<?> type, Level level) {
        try {
            return type.create(level);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean hasDefaultItem(ThrowableItemProjectile projectile, Item item) {
        try {
            return projectile.getItem().is(item);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @Nullable
    private static EntityType<?> findKnownThrowableProjectileType(ItemStack stack) {
        if (stack.getItem() instanceof EnderpearlItem) {
            return EntityType.ENDER_PEARL;
        }
        if (stack.getItem() instanceof SnowballItem) {
            return EntityType.SNOWBALL;
        }
        if (stack.getItem() instanceof EggItem) {
            return EntityType.EGG;
        }
        if (stack.getItem() instanceof ExperienceBottleItem) {
            return EntityType.EXPERIENCE_BOTTLE;
        }
        if (stack.getItem() instanceof ThrowablePotionItem) {
            return EntityType.POTION;
        }
        return null;
    }

    private static float getThrowableProjectileXRotOffset(ItemStack ammo) {
        if (ammo.getItem() instanceof ExperienceBottleItem || ammo.getItem() instanceof ThrowablePotionItem) {
            return -20.0F;
        }
        return 0.0F;
    }

    private static float getThrowableProjectileVelocity(ItemStack ammo) {
        if (ammo.getItem() instanceof ThrowablePotionItem) {
            return 0.5F;
        }
        if (ammo.getItem() instanceof ExperienceBottleItem) {
            return 0.7F;
        }
        return 1.5F;
    }

    private static SoundEvent getThrowableProjectileSound(ItemStack ammo) {
        if (ammo.getItem() instanceof EnderpearlItem) {
            return SoundEvents.ENDER_PEARL_THROW;
        }
        if (ammo.getItem() instanceof SnowballItem) {
            return SoundEvents.SNOWBALL_THROW;
        }
        if (ammo.getItem() instanceof EggItem) {
            return SoundEvents.EGG_THROW;
        }
        if (ammo.getItem() instanceof ExperienceBottleItem) {
            return SoundEvents.EXPERIENCE_BOTTLE_THROW;
        }
        if (ammo.getItem() instanceof ThrowablePotionItem) {
            return ammo.is(Items.LINGERING_POTION) ? SoundEvents.LINGERING_POTION_THROW : SoundEvents.SPLASH_POTION_THROW;
        }
        return SoundEvents.TRIDENT_THROW;
    }

    public record LaunchProjectile(@NotNull Projectile entity, @NotNull SoundEvent sound, float velocity,
                                   float inaccuracy, float xRotOffset) {
    }
}
