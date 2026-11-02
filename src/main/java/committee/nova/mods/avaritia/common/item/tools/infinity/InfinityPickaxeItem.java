package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class InfinityPickaxeItem extends Item implements InitEnchantItem, IFilterItem, ISwitchable, ITooltip, IUndamageable {
    private static final ToolMaterial INFINITY_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 9999, 100.0F, 100.0F, 9999,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("avaritia", "infinity_tool_materials")));

    public InfinityPickaxeItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant()
                .pickaxe(INFINITY_MATERIAL, 0, INFINITY_MATERIAL.speed()));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        if (isActive(stack, "infinity_pickaxe_hammer")) {
            return 8888.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 9999.0F);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(world, player, hand, "infinity_pickaxe_hammer");
            return InteractionResult.SUCCESS;
        }
        Holder<Enchantment> silkTouch = player.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) > 0) {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendSystemMessage(Component.translatable("tooltip.infinity_pickaxe.enchant_1"), true);
            ItemUtils.clearEnchants(stack, Enchantments.SILK_TOUCH);
            return InteractionResult.SUCCESS;
        } else {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendSystemMessage(Component.translatable("tooltip.infinity_pickaxe.enchant_2"), true);
            stack.enchant(silkTouch, 1);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity victim, @NotNull LivingEntity player) {
        if (isActive(stack, "infinity_pickaxe_hammer")) {
            if (!(victim instanceof Player)) {
                int i = 10;
                victim.setDeltaMovement(-Mth.sin(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F, 2.0D, Mth.cos(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F);
            }
        }
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (miningEntity instanceof ServerPlayer player && isActive(stack, "infinity_pickaxe_hammer")) {
            ToolUtils.destroyMaterialBlocks(player, pos, ModConfig.pickAxeBreakRange.get());
        }
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.FORTUNE) ? 10 : 0;
    }

    @Override
    public boolean hasDescTooltip() {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            Holder<Enchantment> fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(fortune, 10)).build());
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
