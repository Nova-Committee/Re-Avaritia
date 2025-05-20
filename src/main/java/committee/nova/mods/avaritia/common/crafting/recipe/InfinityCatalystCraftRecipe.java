package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
    private final String group;
    private final int count;

    public InfinityCatalystCraftRecipe(String pGroup, NonNullList<Ingredient> inputs, int count) {
        super(inputs, new ItemStack(ModItems.infinity_catalyst.get()), 4);
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
        public static final MapCodec<InfinityCatalystCraftRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.fieldOf("group").forGetter(recipe -> recipe.group),
                        Ingredient.CODEC_NONEMPTY
                                .listOf()
                                .fieldOf("ingredients")
                                .flatXmap(
                                        field -> {
                                            var max = 81;
                                            var ingredients = field.toArray(Ingredient[]::new);
                                            if (ingredients.length == 0) {
                                                return DataResult.error(() -> "No ingredients for Combination recipe");
                                            } else {
                                                return ingredients.length > max
                                                        ? DataResult.error(() -> "Too many ingredients for Combination recipe. The maximum is: %s".formatted(max))
                                                        : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                                            }
                                        },
                                        DataResult::success
                                )
                                .forGetter(ShapelessTableCraftingRecipe::getInputs),
                        Codec.INT.fieldOf("count").forGetter(recipe -> recipe.count)
                ).apply(builder, InfinityCatalystCraftRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, InfinityCatalystCraftRecipe> STREAM_CODEC = StreamCodec.of(
                InfinityCatalystCraftRecipe.Serializer::toNetwork, InfinityCatalystCraftRecipe.Serializer::fromNetwork
        );
        @Override
        public @NotNull MapCodec<InfinityCatalystCraftRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, InfinityCatalystCraftRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static InfinityCatalystCraftRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            int count = buffer.readInt();
            return new InfinityCatalystCraftRecipe(group, inputs, count);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, InfinityCatalystCraftRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.getInputs().size());

            for (var ingredient : recipe.getInputs()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            buffer.writeInt(recipe.count);
        }
    }
}
