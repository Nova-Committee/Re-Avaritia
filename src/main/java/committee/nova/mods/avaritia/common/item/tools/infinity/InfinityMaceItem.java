package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfinityMaceItem extends MaceItem implements IUndamageable,InitEnchantItem{

    private final InitEnchantment WIND_BURST = new InitEnchantment(Enchantments.WIND_BURST, 5);
    private final InitEnchantment BREACH = new InitEnchantment(Enchantments.BREACH, 10);

    public InfinityMaceItem() {
        super((new Properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .attributes(createAttributes())
        );
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker instanceof ServerPlayer serverPlayer && canSmashAttack(serverPlayer)) {
            ServerLevel serverLevel = (ServerLevel) attacker.level();

            serverLevel.sendParticles(
                    ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS,
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2,
                    target.getZ(),
                    100,
                    0.5, 1, 0.5,
                    0.1
            );
            serverLevel.sendParticles(
                    ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER,
                    target.getX(),
                    target.getY() + target.getBbHeight() / 2,
                    target.getZ(),
                    50,
                    5, 7, 5,
                    0.1
            );
        }

        return result;
    }

    public static @NotNull ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 100.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, 100.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        for (int i = 0; i < 3; i++) {
            WindCharge windProjectile = EntityType.WIND_CHARGE.create(level);
            if (windProjectile != null) {
                Vec3 lookVec = player.getLookAngle();
                windProjectile.setPos(player.getX(), player.getEyeY(), player.getZ());
                windProjectile.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 1.0F);
                level.addFreshEntity(windProjectile);
            }
        }
        player.getCooldowns().addCooldown(itemStack.getItem(), 20);
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        float baseDamage = super.getAttackDamageBonus(target, damage, damageSource);

        if (damageSource.getDirectEntity() instanceof LivingEntity attacker && canSmashAttack(attacker)) {

            float entityHealthDamage = 0.25F;
            float additionalDamage = target instanceof LivingEntity livingTarget ? livingTarget.getHealth() * entityHealthDamage : 0.0F;
            return baseDamage + additionalDamage;
        }

        return baseDamage;
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.BREACH)) {
            return 10;
        }else if (enchantmentHolder.is(Enchantments.WIND_BURST)) {
            return 5;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.WIND_BURST.appendHoverText(context, tooltipComponents);
        this.BREACH.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }
}
