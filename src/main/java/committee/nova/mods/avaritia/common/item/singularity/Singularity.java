package committee.nova.mods.avaritia.common.item.singularity;

import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

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
    private int overlayColor;
    private int underlayColor;
    private String tag;
    private int ingredientCount;
    private int timeRequired;
    private boolean enabled;
    private boolean recipeEnabled;
    public Singularity(ResourceLocation id, String name, int overlayColor, int underlayColor, String tag) {
        this(id, name, overlayColor, underlayColor, tag, 1000, 240, true, true);
    }

    public Singularity(ResourceLocation id, String name, int overlayColor, int underlayColor, String tag,
                       int ingredientCount, int timeRequired,
                       boolean enabled, boolean recipeEnabled) {
        this.id = id;
        this.name = name;
        this.overlayColor = overlayColor;
        this.underlayColor = underlayColor;
        this.tag = tag;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
        this.enabled = enabled;
        this.recipeEnabled = recipeEnabled;
    }


    public Ingredient getIngredient() {
        if (this.tag != null) {
            var tag = ItemTags.create(ResourceLocation.parse(this.tag));
            return Ingredient.of(tag);
        }

        return Ingredient.EMPTY;
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


    public static Singularity fromNetwork(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var name = buffer.readUtf();
        int overlayColor = buffer.readInt();
        int underlayColor = buffer.readInt();
        var tag = buffer.readUtf();
        int timeRequired = buffer.readVarInt();
        int ingredientCount = buffer.readVarInt();
        boolean enabled = buffer.readBoolean();
        boolean recipeEnabled = buffer.readBoolean();

        return new Singularity(id, name, overlayColor, underlayColor,
                tag, ingredientCount, timeRequired, enabled, recipeEnabled);
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
        buffer.writeUtf(this.name);
        buffer.writeInt(this.overlayColor);
        buffer.writeInt(this.underlayColor);
        buffer.writeUtf(this.tag);
        buffer.writeInt(this.timeRequired);
        buffer.writeInt(this.ingredientCount);
        buffer.writeBoolean(this.enabled);
        buffer.writeBoolean(this.recipeEnabled);
    }
}
