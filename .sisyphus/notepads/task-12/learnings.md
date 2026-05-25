# Task 1.2 — Learnings

## Directory structure
- `src/main/java/com/example/avaritia/` — main source package (group: `com.example.avaritia`)
- `src/main/templates/META-INF/` — template directory for neoforge.mods.toml (build.gradle expands via `ProcessResources` from `src/main/templates`)

## Template expansion
- `build.gradle` uses `generateModMetadata` task with `expand replaceProperties` from `src/main/templates` → `build/generated/sources/modMetadata`
- Available template variables: `${minecraft_version}`, `${minecraft_version_range}`, `${neo_version}`, `${loader_version_range}`, `${mod_id}`, `${mod_name}`, `${mod_license}`, `${mod_version}`

## Gradle properties
- `mod_id=avaritia`, `mod_name=Avaritia`, `mod_version=1.0.0`
- `mod_group_id=com.avaritia` (used for Maven, not matching the Java package `com.example.avaritia`)

## Dependencies in neoforge.mods.toml
- Required: neoforge, minecraft
- Optional: jei (CLIENT only), jade (BOTH), curios (BOTH)
- No CraftTweaker, KubeJS, CCL, ProjectE, EMI, REI
