package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeOutput extends net.neoforged.neoforge.common.extensions.IRecipeOutputExtension {
    default void accept(ResourceLocation p_312249_, Recipe<?> p_312328_, @Nullable AdvancementHolder p_312176_) {
        accept(p_312249_, p_312328_, p_312176_, new net.neoforged.neoforge.common.conditions.ICondition[0]);
    }

    Advancement.Builder advancement();
}
