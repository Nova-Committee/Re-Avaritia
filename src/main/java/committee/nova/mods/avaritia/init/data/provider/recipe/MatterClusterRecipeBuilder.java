package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.Consumer;

public class MatterClusterRecipeBuilder {

    public static void createFullMatterClusterRecipe(Consumer<FinishedRecipe> consumer, ItemLike result, ItemLike ingredient) {
        consumer.accept(new FullMatterClusterRecipe(new ResourceLocation("avaritia", "full_matter_cluster"), result, ingredient));
    }

    private static class FullMatterClusterRecipe implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final Item ingredient;

        public FullMatterClusterRecipe(ResourceLocation id, ItemLike result, ItemLike ingredient) {
            this.id = id;
            this.result = result.asItem();
            this.ingredient = ingredient.asItem();
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            // 创建配方材料数组
            JsonArray ingredients = new JsonArray();
            JsonObject ingredientObj = new JsonObject();

            // 定义需要1个matter_cluster（但需要内部存储4096个物品）
            ingredientObj.addProperty("item", ForgeRegistries.ITEMS.getKey(this.ingredient).toString());
            ingredientObj.addProperty("count", 1);

            ingredients.add(ingredientObj);
            json.add("ingredients", ingredients);

            // 结果
            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            json.add("result", resultObj);

            // 类型
            json.addProperty("type", "minecraft:crafting_shapeless");

            // 添加自定义数据来标记这是一个特殊配方
            JsonObject customData = new JsonObject();
            customData.addProperty("required_stored_items", 4096);
            json.add("custom_data", customData);
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
