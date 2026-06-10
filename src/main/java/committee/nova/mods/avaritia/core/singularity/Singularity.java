package committee.nova.mods.avaritia.core.singularity;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.init.config.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 奇点数据对象。
 */
public class Singularity {
    public static final Codec<Singularity> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("name").forGetter(singularity -> singularity.registryName),
                    Codec.STRING.optionalFieldOf("displayName", "").forGetter(singularity -> singularity.displayName),
                    Codec.INT.optionalFieldOf("overlayColor", 0x3B2754).forGetter(singularity -> singularity.overlayColor & 0xFFFFFF),
                    Codec.INT.optionalFieldOf("underlayColor", 0x3B2754).forGetter(singularity -> singularity.underlayColor & 0xFFFFFF),
                    Codec.INT.optionalFieldOf("count", 1000).forGetter(singularity -> singularity.count),
                    Codec.INT.optionalFieldOf("timeCost", 240).forGetter(singularity -> singularity.timeCost),
                    Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(Singularity::getOptionalIngredient),
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(singularity -> singularity.enabled),
                    Codec.BOOL.optionalFieldOf("recipeEnabled", true).forGetter(singularity -> singularity.recipeEnabled)
            ).apply(builder, Singularity::new)
    );

    public static final Codec<Optional<WithConditions<Singularity>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::write, Singularity::read
    );

    private final Identifier registryName;
    private String displayName;
    private int overlayColor = ARGB.opaque(0x3B2754);
    private int underlayColor = ARGB.opaque(0x3B2754);
    private int count = 1000;
    private int timeCost = FMLLoader.getCurrent().isProduction() ? ModConfig.singularityTimeRequired.get() : 240;
    private Ingredient ingredient;
    private boolean enabled = true;
    private boolean recipeEnabled = true;
    private List<ICondition> conditions = new CopyOnWriteArrayList<>();

    public Singularity(Identifier registryName, String displayName, int overlayColor, int underlayColor,
                       int count, int timeCost, Ingredient ingredient, boolean enabled, boolean recipeEnable) {
        this.registryName = registryName;
        this.displayName = displayName;
        this.overlayColor = ARGB.opaque(overlayColor);
        this.underlayColor = ARGB.opaque(underlayColor);
        this.count = count;
        this.timeCost = timeCost;
        this.ingredient = ingredient;
        this.enabled = enabled;
        this.recipeEnabled = recipeEnable;
    }

    public Singularity(Identifier registryName, String displayName, int overlayColor, int underlayColor,
                       int count, int timeCost, Optional<Ingredient> ingredient, boolean enabled, boolean recipeEnable) {
        this(registryName, displayName, overlayColor, underlayColor, count, timeCost, ingredient.orElse(null), enabled, recipeEnable);
    }

    public Singularity(Identifier registryName) {
        this.registryName = registryName;
    }

    public Identifier getRegistryName() {
        return this.registryName;
    }

    public Identifier getRecipeId() {
        return recipeId(this.registryName);
    }

    public ResourceKey<Recipe<?>> getRecipeKey() {
        return recipeKey(this.registryName);
    }

    /**
     * 默认压缩机配方 ID 始终由奇点 ID 派生，脚本移除和运行时生成必须共用这条规则。
     */
    public static Identifier recipeId(Identifier singularityId) {
        return Identifier.fromNamespaceAndPath(singularityId.getNamespace(), singularityId.getPath() + "_singularity");
    }

    public static ResourceKey<Recipe<?>> recipeKey(Identifier singularityId) {
        return ResourceKey.create(Registries.RECIPE, recipeId(singularityId));
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getOverlayColor() {
        return this.overlayColor;
    }

    public int getUnderlayColor() {
        return this.underlayColor;
    }

    public int getTimeCost() {
        return this.timeCost;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    /**
     * 序列化时保留已经声明的输入；标签类 Ingredient 在 datagen 阶段可能暂时没有实际物品。
     */
    public Optional<Ingredient> getOptionalIngredient() {
        return this.ingredient != null ? Optional.of(this.ingredient) : Optional.empty();
    }

    public boolean hasIngredient() {
        return this.ingredient != null && !this.ingredient.isEmpty();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isRecipeEnabled() {
        return this.recipeEnabled;
    }

    public List<ICondition> getConditions() {
        return this.conditions;
    }

    public Singularity setColors(int overlayColor, int underlayColor) {
        this.overlayColor = ARGB.opaque(overlayColor);
        this.underlayColor = ARGB.opaque(underlayColor);
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

    public Singularity setConditions(List<ICondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public Singularity addCondition(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * 快照会按脚本覆盖生成副本，避免查询奇点列表时修改数据包或脚本注册的原始对象。
     */
    public Singularity copy() {
        Singularity copy = new Singularity(this.registryName, this.displayName, this.overlayColor, this.underlayColor,
                this.count, this.timeCost, this.ingredient, this.enabled, this.recipeEnabled);
        copy.setConditions(new CopyOnWriteArrayList<>(this.conditions));
        return copy;
    }

    public Singularity copyWithRecipeEnabled(boolean recipeEnabled) {
        return this.copy().setRecipeEnabled(recipeEnabled);
    }

    public static Singularity create(Identifier registryName, String displayName, int[] colors, Ingredient ingredient, ICondition condition) {
        return create(registryName, displayName, colors, ingredient).addCondition(condition);
    }

    public static Singularity create(Identifier registryName, String displayName, int[] colors, Ingredient ingredient) {
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
        if (this.count == -1) {
            return 1000;
        }
        return this.count;
    }

    public static Singularity read(RegistryFriendlyByteBuf buffer) {
        var id = buffer.readIdentifier();
        var displayName = buffer.readUtf();
        int overlayColor = buffer.readInt();
        int underlayColor = buffer.readInt();

        var ingredient = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
        int timeCost = buffer.readVarInt();
        int count = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeEnable = buffer.readBoolean();

        return new Singularity(id).setDisplayName(displayName).setColors(overlayColor, underlayColor)
                .setIngredient(ingredient.orElse(null)).setCount(count).setTimeCost(timeCost).setEnabled(enabled).setRecipeEnabled(recipeEnable);
    }

    public static void write(RegistryFriendlyByteBuf buffer, Singularity singularity) {
        buffer.writeIdentifier(singularity.registryName);
        buffer.writeUtf(singularity.displayName);
        buffer.writeInt(singularity.overlayColor);
        buffer.writeInt(singularity.underlayColor);
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, singularity.getOptionalIngredient());
        buffer.writeVarInt(singularity.timeCost);
        buffer.writeVarInt(singularity.getCount());
        buffer.writeBoolean(singularity.enabled);
        buffer.writeBoolean(singularity.recipeEnabled);
    }

    public static Singularity wrap(Object object) {
        return switch (object) {
            case null -> null;
            case Singularity singularity -> singularity;
            case Identifier identifier -> SingularityReloadListener.INSTANCE.getSingularity(identifier);
            case String string -> SingularityReloadListener.INSTANCE.getSingularity(Identifier.tryParse(string));
            default -> throw new IllegalArgumentException("Cannot convert object to Singularity: " + object);
        };
    }

    private static boolean isProjectELoaded() {
        try {
            return net.neoforged.fml.ModList.get().isLoaded("projecte");
        } catch (Throwable ignored) {
            Const.LOGGER.debug("ProjectE load state is unavailable while resolving singularity count.");
            return false;
        }
    }
}
