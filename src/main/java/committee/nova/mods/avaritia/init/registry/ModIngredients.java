package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.ingredient.ItemIngredient;
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
    public static final DeferredRegister<IngredientType<?>> INGREDIENT = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Static.MOD_ID);
    public static Supplier<IngredientType<ItemIngredient>> NBT_ITEM = INGREDIENT.register("nbt_item", () -> new IngredientType<>(ItemIngredient.CODEC, ItemIngredient.STREAM_CODEC));

}
