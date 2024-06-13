package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe implements ISpecialRecipe{
    private static boolean ingredientsLoaded = false;
    private final ResourceLocation recipeId;
    public NonNullList<Ingredient> inputs = NonNullList.create();
    private BiFunction<Integer, ItemStack, ItemStack> transformers;

    public EternalSingularityCraftRecipe(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public static void invalidate() {
        ingredientsLoaded = false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputs.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return new ItemStack(ModItems.eternal_singularity.get());
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!ingredientsLoaded) {
            inputs.clear();

            SingularityRegistryHandler.getInstance().getSingularities()
                    .stream()
                    .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                    .limit(81)
                    .map(SingularityUtils::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(inputs::add);

            ingredientsLoaded = true;
        }
        return this.inputs;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_CRAFT_RECIPE.get();
    }

    @Override
    public ItemStack assemble(IItemHandler var1) {
        return new ItemStack(ModItems.eternal_singularity.get());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return new ItemStack(ModItems.eternal_singularity.get());
    }
    @Override
    public boolean matches(IItemHandler inventory) {
        var ingredients = this.getIngredients();
        List<ItemStack> inputs = new ArrayList<>();
        int matched = 0;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);

            if (!stack.isEmpty()) {
                inputs.add(stack);

                matched++;
            }
        }

        return !ingredients.isEmpty() &&  matched == this.inputs.size() && RecipeMatcher.findMatches(inputs, this.inputs) != null;
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

    public static class Serializer implements RecipeSerializer<EternalSingularityCraftRecipe> {
        @Override
        public @NotNull EternalSingularityCraftRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            return new EternalSingularityCraftRecipe(recipeId);
        }

        @Override
        public EternalSingularityCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            return new EternalSingularityCraftRecipe(recipeId);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull EternalSingularityCraftRecipe recipe) {}
    }
}
