package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.common.ingredient.StackIngredient;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

        ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Const.rl(uuid.toString()));
        return new RecipeHolder<>(key, new ShapelessTableCraftingRecipe(getList(arraylist), result, tier));
    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(new StackIngredient(stack).toVanilla());
        }
        return ingredients;
    }

    @ApiStatus.AvailableSince("1.3.9.2")
    public static RecipeHolder<CompressorRecipe> addSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (!singularity.hasIngredient())
            return null;

        var output = SingularityUtils.getItemForSingularity(singularity);
        int ingredientCount = singularity.getCount();
        int timeRequired = singularity.getTimeCost();

        return new RecipeHolder<>(singularity.getRecipeKey(), new CompressorRecipe(ingredient, output, ingredientCount, timeRequired));
    }

    /**
     * 使用构建器注册自定义奇点，通常放在{@link FMLCommonSetupEvent}中
     *
     * @param resourceLocation 模组ID奇点名称
     * @param builder 构建器配置
     * @return 注册的奇点
     */
    @ApiStatus.AvailableSince("1.3.9.3")
    public static Singularity registerSingularity(@NotNull Identifier resourceLocation, @NotNull Consumer<Singularity> builder) {
        var singularity = new Singularity(resourceLocation);
        builder.accept(singularity);
        // 注册奇点到数据管理器
        SingularityReloadListener.INSTANCE.registerSingularity(singularity);
        return singularity;
    }
}
