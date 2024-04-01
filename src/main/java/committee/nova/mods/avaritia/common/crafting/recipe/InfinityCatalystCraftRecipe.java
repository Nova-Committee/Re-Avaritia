package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class InfinityCatalystCraftRecipe implements ISpecialRecipe{
    private final ResourceLocation recipeId;
    public NonNullList<Ingredient> inputs;
    private BiFunction<Integer, ItemStack, ItemStack> transformers;

    public InfinityCatalystCraftRecipe(ResourceLocation recipeId, NonNullList<Ingredient> inputs) {
        this.recipeId = recipeId;
        this.inputs = inputs;

    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputs.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return new ItemStack(ModItems.infinity_catalyst.get());
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFINITY_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_CRAFT_RECIPE.get();
    }

    @Override
    public ItemStack assemble(IItemHandler var1) {
        return new ItemStack(ModItems.infinity_catalyst.get());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return new ItemStack(ModItems.infinity_catalyst.get());
    }
    @Override
    public boolean matches(IItemHandler inventory) {
        List<ItemStack> inputs = new ArrayList<>();
        int matched = 0;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);

            if (!stack.isEmpty()) {
                inputs.add(stack);

                matched++;
            }
        }

        return matched == this.inputs.size() && RecipeMatcher.findMatches(inputs, this.inputs) != null;
    }

    @Override
    public boolean matches(@NotNull Container inv, @NotNull Level level) {
        return this.matches(new InvWrapper(inv));
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull IItemHandler inv) {
        var remaining = ISpecialRecipe.super.getRemainingItems(inv);

        if (this.transformers != null) {
            var used = new boolean[remaining.size()];

            for (int i = 0; i < remaining.size(); i++) {
                var stack = inv.getStackInSlot(i);

                for (int j = 0; j < this.inputs.size(); j++) {
                    var input = this.inputs.get(j);

                    if (!used[j] && input.test(stack)) {
                        var ingredient = this.transformers.apply(j, stack);

                        used[j] = true;
                        remaining.set(i, ingredient);

                        break;
                    }
                }
            }
        }

        return remaining;
    }

    public void setTransformers(BiFunction<Integer, ItemStack, ItemStack> transformers) {
        this.transformers = transformers;
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
