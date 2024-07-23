package committee.nova.mods.avaritia.common.item.singularity;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import committee.nova.mods.avaritia.util.SingularityUtils;
import committee.nova.mods.avaritia.util.lang.Localizable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:42
 * Version: 1.0
 */
public final class SingularityItem extends Item implements IColored {
    public SingularityItem(Function<Properties, Properties> properties) {
        super(properties.apply(new Properties().rarity(Rarity.UNCOMMON)));
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity == null) {
            return Localizable.of(this.getDescriptionId(stack)).args("NULL").build();
        }

        return Localizable.of(this.getDescriptionId(stack)).args(singularity.getDisplayName()).build();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity != null) {
            var modid = singularity.getId().getNamespace();

            if (!modid.equals(Static.MOD_ID))
                tooltip.add(ModTooltips.getAddedByTooltip(modid));

            if (flag.isAdvanced())
                tooltip.add(ModTooltips.SINGULARITY_ID.args(singularity.getId()).color(ChatFormatting.DARK_GRAY).build());
        }
    }

    @Override
    public int getColor(int i, ItemStack stack) {
        var singularity = SingularityUtils.getSingularity(stack);

        if (singularity == null)
            return -1;

        return i == 0 ? singularity.getUnderlayColor() : i == 1 ? singularity.getOverlayColor() : -1;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
