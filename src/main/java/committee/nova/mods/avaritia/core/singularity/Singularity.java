package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    private static final int PROJECTE_MINIMUM_SINGULARITY_COUNT = 10_000;
    // Match Loader's startup property without requiring a launched game for data-model consumers.
    private static final boolean DEVELOPMENT = !"false".equalsIgnoreCase(System.getProperty("fabric.development", "false"));

    // Fabric 的网络桥（NetBaseHandler）使用普通 FriendlyByteBuf，这里用内置注册表的
    // RegistryOps 以 NBT 形式编解码 Ingredient，等价于 Neo 版 OPTIONAL_CONTENTS_STREAM_CODEC。
    private static final RegistryOps<Tag> INGREDIENT_OPS = RegistryAccess
            .fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);

    public static final StreamCodec<FriendlyByteBuf, Singularity> STREAM_CODEC = StreamCodec.of(
            Singularity::write, Singularity::read
    );

    @Getter private final Identifier registryName;
    @Getter private String displayName;
    @Getter private int overlayColor = 0x3B2754;
    @Getter private int underlayColor = 0x3B2754;
    private int count = 1000;
    @Getter private int timeCost = DEVELOPMENT ? 240 : ModConfig.singularityTimeRequired.get();
    @Getter private Ingredient ingredient;
    @Getter private boolean enabled = true;
    @Getter private boolean recipeEnabled  = true;
    private final List<JsonObject> conditions = new CopyOnWriteArrayList<>();


    public Singularity(Identifier registryName, String displayName, int overlayColor, int underlayColor,
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

    public Singularity(Identifier registryName) {
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
        this.ingredient = ingredient;
        return this;
    }

    /**
     * 26.3 不再有 Ingredient.of(TagKey)，标签配料需要通过 HolderGetter 解析成 HolderSet.Named。
     */
    public Singularity setTag(String tag) {
        Identifier tagId = Identifier.tryParse(tag);
        if (tagId == null) {
            throw new SingularityValidationException("Invalid item tag: " + tag);
        }
        TagKey<net.minecraft.world.item.Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        return this.setIngredient(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tagKey)));
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

    public Singularity addCondition(JsonObject condition) {
        this.conditions.add(Objects.requireNonNull(condition, "condition").deepCopy());
        return this;
    }

    public List<JsonObject> getConditions() {
        return this.conditions.stream().map(JsonObject::deepCopy).toList();
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

    public Singularity validate() {
        Objects.requireNonNull(this.registryName, "registryName");
        Objects.requireNonNull(this.displayName, "displayName");
        requirePositive("count", this.count);
        requirePositive("timeCost", this.timeCost);
        return this;
    }

    public Singularity copy() {
        this.validate();
        Singularity copy = new Singularity(this.registryName, this.displayName, this.overlayColor, this.underlayColor,
                this.count, this.timeCost, this.ingredient, this.enabled, this.recipeEnabled);
        this.conditions.forEach(condition -> copy.conditions.add(condition.deepCopy()));
        return copy;
    }

    private static int requirePositive(String field, int value) {
        if (value <= 0) {
            throw new SingularityValidationException(field + " must be greater than zero, got " + value);
        }
        return value;
    }

    public static Singularity create(Identifier registryName, String displayName, int[] colors, Ingredient ingredient, JsonObject condition) {
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
        boolean countBoostEnabled = ModConfig.enableProjectESingularityCountBoost.get();
        return resolveRecipeCount(this.count, Const.isLoad("projecte"), countBoostEnabled);
    }

    static int resolveRecipeCount(int configuredCount, boolean projectELoaded, boolean countBoostEnabled) {
        return projectELoaded && countBoostEnabled
                ? Math.max(configuredCount, PROJECTE_MINIMUM_SINGULARITY_COUNT)
                : configuredCount;
    }


    public static Singularity read(FriendlyByteBuf buffer) {
        var id = buffer.readIdentifier();
        var displayName = buffer.readUtf();
        int overlayColor = buffer.readInt();
        int underlayColor = buffer.readInt();

        Ingredient ingredient = readIngredient(buffer);
        int timeCost = buffer.readVarInt();
        int count = buffer.readVarInt();
        var enabled = buffer.readBoolean();
        var recipeEnable = buffer.readBoolean();

        return new Singularity(id).setDisplayName(displayName).setColors(overlayColor, underlayColor)
                .setIngredient(ingredient).setCount(count).setTimeCost(timeCost).setEnabled(enabled).setRecipeEnabled(recipeEnable);
    }

    public static void write(FriendlyByteBuf buffer, Singularity singularity) {
        Singularity validated = singularity.validate();
        buffer.writeIdentifier(validated.registryName);
        buffer.writeUtf(validated.displayName);
        buffer.writeInt(validated.overlayColor);
        buffer.writeInt(validated.underlayColor);
        writeIngredient(buffer, validated.ingredient);
        buffer.writeVarInt(validated.timeCost);
        buffer.writeVarInt(validated.getRealCount());
        buffer.writeBoolean(validated.enabled);
        buffer.writeBoolean(validated.recipeEnabled);
    }

    private static Ingredient readIngredient(FriendlyByteBuf buffer) {
        CompoundTag wrapper = buffer.readNbt();
        if (wrapper == null) return null;
        Tag tag = wrapper.get("ingredient");
        if (tag == null) return null;
        return Ingredient.CODEC.parse(INGREDIENT_OPS, tag).result().orElse(null);
    }

    private static void writeIngredient(FriendlyByteBuf buffer, Ingredient ingredient) {
        CompoundTag wrapper = new CompoundTag();
        if (ingredient != null) {
            wrapper.put("ingredient",
                    Ingredient.CODEC.encodeStart(INGREDIENT_OPS, ingredient).getOrThrow(IllegalArgumentException::new));
        }
        buffer.writeNbt(wrapper);
    }


    public static Singularity wrap(Object object) {
        if (object == null ) {
            return null;
        } else if (object instanceof Singularity) {
            return (Singularity) object;
        } else if (object instanceof Identifier) {
            return SingularityReloadListener.INSTANCE.getSingularity((Identifier) object);
        }else if (object instanceof String) {
            return SingularityReloadListener.INSTANCE.getSingularity(Identifier.tryParse((String) object));
        }else {
            throw new IllegalArgumentException("Cannot convert object to Singularity: " + object);
        }
    }

}
