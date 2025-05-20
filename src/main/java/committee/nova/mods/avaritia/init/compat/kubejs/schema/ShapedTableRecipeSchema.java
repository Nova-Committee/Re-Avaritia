//package committee.nova.mods.avaritia.init.compat.kubejs.schema;
//
//import committee.nova.mods.avaritia.init.compat.kubejs.component.ShapedRecipePatternComponent;
//import dev.latvian.mods.kubejs.recipe.RecipeKey;
//import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
//import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
//import net.minecraft.world.item.crafting.ShapedRecipePattern;
//import net.minecraft.world.level.block.state.pattern.BlockPattern;
//
//
///**
// * @Project: Avaritia
// * @Author: cnlimiter
// * @CreateTime: 2024/11/9 22:47
// * @Description:
// */
//public interface ShapedTableRecipeSchema {
//    RecipeKey<ShapedRecipePattern> RECIPE_PATTERN = ShapedRecipePatternComponent.SHAPE_RECIPE.inputKey("pattern").defaultOptional();
//
//    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);
//    RecipeSchema SCHEMA = new RecipeSchema(ShapedRecipeSchema.ShapedRecipeJS.class, ShapedRecipeSchema.ShapedRecipeJS::new, TIER, RESULT, BLOCK_PATTERN, KEY, KJS_MIRROR, KJS_SHRINK)
//            .constructor(TIER, RESULT, PATTERN, KEY)
//            .uniqueOutputId(RESULT);
//}
