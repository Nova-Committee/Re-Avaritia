package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.Const;
import com.avaritia.api.common.crafting.RecipeCodecs;
import com.avaritia.api.common.crafting.TierInput;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.core.singularity.SingularityReloadListener;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRecipeSerializers;
import com.avaritia.util.SingularityUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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
        super(NonNullList.create(), new ItemStackTemplate(ModItems.eternal_singularity.get(), count), 4);
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
                .filter(Singularity::hasIngredient)
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
                    if (singularity.hasIngredient()) {
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
                        .filter(Singularity::hasIngredient)
                        .map(SingularityUtils::getItemForSingularity)
                        .map(Const::getStackIngredient)
                        .forEach(super.getIngredients()::add);
            if (!originalInputs.isEmpty()) {
                super.getIngredients().addAll(originalInputs);
            }
            INGREDIENTS_LOADED.put(this, true);
        }
        return super.getIngredients();
    }

    @Override
    public @NotNull RecipeSerializer<EternalSingularityCraftRecipe> getSerializer() {
        return ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
    }

    public static class Serializer {
        public static final MapCodec<EternalSingularityCraftRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        RecipeCodecs.ingredientList(81, true, "Combination recipe")
                                .fieldOf("ingredients")
                                .forGetter(recipe -> recipe.originalInputs),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.count)
                ).apply(builder, EternalSingularityCraftRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, EternalSingularityCraftRecipe> STREAM_CODEC = StreamCodec.of(
                EternalSingularityCraftRecipe.Serializer::toNetwork, EternalSingularityCraftRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<EternalSingularityCraftRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public static EternalSingularityCraftRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            var inputs = NonNullList.<Ingredient>createWithCapacity(size);

            for (int i = 0; i < size; ++i) {
                inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
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
