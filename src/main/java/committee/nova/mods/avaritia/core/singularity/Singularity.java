package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.init.config.ModConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    @Getter
    private final ResourceLocation id;
    @Getter
    private final String name;
    @Getter
    private final int[] colors;
    @Getter
    private final String tag;
    private final int ingredientCount;
    @Getter
    private final int timeRequired;
    private Ingredient ingredient;
    @Setter
    private boolean enabled = true;
    @Setter
    private boolean recipeDisabled = false;

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient, int ingredientCount, int timeRequired) {
        this.id = id;
        this.name = name;
        this.colors = colors;
        this.ingredient = ingredient;
        this.tag = null;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient) {
        this(id, name, colors, ingredient, -1, FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240);
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag, int ingredientCount, int timeRequired) {
        this.id = id;
        this.name = name;
        this.colors = colors;
        this.ingredient = Ingredient.EMPTY;
        this.tag = tag;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag) {
        this(id, name, colors, tag, -1, FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240);
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

        Singularity singularity = isTagIngredient ? new Singularity(id, name, colors, tag, ingredientCount, timeRequired)
                : new Singularity(id, name, colors, ingredient, ingredientCount, timeRequired);

        singularity.enabled = buffer.readBoolean();
        singularity.recipeDisabled = buffer.readBoolean();

        return singularity;
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

    public int getIngredientCount() {
        if (this.ingredientCount == -1) {
            return 1000;
        }
        return this.ingredientCount;
    }

    public Component getDisplayName() {
        return Localizable.of(this.name).build();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
        buffer.writeUtf(this.name);
        buffer.writeVarIntArray(this.colors);
        buffer.writeBoolean(this.tag != null);
        buffer.writeVarInt(this.timeRequired);

        if (this.tag != null) {
            buffer.writeUtf(this.tag);
        } else {
            this.ingredient.toNetwork(buffer);
        }

        buffer.writeVarInt(this.ingredientCount);
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.recipeDisabled);
    }

    // 手动添加isEnabled()和isRecipeDisabled()方法以确保编译成功
    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isRecipeDisabled() {
        return this.recipeDisabled;
    }
}
