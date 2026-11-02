package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IToolTransform;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 19:41
 * Version: 1.0
 */
public class InfinitySwordItem extends Item implements InitEnchantItem, ISwitchable, IUndamageable, IToolTransform {
    private static final ToolMaterial INFINITY_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 9999, 100.0F, 100.0F, 9999,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("avaritia", "infinity_tool_materials")));

    public InfinitySwordItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant()
                .sword(INFINITY_MATERIAL, 0, INFINITY_MATERIAL.speed()));
    }

    public ToolMaterial getTier() {
        return INFINITY_MATERIAL;
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        var endlessDamage = ModConfig.isSwordAttackEndless.get();
        LivingEntity victim = InfinityDamageUtils.resolveLivingTarget(entity);
        if (player.level() instanceof ServerLevel serverLevel && victim != null) {
            var damageSource = player.damageSources().source(ModDamageTypes.INFINITY, victim, player);
            ToolUtils.sweepAttack(serverLevel, player, victim);//横扫
            if (victim instanceof Player pvp) {
                if (ToolUtils.isInfinite(pvp)) {
                    if (ModConfig.isSwordAttackExplode.get()) {
                        // 玩家身着无尽甲则只造成爆炸伤害
                        serverLevel.explode(player, pvp.getBlockX(), pvp.getBlockY(), pvp.getBlockZ(), 25.0F, Level.ExplosionInteraction.MOB);
                    }
                    return true;//直接返回
                }
            }

            if (endlessDamage) {
                InfinityDamageUtils.forceKill(serverLevel, victim, damageSource);
            } else if (victim instanceof EnderDragon dragon) {
                dragon.hurt(serverLevel, dragon.head, damageSource, INFINITY_MATERIAL.attackDamageBonus());
            } else {
                victim.setInvulnerableTime(0);
                victim.hurtServer(serverLevel, damageSource, INFINITY_MATERIAL.attackDamageBonus());
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, hand, "infinity_sword_kill");
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (isActive(heldItem, "infinity_sword_kill")) {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), true, ModConfig.isSwordAttackLightning.get());
            } else {
                ToolUtils.aoeAttack(player, ModConfig.swordAttackRange.get(), ModConfig.swordRangeDamage.get(), false, ModConfig.isSwordAttackLightning.get());
            }
            player.getCooldowns().addCooldown(heldItem, 20);
        }
        level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 5.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.LOOTING) ? 10 : 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            Holder<Enchantment> looting = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(looting, 10)).build());
        }
        if (isActive(stack, "infinity_sword_kill")) {
            tooltipComponents.accept(Component.translatable("tooltip.avaritia.sword_kill_mode.active").withStyle(net.minecraft.ChatFormatting.RED));
        }
    }
}
