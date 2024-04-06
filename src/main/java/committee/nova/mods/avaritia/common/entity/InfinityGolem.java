package committee.nova.mods.avaritia.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
* InfinityGolem
* @description
* @author cnlimiter
* @date 2024/4/5 11:24
* @version 1.0
*/
public class InfinityGolem extends IronGolem {
    public InfinityGolem(EntityType<? extends IronGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, Double.MAX_VALUE).add(Attributes.MOVEMENT_SPEED, 1.5D).add(Attributes.KNOCKBACK_RESISTANCE, 10.0D).add(Attributes.ATTACK_DAMAGE, Double.MAX_VALUE);
    }

}
