package committee.nova.mods.avaritia.common.item.resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class StarFuelItem extends Item {


    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem(Rarity rarity) {
        super(new Properties().stacksTo(1).rarity(rarity));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        list.add(Component.translatable("tooltip.star_fuel.desc").withStyle(ChatFormatting.GRAY));
    }

//    @Override
//    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
//        return BURN_TIME;
//    }

}
