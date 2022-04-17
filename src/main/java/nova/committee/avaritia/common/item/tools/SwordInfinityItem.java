package nova.committee.avaritia.common.item.tools;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import nova.committee.avaritia.init.handler.InfinityHandler;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class SwordInfinityItem extends SwordItem {
    public SwordInfinityItem() {
        super(Tier.INFINITY_SWORD, 1, -2.8F, (new Properties()).tab(ModTab.TAB).fireResistant());
        setRegistryName("infinity_sword");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity player) {
        if (player.level.isClientSide) {
            return true;
        }
        if (victim instanceof Player) {
            Player pvp = (Player) victim;
            if (InfinityHandler.isInfinite(pvp)) {
                victim.hurt(new DamageSourceInfinitySword(player).bypassArmor(), 4.0F);
                return true;
            }
            if (pvp.getMainHandItem().getItem() == ModItems.infinity_sword && pvp.swinging) {
                return true;
            }
        }

        victim.lastHurtByPlayerTime = 60;
        victim.getCombatTracker().recordDamage(new DamageSourceInfinitySword(player), victim.getHealth(), victim.getHealth());
        victim.setHealth(0);
        victim.die(new EntityDamageSource("infinity", player));
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level.isClientSide && entity instanceof Player) {
            Player victim = (Player) entity;
            if (victim.isCreative() && !victim.isDeadOrDying() && victim.getHealth() > 0 && !InfinityHandler.isInfinite(victim)) {
                victim.getCombatTracker().recordDamage(new DamageSourceInfinitySword(player), victim.getHealth(), victim.getHealth());
                victim.setHealth(0);
                victim.die(new EntityDamageSource("infinity", player));
                //TODO add creative kill count
                //player.addStat(Achievements.creative_kill, 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }


}
