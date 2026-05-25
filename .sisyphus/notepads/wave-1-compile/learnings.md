# Wave 1 Compile Gate Learnings

## Build Config Fixes (Task 1.5)

### gradle.properties
- `neo_version=26.1.2.15-beta` — the 4-component format (mc_major.mc_minor.hotfix.neoforge_release-beta)
- `java_version=25` — required by NeoForge 26.1 (JEP 447)
- `mod_version=1.0.0-alpha` — NeoForge is still beta, mark mod as alpha
- Removed parchment config block entirely (Mojang provides official parameter names now)

### gradle/libs.versions.toml
- `neoforge = "26.1.2.15-beta"`
- `mixinextras = "0.5.4"` (was 0.4.1)
- `jade = "26.1.1+neoforge"` — Modrinth maven requires the `+neoforge` suffix
- `curios = "15.0.0-beta.2+26.1.2"` — old Curios maven uses this format, not `15.0.0-beta.2`

### build.gradle
- `JavaLanguageVersion.of(25)` (was 21)
- Removed commented-out Parchment block
- Gradle wrapper was already 9.1.0 — no update needed

### Key Lessons
- NeoForge version format: 4 components with optional `-beta` suffix
- Modrinth maven versions need `+neoforge` suffix (e.g., `26.1.1+neoforge`)
- Curios API maven uses `+26.1.2` suffix (not just `-beta.2`)
