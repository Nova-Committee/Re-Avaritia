# Learnings

## Task 3.0 — AvaritiaData Hub Class

- `Const.MOD_ID` = `"avaritia"` (confirmed from `src/main/java/com/avaritia/Avaritia.java`)
- `GatherDataEvent.Client` is the correct event class for NeoForge 26.1.2 datagen setup
- Template pattern: `@EventBusSubscriber(modid = Const.MOD_ID, bus = EventBusSubscriber.Bus.MOD)` on the class, `@SubscribeEvent` on `gatherData(GatherDataEvent.Client event)`
- All 6 providers use `generator.addProvider(true, new XxxProvider(packOutput))` with `true` for full re-run
- Provider class names chosen: AvaritiaLanguageProvider, AvaritiaModelProvider, AvaritiaRecipeProvider, AvaritiaLootTableProvider, AvaritiaTagProvider, AvaritiaBlockStateProvider

## Wave 3 Task 3.3 - Recipe datagen
- Added src/main/java/com/avaritia/data/recipe/AvaritiaRecipeProvider.java with Chinese class Javadoc and nested Runner for the current 26.1.2 patched RecipeProvider API (RecipeProvider.Runner creates provider from HolderLookup.Provider + RecipeOutput).
- Registered recipe provider in AvaritiaData.gatherData() with 
ew AvaritiaRecipeProvider.Runner(packOutput, event.getLookupProvider()) at the existing recipe provider comment.
- Recipe keys are created with ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Const.MOD_ID, name)); equivalent to the requested mod-scoped recipe key creation in this workspace's patched Identifier API (no Identifier.of method exists).
- Verification: LSP unavailable because jdtls is not installed. compileJava and 
unData are currently blocked by pre-existing non-task UTF-8 decode errors in migrated client/util files (e.g. TextureUtils.java, WorldUtils.java); filtered compile output showed no AvaritiaRecipeProvider or AvaritiaData errors before those global errors stop compilation.
