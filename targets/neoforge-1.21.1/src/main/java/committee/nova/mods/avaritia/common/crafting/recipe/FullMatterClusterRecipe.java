package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FullMatterClusterRecipe extends ShapelessTableCraftingRecipe {
    private final String group;
    private final int count;

    public FullMatterClusterRecipe(String group, NonNullList<Ingredient> inputs, int count) {
        super(inputs, new ItemStack(ModItems.full_matter_cluster.get()), 1);
        this.group = group;
        this.count = count;
    }

    @Override
    public boolean matches(@NotNull TierInput container, @NotNull Level level) {

        if (!super.matches(container, level)) {
            return false;
        }

        // 额外检查物质团数量是否≥4096
        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof MatterClusterItem clusterItem) {

                if (MatterClusterItem.getClusterSize(MatterClusterItem.getClusterItems(stack)) >= 4096) {
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


    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FULL_MATTER_CLUSTER_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<FullMatterClusterRecipe> {
        public static final MapCodec<FullMatterClusterRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.optionalFieldOf("group", "default").forGetter(recipe -> recipe.group),
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
                                .forGetter(recipe -> recipe.inputs),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.count)
                ).apply(builder, FullMatterClusterRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, FullMatterClusterRecipe> STREAM_CODEC = StreamCodec.of(
                FullMatterClusterRecipe.Serializer::toNetwork, FullMatterClusterRecipe.Serializer::fromNetwork
        );
        @Override
        public @NotNull MapCodec<FullMatterClusterRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, FullMatterClusterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
        private static FullMatterClusterRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            var inputs = NonNullList.withSize(size, Ingredient.EMPTY);

            for (int i = 0; i < size; ++i) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            int count = buffer.readInt();

            return new FullMatterClusterRecipe(group, inputs, count);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, FullMatterClusterRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputs.size());

            for (var ingredient : recipe.inputs) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            buffer.writeInt(recipe.count);
        }
    }
}