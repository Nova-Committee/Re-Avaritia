package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.ingredient.ItemIngredient;
import committee.nova.mods.avaritia.common.ingredient.StackIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/25 00:25
 * @Description:
 */
public class ModIngredients {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Const.MOD_ID);
    public static Supplier<IngredientType<ItemIngredient>> NBT_ITEM = INGREDIENT.register("nbt_item", () -> new IngredientType<>(ItemIngredient.MAP_CODEC, ItemIngredient.STREAM_CODEC));
    public static Supplier<IngredientType<StackIngredient>> STACK_ITEM = INGREDIENT.register("stack", () -> new IngredientType<>(StackIngredient.MAP_CODEC, StackIngredient.STREAM_CODEC));

}
