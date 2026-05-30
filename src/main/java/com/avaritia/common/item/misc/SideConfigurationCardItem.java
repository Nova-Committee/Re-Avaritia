package com.avaritia.common.item.misc;

import com.avaritia.api.iface.ITileIO;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.core.io.SideConfiguration;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * 侧面配置�? * 用于读取、存储和传输机器的SideConfiguration设置
 *
 * 功能说明�? * 1. Shift+右键机器：读取其SideConfiguration设置并保存到配置�? * 2. 右键对准同种机器：覆写机器的SideConfiguration配置
 * 3. Shift+右键空气：清除物品保存的SideConfiguration设置
 *
 * @author cnlimiter
 * Date: 2025/11/14
 * Version: 1.0
 */
public class SideConfigurationCardItem extends Item {

    public SideConfigurationCardItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.RARE));
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, Item.@NonNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);

        //check saved config置
        if (ItemUtils.hasTag(stack) && ItemUtils.getOrCreateTag(stack).contains("SideConfig")) {
            builder.accept(Component.translatable("tooltip.avaritia.side_config_card.has_config"));
            builder.accept(Component.translatable("tooltip.avaritia.side_config_card.instruction_right_click"));
        } else {
            builder.accept(Component.translatable("tooltip.avaritia.side_config_card.no_config"));
            builder.accept(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_right_click"));
        }
        builder.accept(Component.translatable("tooltip.avaritia.side_config_card.instruction_shift_air"));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        if (player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }


        BlockEntity blockEntity = level.getBlockEntity(pos);

        // 检查是否是实现了ITileIO接口的机器?
        if (player.isShiftKeyDown()) {
            if (blockEntity instanceof ITileIO tileIO) {
                if (!ItemUtils.getOrCreateTag(stack).contains("SideConfig")) {
                    // Shift+右键：读取配置?
                    SideConfiguration config = tileIO.getSideConfiguration();
                    saveConfigToItem(stack, config);
                    player.sendOverlayMessage(Component.translatable("tooltip.avaritia.side_config_card.read_success"));
                    return InteractionResult.SUCCESS;
                } else {
                    SideConfiguration config = loadConfigFromItem(stack);
                    tileIO.setSideConfiguration(config);
                    player.sendOverlayMessage(Component.translatable("tooltip.avaritia.side_config_card.apply_success"));
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (ItemUtils.hasTag(stack)) {
                    if (ItemUtils.getOrCreateTag(stack).contains("SideConfig")) {
                        ItemUtils.updateTag(stack, tag -> tag.remove("SideConfig"));
                        player.sendOverlayMessage(Component.translatable("tooltip.avaritia.side_config_card.cleared"));
                    } else {
                        player.sendOverlayMessage(Component.translatable("tooltip.avaritia.side_config_card.already_empty"));
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 将SideConfiguration保存到物品的NBT�?     */
    private void saveConfigToItem(ItemStack stack, SideConfiguration config) {
        ItemUtils.updateTag(stack, tag -> tag.put("SideConfig", config.toNBT()));
    }

    /**
     * 从物品的NBT中加载SideConfiguration
     */
    private SideConfiguration loadConfigFromItem(ItemStack stack) {
        if (!ItemUtils.hasTag(stack) || !ItemUtils.getOrCreateTag(stack).contains("SideConfig")) {
            return new SideConfiguration();
        }
        return SideConfiguration.fromNBT(ItemUtils.getOrCreateTag(stack).getCompound("SideConfig").orElseThrow());
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        // 如果配置卡中有配置，则显示附魔光效?
        return ItemUtils.hasTag(stack) && ItemUtils.getOrCreateTag(stack).contains("SideConfig");
    }
}
