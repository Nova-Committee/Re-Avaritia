package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.INFINITY;

public class InfinitySpearItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable {
    public InfinitySpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .spear(INFINITY,0.25f,9999f,0.4f,7.5f,5.1f,10.5f,5.1f,15f,4.6f)
                .component(DataComponents.ATTACK_RANGE, new AttackRange(1.0F, 9.5F, 0.0F, 11.5F, 0.125F, 0.5F))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.INFINITY.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.INFINITY.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()
                        ));
    }
    private static final String MODE_LUNGE = "infinity_spear_lunge";
    private static final int LUNGE_LEVEL = 10;
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.LOOTING, 10);

    public ToolMaterial getTier() {
        return ModToolTiers.INFINITY;
    }
    // ==================== 模式切换：shift+右键 ====================
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, MODE_LUNGE);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    // ==================== Looting 10 常驻 + 突进模式才生效的原版 LUNGE ====================
    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.LOOTING)) return 10;
        if (enchantmentHolder.is(Enchantments.LUNGE) && isActive((ItemStack) stack, MODE_LUNGE)) return LUNGE_LEVEL;
        return 0;
    }

    // ==================== 一击必杀 ====================
    /**
     * 矛的两条攻击路径（左键穿刺 PiercingWeapon / 右键蓄力 KineticWeapon）
     * 都会在伤害结算后调用 hurtEnemy，这里统一挂秒杀逻辑。
     */
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player && target.level() instanceof ServerLevel serverLevel) {
            // 获取是否启用无限伤害的配置选项
            var endlessDamage = ModConfig.isSwordAttackEndless.get();
            // 创建伤害源
            var damageSource = ModDamageTypes.source(player.level(), target, player);
            // PvP 保护：穿全套无尽甲的玩家不秒杀，只产生爆炸（与无尽剑一致）
            if (target instanceof Player pvp && ToolUtils.isInfinite(pvp)) {
                serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                return;
            }
            // 如果启用无限伤害
            if (endlessDamage) {
                // 直接强制杀死目标
                InfinityDamageUtils.forceKill(serverLevel, target, damageSource);
            } else if (target instanceof EnderDragon dragon) {
                dragon.hurt(serverLevel, dragon.head, damageSource, ModToolTiers.INFINITY.attackDamageBonus());
            } else {
                target.invulnerableTime = 0;
                target.hurtServer(serverLevel, damageSource, ModToolTiers.INFINITY.attackDamageBonus());
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    // ==================== 不朽物品实体 ====================
    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    // ==================== 外观/耐久 ====================
    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        // Looting 10 常驻显示
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        if (isActive(stack, MODE_LUNGE)) {
            // 切换后显示：“自带 突刺 X”（原版 LUNGE 附魔名 + 等级）
            var registries = context.registries();
            if (registries != null) {
                registries.lookupOrThrow(Registries.ENCHANTMENT)
                        .get(Enchantments.LUNGE)
                        .ifPresent(holder -> tooltipComponents.accept(
                                ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(holder, LUNGE_LEVEL)).build()));
            }
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.infinity_spear_lunge.active")
                    .withStyle(ChatFormatting.RED));
        }
    }
}

