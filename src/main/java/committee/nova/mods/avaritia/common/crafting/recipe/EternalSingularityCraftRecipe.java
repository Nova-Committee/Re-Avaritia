package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.util.SingularityUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / InfinityCatalystRecipe
 * Author: cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe extends ShapelessTableCraftingRecipe {
    private static final Object2BooleanOpenHashMap<EternalSingularityCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    private final int count;
    public final NonNullList<Ingredient> originalInputs;

    public EternalSingularityCraftRecipe(NonNullList<Ingredient> originalInputs, int count) {
        super(NonNullList.create(), new ItemStack(ModItems.eternal_singularity.get()), 4);
        this.count = count;
        this.originalInputs = originalInputs;
    }

    public static void invalidate() {
        INGREDIENTS_LOADED.clear();
    }

    @Override
    public boolean matches(@NotNull TierInput input, @NotNull Level level) {
        var ingredients = this.getIngredients();
        if (ingredients.isEmpty()) return false;

        int singularityCount = SingularityReloadListener.INSTANCE.getAllSingularities().values()
                .stream()
                .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                .mapToInt(singularity -> 1)
                .sum();

        boolean[] found = new boolean[singularityCount];
        int validItems = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                validItems++;
                boolean matched = false;
                int index = 0;
                for (var singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
                    if (singularity.getIngredient() != Ingredient.EMPTY) {
                        ItemStack singularityStack = SingularityUtils.getItemForSingularity(singularity);
                        if (ItemStack.isSameItemSameComponents(stack, singularityStack)) {
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
            SingularityReloadListener.INSTANCE.getAllSingularities()
                        .values()
                        .stream()
                        .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
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
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<EternalSingularityCraftRecipe> {
        public static final MapCodec<EternalSingularityCraftRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Ingredient.CODEC
                                .listOf()
                                .fieldOf("ingredients")
                                .flatXmap(
                                        field -> {
                                            var max = 81;
                                            var ingredients = field.toArray(Ingredient[]::new);
                                            return ingredients.length > max
                                                    ? DataResult.error(() -> "Too many ingredients for Combination recipe. The maximum is: %s".formatted(max))
                                                    : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));

                                        },
                                        DataResult::success
                                )
                                .forGetter(recipe -> recipe.originalInputs),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.count)
                ).apply(builder, EternalSingularityCraftRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, EternalSingularityCraftRecipe> STREAM_CODEC = StreamCodec.of(
                EternalSingularityCraftRecipe.Serializer::toNetwork, EternalSingularityCraftRecipe.Serializer::fromNetwork
        );

        @Override
        public @NotNull MapCodec<EternalSingularityCraftRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, EternalSingularityCraftRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static EternalSingularityCraftRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            int count = buffer.readInt();
            return new EternalSingularityCraftRecipe(inputs, count);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, EternalSingularityCraftRecipe recipe) {
            buffer.writeVarInt(recipe.originalInputs.size());
            for (var ingredient : recipe.originalInputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            buffer.writeInt(recipe.count);
        }
    }
}
