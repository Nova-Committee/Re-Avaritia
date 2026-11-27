package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ITierRecipe;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.api.util.java.TriFunction;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:16
 * Version: 1.0
 * from <a href="https://github.com/BlakeBr0/ExtendedCrafting/blob/1.20/src/main/java/com/blakebr0/extendedcrafting/crafting/recipe/ShapedTableRecipe.java">...</a>
 */
public class ShapedTableCraftingRecipe implements ITierCraftingRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStackTemplate result;
    public final int tier;
    @Getter
    private final boolean compatible;
    private TriFunction<Integer, Integer, ItemStack, ItemStack> transformers;

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStack result) {
        this(pattern, ItemStackTemplate.fromNonEmptyStack(result), 0, false);
    }

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStackTemplate result) {
        this(pattern, result, 0, false);
    }

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStack result, int tier, boolean compatible) {
        this(pattern, ItemStackTemplate.fromNonEmptyStack(result), tier, compatible);
    }

    public ShapedTableCraftingRecipe(ShapedRecipePattern pattern, ItemStackTemplate result, int tier, boolean compatible) {
        this.pattern = pattern;
        this.result = result;
        this.tier = tier;
        this.compatible = compatible;
    }

    public ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return this.result.create();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ItemHandler inventory) {
        return this.result.create();
    }

    @Override
    public boolean matches(@NotNull ItemHandler inventory) {
        if (this.tier != 0 && this.tier != this.getTierFromGridSize(inventory))
            return false;
        int size = (int) Math.sqrt(inventory.getSlots());
        for (int i = 0; i <= size - this.pattern.width(); i++) {
            for (int j = 0; j <= size - this.pattern.height(); j++) {
                if (this.checkMatch(inventory, i, j, true)) {
                    return true;
                }

                if (this.checkMatch(inventory, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        this.pattern.ingredients().forEach(ingredient -> ingredient.ifPresent(ingredients::add));
        return ingredients;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return (RecipeSerializer<ShapedTableCraftingRecipe>) ModRecipeSerializers.SHAPED_CRAFT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<CraftingInput>> getType() {
        return ModRecipeTypes.CRAFTING_TABLE_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(this.pattern.ingredients());
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(
                this.pattern.width(),
                this.pattern.height(),
                this.pattern.ingredients().stream().map(Ingredient::optionalIngredientToDisplay).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull ItemHandler inventory) {
        if (this.transformers != null) {
            var remaining = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);
            int size = (int) Math.sqrt(inventory.getSlots());

            for (int i = 0; i <= size - this.pattern.width(); i++) {
                for (int j = 0; j <= size - this.pattern.height(); j++) {
                    if (this.checkMatch(inventory, i, j, true)) {
                        for (int k = 0; k < this.pattern.height(); k++) {
                            for (int l = 0; l < this.pattern.width(); l++) {
                                int index = (this.pattern.width() - 1 - l) + i + (k + j) * size;
                                var stack = inventory.getStackInSlot(index);

                                remaining.set(index, this.transformers.apply(l, k, stack));
                            }
                        }

                        break;
                    }

                    if (this.checkMatch(inventory, i, j, false)) {
                        for (int k = 0; k < this.pattern.height(); k++) {
                            for (int l = 0; l < this.pattern.width(); l++) {
                                int index = l + i + (k + j) * size;
                                var stack = inventory.getStackInSlot(index);

                                remaining.set(index, this.transformers.apply(l, k, stack));
                            }
                        }

                        break;
                    }
                }
            }

            return remaining;
        }
        return ITierCraftingRecipe.super.getRemainingItems(inventory);
    }

    @Override
    public int getTier() {
        if (this.tier > 0) return this.tier;
        var width = this.pattern.width();
        var height = this.pattern.height();
        return width < 4 && height < 4 ? 1
                : width < 6 && height < 6 ? 2
                : width < 8 && height < 8 ? 3
                : 4;
    }

    @Override
    public boolean hasRequiredTier() {
        return this.tier > 0;
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    private int getTierFromGridSize(ItemHandler inv) {
        int size = inv.getSlots();
        return size < 10 ? 1
                : size < 26 ? 2
                : size < 50 ? 3
                : 4;
    }

    private boolean checkMatch(ItemHandler inventory, int x, int y, boolean mirror) {
        int size = (int) Math.sqrt(inventory.getSlots());
        var width = this.pattern.width();
        var height = this.pattern.height();
        var ingredients = this.pattern.ingredients();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int k = i - x;
                int l = j - y;
                var ingredient = java.util.Optional.<Ingredient>empty();

                if (k >= 0 && l >= 0 && k < width && l < height) {
                    if (mirror) {
                        ingredient = ingredients.get(width - k - 1 + l * width);
                    } else {
                        ingredient = ingredients.get(k + l * width);
                    }
                }

                if (!Ingredient.testOptionalIngredient(ingredient, inventory.getStackInSlot(i + j * size))) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setTransformers(TriFunction<Integer, Integer, ItemStack, ItemStack> transformers) {
        this.transformers = transformers;
    }

    public static class Serializer {
        public static final MapCodec<ShapedTableCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        ShapedRecipePatternCodecs.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                        RecipeCodecs.LEGACY_ITEM_STACK_TEMPLATE.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.INT.optionalFieldOf("tier", 0).forGetter(recipe -> recipe.tier),
                        Codec.BOOL.optionalFieldOf("compatible", false).forGetter(recipe -> recipe.compatible)
                        ).apply(builder, ShapedTableCraftingRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedTableCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedTableCraftingRecipe.Serializer::toNetwork, ShapedTableCraftingRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<ShapedTableCraftingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static ShapedTableCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            int tier = buffer.readVarInt();
            var compatible = buffer.readBoolean();

            return new ShapedTableCraftingRecipe(pattern, result, tier, compatible);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedTableCraftingRecipe recipe) {
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.tier);
            buffer.writeBoolean(recipe.compatible);
        }
    }

}
