package committee.nova.mods.avaritia;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityBuilder;
import committee.nova.mods.avaritia.init.handler.SingularityDataHandler;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @apiNote api提供
 * @author cnlimiter
 * @version 2.0
 */
public class ModApi {
    @ApiStatus.AvailableSince("1.3.9.0")
    public static ShapelessTableCraftingRecipe addModShapelessRecipe(ItemStack result, List<ItemStack> ingredients, int tier) {
        UUID uuid = UUID.randomUUID();
        List<ItemStack> arraylist = new ArrayList<>();

        for (ItemStack stack : ingredients) {
            if (stack != null) {
                arraylist.add(stack.copy());
            } else {
                throw new RuntimeException("Invalid shapeless recipes!");
            }
        }

        return new ShapelessTableCraftingRecipe(Const.rl(uuid.toString()), getList(arraylist), result, tier);
    }

    private static NonNullList<Ingredient> getList(List<ItemStack> arrayList) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (ItemStack stack : arrayList) {
            ingredients.add(Ingredient.of(stack));
        }
        return ingredients;
    }

    @ApiStatus.AvailableSince("1.3.9.2")
    public static CompressorRecipe addSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (ingredient == Ingredient.EMPTY)
            return null;

        var id = singularity.getId();
        var recipeId = new ResourceLocation(id.getNamespace(), id.getPath() + "_singularity");
        var output = SingularityUtils.getItemForSingularity(singularity);
        int ingredientCount = singularity.getIngredientCount();
        int timeRequired = singularity.getTimeRequired();

        return new CompressorRecipe(recipeId, ingredient, output, ingredientCount, timeRequired);
    }

    /**
     * 使用构建器注册自定义奇点，通常放在{@link FMLCommonSetupEvent}中
     *
     * @param modId 模组ID
     * @param name 奇点名称
     * @param builder 构建器配置
     * @return 注册的奇点
     */
    @ApiStatus.AvailableSince("1.3.9.3")
    public static Singularity registerSingularity(@NotNull String modId, @NotNull String name, @NotNull Consumer<SingularityBuilder> builder) {
        SingularityBuilder singularityBuilder = new SingularityBuilder(modId, name);
        builder.accept(singularityBuilder);

        Singularity singularity = singularityBuilder.build();
        SingularityDataHandler manager = SingularityDataHandler.getInstance();
        if (manager.isInitialized()) {
            // 使用数据管理器的运行时注册方法
            manager.registerRuntimeSingularity(singularity);
        }
        return singularity;
    }
}
