package com.avaritia.common.item.singularity;

import com.avaritia.Const;
import com.avaritia.api.iface.IColored;
import com.avaritia.api.utils.lang.Localizable;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.core.singularity.SingularityReloadListener;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModTooltips;
import com.avaritia.util.SingularityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:42
 * Version: 1.0
 */
public class SingularityItem extends Item implements IColored {
    public static final AtomicInteger currentSingularityIndex = new AtomicInteger(0);
    public static List<Singularity> enabledSingularities = null;
    public SingularityItem() {
        super(new Properties().rarity(ModRarities.UNCOMMON));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity == null) {
            return Localizable.of(this.getDescriptionId()).args("NULL").build();
        }

        return Localizable.of(this.getDescriptionId()).args(Component.translatable(singularity.getDisplayName())).build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity != null) {
            var modid = singularity.getRegistryName().getNamespace();

            if (!modid.equals(Const.MOD_ID))
                tooltip.accept(ModTooltips.getAddedByTooltip(modid));

            if (flag.isAdvanced())
                tooltip.accept(ModTooltips.SINGULARITY_ID.args(singularity.getRegistryName().toString()).color(ChatFormatting.DARK_GRAY).build());
        }
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getColor(ItemStack stack, int i) {
        // 检查是否是创造模式标签页的图�?- 使用数据组件方式
        if (stack.has(ModDataComponents.IS_CREATIVE_TAB_ICON.get())) {
            // 初始化奇点列表（如果尚未初始化）
            if (enabledSingularities == null) {
                enabledSingularities = SingularityReloadListener.INSTANCE.getAllSingularities()
                        .values()
                        .stream()
                        .filter(s -> s.isEnabled() && s.hasIngredient())
                        .toList();
            }

            //use current singularity color色
            if (!enabledSingularities.isEmpty()) {
                Singularity currentSingularity = enabledSingularities.get(currentSingularityIndex.get());
                return i == 0 ? currentSingularity.getUnderlayColor() : i == 1 ? currentSingularity.getOverlayColor() : -1;
            }
        }

        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity == null)
            return -1;

        return i == 0 ? singularity.getUnderlayColor() : i == 1 ? singularity.getOverlayColor() : -1;
    }
}
