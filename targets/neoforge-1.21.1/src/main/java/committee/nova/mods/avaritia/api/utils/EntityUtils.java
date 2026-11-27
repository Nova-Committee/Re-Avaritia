package committee.nova.mods.avaritia.api.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/16 01:23
 * @Description:
 */
public class EntityUtils {
    public static Stream<EntityHitResult> findHitEntities(Level world, Projectile projectile, Vec3 startPos, Vec3 endPos, Predicate<Entity> filter) {

        Vec3 padding = new Vec3(projectile.getBbWidth() * 0.5, projectile.getBbHeight() * 0.5, projectile.getBbWidth() * 0.5);
        return findHitEntities(world, projectile, startPos, endPos, projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()).inflate(1.5D), padding, filter);
    }

    public static Stream<EntityHitResult> findHitEntities(Level world, Entity exclude, Vec3 startPos, Vec3 endPos, double padding, Predicate<Entity> filter) {

        return findHitEntities(world, exclude, startPos, endPos, new Vec3(padding, padding, padding), filter);
    }

    public static Stream<EntityHitResult> findHitEntities(Level world, Entity exclude, Vec3 startPos, Vec3 endPos, Vec3 padding, Predicate<Entity> filter) {

        return findHitEntities(world, exclude, startPos, endPos, new AABB(startPos, endPos).inflate(padding.x, padding.y, padding.z), padding, filter);
    }

    public static Stream<EntityHitResult> findHitEntities(Level world, Entity exclude, Vec3 startPos, Vec3 endPos, AABB searchArea, Vec3 padding, Predicate<Entity> filter) {

        return findHitEntities(world.getEntities(exclude, searchArea, filter).stream(), startPos, endPos, padding);
    }

    public static Stream<EntityHitResult> findHitEntities(Stream<Entity> entities, Vec3 startPos, Vec3 endPos, Vec3 padding) {

        return entities.map(entity -> entity.getBoundingBox().inflate(padding.x(), padding.y(), padding.z()).clip(startPos, endPos).map(hitPos -> new EntityHitResult(entity, hitPos)).orElse(null))
                .filter(Objects::nonNull);
    }
}
