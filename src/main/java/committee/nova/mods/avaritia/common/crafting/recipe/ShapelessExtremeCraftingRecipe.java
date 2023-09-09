package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 */
public class ShapelessExtremeCraftingRecipe implements ISpecialRecipe, ICraftRecipe {
    private final ResourceLocation recipeId;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private Map<Integer, Function<ItemStack, ItemStack>> transformers;


    public ShapelessExtremeCraftingRecipe(ResourceLocation recipeId, NonNullList<Ingredient> inputs, ItemStack output) {
        this.recipeId = recipeId;
        this.inputs = inputs;
        this.output = output;

    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return this.output;
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
        return ModRecipeSerializers.SHAPELESS_EXTREME_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_CRAFT_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputs.size();
    }

    @Override
    public ItemStack assemble(IItemHandler inventory) {
        return this.output.copy();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container inv) {
        return this.output.copy();
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
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull Container inv) {
        if (this.transformers != null) {
            NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

            this.transformers.forEach((i, stack) -> {
                remaining.set(i, stack.apply(inv.getItem(i)));
            });

            return remaining;
        }

        return ISpecialRecipe.super.getRemainingItems(inv);
    }

    public void setTransformers(Map<Integer, Function<ItemStack, ItemStack>> transformers) {
        this.transformers = transformers;
    }

    public static class Serializer implements RecipeSerializer<ShapelessExtremeCraftingRecipe> {
        @Override
        public @NotNull ShapelessExtremeCraftingRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");

            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));
            }

            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            return new ShapelessExtremeCraftingRecipe(recipeId, inputs, output);
        }

        @Override
        public ShapelessExtremeCraftingRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }

            var output = buffer.readItem();

            return new ShapelessExtremeCraftingRecipe(recipeId, inputs, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapelessExtremeCraftingRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());

            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
        }
    }

}
