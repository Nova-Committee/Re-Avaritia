package committee.nova.mods.avaritia.common.crafting.recipe;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/12/20 12:52
 * @Description:
 */
public class ExtremeSmithingRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    public final Ingredient template;
    public final Ingredient base;
    public final Ingredient additions;
    public final ItemStack result;

    public ExtremeSmithingRecipe(ResourceLocation pId, Ingredient pTemplate, Ingredient pBase, Ingredient additions, ItemStack pResult) {
        this.id = pId;
        this.template = pTemplate;
        this.base = pBase;
        this.additions = additions;
        this.result = pResult;
    }

    @Override
    public boolean matches(Container pContainer, @NotNull Level pLevel) {
        return this.template.test(pContainer.getItem(0)) && this.base.test(pContainer.getItem(1))
                && this.additions.test(pContainer.getItem(2))
                && this.additions.test(pContainer.getItem(3))
                && this.additions.test(pContainer.getItem(4));
    }

    @Override
    public @NotNull ItemStack assemble(Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        ItemStack itemstack = this.result.copy();
        CompoundTag compoundtag = pContainer.getItem(1).getTag();
        if (compoundtag != null) {
            itemstack.setTag(compoundtag.copy());
        }

        return itemstack;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(@NotNull ItemStack pStack) {
        return this.template.test(pStack);
    }

    @Override
    public boolean isBaseIngredient(@NotNull ItemStack pStack) {
        return this.base.test(pStack);
    }

    @Override
    public boolean isAdditionIngredient(@NotNull ItemStack pStack) {
        return this.additions.test(pStack);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EXTREME_SMITHING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.additions).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<ExtremeSmithingRecipe> {
        @Override
        public @NotNull ExtremeSmithingRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pSerializedRecipe) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(pSerializedRecipe, "template"));
            Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(pSerializedRecipe, "base"));
            Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(pSerializedRecipe, "addition"));
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));
            return new ExtremeSmithingRecipe(pRecipeId, ingredient, ingredient1, ingredient2, itemstack);
        }

        @Override
        public ExtremeSmithingRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, @NotNull FriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            Ingredient ingredient1 = Ingredient.fromNetwork(pBuffer);
            Ingredient ingredient2 = Ingredient.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            return new ExtremeSmithingRecipe(pRecipeId, ingredient, ingredient1, ingredient2, itemstack);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf pBuffer, ExtremeSmithingRecipe pRecipe) {
            pRecipe.template.toNetwork(pBuffer);
            pRecipe.base.toNetwork(pBuffer);
            pRecipe.additions.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.result);
        }
    }
}
