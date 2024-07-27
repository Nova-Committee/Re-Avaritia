package committee.nova.mods.avaritia.init.data.provider.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.init.registry.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Name: Avaritia-forge / ModRecipeBuilder
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:59
 * Description:
 */

public class ModShapedRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemLike result;
    private final ResourceLocation result2;
    private final int count;
    private final CompoundTag nbt;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private final Advancement.Builder advancement = Advancement.Builder.advancement();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public ModShapedRecipeBuilder(RecipeCategory category, ItemLike itemLike, ResourceLocation itemLocation, int count, CompoundTag nbt) {
        this.category = category;
        this.result = itemLike;
        this.result2 = itemLocation;
        this.count = count;
        this.nbt = nbt;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ResourceLocation itemLocation, CompoundTag nbt) {
        return shaped(category, null, itemLocation, 1, nbt);
    }

    @Contract("_, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ResourceLocation itemLocation) {
        return shaped(category, null, itemLocation, 1, new CompoundTag());
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, CompoundTag nbt) {
        return shaped(category, itemLike, null, 1, nbt);
    }

    @Contract("_, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike) {
        return shaped(category, itemLike, null, 1, new CompoundTag());
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull ModShapedRecipeBuilder shaped(RecipeCategory category, ItemLike itemLike, ResourceLocation itemLocation, int count, CompoundTag nbt) {
        return new ModShapedRecipeBuilder(category, itemLike, itemLocation, count, nbt);
    }

    public ModShapedRecipeBuilder define(Character character, TagKey<Item> tagKey) {
        return this.define(character, Ingredient.of(tagKey));
    }

    public ModShapedRecipeBuilder define(Character character, ItemLike itemLike) {
        return this.define(character, Ingredient.of(itemLike));
    }

    public ModShapedRecipeBuilder define(Character character, ItemStack stack) {
        return this.define(character, StrictNBTIngredient.of(stack));
    }

    public ModShapedRecipeBuilder define(Character character, Ingredient ingredient) {
        if (this.key.containsKey(character)) {
            throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
        } else if (character == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(character, ingredient);
            return this;
        }
    }

    public ModShapedRecipeBuilder pattern(String s) {
        if (!this.rows.isEmpty() && s.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(s);
            return this;
        }
    }

    @Override
    public @NotNull ModShapedRecipeBuilder unlockedBy(@NotNull String string, @NotNull CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(string, pCriterionTrigger);
        return this;
    }

    @Override
    public @NotNull ModShapedRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public ModShapedRecipeBuilder showNotification(boolean show) {
        this.showNotification = show;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        if (this.result != null){
            return this.result.asItem();
        }
        if (this.result2 != null){
            return Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(this.result2));
        }
        return Items.AIR;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> recipeConsumer, @NotNull ResourceLocation location) {
        this.ensureValid(location);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(location)).rewards(AdvancementRewards.Builder.recipe(location)).requirements(RequirementsStrategy.OR);
        recipeConsumer.accept(new ModShapedRecipeBuilder.Result(location, this.result, this.result2, this.count, this.nbt, this.group == null ? "" : this.group,
                determineBookCategory(this.category), this.rows, this.key, this.advancement,
                location.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.showNotification));
    }

    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + resourceLocation + "!");
        } else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');

            for(String s : this.rows) {
                for(int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if (!this.key.containsKey(c0) && c0 != ' ') {
                        throw new IllegalStateException("Pattern in recipe " + resourceLocation + " uses undefined symbol '" + c0 + "'");
                    }
                    set.remove(c0);
                }
            }

            if (!set.isEmpty()) {
                throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + resourceLocation);
            } else if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
                throw new IllegalStateException("Shaped recipe " + resourceLocation + " only takes in a single item - should it be a shapeless recipe instead?");
            } else if (this.advancement.getCriteria().isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
            }
        }
    }

    public static class Result extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final ItemLike result;
        private final ResourceLocation result2;
        private final int count;
        private final CompoundTag nbt;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final boolean showNotification;

        public Result(ResourceLocation resourceLocation, ItemLike item, ResourceLocation result2, int count, CompoundTag nbt, String group, CraftingBookCategory p_273500_, List<String> p_273744_, Map<Character, Ingredient> p_272991_, Advancement.Builder p_273260_, ResourceLocation p_273106_, boolean p_272862_) {
            super(p_273500_);
            this.id = resourceLocation;
            this.result = item;
            this.result2 = result2;
            this.count = count;
            this.nbt = nbt;
            this.group = group;
            this.pattern = p_273744_;
            this.key = p_272991_;
            this.advancement = p_273260_;
            this.advancementId = p_273106_;
            this.showNotification = p_272862_;
        }

        public void serializeRecipeData(@NotNull JsonObject pJson) {
            super.serializeRecipeData(pJson);
            if (!this.group.isEmpty()) {
                pJson.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for(String s : this.pattern) {
                jsonarray.add(s);
            }

            pJson.add("pattern", jsonarray);
            JsonObject jsonobject = new JsonObject();

            for(Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }

            pJson.add("key", jsonobject);
            JsonObject jsonobject1 = new JsonObject();

            if (this.result != null) jsonobject1.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result.asItem())).toString());
            if (this.result2 != null) jsonobject1.addProperty("item", this.result2.toString());

            if (this.count > 1) {
                jsonobject1.addProperty("count", this.count);
            }

            if (!this.nbt.isEmpty()){
                jsonobject1.addProperty("nbt", this.nbt.toString());
            }

            pJson.add("result", jsonobject1);
            pJson.addProperty("show_notification", this.showNotification);
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.SHAPED_EXTREME_CRAFT_SERIALIZER.get();
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
