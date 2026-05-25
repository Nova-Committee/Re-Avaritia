# Wave 2.5: ModDamageTypes Migration

## Summary
Migrated `ModDamageTypes.java` from Avaritia-1.21 (`committee.nova.mods.avaritia`) to Avaritia-26 (`com.avaritia`).

## Key Changes
- **Package**: `committee.nova.mods.avaritia.init.registry` → `com.avaritia.init.registry`
- **Imports**: `committee.nova.mods.avaritia.Const` → `com.avaritia.Avaritia` + `ResourceLocation.fromNamespaceAndPath`
- **Registration**: `BootstrapContext<DamageType>` (datagen) → `DeferredRegister<DamageType>` with static initializer
- **No Lombok**: Not present in source, no changes needed

## Verified APIs (NeoForge 26.1.2 / MC 1.21.2)
- `DamageType(String, DamageScaling, float)` — constructor still exists
- `DamageSource(Holder<DamageType>, Entity)` — 2-arg constructor still exists
- `getLocalizedDeathMessage(LivingEntity)` — method still exists
- `DeferredRegister.create(Registries.DAMAGE_TYPE, modId)` — valid for DamageType registry
