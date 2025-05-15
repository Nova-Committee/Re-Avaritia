package committee.nova.mods.avaritia.common.item.singularity;

import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
@Getter
@Setter
public class Singularity {
    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.ofMember(
            Singularity::toNetwork, Singularity::fromNetwork
    );

    public static StreamCodec<RegistryFriendlyByteBuf, Singularity> streamCodec() {
        return STREAM_CODEC;
    }

    private ResourceLocation id;
    private String name;
    private final int[] colors;
    private String tag;
    private int ingredientCount;
    private int timeRequired;
    @Nullable
    private Ingredient ingredient;
    private boolean enabled;
    private boolean recipeEnabled;
    public Singularity(ResourceLocation id, String name, int[] colors, String tag) {
        this(id, name, colors, tag, 1000, 240, true, true);
    }

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient) {
        this(id, name, colors, ingredient, 1000, 240, true, true);
    }

    public Singularity(ResourceLocation id, String name, int[] colors, @Nullable Ingredient ingredient,
                       int ingredientCount, int timeRequired,
                       boolean enabled, boolean recipeEnabled) {
        this.id = id;
        this.name = name;
        this.colors = Arrays.stream(colors).map(c -> FastColor.ARGB32.color(255, c)).toArray();
        this.ingredient = ingredient;
        this.tag = null;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
        this.enabled = enabled;
        this.recipeEnabled = recipeEnabled;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag,
                       int ingredientCount, int timeRequired,
                       boolean enabled, boolean recipeEnabled) {
        this.id = id;
        this.name = name;
        this.colors = Arrays.stream(colors).map(c -> FastColor.ARGB32.color(255, c)).toArray();
        this.ingredient = null;
        this.tag = tag;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
        this.enabled = enabled;
        this.recipeEnabled = recipeEnabled;
    }


    public Ingredient getIngredient() {
        if (this.tag != null && this.ingredient == null) {
            var tag = ItemTags.create(ResourceLocation.parse(this.tag));
            if (BuiltInRegistries.ITEM.getTag(tag).isPresent()) {
                this.ingredient = Ingredient.of(tag);
            } else {
                this.ingredient = Ingredient.EMPTY;
            }
        }

        return this.ingredient != null ? this.ingredient : Ingredient.EMPTY;
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

    public int getOverlayColor() {
        return this.colors[0];
    }

    public int getUnderlayColor() {
        return this.colors[1];
    }

    public static Singularity fromNetwork(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var name = buffer.readUtf();
        int[] colors = buffer.readVarIntArray();
        var isTagIngredient = buffer.readBoolean();
        String tag = null;
        var ingredient = Ingredient.EMPTY;

        if (isTagIngredient) {
            tag = buffer.readUtf();
        } else {
            ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        }
        int ingredientCount = buffer.readVarInt();
        int timeRequired = buffer.readVarInt();
        boolean enabled = buffer.readBoolean();
        boolean recipeEnabled = buffer.readBoolean();


        Singularity singularity;
        if (isTagIngredient) {
            singularity = new Singularity(id, name, colors, tag, ingredientCount, timeRequired, enabled, recipeEnabled);
        } else {
            singularity = new Singularity(id, name, colors, ingredient, ingredientCount, timeRequired, enabled, recipeEnabled);
        }

        return singularity;
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
        buffer.writeUtf(this.name);
        buffer.writeVarIntArray(this.colors);
        buffer.writeBoolean(this.tag != null);
        if (this.tag != null) {
            buffer.writeUtf(this.tag);
        } else {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, this.ingredient != null ? this.ingredient : Ingredient.EMPTY);
        }
        buffer.writeInt(this.timeRequired);
        buffer.writeInt(this.ingredientCount);
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.recipeEnabled);
    }
}
