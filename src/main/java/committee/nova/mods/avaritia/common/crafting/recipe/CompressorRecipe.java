package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:40
 * Version: 1.0
 */
public class CompressorRecipe implements ICompressorRecipe {
    private final String group;
    private final Ingredient inputs;
    private final ItemStack output;
    private final int inputCount;
    private final int timeRequire;


    public CompressorRecipe(String group, Ingredient input, ItemStack output, int inputCount, int timeRequire) {
        this.group = group;
        this.inputs = input;
        this.output = output;
        this.inputCount = inputCount;
        this.timeRequire = timeRequire;

    }
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COMPRESSOR_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess p_267052_) {
        return this.output;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.inputs);
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }


    @Override
    public @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return this.output.copy();
    }

    @Override
    public boolean matches(@NotNull Container inv, @NotNull Level level) {
        var inventory = new InvWrapper(inv);
        var input = inventory.getStackInSlot(0);
        return this.inputs.test(input);
    }

    @Override
    public int getInputCount() {
        return this.inputCount;
    }

    @Override
    public int getTimeRequire() {
        return timeRequire;
    }


    public static class Serializer implements RecipeSerializer<CompressorRecipe> {
        public static final Codec<CompressorRecipe> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Codec.STRING.fieldOf("group").forGetter(compressorRecipe -> compressorRecipe.group),
                                Ingredient.CODEC.fieldOf("ingredient").forGetter(compressorRecipe -> compressorRecipe.inputs),
                                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(compressorRecipe -> compressorRecipe.output),
                                ExtraCodecs.strictOptionalField(Codec.INT, "inputCount", 10000).forGetter(compressorRecipe -> compressorRecipe.inputCount),
                                ExtraCodecs.strictOptionalField(Codec.INT, "timeCost", 240).forGetter(compressorRecipe -> compressorRecipe.timeRequire)
                        )
                        .apply(instance, CompressorRecipe::new)
        );


        @Override
        public @NotNull Codec<CompressorRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull CompressorRecipe fromNetwork(@NotNull FriendlyByteBuf pBuffer) {
            var group = pBuffer.readUtf();
            var input = Ingredient.fromNetwork(pBuffer);
            var output = pBuffer.readItem();
            int inputCount = pBuffer.readInt();
            int timeCost = pBuffer.readInt();

            return new CompressorRecipe(group, input, output, inputCount, timeCost);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, CompressorRecipe recipe) {
            buffer.writeUtf(recipe.group);
            recipe.inputs.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeInt(recipe.inputCount);
            buffer.writeInt(recipe.timeRequire);

        }
    }
}
