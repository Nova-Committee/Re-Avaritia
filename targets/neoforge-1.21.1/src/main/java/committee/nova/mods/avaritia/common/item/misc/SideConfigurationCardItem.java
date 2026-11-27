package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 侧面配置卡
 * 用于读取、存储和传输机器的SideConfiguration设置
 *
 * 功能说明：
 * 1. Shift+右键机器：读取其SideConfiguration设置并保存到配置卡
 * 2. 右键对准同种机器：覆写机器的SideConfiguration配置
 * 3. Shift+右键空气：清除物品保存的SideConfiguration设置
 *
 * @author cnlimiter
 * Date: 2025/11/14
 * Version: 1.0
 */
public class SideConfigurationCardItem extends Item {
    private static final String SIDE_CONFIG_TAG = "SideConfig";

    enum CardAction {
        SAVE,
        APPLY,
        CLEAR,
        PASS
    }

    public SideConfigurationCardItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.RARE));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // 检查是否有保存的配置
        if (hasSavedConfig(stack)) {
            tooltipComponents.add(Component.translatable("tooltip.avaritia.side_config_card.has_config"));
            tooltipComponents.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_right_click"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.avaritia.side_config_card.no_config"));
            tooltipComponents.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_right_click"));
        }
        tooltipComponents.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_air"));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, @NotNull UseOnContext context) {
        return handleBlockUse(stack, context);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return handleBlockUse(context.getItemInHand(), context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                            @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !hasSavedConfig(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            clearSavedConfig(stack);
            player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.cleared"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private InteractionResult handleBlockUse(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        ITileIO tileIO = blockEntity instanceof ITileIO compatibleTile ? compatibleTile : null;
        CardAction action = decideAction(tileIO != null, player.isShiftKeyDown(), hasSavedConfig(stack));
        if (action == CardAction.PASS) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            executeAction(action, stack, player, tileIO);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void executeAction(CardAction action, ItemStack stack, Player player, ITileIO tileIO) {
        switch (action) {
            case SAVE -> {
                saveConfigToItem(stack, tileIO.getSideConfiguration());
                player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.read_success"), true);
            }
            case APPLY -> {
                tileIO.setSideConfiguration(loadConfigFromItem(stack));
                player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.apply_success"), true);
            }
            case CLEAR -> {
                clearSavedConfig(stack);
                player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.cleared"), true);
            }
            case PASS -> {
            }
        }
    }

    static CardAction decideAction(boolean compatibleTile, boolean shiftDown, boolean hasSavedConfig) {
        if (compatibleTile) {
            if (shiftDown) {
                return CardAction.SAVE;
            }
            return hasSavedConfig ? CardAction.APPLY : CardAction.PASS;
        }
        return shiftDown && hasSavedConfig ? CardAction.CLEAR : CardAction.PASS;
    }

    /**
     * 将SideConfiguration保存到物品的NBT中
     */
    static void saveConfigToItem(ItemStack stack, SideConfiguration config) {
        ItemUtils.updateTag(stack, tag -> tag.put(SIDE_CONFIG_TAG, config.toNBT()));
    }

    /**
     * 从物品的NBT中加载SideConfiguration
     */
    static SideConfiguration loadConfigFromItem(ItemStack stack) {
        if (!hasSavedConfig(stack)) {
            return new SideConfiguration();
        }
        return SideConfiguration.fromNBT(ItemUtils.getOrCreateTag(stack).getCompound(SIDE_CONFIG_TAG));
    }

    static boolean hasSavedConfig(ItemStack stack) {
        return ItemUtils.hasTag(stack)
                && ItemUtils.getOrCreateTag(stack).contains(SIDE_CONFIG_TAG, CompoundTag.TAG_COMPOUND);
    }

    static void clearSavedConfig(ItemStack stack) {
        ItemUtils.updateTag(stack, tag -> tag.remove(SIDE_CONFIG_TAG));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        // 如果配置卡中有配置，则显示附魔光效
        return hasSavedConfig(stack);
    }
}
