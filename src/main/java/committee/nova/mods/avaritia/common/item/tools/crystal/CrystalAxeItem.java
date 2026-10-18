package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalAxeItem extends Item implements ITooltip {

    public CrystalAxeItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .axe(ModToolTiers.CRYSTAL, 0, ModToolTiers.CRYSTAL.speed()));
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean isJumpAttack = this.isJumpAttack(attacker);

        if (!target.level().isClientSide() && target.level() instanceof ServerLevel serverLevel) {
            if (isJumpAttack) {
                DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();

                target.hurtServer(serverLevel, voidDamage, 54.0F);

                Vec3 pos = target.position();
                target.level().addParticle(ParticleTypes.PORTAL,
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
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        return super.use(world, player, hand);
    }

    //由攻击事件 hook 调用（26.3 无原版 onLeftClickEntity 回调）
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            serverPlayer.getCooldowns().addCooldown(serverPlayer.getUseItem(), 1200);
            serverPlayer.stopUsingItem();
            if (serverPlayer.getOffhandItem().getItem() instanceof ShieldItem) {
                serverPlayer.getOffhandItem().setDamageValue(serverPlayer.getOffhandItem().getDamageValue() / 2);
            }
            serverPlayer.level().broadcastEntityEvent(serverPlayer, (byte) 30);
        }
        return false;//返回 false 让原版攻击继续（true 会取消攻击）
    }
}
