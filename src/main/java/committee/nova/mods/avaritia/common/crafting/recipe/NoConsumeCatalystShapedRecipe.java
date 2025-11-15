package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.ShapedRecipePatternCodecs;
import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;

public class NoConsumeCatalystShapedRecipe extends ShapedTableCraftingRecipe {

    public NoConsumeCatalystShapedRecipe(ShapedRecipePattern pattern, ItemStack output, int tier) {
        super(pattern, output, tier, false);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull TierInput inventory) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(inventory);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.infinity_catalyst.get())) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.get();
    }

    public static class Serializer extends ShapedTableCraftingRecipe.Serializer {
        public static final MapCodec<NoConsumeCatalystShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        ShapedRecipePatternCodecs.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("tier", 0).forGetter(recipe -> recipe.tier)
                ).apply(builder, NoConsumeCatalystShapedRecipe::new)
        );
    }
}