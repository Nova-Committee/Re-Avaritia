// ... existing code ...
package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class InfinityCatalystCraftRecipe extends ShapelessTableCraftingRecipe {
    private static final Object2BooleanOpenHashMap<InfinityCatalystCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    private final String group;
    private final int count;


    public InfinityCatalystCraftRecipe(ResourceLocation recipeId, String pGroup, NonNullList<Ingredient> inputs, int count) {
        super(recipeId, inputs, new ItemStack(ModItems.infinity_catalyst.get()), 4);
        this.group = pGroup;
        this.count = count;
    }

    @Override
    public @NotNull String getGroup() {
        return this.group;
    }


    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFINITY_CATALYST_CRAFT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<InfinityCatalystCraftRecipe> {
        @Override
        public @NotNull InfinityCatalystCraftRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            String group1 = GsonHelper.getAsString(json, "group", "default");
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));

            }
            int count = GsonHelper.getAsInt(json, "count", 1);

            return new InfinityCatalystCraftRecipe(recipeId, group1, inputs, count);
        }

        @Override
        public InfinityCatalystCraftRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            int count = buffer.readInt();
            return new InfinityCatalystCraftRecipe(recipeId, group, inputs, count);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull InfinityCatalystCraftRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputs.size());
            for (var ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeInt(recipe.count);
        }
    }
}
