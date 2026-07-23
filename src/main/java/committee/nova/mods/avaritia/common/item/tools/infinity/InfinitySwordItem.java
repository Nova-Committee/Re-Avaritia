package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.api.iface.transform.IToolTransform;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class InfinitySwordItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable, IToolTransform {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.LOOTING, 10);
    public InfinitySwordItem() {
        super(ModItems.properties()
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
                        .sword(ModToolTiers.INFINITY, 0, ModToolTiers.INFINITY.speed())
                        .attributes(ItemAttributeModifiers.builder()
                                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.INFINITY.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.INFINITY.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                                .build()
                                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("attack_range_modifier"), 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND))
        );
    }

    public ToolMaterial getTier() {
        return ModToolTiers.INFINITY;
    }


    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, Player player, @NotNull Entity entity) {
        var endlessDamage = ModConfig.isSwordAttackEndless.get();
        LivingEntity victim = InfinityDamageUtils.resolveLivingTarget(entity);
        if (player.level() instanceof ServerLevel serverLevel && victim != null) {
            var damageSource = ModDamageTypes.source(player.level(), victim, player);
            ToolUtils.sweepAttack(serverLevel, player, victim);
            if (victim instanceof Player pvp && ToolUtils.isInfinite(pvp)) {
                // 玩家身着无尽甲时保留原有 PvP 保护，只产生爆炸效果。
                serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                return true;
            }

            if (endlessDamage) {
                InfinityDamageUtils.forceKill(serverLevel, victim, damageSource);
            } else if (victim instanceof EnderDragon dragon) {
                dragon.hurt(serverLevel, dragon.head, damageSource, ModToolTiers.INFINITY.attackDamageBonus());
            } else {
                victim.invulnerableTime = 0;
                victim.hurtServer(serverLevel, damageSource, ModToolTiers.INFINITY.attackDamageBonus());
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_sword_kill");
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (isActive(heldItem, "infinity_sword_kill")) {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), true,
                        ModConfig.isSwordAttackLightning.get(), ModConfig.isSwordAttackEndless.get());
            } else {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), false,
                        ModConfig.isSwordAttackLightning.get(), ModConfig.isSwordAttackEndless.get());
            }
            player.getCooldowns().addCooldown(heldItem, 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResult.SUCCESS;
    }


    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }


    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.LOOTING)) {
            return 10;
        }
        return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        if (isActive(stack, "infinity_sword_kill")) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.sword_kill_mode.active").withStyle(net.minecraft.ChatFormatting.RED));
        }
    }
}
