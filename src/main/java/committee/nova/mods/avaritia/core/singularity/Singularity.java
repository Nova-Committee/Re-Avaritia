package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import dev.latvian.mods.rhino.Context;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    @Getter private final ResourceLocation registryName;
    @Getter private String displayName = "";
    @Getter private int[] colors = new int[] {0x3B2754, 0x3B2754};
    @Getter private String tag = null;
    private int count = Const.isLoad("projecte") ? 10000 : 1000;
    @Getter private int timeCost = FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240;
    private Ingredient ingredient = Ingredient.EMPTY;
    private ICondition condition = null;
    @Getter private boolean enabled = true;
    @Getter private boolean recipeDisabled = false;

    public Singularity(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public Singularity setColors(int overlayColor, int underlayColor) {
        this.colors = new int[] {overlayColor, underlayColor};
        return this;
    }

    public Singularity setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Singularity setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Singularity setCount(int count) {
        this.count = count;
        return this;
    }

    public Singularity setTimeCost(int timeCost) {
        this.timeCost = timeCost;
        return this;
    }

    public Singularity setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public Singularity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Singularity setRecipeDisabled(boolean recipeDisabled) {
        this.recipeDisabled = recipeDisabled;
        return this;
    }

    public Singularity setCondition(ICondition condition) {
        this.condition = condition;
        return this;
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, Ingredient ingredient) {
        Singularity singularity = new Singularity(registryName);
        singularity.setDisplayName(displayName);
        singularity.setColors(colors[0], colors[1]);
        singularity.setIngredient(ingredient);
        return singularity;
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, String tag) {
        Singularity singularity = new Singularity(registryName);
        singularity.setDisplayName(displayName);
        singularity.setColors(colors[0], colors[1]);
        singularity.setTag(tag);
        return singularity;
    }


    public static Singularity read(FriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var name = buffer.readUtf();
        int[] colors = buffer.readVarIntArray();
        var isTagIngredient = buffer.readBoolean();
        int timeRequired = buffer.readVarInt();

        String tag = null;
        var ingredient = Ingredient.EMPTY;

        if (isTagIngredient) {
            tag = buffer.readUtf();
        } else {
            ingredient = Ingredient.fromNetwork(buffer);
        }

        int ingredientCount = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeDisabled = buffer.readBoolean();

        return isTagIngredient
                ? new Singularity(id).setDisplayName(name).setColors(colors[0], colors[1])
                .setTag(tag).setCount(ingredientCount).setTimeCost(timeRequired).setEnabled(enabled).setRecipeDisabled(recipeDisabled)
                : new Singularity(id).setDisplayName(name).setColors(colors[0], colors[1])
                .setIngredient(ingredient).setCount(ingredientCount).setTimeCost(timeRequired).setEnabled(enabled).setRecipeDisabled(recipeDisabled);
    }

    public int getOverlayColor() {
        return this.colors[0];
    }

    public int getUnderlayColor() {
        return this.colors[1];
    }

    public Ingredient getIngredient() {
        if (this.tag != null && this.ingredient == Ingredient.EMPTY) {
            var tag = ItemTags.create(new ResourceLocation(this.tag));
            this.ingredient = Ingredient.of(tag);
        }

        return this.ingredient;
    }

    public int getCount() {
        if (this.count == -1) {
            return 1000;
        }
        return this.count;
    }

    public ICondition getCondition() {
        if (this.tag != null) return new NotCondition(new TagEmptyCondition(this.tag));
        else return null;
    }

    public static Singularity wrap(Context context, Object object) {
        if (object == null ) {
            return null;
        } else if (object instanceof Singularity) {
            return (Singularity) object;
        } else if (object instanceof ResourceLocation) {
            return SingularityDataManager.getInstance().getSingularity((ResourceLocation) object);
        }else if (object instanceof String) {
            return SingularityDataManager.getInstance().getSingularity(ResourceLocation.tryParse((String) object));
        }else {
            throw new IllegalArgumentException("Cannot convert object to Singularity: " + object);
        }
    }

}
