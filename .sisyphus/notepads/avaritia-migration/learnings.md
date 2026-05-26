# Learnings

## 2026-05-27: Lombok dependency added

- Version catalog (`gradle/libs.versions.toml`) already had `lombok` defined at version `1.18.38` (line 6) with library entry `lombok = { group = "org.projectlombok", name = "lombok", version.ref = "lombok" }` (line 22).
- Added to `build.gradle` dependencies block (after Curios, before JUnit):
  ```
  compileOnly libs.lombok
  annotationProcessor libs.lombok
  ```
- No changes to version catalog needed — only `build.gradle` was modified.
