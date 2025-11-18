package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.init.config.ModConfig;
import dev.latvian.mods.rhino.Context;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.loading.FMLLoader;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::encode, Singularity::read
    );

    @Getter private final ResourceLocation registryName;
    @Getter private String displayName;
    @Getter private int[] colors = new int[] {0x3B2754, 0x3B2754};
    @Getter private String tag = null;
    private int count = Const.isLoad("projecte") ? 10000 : 1000;
    @Getter private int timeCost = FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240;
    private Ingredient ingredient = Ingredient.EMPTY;
    @Getter private boolean enabled = true;
    @Getter private boolean recipeEnabled  = true;

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

    public Singularity setRecipeEnabled(boolean recipeEnable) {
        this.recipeEnabled = recipeEnable;
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

    public int getOverlayColor() {
        return this.colors[0];
    }

    public int getUnderlayColor() {
        return this.colors[1];
    }

    public Ingredient getIngredient() {
        if (this.tag != null && this.ingredient == Ingredient.EMPTY) {
            var tag = ItemTags.create(ResourceLocation.parse(this.tag));
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

    public static Singularity read(RegistryFriendlyByteBuf buffer) {
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
            ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        }

        int ingredientCount = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeEnable = buffer.readBoolean();

        return isTagIngredient
                ? new Singularity(id).setDisplayName(name).setColors(colors[0], colors[1])
                .setTag(tag).setCount(ingredientCount).setTimeCost(timeRequired).setEnabled(enabled).setRecipeEnabled(recipeEnable)
                : new Singularity(id).setDisplayName(name).setColors(colors[0], colors[1])
                .setIngredient(ingredient).setCount(ingredientCount).setTimeCost(timeRequired).setEnabled(enabled).setRecipeEnabled(recipeEnable);
    }

    public static void encode(RegistryFriendlyByteBuf buffer, Singularity singularity) {
        singularity.write(buffer);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.registryName);
        buffer.writeUtf(this.displayName);
        buffer.writeVarIntArray(this.colors);
        buffer.writeBoolean(this.tag != null);
        if (this.tag != null) {
            buffer.writeUtf(this.tag);
        } else {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, this.ingredient != null ? this.ingredient : Ingredient.EMPTY);
        }
        buffer.writeVarInt(this.timeCost);
        buffer.writeVarInt(this.getCount());
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.recipeEnabled);
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
