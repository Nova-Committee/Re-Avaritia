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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
                .component(DataComponents.ATTACK_RANGE, new AttackRange(1.5F, 7.5F, 0.0F, 9.5F, 0.125F, 0.5F))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.CRYSTAL.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.CRYSTAL.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()));
    }
    private static final String MODE_SHATTER = "crystal_shatter";
    /** 每点护甲值增加的伤害倍率 */
    private static final float ARMOR_BONUS = 0.25F;
    /** 每点盔甲韧性增加的伤害倍率 */
    private static final float TOUGHNESS_BONUS = 0.40F;
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
    // ==================== 攻击结算：护甲/韧性加成虚空伤害 + 晶爆 + 破盾 ====================
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player && target.level() instanceof ServerLevel serverLevel && target.isAlive()) {
            // 破盾：被击中的持盾玩家盾牌失效（只强制停用+冷却）
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCooldowns().addCooldown(serverPlayer.getUseItem(), 1200);
                serverPlayer.stopUsingItem();
                serverPlayer.level().broadcastEntityEvent(serverPlayer, (byte) 30);
            }

            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // 每点护甲 +25% 伤害、每点韧性 +40% 伤害，可叠加
            float multiplier = 1.0F + target.getArmorValue() * ARMOR_BONUS
                    + (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * TOUGHNESS_BONUS;

            DamageSource voidDamage = target.level().damageSources().fellOutOfWorld();

            target.invulnerableTime = 0; // 重置无敌帧，防止伤害丢失
            target.hurtServer(serverLevel, voidDamage, baseDamage * multiplier);

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
                    float eMultiplier = 1.0F + e.getArmorValue() * ARMOR_BONUS
                            + (float) e.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * TOUGHNESS_BONUS;
                    e.invulnerableTime = 0;
                    e.hurtServer(serverLevel, voidDamage, baseDamage * 0.5F * eMultiplier);
                }
            }

            // 蓄力突刺（KineticWeapon）原版不会触发 postPiercingAttack，这里补调用以保留自带突刺附魔；
            // 左键穿刺（PiercingWeapon）原版已触发，重复调用会造成二次突进，所以用 isUsingItem 区分
            if (player.isUsingItem()) {
                EnchantmentHelper.doPostPiercingAttackEffects(serverLevel, player);
            }
        }
        super.hurtEnemy(stack, target, attacker);
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
}
