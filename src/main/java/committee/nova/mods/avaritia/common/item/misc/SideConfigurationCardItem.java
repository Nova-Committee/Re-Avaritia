package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;

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
    private static final String TAG_SIDE_CONFIG = "SideConfig";

    public SideConfigurationCardItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.RARE));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 检查是否有保存的配置
        if (hasSavedConfig(stack)) {
            tooltip.add(Component.translatable("tooltip.avaritia.side_config_card.has_config"));
            tooltip.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_right_click"));
        } else {
            tooltip.add(Component.translatable("tooltip.avaritia.side_config_card.no_config"));
            tooltip.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_right_click"));
        }
        tooltip.add(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_air"));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof ITileIO tileIO) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    saveConfigToItem(stack, tileIO.getSideConfiguration());
                    player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.read_success"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!hasSavedConfig(stack)) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.no_config_to_apply"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                tileIO.setSideConfiguration(loadConfigFromItem(stack));
                player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.apply_success"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                clearConfig(stack, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            clearConfig(stack, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 将SideConfiguration保存到物品的NBT中
     */
    private void saveConfigToItem(ItemStack stack, SideConfiguration config) {
        stack.getOrCreateTag().put(TAG_SIDE_CONFIG, config.toNBT());
    }

    /**
     * 从物品的NBT中加载SideConfiguration
     */
    private SideConfiguration loadConfigFromItem(ItemStack stack) {
        if (!hasSavedConfig(stack)) {
            return new SideConfiguration();
        }
        return SideConfiguration.fromNBT(stack.getTag().getCompound(TAG_SIDE_CONFIG));
    }

    private boolean hasSavedConfig(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_SIDE_CONFIG);
    }

    private void clearConfig(ItemStack stack, Player player) {
        if (hasSavedConfig(stack)) {
            stack.getTag().remove(TAG_SIDE_CONFIG);
            player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.cleared"), true);
        } else {
            player.displayClientMessage(Component.translatable("tooltip.avaritia.side_config_card.already_empty"), true);
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        // 如果配置卡中有配置，则显示附魔光效
        return hasSavedConfig(stack);
    }
}
