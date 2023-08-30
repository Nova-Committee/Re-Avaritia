package committee.nova.mods.avaritia.init.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author cnlimiter
 * CreateTime 2023/6/15 0:39
 * Name ModDamageTypes
 * Description
 */

public class ModDamageTypes  extends EntityDamageSource {
    public ModDamageTypes(Entity source) {
        super("infinity", source);
        bypassArmor();
        bypassInvul();
        bypassMagic();
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity damageSourceEntity) {
        ItemStack itemstack = damageSourceEntity.getMainHandItem();
        String s = "death.attack.infinity";
        int rando = entity.getLevel().random.nextInt(5);
        if (rando != 0) {
            s = s + "." + rando;
        }
        return Component.translatable(s, entity.getDisplayName(), itemstack.getDisplayName());
    }

    @Override
    public boolean scalesWithDifficulty() {
        return false;
    }
}
