# Wave 2.4: ModEnchants.java Migration — Learnings

## Migration Details
- **Source**: `committee.nova.mods.avaritia.init.registry.ModEnchants` (Avaritia-1.21)
- **Target**: `com.avaritia.init.registry.ModEnchants` (Avaritia-26)
- **Enchantment API**: No changes needed between 1.21 and 1.21.1 (26.1.2) — `Enchantment.Builder.build(ResourceLocation)` API is identical

## Key Points
1. Enchantments in 1.21+ use `BootstrapContext<Enchantment>` for data-driven registration — NOT `DeferredRegister`
2. `ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name))` uses `minecraft:` namespace to override vanilla enchantments (intentional for Frost Walker replacement)
3. All vanilla enchantment classes used (`ReplaceDisk`, `DamageImmunity`, `LevelBasedValue`, `BlockPredicate`, etc.) are in the same packages across 1.21 → 1.21.1
4. No Lombok, no custom Enchantment subclasses, no AnvilLib dependencies in the original

## Cleanup Applied
- Removed unused `holdergetter3` (Block lookup) — was declared but never used
- Removed unused `holdergetter` (DamageType lookup) — was declared but never used
- Removed corresponding unused imports (`Block`, `DamageType`)
- Added `@SuppressWarnings("unused")` to class to prevent field warnings
