package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.CRYSTAL;

public class CrystalSpearItem extends Item implements ITooltip, ISwitchable, InitEnchantItem {
    public CrystalSpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .spear(CRYSTAL, 0.35f, 51f, 0.4f, 2.4f, 1.7f, 3.6f, 1.7f, 5.1f, 1.6f)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.CRYSTAL.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.CRYSTAL.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()
                        .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("attack_range_modifier"), 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)));
    }
    private static final String MODE_SHATTER = "crystal_shatter";
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.LUNGE, 3);
    public ToolMaterial getTier() {
        return ModToolTiers.CRYSTAL;
    }
    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.LUNGE)) return 3;
        return 0;
    }
    // ==================== 模式切换：shift+右键 ====================
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, MODE_SHATTER);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    // ==================== 外观 ====================
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
        if (isActive(stack, MODE_SHATTER)) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.crystal_shatter.active")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    // ==================== 专属机制：虚空伤害 + 晶爆 ====================
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();
            // 单体虚空伤害
            target.hurt(voidDamage, 50.0F);

            // 晶爆模式：3 格 AOE 虚空溅射（50%）
            if (isActive(stack, MODE_SHATTER)) {
                AABB aoeBox = target.getBoundingBox().inflate(3.0);
                List<LivingEntity> nearby = target.level().getEntitiesOfClass(
                        LivingEntity.class, aoeBox,
                        e -> e != target && e != attacker && e.isAlive());
                for (LivingEntity e : nearby) {
                    e.hurt(voidDamage, 25.0F);
                }
            }

            // 水晶粒子
            Vec3 pos = target.position();
            target.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                    pos.x, pos.y + target.getBbHeight() / 2, pos.z,
                    (target.level().getRandom().nextDouble() - 0.5) * 2.0,
                    (target.level().getRandom().nextDouble() - 0.5) * 2.0,
                    (target.level().getRandom().nextDouble() - 0.5) * 2.0);
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // ==================== 专属机制：无视盾牌格挡 ====================
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
