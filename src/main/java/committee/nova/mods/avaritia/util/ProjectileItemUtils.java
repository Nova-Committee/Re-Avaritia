package committee.nova.mods.avaritia.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
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
import net.minecraftforge.registries.ForgeRegistries;
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
            if (entity instanceof ThrowableItemProjectile projectile && projectile.getItem().is(item)) {
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
}
