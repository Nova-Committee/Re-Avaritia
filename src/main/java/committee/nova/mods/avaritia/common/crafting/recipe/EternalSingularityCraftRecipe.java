package committee.nova.mods.avaritia.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.util.SingularityUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / EternalSingularityCraftRecipe
 * @author cnlimiter
 * CreateTime: 2023/9/16 17:19
 * Description:
 */

public class EternalSingularityCraftRecipe extends ShapelessTableCraftingRecipe {
    public static Object2BooleanOpenHashMap<EternalSingularityCraftRecipe> INGREDIENTS_LOADED = new Object2BooleanOpenHashMap<>();
    public final NonNullList<Ingredient> originalInputs;
    private final int count;

    public EternalSingularityCraftRecipe(NonNullList<Ingredient> originalInputs, int count) {
        super(NonNullList.create(), new ItemStackTemplate(ModItems.eternal_singularity.get(), count), 4);
        this.originalInputs = originalInputs;
        this.count = count;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if (!INGREDIENTS_LOADED.getOrDefault(this, false)) {
            super.getIngredients().clear();
            SingularityReloadListener.INSTANCE.getAllSingularities().values()
                    .stream()
                    .filter(singularity -> singularity.hasIngredient())
                    .map(SingularityUtils::getItemForSingularity)
                    .map(NbtIngredients::of)
                    .forEach(super.getIngredients()::add);
            if (!originalInputs.isEmpty()) {
                super.getIngredients().addAll(originalInputs);
            }
            INGREDIENTS_LOADED.put(this, true);
        }
        return super.getIngredients();
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public @NotNull RecipeSerializer<? extends net.minecraft.world.item.crafting.Recipe<net.minecraft.world.item.crafting.CraftingInput>> getSerializer() {
        return (RecipeSerializer<EternalSingularityCraftRecipe>) ModRecipeSerializers.ETERNAL_SINGULARITY_CRAFT_SERIALIZER.get();
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

        private static EternalSingularityCraftRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
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
