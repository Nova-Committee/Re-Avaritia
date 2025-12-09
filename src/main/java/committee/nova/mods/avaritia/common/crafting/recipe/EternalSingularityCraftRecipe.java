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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / EternalSingularityCraftRecipe
 * @author cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe extends ShapelessTableCraftingRecipe {
    private static final Object2BooleanOpenHashMap<EternalSingularityCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    public final NonNullList<Ingredient> inputs;
    public final boolean custom;

    public EternalSingularityCraftRecipe(ResourceLocation recipeId, NonNullList<Ingredient> inputs, boolean custom) {
        super(recipeId, NonNullList.create(), new ItemStack(ModItems.eternal_singularity.get()), 4);
        this.inputs = inputs;
        this.custom = custom;
    }

    public static void invalidate() {
        INGREDIENTS_LOADED.clear();
    }

    @Override
    public boolean matches(@NotNull Container input, @NotNull Level level) {
        var ingredients = this.getIngredients();
        if (ingredients.isEmpty()) return false;

        int singularityCount = SingularityReloadListener.INSTANCE.getAllSingularities().values()
                .stream()
                .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                .mapToInt(singularity -> 1)
                .sum();

        boolean[] found = new boolean[singularityCount];
        int validItems = 0;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                validItems++;
                boolean matched = false;
                int index = 0;
                for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
                    if (singularity.getIngredient() != Ingredient.EMPTY) {
                        ItemStack singularityStack = SingularityUtils.getItemForSingularity(singularity);
                        if (ItemStack.isSameItemSameTags(stack, singularityStack)) {
                            if (!found[index]) {
                                found[index] = true;
                                matched = true;
                                break;
                            }
                        }
                        index++;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
        }

        for (boolean b : found) {
            if (!b) return false;
        }


        return validItems == singularityCount;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!INGREDIENTS_LOADED.getOrDefault(this, false)) {
            super.getIngredients().clear();
            if (this.custom) {
                super.getIngredients().addAll(inputs);
            } else {
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
        return ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<EternalSingularityCraftRecipe> {
        @Override
        public @NotNull EternalSingularityCraftRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");

            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));
            }
            boolean custom = GsonHelper.getAsBoolean(json, "custom", false);
            return new EternalSingularityCraftRecipe(recipeId, inputs, custom);
        }

        @Override
        public EternalSingularityCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            boolean custom = buffer.readBoolean();
            return new EternalSingularityCraftRecipe(recipeId, inputs, custom);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull EternalSingularityCraftRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeBoolean(recipe.custom);
        }
    }
}
