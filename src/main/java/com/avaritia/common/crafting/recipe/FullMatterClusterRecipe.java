package com.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.avaritia.api.common.crafting.RecipeCodecs;
import com.avaritia.api.common.crafting.TierInput;
import com.avaritia.common.item.resources.MatterClusterItem;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRecipeSerializers;
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
    public @NotNull RecipeSerializer<FullMatterClusterRecipe> getSerializer() {
        return ModRecipeSerializers.FULL_MATTER_CLUSTER_SERIALIZER.get();
    }

    public static class Serializer {
        public static final MapCodec<FullMatterClusterRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.optionalFieldOf("group", "default").forGetter(recipe -> recipe.group),
                        RecipeCodecs.ingredientList(81, false, "Combination recipe")
                                .fieldOf("ingredients")
                                .forGetter(recipe -> recipe.inputs),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.count)
                ).apply(builder, FullMatterClusterRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, FullMatterClusterRecipe> STREAM_CODEC = StreamCodec.of(
                FullMatterClusterRecipe.Serializer::toNetwork, FullMatterClusterRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<FullMatterClusterRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static FullMatterClusterRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int size = buffer.readVarInt();
            var inputs = NonNullList.<Ingredient>createWithCapacity(size);

            for (int i = 0; i < size; ++i) {
                inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
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
