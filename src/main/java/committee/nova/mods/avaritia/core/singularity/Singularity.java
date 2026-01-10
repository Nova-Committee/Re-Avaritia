package committee.nova.mods.avaritia.core.singularity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import dev.latvian.mods.rhino.Context;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    public static final Codec<Singularity> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(singularity -> singularity.registryName),
                    Codec.STRING.optionalFieldOf("displayName", "").forGetter(singularity -> singularity.displayName),
                    Codec.INT.optionalFieldOf("overlayColor", 0x3B2754).forGetter(singularity -> singularity.overlayColor),
                    Codec.INT.optionalFieldOf("underlayColor", 0x3B2754).forGetter(singularity -> singularity.underlayColor),
                    Codec.INT.optionalFieldOf("count", 1000).forGetter(singularity -> singularity.count),
                    Codec.INT.optionalFieldOf("timeCost", 240).forGetter(singularity -> singularity.timeCost),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(singularity -> singularity.ingredient),
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(singularity -> singularity.enabled),
                    Codec.BOOL.optionalFieldOf("recipeEnabled", true).forGetter(singularity -> singularity.recipeEnabled)
            ).apply(builder, Singularity::new)
    );

    public static final Codec<Optional<WithConditions<Singularity>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);


    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::write, Singularity::read
    );

    @Getter private final ResourceLocation registryName;
    @Getter private String displayName;
    @Getter private int overlayColor = 0x3B2754;
    @Getter private int underlayColor = 0x3B2754;
    private int count = 1000;
    @Getter private int timeCost = FMLLoader.isProduction() ? ModConfig.singularityTimeRequired.get() : 240;
    @Getter private Ingredient ingredient = Ingredient.EMPTY;
    @Getter private boolean enabled = true;
    @Getter private boolean recipeEnabled  = true;
    @Getter private List<ICondition> conditions = new CopyOnWriteArrayList<>();

    public Singularity(ResourceLocation registryName, String displayName, int overlayColor, int underlayColor,
                       int count, int timeCost, Ingredient ingredient, boolean enabled, boolean recipeEnable) {
        this.registryName = registryName;
        this.displayName = displayName;
        this.overlayColor = overlayColor;
        this.underlayColor = underlayColor;
        this.count = count;
        this.timeCost = timeCost;
        this.ingredient = ingredient;
        this.enabled = enabled;
        this.recipeEnabled = recipeEnable;
    }

    public Singularity(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public Singularity setColors(int overlayColor, int underlayColor) {
        this.overlayColor = overlayColor;
        this.underlayColor = underlayColor;
        return this;
    }

    public Singularity setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Singularity addCondition(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, Ingredient ingredient, ICondition condition) {
        return create(registryName, displayName, colors, ingredient).addCondition(condition);
    }

    public static Singularity create(ResourceLocation registryName, String displayName, int[] colors, Ingredient ingredient) {
        Singularity singularity = new Singularity(registryName);
        singularity.setDisplayName(displayName);
        singularity.setCount(Const.isLoad("projecte") ? 10000 : 1000);
        singularity.setColors(colors[0], colors[1]);
        singularity.setIngredient(ingredient);
        return singularity;
    }

    public int getCount() {
        if (this.count == -1) {
            return 1000;
        }
        return this.count;
    }

    public static Singularity read(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var displayName = buffer.readUtf();
        int overlayColor = buffer.readInt();
        int underlayColor = buffer.readInt();

        var ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int timeCost = buffer.readVarInt();
        int count = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeEnable = buffer.readBoolean();

        return new Singularity(id).setDisplayName(displayName).setColors(overlayColor, underlayColor)
                .setIngredient(ingredient).setCount(count).setTimeCost(timeCost).setEnabled(enabled).setRecipeEnabled(recipeEnable);
    }

    public static void write(RegistryFriendlyByteBuf buffer, Singularity singularity) {
        buffer.writeResourceLocation(singularity.registryName);
        buffer.writeUtf(singularity.displayName);
        buffer.writeInt(singularity.overlayColor);
        buffer.writeInt(singularity.underlayColor);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, singularity.ingredient != null ? singularity.ingredient : Ingredient.EMPTY);
        buffer.writeVarInt(singularity.timeCost);
        buffer.writeVarInt(singularity.getCount());
        buffer.writeBoolean(singularity.enabled);
        buffer.writeBoolean(singularity.recipeEnabled);
    }

    public static Singularity wrap(Context context, Object object) {
        return switch (object) {
            case null -> null;
            case Singularity singularity -> singularity;
            case ResourceLocation resourceLocation ->
                    SingularityReloadListener.INSTANCE.getSingularity(resourceLocation);
            case String s -> SingularityReloadListener.INSTANCE.getSingularity(ResourceLocation.tryParse(s));
            default -> throw new IllegalArgumentException("Cannot convert object to Singularity: " + object);
        };
    }
}
