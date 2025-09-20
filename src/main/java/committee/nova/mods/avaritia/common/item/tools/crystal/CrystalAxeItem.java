package committee.nova.mods.avaritia.common.item.tools.crystal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
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
        super(ModToolTiers.CRYSTAL, 25, -25F,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant());
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean isJumpAttack = this.isJumpAttack(attacker);

        if (!target.level().isClientSide) {
            if (isJumpAttack) {

                DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();

                target.hurt(voidDamage, 54.0F);

                Vec3 pos = target.position();
                target.level().addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,
                        pos.x, pos.y + target.getBbHeight() / 2, pos.z,
                        (target.level().random.nextDouble() - 0.5) * 2.0,
                        (target.level().random.nextDouble() - 0.5) * 2.0,
                        (target.level().random.nextDouble() - 0.5) * 2.0);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        return super.use(world, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            serverPlayer.getCooldowns().addCooldown(serverPlayer.getUseItem().getItem(), 1200);
            serverPlayer.stopUsingItem();
            if (serverPlayer.getOffhandItem().getItem() instanceof ShieldItem) {
                serverPlayer.getOffhandItem().setDamageValue(serverPlayer.getOffhandItem().getDamageValue() / 2);
            }
            serverPlayer.level().broadcastEntityEvent(serverPlayer, (byte) 30);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getTier().getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getTier().getSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}
