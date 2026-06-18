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
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / EternalSingularityCraftRecipe
 * @author cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe extends ShapelessTableCraftingRecipe {
    public static Object2BooleanOpenHashMap<EternalSingularityCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    public final NonNullList<Ingredient> originalInputs;
    private final int count;

    public EternalSingularityCraftRecipe(ResourceLocation recipeId, NonNullList<Ingredient> originalInputs, int count) {
        super(recipeId, NonNullList.create(), new ItemStack(ModItems.eternal_singularity.get()), 4);
        this.originalInputs = originalInputs;
        this.count = count;
    }

    @Override
    public boolean matches(@NotNull Container input, @NotNull Level level) {
        var ingredients = this.getIngredients();
        if (ingredients.isEmpty()) return false;

        var singularities = SingularityReloadListener.INSTANCE.getSingularitiesWithIngredient();
        int singularityCount = singularities.size();

        boolean[] found = new boolean[singularityCount];
        int validItems = 0;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                validItems++;
                boolean matched = false;
                int index = 0;
                for (var singularity : singularities) {
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
            SingularityReloadListener.INSTANCE.getSingularitiesWithIngredient()
                    .stream()
                    .map(SingularityUtils::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(super.getIngredients()::add);
            if (!originalInputs.isEmpty()) {
                super.getIngredients().addAll(originalInputs);
            }
            INGREDIENTS_LOADED.put(this, true);
        }
        return super.getIngredients();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull IItemHandler inv) {
        var remaining = super.getRemainingItems(inv);

        var singularities = SingularityReloadListener.INSTANCE.getSingularitiesWithIngredient();
        if (!singularities.isEmpty()) {
            for (int i = 0; i < remaining.size(); i++) {
                var stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    for (var singularity : singularities) {
                        var singularityStack = SingularityUtils.getItemForSingularity(singularity);
                        if (ItemStack.isSameItemSameTags(stack, singularityStack)) {
                            var rem = singularityStack.copy();
                            rem.setCount(1);
                            remaining.set(i, rem);
                            break;
                        }
                    }
                }
            }
        }

        return remaining;
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
            int count = GsonHelper.getAsInt(json, "count", 1);
            return new EternalSingularityCraftRecipe(recipeId, inputs, count);
        }

        @Override
        public EternalSingularityCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            int count = buffer.readInt();
            return new EternalSingularityCraftRecipe(recipeId, inputs, count);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull EternalSingularityCraftRecipe recipe) {
            buffer.writeVarInt(recipe.originalInputs.size());
            for (var ingredient : recipe.originalInputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeInt(recipe.count);
        }
    }
}
