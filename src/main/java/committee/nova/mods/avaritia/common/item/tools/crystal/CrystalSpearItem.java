package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.common.component.CrystalSpearTarget;
import committee.nova.mods.avaritia.common.item.tools.SpearMarkUtils;
import committee.nova.mods.avaritia.common.item.tools.SpearThrustUtils;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.CRYSTAL;

public class CrystalSpearItem extends Item implements ITooltip, ISwitchable {
    private static final String MODE_NORMAL = "crystal_spear_normal";
    private static final String MODE_SHATTER = "crystal_shatter";
    private static final String MODE_SEVENFOLD = "crystal_spear_sevenfold";
    private static final List<String> MODES = List.of(MODE_NORMAL, MODE_SHATTER, MODE_SEVENFOLD);

    public CrystalSpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .spear(CRYSTAL, 0.35f, 51f, 0.3f, 5f, 1.7f, 5f, 4.5f, 5f, 2f)
                .component(DataComponents.ATTACK_RANGE, new AttackRange(1.5F, 7.5F, 0.0F, 9.5F, 0.125F, 0.5F))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.CRYSTAL.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.CRYSTAL.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()));
    }
    /** 每点护甲值增加的伤害倍率 */
    private static final float ARMOR_BONUS = 0.025F;
    /** 每点盔甲韧性增加的伤害倍率 */
    private static final float TOUGHNESS_BONUS = 0.04F;
    private static final double AUTO_TARGET_RANGE = 128.0D;
    private static final int AUTO_TARGET_POOL_SIZE = 5;
    public ToolMaterial getTier() {
        return ModToolTiers.CRYSTAL;
    }
    // ==================== 模式切换：shift+右键 ====================
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(level, player, hand, MODES);
            if (!level.isClientSide()) {
                stack.remove(ModDataComponents.CRYSTAL_SPEAR_TARGET.get());
                if (isActive(stack, MODE_SEVENFOLD)) {
                    stack.set(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get(),
                            CrystalSpearTarget.MAX_THRUSTS);
                } else {
                    stack.remove(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get());
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!isActive(stack, MODE_SEVENFOLD)) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        Integer remainingThrusts = stack.get(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get());
        if (remainingThrusts == null) {
            remainingThrusts = CrystalSpearTarget.MAX_THRUSTS;
            stack.set(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get(), remainingThrusts);
        }
        if (remainingThrusts <= 0) {
            player.sendOverlayMessage(Component.translatable("message.avaritia.crystal_spear.exhausted"));
            return InteractionResult.SUCCESS_SERVER;
        }

        LivingEntity target = SpearThrustUtils.selectRandomTarget(
                serverLevel, serverPlayer, AUTO_TARGET_RANGE, AUTO_TARGET_POOL_SIZE);
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("message.avaritia.crystal_spear.no_target"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!SpearThrustUtils.movePlayerToTarget(serverLevel, serverPlayer, target)) {
            player.sendOverlayMessage(Component.translatable("message.avaritia.crystal_spear.target_unavailable"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (SpearThrustUtils.stabTarget(serverPlayer, hand, target)) {
            serverPlayer.onAttack();
            int nextRemaining = remainingThrusts - 1;
            stack.set(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get(), nextRemaining);
            if (nextRemaining == 0) {
                player.sendOverlayMessage(Component.translatable("message.avaritia.crystal_spear.exhausted"));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }
    // ==================== 攻击结算：护甲/韧性加成虚空伤害 + 晶爆 + 破盾 ====================
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player && target.level() instanceof ServerLevel serverLevel && target.isAlive()) {
            boolean newlyMarked = !SpearMarkUtils.isMarkedBy(target, player);
            SpearMarkUtils.apply(target, player);
            if (newlyMarked) {
                player.sendOverlayMessage(Component.translatable(
                        "message.avaritia.crystal_spear.marked", target.getDisplayName()));
            }

            // 破盾：被击中的持盾玩家盾牌失效（只强制停用+冷却）
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCooldowns().addCooldown(serverPlayer.getUseItem(), 1200);
                serverPlayer.stopUsingItem();
                serverPlayer.level().broadcastEntityEvent(serverPlayer, (byte) 30);
            }

            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();

            target.invulnerableTime = 0; // 重置无敌帧，防止伤害丢失
            float remoteMultiplier = SpearThrustUtils.remoteDamageMultiplier(player, target);
            target.hurtServer(serverLevel, voidDamage,
                    calculateBonusDamage(baseDamage, target) * remoteMultiplier);

            // 水晶粒子
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    8, 0.5, 0.5, 0.5, 0.1);

            // 晶爆模式：3 格 AOE 虚空溅射（50%），按每个目标自己的护甲/韧性加成
            if (isActive(stack, MODE_SHATTER)) {
                AABB aoeBox = target.getBoundingBox().inflate(3.0);
                List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                        LivingEntity.class, aoeBox,
                        e -> e != target && e != player && e.isAlive());
                for (LivingEntity e : nearby) {
                    e.invulnerableTime = 0;
                    e.hurtServer(serverLevel, voidDamage, calculateBonusDamage(baseDamage * 0.5F, e));
                }
            }

        }
        super.hurtEnemy(stack, target, attacker);
    }

    private static float calculateBonusDamage(float baseDamage, LivingEntity target) {
        float multiplier = 1.0F + target.getArmorValue() * ARMOR_BONUS
                + (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * TOUGHNESS_BONUS;
        return baseDamage * multiplier;
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
        if (isActive(stack, MODE_SEVENFOLD)) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.tool.crystal_spear_sevenfold")
                    .withStyle(ChatFormatting.AQUA));
            Integer remainingThrusts = stack.get(ModDataComponents.CRYSTAL_SPEAR_REMAINING_THRUSTS.get());
            tooltipComponents.accept(Component.translatable(
                            "tooltip.avaritia.crystal_spear_sevenfold.remaining",
                            remainingThrusts == null ? CrystalSpearTarget.MAX_THRUSTS : remainingThrusts)
                    .withStyle(ChatFormatting.AQUA));
        }
        if (isActive(stack, MODE_SHATTER)) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.crystal_shatter.active")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
