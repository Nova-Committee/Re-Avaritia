package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.core.singularity.Singularity;
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
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InfinityCatalystCraftRecipe extends ShapelessTableCraftingRecipe {
    public static Object2BooleanOpenHashMap<InfinityCatalystCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    private final String group;
    private final int count;
    // 存储原始输入配料（用于非默认组）
    private final NonNullList<Ingredient> originalInputs;

    public InfinityCatalystCraftRecipe(ResourceLocation recipeId, String pGroup, NonNullList<Ingredient> inputs, int count) {
        super(recipeId, NonNullList.create(), new ItemStack(ModItems.infinity_catalyst.get(), count), 4);
        this.group = pGroup;
        this.count = count;
        this.originalInputs = inputs;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!INGREDIENTS_LOADED.getOrDefault(this, false)) {
            super.getIngredients().clear();
            super.getIngredients().addAll(originalInputs);
            if ("default".equals(group)) {
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
    public @NotNull String getGroup() {
        return this.group;
    }

    @Override
    public boolean matches(@NotNull Container input, @NotNull Level level) {
        // 如果是默认组，需要特殊处理（包含所有奇点）
        if ("default".equals(group)) {
            return matchesSingularityRecipe(input);
        } else {
            // 对于非默认组，使用标准的无形合成匹配
            return matchesStandardRecipe(input);
        }
    }

    /**
     * 处理默认组的奇点匹配逻辑
     * 要求玩家必须放置所有已注册的奇点（每个奇点只能使用一次），
     * 以及 originalInputs 中的物品
     */
    private boolean matchesSingularityRecipe(@NotNull Container input) {
        // 获取配料列表（会加载所有奇点）
        var ingredients = this.getIngredients();
        if (ingredients.isEmpty()) return false;

        // 统计所有有效的奇点数量
        var singularities = SingularityReloadListener.INSTANCE.getAllSingularities();
        if (singularities == null || singularities.isEmpty()) {
            return false;
        }

        // 收集所有有效的奇点对象
        List<Singularity> validSingularities = singularities.values()
                .stream()
                .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                .toList();

        int singularityCount = validSingularities.size();
        if (singularityCount == 0) return false;

        // 使用boolean数组追踪哪些奇点已被放置
        boolean[] found = new boolean[singularityCount];
        int totalInputItems = 0;

        // 收集容器中的所有非空物品
        List<ItemStack> inputStacks = new ArrayList<>();
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                inputStacks.add(stack);
                totalInputItems++;
            }
        }

        // 计算期望的总物品数量：奇点数 + originalInputs数
        int expectedTotalItems = singularityCount + this.originalInputs.size();
        if (totalInputItems != expectedTotalItems) {
            return false;
        }

        List<ItemStack> standardInputs = new ArrayList<>();

        // 检查每个输入物品：优先一对一匹配奇点，其余交给 RecipeMatcher 匹配普通材料
        for (ItemStack stack : inputStacks) {
            boolean matched = false;

            // 首先检查是否与某个奇点匹配
            for (int i = 0; i < validSingularities.size(); i++) {
                var singularity = validSingularities.get(i);
                ItemStack singularityStack = SingularityUtils.getItemForSingularity(singularity);
                if (ItemStack.isSameItemSameTags(stack, singularityStack)) {
                    // 确保每个奇点只能使用一次
                    if (!found[i]) {
                        found[i] = true;
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                standardInputs.add(stack);
            }
        }

        // 验证所有奇点都被放置
        for (boolean b : found) {
            if (!b) return false;
        }

        return standardInputs.size() == this.originalInputs.size()
                && RecipeMatcher.findMatches(standardInputs, this.originalInputs) != null;
    }

    /**
     * 处理非默认组的标准无形合成匹配逻辑
     */
    private boolean matchesStandardRecipe(@NotNull Container input) {
        List<ItemStack> inputs = new ArrayList<>();
        int matched = 0;

        // 收集容器中的所有非空物品
        for (int i = 0; i < input.getContainerSize(); i++) {
            var stack = input.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
                matched++;
            }
        }

        // 使用RecipeMatcher进行标准匹配
        return matched == this.originalInputs.size() &&
               RecipeMatcher.findMatches(inputs, this.originalInputs) != null;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull IItemHandler inv) {
        var remaining = super.getRemainingItems(inv);

        if ("default".equals(group)) {
            var singularities = SingularityReloadListener.INSTANCE.getAllSingularities();
            if (singularities != null && !singularities.isEmpty()) {
                for (int i = 0; i < remaining.size(); i++) {
                    var stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        for (var singularity : singularities.values()) {
                            if (singularity.getIngredient() != Ingredient.EMPTY) {
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
            }
        }

        return remaining;
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
            buffer.writeVarInt(recipe.originalInputs.size());
            for (var ingredient : recipe.originalInputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeInt(recipe.count);
        }
    }
}
