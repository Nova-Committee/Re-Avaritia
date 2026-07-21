package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import dev.latvian.mods.rhino.Context;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    @Getter private final ResourceLocation registryName;
    @Getter private String displayName;
    @Getter private int overlayColor = 0x3B2754;
    @Getter private int underlayColor = 0x3B2754;
    private int count = 1000;
    @Getter private int timeCost = FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240;
    @Getter private Ingredient ingredient = Ingredient.EMPTY;
    @Getter private boolean enabled = true;
    @Getter private boolean recipeEnabled  = true;
    private final List<ICondition> conditions = new CopyOnWriteArrayList<>();


    public Singularity(ResourceLocation registryName, String displayName, int overlayColor, int underlayColor,
                       int count, int timeCost, Ingredient ingredient, boolean enabled, boolean recipeEnable) {
        this.registryName = Objects.requireNonNull(registryName, "registryName");
        this.setDisplayName(displayName);
        this.setColors(overlayColor, underlayColor);
        this.setCount(count);
        this.setTimeCost(timeCost);
        this.setIngredient(ingredient);
        this.setEnabled(enabled);
        this.setRecipeEnabled(recipeEnable);
    }

    public Singularity(ResourceLocation registryName) {
        this.registryName = Objects.requireNonNull(registryName, "registryName");
        this.displayName = registryName.toString();
    }

    public Singularity setColors(int overlayColor, int underlayColor) {
        this.overlayColor = overlayColor;
        this.underlayColor = underlayColor;
        return this;
    }

    public Singularity setDisplayName(String displayName) {
        this.displayName = displayName == null || displayName.isBlank() ? this.registryName.toString() : displayName;
        return this;
    }

    public Singularity setCount(int count) {
        this.count = requirePositive("count", count);
        return this;
    }

    public Singularity setTimeCost(int timeCost) {
        this.timeCost = requirePositive("timeCost", timeCost);
        return this;
    }

    public Singularity setIngredient(Ingredient ingredient) {
        this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
        return this;
    }

    public Singularity setTag(String tag) {
        ResourceLocation tagId = ResourceLocation.tryParse(tag);
        if (tagId == null) {
            throw new SingularityValidationException("Invalid item tag: " + tag);
        }
        return this.setIngredient(Ingredient.of(TagKey.create(Registries.ITEM, tagId)));
    }

    public Singularity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Singularity setRecipeEnabled(boolean recipeEnable) {
        this.recipeEnabled = recipeEnable;
        return this;
    }

    public Singularity setRecipeDisabled(boolean recipeDisabled) {
        return this.setRecipeEnabled(!recipeDisabled);
    }

    public Singularity addCondition(ICondition condition) {
        this.conditions.add(Objects.requireNonNull(condition, "condition"));
        return this;
    }

    public List<ICondition> getConditions() {
        return List.copyOf(this.conditions);
    }

    public Singularity validate() {
        Objects.requireNonNull(this.registryName, "registryName");
        Objects.requireNonNull(this.displayName, "displayName");
        Objects.requireNonNull(this.ingredient, "ingredient");
        requirePositive("count", this.count);
        requirePositive("timeCost", this.timeCost);
        return this;
    }

    public Singularity copy() {
        this.validate();
        Singularity copy = new Singularity(this.registryName, this.displayName, this.overlayColor, this.underlayColor,
                this.count, this.timeCost, this.ingredient, this.enabled, this.recipeEnabled);
        copy.conditions.addAll(this.conditions);
        return copy;
    }

    private static int requirePositive(String field, int value) {
        if (value <= 0) {
            throw new SingularityValidationException(field + " must be greater than zero, got " + value);
        }
        return value;
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, Ingredient ingredient, ICondition condition) {
        return create(registryName, displayName, colors, ingredient).addCondition(condition);
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, Ingredient ingredient) {
        Singularity singularity = new Singularity(registryName);
        singularity.setDisplayName(displayName);
        singularity.setColors(colors[0], colors[1]);
        singularity.setIngredient(ingredient);
        return singularity;
    }

    public int getRealCount() {
        return this.count;
    }

    public int getCount() {
        return this.count > 10000 ? this.count : Const.isLoad("projecte") ? 10000 : this.count;
    }


    public static Singularity read(FriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var displayName = buffer.readUtf();
        int overlayColor = buffer.readInt();
        int underlayColor = buffer.readInt();

        var ingredient = Ingredient.fromNetwork(buffer);
        int timeCost = buffer.readVarInt();
        int count = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeEnable = buffer.readBoolean();

        return new Singularity(id).setDisplayName(displayName).setColors(overlayColor, underlayColor)
                .setIngredient(ingredient).setCount(count).setTimeCost(timeCost).setEnabled(enabled).setRecipeEnabled(recipeEnable);
    }

    public static void write(FriendlyByteBuf buffer, Singularity singularity) {
        Singularity validated = singularity.validate();
        buffer.writeResourceLocation(validated.registryName);
        buffer.writeUtf(validated.displayName);
        buffer.writeInt(validated.overlayColor);
        buffer.writeInt(validated.underlayColor);
        validated.ingredient.toNetwork(buffer);
        buffer.writeVarInt(validated.timeCost);
        buffer.writeVarInt(validated.getRealCount());
        buffer.writeBoolean(validated.enabled);
        buffer.writeBoolean(validated.recipeEnabled);
    }


    public static Singularity wrap(Context context, Object object) {
        if (object == null ) {
            return null;
        } else if (object instanceof Singularity) {
            return (Singularity) object;
        } else if (object instanceof ResourceLocation) {
            return SingularityReloadListener.INSTANCE.getSingularity((ResourceLocation) object);
        }else if (object instanceof String) {
            return SingularityReloadListener.INSTANCE.getSingularity(ResourceLocation.tryParse((String) object));
        }else {
            throw new IllegalArgumentException("Cannot convert object to Singularity: " + object);
        }
    }

}
