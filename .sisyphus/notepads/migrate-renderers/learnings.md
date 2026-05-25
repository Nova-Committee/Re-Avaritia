2026-05-25 16:27
## Render Migration (May 25 2026)
- committee.nova.mods.avaritia → com.avaritia (all packages)
- Const → Avaritia (main mod class, static rl() method)
- ResourceLocation → Identifier (MC 1.21.5 rename)
- Identifier.fromNamespaceAndPath() replaces ResourceLocation.fromNamespaceAndPath()
- Res.ARC_TEX already exists in target Res.java
- Res.SHIELD_TEX missing in target; will need adding when InfinityShieldModel migrated
