package committee.nova.mods.avaritia.api.init.event;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.List;

public class RegisterRecipesEvent extends Event {
    private final RecipeManager manager;
    private final List<RecipeHolder<?>> recipes;
    @Getter private final ICondition.IContext context;
    @Getter private final HolderLookup.Provider registries;

    public RegisterRecipesEvent(RecipeManager manager, List<RecipeHolder<?>> recipes, HolderLookup.Provider registries, ICondition.IContext context) {
        this.manager = manager;
        this.recipes = recipes;
        this.context = context;
        this.registries = registries;
    }

    public final ConditionalOps<JsonElement> makeConditionalOps() {
        return new ConditionalOps<>(registries.createSerializationContext(JsonOps.INSTANCE), getContext());
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(RecipeHolder<?> recipe) {
        this.recipes.add(recipe);
    }

    /**
     * 默认奇点配方来自 JSON；运行时扩展配方加入前用此方法避免重复 ID。
     */
    public boolean hasRecipe(ResourceKey<Recipe<?>> key) {
        return this.recipes.stream().anyMatch(recipe -> recipe.id().equals(key));
    }
}
    
