package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
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

// 继承无序工作台配方，与InfinityCatalystCraftRecipe保持一致的父类
public class FullMatterClusterRecipe extends ShapelessTableCraftingRecipe {
    private final String group;
    private final int count;

    // 构造方法与InfinityCatalystCraftRecipe参数结构一致
    public FullMatterClusterRecipe(ResourceLocation recipeId, String group, NonNullList<Ingredient> inputs, int count) {
        // 调用父类构造，指定输出为满载物质团，工作台等级4（参考同类配方）
        super(recipeId, inputs, new ItemStack(ModItems.full_matter_cluster.get()), 4);
        this.group = group;
        this.count = count;
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        // 先执行基础的无序配方匹配（检查物品是否符合）
        if (!super.matches(container, level)) {
            return false;
        }

        // 额外检查物质团数量是否≥4096
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof MatterClusterItem clusterItem) {
                // 使用物质团物品的方法获取内部物品数量
                if (clusterItem.getClusterSize(stack) >= 4096) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    // 关联到注册的序列化器，与InfinityCatalystCraftRecipe保持一致
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FULL_MATTER_CLUSTER_SERIALIZER.get();
    }

    // 序列化器实现，完全参考InfinityCatalystCraftRecipe的风格
    public static class Serializer implements RecipeSerializer<FullMatterClusterRecipe> {
        @Override
        public @NotNull FullMatterClusterRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            // 解析JSON字段，与InfinityCatalyst配方格式一致
            String group = GsonHelper.getAsString(json, "group", "default");
            NonNullList<Ingredient> inputs = NonNullList.create();
            var ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredients.get(i)));
            }
            int count = GsonHelper.getAsInt(json, "count", 1);
            return new FullMatterClusterRecipe(recipeId, group, inputs, count);
        }

        @Override
        public FullMatterClusterRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            // 网络同步逻辑，与同类配方保持一致
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            int count = buffer.readInt();
            return new FullMatterClusterRecipe(recipeId, group, inputs, count);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull FullMatterClusterRecipe recipe) {
            // 写入网络数据，遵循相同格式
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeInt(recipe.count);
        }
    }
}