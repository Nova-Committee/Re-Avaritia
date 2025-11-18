package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/13 20:53
 * @Description:
 */
public class ModApi {
    @ApiStatus.AvailableSince("1.3.9.0")
    public static RecipeHolder<ShapelessTableCraftingRecipe> addModShapelessRecipe(ItemStack result, List<ItemStack> ingredients, int tier) {
        UUID uuid = UUID.randomUUID();
        List<ItemStack> arraylist = new ArrayList<>();

        for (ItemStack stack : ingredients) {
            if (stack != null) {
                arraylist.add(stack.copy());
            } else {
                throw new RuntimeException("Invalid shapeless recipes!");
            }
        }

        return new RecipeHolder<>(Const.rl(uuid.toString()), new ShapelessTableCraftingRecipe(getList(arraylist), result, tier));
    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(Ingredient.of(stack));
        }
        return ingredients;
    }

    @ApiStatus.AvailableSince("1.3.9.2")
    public static RecipeHolder<CompressorRecipe> addSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (ingredient == Ingredient.EMPTY)
            return null;

        var id = singularity.getRegistryName();
        var recipeId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_singularity");
        var output = SingularityUtils.getItemForSingularity(singularity);
        int ingredientCount = singularity.getCount();
        int timeRequired = singularity.getTimeCost();

        return new RecipeHolder<>(recipeId, new CompressorRecipe(ingredient, output, ingredientCount, timeRequired));
    }

    /**
     * 使用构建器注册自定义奇点，通常放在{@link FMLCommonSetupEvent}中
     *
     * @param resourceLocation 模组ID奇点名称
     * @param builder 构建器配置
     * @return 注册的奇点
     */
    @ApiStatus.AvailableSince("1.3.9.3")
    public static Singularity registerSingularity(@NotNull ResourceLocation resourceLocation, @NotNull Consumer<Singularity> builder) {
        var singularity = new Singularity(resourceLocation);
        builder.accept(singularity);

        // 注册奇点到数据管理器
        SingularityDataManager manager = SingularityDataManager.getInstance();
        if (manager.isInitialized()) {
            // 使用数据管理器的运行时注册方法
            manager.registerRuntimeSingularity(singularity);
        }
        return singularity;
    }
}
