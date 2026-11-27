package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModToolTiers.BLAZE;

public class BlazeSpearItem extends Item implements ITooltip, InitEnchantItem, ISwitchable {
    public BlazeSpearItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .spear(BLAZE, 0.5f, 26f, 0.4f, 4f, 1.4f, 4f, 5f, 4f, 3f)
                .component(DataComponents.ATTACK_RANGE, new AttackRange(2.0F, 6.5F, 2.0F, 8.5F, 0.125F, 0.5F))
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, ModToolTiers.BLAZE.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ModToolTiers.BLAZE.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()));
    }
    private static final String MODE_BLAST = "blaze_spear_blast";
    /** 计算“附近着火单位”的半径 */
    private static final int BURNING_COUNT_RANGE = 8;
    /** 炎爆爆炸半径，与苦力怕一致 */
    private static final float BLAST_RADIUS = 3.0F;

    public ToolMaterial getTier() {
        return ModToolTiers.BLAZE;
    }
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);

    // ==================== Fire Aspect 10 ====================
    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.FIRE_ASPECT) ? 10 : 0;
    }
    // ==================== 模式切换：shift+右键 ====================
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, MODE_BLAST);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    // ==================== 攻击结算：着火数量加成 + 点燃 + 炎爆 ====================
    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (attacker instanceof Player player && target.level() instanceof ServerLevel serverLevel && target.isAlive()) {
            target.setInvulnerable(false);

            // 基础伤害 = 武器攻击伤害（ATTACK_DAMAGE 属性）
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            // 附近着火单位 N 个 → 下次伤害 × (1 + 50%×N)
            int burningCount = countBurningNearby(player);
            float finalDamage = baseDamage * (1.0F + 0.5F * burningCount);

            DamageSource source = ModDamageTypes.source(player);
            target.invulnerableTime = 0; // 重置无敌帧，防止伤害丢失
            target.hurtServer(serverLevel, source, finalDamage);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SPEAR_HIT, SoundSource.PLAYERS, 1.0F, 1.2F);

            // 火焰粒子
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    10, target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, 0.05);

            // 炎爆模式：仅当目标在被击中时已经着火，才在其位置爆炸
            boolean wasBurning = target.getRemainingFireTicks() > 0;

            // 烈焰迸发：点燃主目标 + 3 格 AOE 点燃
            igniteTargetAndNearby(target, player);

            // 炎爆模式：被击中的着火单位位置产生爆炸（伤害/范围随附近着火数动态变化）
            if (isActive(stack, MODE_BLAST) && wasBurning) {
                blast(serverLevel, player, target, finalDamage, burningCount);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    /** 统计玩家周围 BURNING_COUNT_RANGE 格内着火的存活单位数量 */
    private static int countBurningNearby(Player player) {
        AABB box = player.getBoundingBox().inflate(BURNING_COUNT_RANGE);
        return player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e.getRemainingFireTicks() > 0).size();
    }
    /** 点燃主目标 15 秒（300 tick），周围 3 格敌人 10 秒（200 tick） */
    private static void igniteTargetAndNearby(LivingEntity target, LivingEntity attacker) {
        target.setRemainingFireTicks(300);
        AABB aoeBox = target.getBoundingBox().inflate(3.0);
        List<LivingEntity> nearby = target.level().getEntitiesOfClass(
                LivingEntity.class, aoeBox,
                e -> e != target && e != attacker && e.isAlive());
        for (LivingEntity e : nearby) {
            e.setRemainingFireTicks(200);
        }
    }

    /** 在目标位置产生爆炸：伤害=本次攻击伤害，范围随附近着火数增长并封顶（苦力怕基准 3.0） */
    private static void blast(ServerLevel serverLevel, Player player, LivingEntity target,
                              float finalDamage, int burningCount) {
        float explosionRadius = Math.min(6.0F, BLAST_RADIUS * (1.0F + 0.15F * burningCount));
        Vec3 pos = target.position();
        serverLevel.explode(player, ModDamageTypes.source(player),
                new ExplosionDamageCalculator() {
                    @Override
                    public float getEntityDamageAmount(Explosion explosion, Entity entity, float exposure) {
                        return finalDamage;
                    }
                },
                pos.x, pos.y, pos.z, explosionRadius, false, Level.ExplosionInteraction.MOB);
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
        if (isActive(stack, MODE_BLAST)) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.blaze_spear_blast.active")
                    .withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
