package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;

import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.common.ingredient.ItemIngredient;
import committee.nova.mods.avaritia.common.ingredient.StackIngredient;
import committee.nova.mods.avaritia.common.ingredient.TagIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 注册模组中的所有自定义材料类型（IngredientType）。
 *
 * <p>包含 NBT 匹配与堆栈匹配两种自定义材料。</p>
 */
public class ModIngredients {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Const.MOD_ID);
    public static Supplier<IngredientType<ItemIngredient>> NBT_ITEM = INGREDIENT.register("nbt_item", () -> new IngredientType<>(ItemIngredient.MAP_CODEC, ItemIngredient.STREAM_CODEC));
    public static Supplier<IngredientType<StackIngredient>> STACK_ITEM = INGREDIENT.register("stack", () -> new IngredientType<>(StackIngredient.MAP_CODEC, StackIngredient.STREAM_CODEC));
    public static Supplier<IngredientType<TagIngredient>> TAG_ITEM = INGREDIENT.register("tag", () -> new IngredientType<>(TagIngredient.MAP_CODEC, TagIngredient.STREAM_CODEC));
}
