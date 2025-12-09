package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.util.SingularityUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class InfinityCatalystCraftRecipe extends ShapelessTableCraftingRecipe {
    private static final Object2BooleanOpenHashMap<InfinityCatalystCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    private final int count;
    // 存储原始输入配料（用于非默认组）
    private final NonNullList<Ingredient> originalInputs;

    public InfinityCatalystCraftRecipe(ResourceLocation recipeId, NonNullList<Ingredient> inputs, int count) {
        super(recipeId, NonNullList.create(), new ItemStack(ModItems.infinity_catalyst.get()), 4);
        this.count = count;
        this.originalInputs = inputs;
    }

    public static void invalidate() {
        INGREDIENTS_LOADED.clear();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!INGREDIENTS_LOADED.getOrDefault(this, false)) {
            super.getIngredients().clear();
            if(!originalInputs.isEmpty()){
                super.getIngredients().addAll(originalInputs);
            } else {

                super.getIngredients().addAll(originalInputs);
                SingularityReloadListener.INSTANCE.getAllSingularities().values()
                        .stream()
                        .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                        .map(SingularityUtils::getItemForSingularity)
                        .map(Ingredient::of)
                        .forEach(super.getIngredients()::add);
            }
            INGREDIENTS_LOADED.put(this, true);
        }
        return super.getIngredients();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfinityCatalystCraftRecipe> {
        @Override
        public @NotNull InfinityCatalystCraftRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));
            }
            int count = GsonHelper.getAsInt(json, "count", 1);
            return new InfinityCatalystCraftRecipe(recipeId, inputs, count);
        }

        @Override
        public InfinityCatalystCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            int count = buffer.readInt();
            return new InfinityCatalystCraftRecipe(recipeId, inputs, count);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull InfinityCatalystCraftRecipe recipe) {
            buffer.writeVarInt(recipe.originalInputs.size());
            for (var ingredient : recipe.originalInputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeInt(recipe.count);
        }
    }
}