package committee.nova.mods.avaritia.core.singularity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import dev.latvian.mods.rhino.Context;
import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntSupplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    private static final int PROJECTE_MINIMUM_SINGULARITY_COUNT = 10_000;
    public static final Codec<Singularity> CODEC = createCodec(Singularity::getDefaultTimeCost);

    static Codec<Singularity> createCodec(IntSupplier defaultTimeCost) {
        return RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(singularity -> singularity.registryName),
                    Codec.STRING.optionalFieldOf("displayName", "").forGetter(singularity -> singularity.displayName),
                    Codec.INT.optionalFieldOf("overlayColor", 0x3B2754).forGetter(singularity -> singularity.overlayColor),
                    Codec.INT.optionalFieldOf("underlayColor", 0x3B2754).forGetter(singularity -> singularity.underlayColor),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("count", 1000).forGetter(singularity -> singularity.count),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("timeCost").forGetter(singularity ->
                            singularity.timeCost == ModConfig.singularityTimeRequired.getDefault()
                                    ? Optional.empty()
                                    : Optional.of(singularity.timeCost)),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(singularity -> singularity.ingredient),
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(singularity -> singularity.enabled),
                    Codec.BOOL.optionalFieldOf("recipeEnabled", true).forGetter(singularity -> singularity.recipeEnabled)
            ).apply(builder, (registryName, displayName, overlayColor, underlayColor, count, timeCost, ingredient, enabled, recipeEnabled) ->
                    new Singularity(registryName, displayName, overlayColor, underlayColor, count,
                            timeCost.orElseGet(defaultTimeCost::getAsInt), ingredient, enabled, recipeEnabled))
        );
    }

    public static final Codec<Optional<WithConditions<Singularity>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);


    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::write, Singularity::read
    );

    @Getter private final ResourceLocation registryName;
    @Getter private String displayName;
    @Getter private int overlayColor = 0x3B2754;
    @Getter private int underlayColor = 0x3B2754;
    private int count = 1000;
    @Getter private int timeCost = getDefaultTimeCost();
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

    private static int getDefaultTimeCost() {
        return ModConfig.COMMON.isLoaded()
                ? ModConfig.singularityTimeRequired.get()
                : ModConfig.singularityTimeRequired.getDefault();
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
        boolean countBoostEnabled = ModConfig.COMMON.isLoaded()
                ? ModConfig.enableProjectESingularityCountBoost.get()
                : ModConfig.enableProjectESingularityCountBoost.getDefault();
        return resolveRecipeCount(this.count, Const.isLoad("projecte"), countBoostEnabled);
    }

    static int resolveRecipeCount(int configuredCount, boolean projectELoaded, boolean countBoostEnabled) {
        return projectELoaded && countBoostEnabled
                ? Math.max(configuredCount, PROJECTE_MINIMUM_SINGULARITY_COUNT)
                : configuredCount;
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
        Singularity validated = singularity.validate();
        buffer.writeResourceLocation(validated.registryName);
        buffer.writeUtf(validated.displayName);
        buffer.writeInt(validated.overlayColor);
        buffer.writeInt(validated.underlayColor);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, validated.ingredient);
        buffer.writeVarInt(validated.timeCost);
        buffer.writeVarInt(validated.getRealCount());
        buffer.writeBoolean(validated.enabled);
        buffer.writeBoolean(validated.recipeEnabled);
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
