package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import lombok.NonNull;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.BLAZE;

public class BlazeSpearItem extends Item implements ITooltip, InitEnchantItem {
    public BlazeSpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .spear(BLAZE, 0.5f, 26f, 0.4f, 1.8f, 1.4f, 2.7f, 1.4f, 3.9f, 1.2f)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.BLAZE.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.BLAZE.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()
                        .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("attack_range_modifier"), 4.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)));
    }
    public ToolMaterial getTier() {
        return ModToolTiers.BLAZE;
    }
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);

    // ==================== Fire Aspect 10 ====================
    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.FIRE_ASPECT) ? 10 : 0;
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    // ==================== 专属机制：烈焰迸发 ====================
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            // 主目标点燃 15 秒 (300 ticks)
            target.setRemainingFireTicks(300);

            // AOE：周围 3 格内敌人点燃 10 秒 (200 ticks)
            AABB aoeBox = target.getBoundingBox().inflate(3.0);
            List<LivingEntity> nearby = target.level().getEntitiesOfClass(
                    LivingEntity.class, aoeBox,
                    e -> e != target && e != attacker && e.isAlive());
            for (LivingEntity e : nearby) {
                e.setRemainingFireTicks(200);
            }

            // 火焰粒子爆发
            var pos = target.position();
            for (int i = 0; i < 10; i++) {
                target.level().addParticle(ParticleTypes.FLAME,
                        pos.x + (target.level().getRandom().nextDouble() - 0.5) * target.getBbWidth(),
                        pos.y + target.level().getRandom().nextDouble() * target.getBbHeight(),
                        pos.z + (target.level().getRandom().nextDouble() - 0.5) * target.getBbWidth(),
                        0.0, 0.05, 0.0);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // ==================== 无视无敌帧（Blaze 系列标配） ====================
    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        entity.setInvulnerable(false);
        return super.onLeftClickEntity(stack, player, entity);
    }
}
