package committee.nova.mods.avaritia.core.singularity;

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
public class Singularity {
    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::encode, Singularity::read
    );

    @Getter private final ResourceLocation id;
    @Getter private final String name;
    private final int[] colors;
    @Getter private final String tag;
    private final int ingredientCount;
    @Getter private final int timeRequired;
    @Nullable
    private Ingredient ingredient;
    @Getter @Setter private boolean enabled = true;
    @Getter @Setter private boolean recipeEnabled  = true;
    public Singularity(ResourceLocation id, String name, int[] colors, String tag) {
        this(id, name, colors, tag, 1000, 240);
    }

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient) {
        this(id, name, colors, ingredient, 1000, 240);
    }

    public Singularity(ResourceLocation id, String name, int[] colors, @Nullable Ingredient ingredient,
                       int ingredientCount, int timeRequired) {
        this.id = id;
        this.name = name;
        this.colors = Arrays.stream(colors).map(c -> FastColor.ARGB32.color(255, c)).toArray();
        this.ingredient = ingredient;
        this.tag = null;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag,
                       int ingredientCount, int timeRequired
    ) {
        this.id = id;
        this.name = name;
        this.colors = Arrays.stream(colors).map(c -> FastColor.ARGB32.color(255, c)).toArray();
        this.ingredient = null;
        this.tag = tag;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public int getOverlayColor() {
        return this.colors[0];
    }

    public int getUnderlayColor() {
        return this.colors[1];
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

    public static Singularity read(RegistryFriendlyByteBuf buffer) {
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

        int timeRequired = buffer.readVarInt();
        int ingredientCount = buffer.readVarInt();


        Singularity singularity;
        if (isTagIngredient) {
            singularity = new Singularity(id, name, colors, tag, ingredientCount, timeRequired);
        } else {
            singularity = new Singularity(id, name, colors, ingredient, ingredientCount, timeRequired);
        }

        singularity.enabled = buffer.readBoolean();
        singularity.recipeEnabled = buffer.readBoolean();

        return singularity;
    }

    public static void encode(RegistryFriendlyByteBuf buffer, Singularity singularity) {
        singularity.write(buffer);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
        buffer.writeUtf(this.name);
        buffer.writeVarIntArray(this.colors);
        buffer.writeBoolean(this.tag != null);
        if (this.tag != null) {
            buffer.writeUtf(this.tag);
        } else {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, this.ingredient != null ? this.ingredient : Ingredient.EMPTY);
        }
        buffer.writeVarInt(this.timeRequired);
        buffer.writeVarInt(this.getIngredientCount());
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.recipeEnabled);
    }
}
