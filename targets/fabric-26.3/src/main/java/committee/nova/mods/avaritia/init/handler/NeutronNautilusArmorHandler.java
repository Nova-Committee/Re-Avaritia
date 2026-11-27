package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 中子鹦鹉螺铠的专属机制。
 *
 * <ul>
 *   <li><b>深水护体</b>：在水中受到的一切伤害按配置比例削减（默认 99%）；</li>
 *   <li><b>干养</b>：不在水中时氧气回满，不再窒息；</li>
 *   <li><b>流线</b>：移动速度获得额外倍率（默认 +50%）。</li>
 * </ul>
 */
public final class NeutronNautilusArmorHandler {
    private static final int TICK_INTERVAL = 20;
    private static final Identifier SPEED_MODIFIER_ID = Const.rl("neutron_nautilus_armor_speed");

    private NeutronNautilusArmorHandler() {
    }

    /** Incoming water reduction precedes vanilla armor/enchantment reduction. */
    public static float incomingDamage(LivingEntity target, float amount) {
        if (!target.level().isClientSide() && isArmoredNautilus(target)
                && (target.isInWater() || target.isUnderWater())) return reduceWaterDamage(amount);
        return amount;
    }

    /** 氧气每 tick 顶满（离水时不消耗），移速加成每 20 tick 同步一次。 */
    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity.getType() != EntityTypes.NAUTILUS) {
            return;
        }
        if (shouldRefillAir(entity)) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
        if (entity.tickCount % TICK_INTERVAL != 0) {
            return;
        }
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (isArmoredNautilus(entity)) {
                speed.addOrUpdateTransientModifier(new AttributeModifier(SPEED_MODIFIER_ID,
                        ModConfig.neutronNautilusSpeedBonus.get(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else {
                speed.removeModifier(SPEED_MODIFIER_ID);
            }
        }
    }

    /** 水下伤害换算，供测试断言减伤比例。 */
    public static float reduceWaterDamage(float amount) {
        double remaining = 1.0D - ModConfig.neutronNautilusWaterDamageReduction.get();
        return (float) (amount * Math.max(0.0D, remaining));
    }

    /** 是否应当补满氧气：穿铠且不在水里。 */
    public static boolean shouldRefillAir(LivingEntity entity) {
        return isArmoredNautilus(entity) && !entity.isInWater();
    }

    private static boolean isArmoredNautilus(LivingEntity entity) {
        return entity.isAlive()
                && entity.getType() == EntityTypes.NAUTILUS
                && entity.getItemBySlot(EquipmentSlot.BODY).is(ModItems.neutron_nautilus_armor.get());
    }
}
