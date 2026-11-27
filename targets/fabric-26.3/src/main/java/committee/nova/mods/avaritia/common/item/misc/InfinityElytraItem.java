package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;

public class InfinityElytraItem extends Item {
    public InfinityElytraItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC)
                .fireResistant()
                .stacksTo(1)
                .component(DataComponents.GLIDER, Unit.INSTANCE)
                // 26.3 中能否放进装备槽完全由 EQUIPPABLE 组件决定：缺失时连原版胸甲槽都放不进去
                // （且 Trinkets 的饰品位校验也依赖它）。参数与原版鞘翅/Neo 版一致。
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                        .setAsset(EquipmentAssets.ELYTRA)
                        .setDamageOnHurt(false)
                        .build()));
    }

    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {

        if (!entity.level().isClientSide() && entity.isFallFlying()) {
            entity.resetFallDistance();
            double range = 3.0;
            AABB boundingBox = new AABB(
                    entity.getX() - range,
                    entity.getY() - range,
                    entity.getZ() - range,
                    entity.getX() + range,
                    entity.getY() + range,
                    entity.getZ() + range
            );

            entity.level().getEntitiesOfClass(LivingEntity.class, boundingBox, target -> canDamageInFlight(entity, target))
                    .forEach(target -> {
                        if (target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            target.hurtServer(serverLevel, ModDamageTypes.causeRandomDamage(entity.level(), entity), ModConfig.infinityElytraFlyingRangeDamage.get().floatValue());
                        }

                        double dx = target.getX() - entity.getX();
                        double dz = target.getZ() - entity.getZ();
                        double strength = 1.0;
                        target.push(dx * strength, 0.5, dz * strength);
                    });
        }

        if (entity.level().isClientSide() && entity.isFallFlying()) {
            createTrailParticles(entity);
        }

        return true;
    }

    private static boolean canDamageInFlight(LivingEntity entity, LivingEntity target) {
        if (target == entity || !target.isAlive() || target.isSpectator()) {
            return false;
        }
        return !(target instanceof Player player && player.isCreative());
    }

    private void createTrailParticles(LivingEntity entity) {
        RandomSource random = entity.level().getRandom();

        for (int i = 0; i < 3; i++) {
            double offsetX = random.nextGaussian() * 0.1;
            double offsetY = random.nextGaussian() * 0.1;
            double offsetZ = random.nextGaussian() * 0.1;

            double x = entity.getX() - entity.getLookAngle().x * 1.5 + offsetX;
            double y = entity.getY() + entity.getBbHeight() / 2 + offsetY;
            double z = entity.getZ() - entity.getLookAngle().z * 1.5 + offsetZ;

            entity.level().addParticle(
                    ParticleTypes.CLOUD,
                    x, y, z,
                    -entity.getDeltaMovement().x * 0.5 + offsetX * 0.5,
                    -entity.getDeltaMovement().y * 0.5 + offsetY * 0.5,
                    -entity.getDeltaMovement().z * 0.5 + offsetZ * 0.5
            );
        }
    }

}
