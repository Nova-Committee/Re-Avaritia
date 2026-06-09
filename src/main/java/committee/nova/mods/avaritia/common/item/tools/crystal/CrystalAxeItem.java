package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalAxeItem extends AxeItem implements ITooltip {

    public CrystalAxeItem() {
        super(ModToolTiers.CRYSTAL,0, ModToolTiers.BLAZE.speed(),
                ModItems.properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean isJumpAttack = this.isJumpAttack(attacker);

        if (!target.level().isClientSide()) {
            if (isJumpAttack) {

                DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();

                target.hurt(voidDamage, 54.0F);

                Vec3 pos = target.position();
                target.level().addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        pos.x, pos.y + target.getBbHeight() / 2, pos.z,
                        (target.level().getRandom().nextDouble() - 0.5) * 2.0,
                        (target.level().getRandom().nextDouble() - 0.5) * 2.0,
                        (target.level().getRandom().nextDouble() - 0.5) * 2.0);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    private boolean isJumpAttack(LivingEntity attacker) {
        return !attacker.onGround() && attacker.getDeltaMovement().y() < -0.1;
    }
    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            serverPlayer.getCooldowns().addCooldown(serverPlayer.getUseItem(), 1200);
            serverPlayer.stopUsingItem();
            if (serverPlayer.getOffhandItem().getItem() instanceof ShieldItem) {
                serverPlayer.getOffhandItem().setDamageValue(serverPlayer.getOffhandItem().getDamageValue() / 2);
            }
            serverPlayer.level().broadcastEntityEvent(serverPlayer, (byte) 30);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
