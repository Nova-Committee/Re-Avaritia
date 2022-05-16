package nova.committee.avaritia.common.item.singularity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.util.IColored;
import nova.committee.avaritia.init.ModTooltips;
import nova.committee.avaritia.init.handler.SingularityRegistryHandler;
import nova.committee.avaritia.util.Localizable;
import nova.committee.avaritia.util.SingularityUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:42
 * Version: 1.0
 */
public class SingularityItem extends Item implements IColored {
    public SingularityItem(Function<Properties, Properties> properties) {
        super(properties.apply(new Properties().rarity(Rarity.UNCOMMON)));
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            SingularityRegistryHandler.getInstance().getSingularities().forEach(singularity -> {
                items.add(SingularityUtils.getItemForSingularity(singularity));
            });
        }
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
    public void appendHoverText(@NotNull ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
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

//    @Nullable
//    @Override
//    public Entity createEntity(Level level, Entity location, ItemStack stack) {
//        return  ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
//    }
}
