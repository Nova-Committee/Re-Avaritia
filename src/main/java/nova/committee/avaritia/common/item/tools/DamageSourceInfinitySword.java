package nova.committee.avaritia.common.item.tools;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:50
 * Version: 1.0
 */
public class DamageSourceInfinitySword extends EntityDamageSource {
    public DamageSourceInfinitySword(Entity source) {
        super("infinity", source);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity damageSourceEntity) {
        ItemStack itemstack = damageSourceEntity instanceof LivingEntity ? ((LivingEntity) damageSourceEntity).getMainHandItem() : null;
        String s = "death.attack.infinity";
        int rando = entity.getLevel().random.nextInt(5);
        if (rando != 0) {
            s = s + "." + rando;
        }
        return new TranslatableComponent(s, entity.getDisplayName(), itemstack.getDisplayName());
    }

    @Override
    public boolean scalesWithDifficulty() {
        return false;
    }
}
