package nova.committee.avaritia.common.item.resources;

import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nova.committee.avaritia.init.registry.ModTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class StarFuelItem extends ItemPE implements IItemEmcHolder {


    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem(Rarity rarity, String name) {
        super(new Item.Properties().stacksTo(1).rarity(rarity).tab(ModTab.TAB));
        setRegistryName(name);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        list.add(new TranslatableComponent("tooltip.star_fuel.desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }

    @Override
    public long insertEmc(@NotNull ItemStack itemStack, long l, IEmcStorage.EmcAction emcAction) {
        if (l < 0L)
            return extractEmc(itemStack, -l, emcAction);
        else {
            long toAdd = Math.min(getNeededEmc(itemStack), l);
            if (emcAction.execute())
                addEmcToStack(itemStack, toAdd);

            return toAdd;
        }
    }

    @Override
    public long extractEmc(@NotNull ItemStack itemStack, long l, IEmcStorage.EmcAction emcAction) {
        if (l < 0L)
            return insertEmc(itemStack, -l, emcAction);
        else {
            long storedEmc = getStoredEmc(itemStack);
            long toRemove = Math.min(storedEmc, l);
            if (emcAction.execute())
                setEmc(itemStack, storedEmc - toRemove);

            return toRemove;
        }
    }

    @Override
    public @Range(from = 0L, to = 9223372036854775807L) long getStoredEmc(@NotNull ItemStack itemStack) {
        return getEmc(itemStack);
    }

    @Override
    public @Range(from = 1L, to = 9223372036854775807L) long getMaximumEmc(@NotNull ItemStack itemStack) {
        return 9223372036854775807L;
    }
}
