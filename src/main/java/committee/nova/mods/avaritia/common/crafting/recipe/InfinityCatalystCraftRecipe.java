package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class InfinityCatalystCraftRecipe extends ShapelessExtremeCraftingRecipe{
    private boolean ingredientsLoaded = false;
    public InfinityCatalystCraftRecipe(ResourceLocation recipeId, NonNullList<Ingredient> inputs) {
        super(recipeId, inputs, new ItemStack(ModItems.infinity_catalyst.get()));
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!this.ingredientsLoaded) {
            SingularityRegistryHandler.getInstance().getSingularities()
                    .stream()
                    .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                    .limit(74)
                    .map(SingularityUtils::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(super.getIngredients()::add);

            this.ingredientsLoaded = true;
        }
        return super.getIngredients();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.INFINITY_CATALYST_RECIPE.get();
    }

    @Override
    public boolean matches(IItemHandler inventory) {
        var ingredients = this.getIngredients();
        return !ingredients.isEmpty() && super.matches(inventory);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFINITY_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfinityCatalystCraftRecipe> {
        @Override
        public @NotNull InfinityCatalystCraftRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");

            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));
            }
            return new InfinityCatalystCraftRecipe(recipeId, inputs);
        }

        @Override
        public InfinityCatalystCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            return new InfinityCatalystCraftRecipe(recipeId, inputs);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull InfinityCatalystCraftRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());

            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}
