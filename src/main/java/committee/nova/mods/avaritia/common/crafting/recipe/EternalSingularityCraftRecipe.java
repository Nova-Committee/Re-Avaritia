package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.util.SingularityUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe extends ShapelessTableCraftingRecipe {
    private static final Object2BooleanOpenHashMap<EternalSingularityCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    public NonNullList<Ingredient> inputs = NonNullList.create();

    public EternalSingularityCraftRecipe(ItemStack output) {
        super(NonNullList.create(), output, 4);
    }

    public static void invalidate() {
        INGREDIENTS_LOADED.clear();
    }

    @Override
    public boolean matches(@NotNull TierInput input, @NotNull Level level) {
        var ingredients = this.getIngredients();
        return !ingredients.isEmpty() && super.matches(input, level);
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!INGREDIENTS_LOADED.getOrDefault(this, false)) {
            super.getIngredients().clear();

            SingularityRegistryHandler.getInstance().getSingularities()
                    .stream()
                    .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                    .limit(81)
                    .map(SingularityUtils::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(super.getIngredients()::add);

            INGREDIENTS_LOADED.put(this, true);
        }
        return super.getIngredients();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<EternalSingularityCraftRecipe> {
        public static final MapCodec<EternalSingularityCraftRecipe> CODEC = MapCodec.unit(() -> new EternalSingularityCraftRecipe(new ItemStack(ModItems.eternal_singularity.get())));
        public static final StreamCodec<RegistryFriendlyByteBuf, EternalSingularityCraftRecipe> STREAM_CODEC = StreamCodec.of(
                EternalSingularityCraftRecipe.Serializer::toNetwork, EternalSingularityCraftRecipe.Serializer::fromNetwork
        );

        @Override
        public @NotNull MapCodec<EternalSingularityCraftRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, EternalSingularityCraftRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static EternalSingularityCraftRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            return new EternalSingularityCraftRecipe(new ItemStack(ModItems.eternal_singularity.get()));
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, EternalSingularityCraftRecipe recipe) { }
    }
}
