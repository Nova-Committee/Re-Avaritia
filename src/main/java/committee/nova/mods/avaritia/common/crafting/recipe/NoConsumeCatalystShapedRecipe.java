package committee.nova.mods.avaritia.common.crafting.recipe;

import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;

public class NoConsumeCatalystShapedRecipe extends ShapedTableCraftingRecipe {

    public NoConsumeCatalystShapedRecipe(ShapedRecipePattern pattern, ItemStack output, int tier) {
        super(pattern, output, tier, false);
    }

    public NoConsumeCatalystShapedRecipe(ShapedRecipePattern pattern, ItemStackTemplate result, int tier) {
        super(pattern, result, tier, false);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull ItemHandler inventory) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(inventory);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.is(ModItems.infinity_catalyst.get())) {
                remaining.set(i, stack.copyWithCount(1));
            }
        }
        return remaining;
    }

    @Override
    public @NotNull RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.CraftingInput>> getSerializer() {
        return (RecipeSerializer<NoConsumeCatalystShapedRecipe>) ModRecipeSerializers.NO_CONSUME_CATALYST_SHAPED_SERIALIZER.get();
    }

    public static class Serializer {
        public static final com.mojang.serialization.MapCodec<NoConsumeCatalystShapedRecipe> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(builder ->
                        builder.group(
                                ShapedRecipePatternCodecs.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                                RecipeCodecs.LEGACY_ITEM_STACK_TEMPLATE.fieldOf("result").forGetter(recipe -> recipe.result),
                                com.mojang.serialization.Codec.INT.optionalFieldOf("tier", 0).forGetter(recipe -> recipe.tier)
                        ).apply(builder, NoConsumeCatalystShapedRecipe::new)
                );
        public static final StreamCodec<RegistryFriendlyByteBuf, NoConsumeCatalystShapedRecipe> STREAM_CODEC = StreamCodec.of(
                NoConsumeCatalystShapedRecipe.Serializer::toNetwork, NoConsumeCatalystShapedRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<NoConsumeCatalystShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private static NoConsumeCatalystShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            int tier = buffer.readVarInt();
            return new NoConsumeCatalystShapedRecipe(pattern, result, tier);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, NoConsumeCatalystShapedRecipe recipe) {
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.tier);
        }
    }
}
