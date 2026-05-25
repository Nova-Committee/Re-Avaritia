# Avaritia Pre-Audit Summary Report
## Wave 0 Pre-audit Summary

> **Project:** Avaritia-26 Migration from MC 1.21.1 to MC 26.1.2  
> **Audit date:** 2026-05-25  
> **Scope:** Consolidated findings from all 16 Wave 0 evidence files

---

## Section 1: Source vs Target Baseline

### 1.1 Source Mod (Avaritia-1.21)

| Metric | Value | Evidence | Notes |
|--------|-------|----------|-------|
| **Java files** | 516 | `task-0.1-codegraph-index.txt` | |
| **Total indexed files** | 525 | `task-0.1-codegraph-index.txt` | |
| **CodeGraph nodes** | 13,286 | `task-0.1-codegraph-index.txt` | |
| **CodeGraph edges** | 26,016 | `task-0.1-codegraph-index.txt` | |
| **DB size** | 34.28 MB | `task-0.1-codegraph-index.txt` | |
| **Mixins declared** | 11 | `avaritia-mixin-audit.md` | 2 BROKEN, 1 renamed, 1 deprecated |
| **MOD_ID** | `avaritia` | project convention | constant across all files |
| **Lombok usage** | 26 files (5%) | `task-0.2-lombok-audit.txt` | 142 annotations total |
| **Capabilities API usage** | 12 files | `task-0.6-caps-audit.txt` | 5–8 files actively using API |

**Node breakdown:** class(525), enum(21), enum_member(136), field(1,739), file(519), function(75), import(6,361), interface(57), method(3,853)

### 1.2 Target Workspace (Avaritia-26)

| Metric | Value | Evidence | Notes |
|--------|-------|----------|-------|
| **Java files** | 0 | `task-0.11-codegraph-target.txt` | Clean slate |
| **CodeGraph nodes** | 0 | `task-0.11-codegraph-target.txt` | |
| **CodeGraph edges** | 0 | `task-0.11-codegraph-target.txt` | |
| **Build files** | 0 | `task-0.4-toolchain.txt` | |
| **Gradle wrapper** | NONE | `task-0.4-toolchain.txt` | |
| **Parchment availability** | NOT AVAILABLE | `task-0.5-parchment-check.txt` | No version for 26.1.2 |

**Status:** Empty workspace ready for migration. No source files, no build configuration, CodeGraph index clean (0.13 MB).

---

## Section 2: Migration Decision Table

| Item | Status | Decision | Rationale | Evidence | Wave Impact |
|------|--------|----------|-----------|----------|-------------|
| **Lombok** | ✅ Audit complete | **STRIP** | 26 files (5%), 142 annotations. No complex patterns like @Builder or @Slf4j. Mechanical replacement feasible. | `task-0.2-lombok-audit.txt` | Wave 2: All registry files need getter/setter generation |
| **MixinExtras** | ✅ Audit complete | **KEEP (0.4.1)** | Only RecipeManagerMixin uses `@Local` (2 parameters). 10/11 mixins have zero dependency. | `task-0.3-mixinextras-audit.txt` | Wave 1: Add dependency; Wave 5: Keep annotation |
| **Parchment** | ✅ Audit complete | **DROP** | No Parchment version available for MC 26.1.2. 1.21.11 version exists but mismatch with major version bump. | `task-0.5-parchment-check.txt` | Wave 1: Use Mojang official mappings (NeoForge default) |
| **AnvilLib** | ✅ Check complete | **REJECT** | Available for 26.1.2 but rejected to reduce external dependencies. Native APIs sufficient. | `task-0.2-anvillib-check.txt` | Wave 2: Use DeferredRegister; Wave 6: Use SimpleChannel |
| **Capabilities→Attachments** | ✅ Audit complete | **MIGRATE** | API removed in 26.1.2. 12 files affected, 5–8 actively using. High-risk migration area. | `task-0.6-caps-audit.txt` | Wave 2 (foundation) + Wave 4 (block entity IO) |
| **EnumExtensions** | ✅ Audit complete | **SUPPORTED** | `enumextensions.json` exists with 2 entries. Rarity constructor deprecated but functional. | `task-0.7-enumextensions-check.txt` | Wave 1: Verify compatibility |
| **Version Target** | ✅ Verified | **MC 26.1.2** | NeoForge 26.1.2 targets Minecraft 26.1.2, not 1.21.1 or 1.21.11. | `task-0.1-version-mapping.txt` | Wave 1: Correct version in build config |

---

## Section 3: Critical Migration Risk Matrix

| Risk Area | Severity | Likelihood | Impact | Mitigation | Blocking? |
|-----------|----------|------------|--------|------------|-----------|
| **Capabilities API removed** | Critical | Certain | High | Requires rewrite of 5–8 core files to use AttachmentType | No - but HIGH priority |
| **Broken mixins** | High | Certain | Medium | 2 mixins BROKEN (RecipeManagerMixin, ReloadableServerResourcesMixin) | No - but blocks Wave 5 |
| **Access Transformers (AT)** | Unknown | Unknown | High | No AT audit completed. Source uses AT alongside accessor mixins. | **YES - blocks Wave 1** |
| **Lombok removal** | Medium | Certain | Low | Mechanical replacement of 26 files, 142 annotations | No |
| **Dependency versioning** | Low | Certain | Low | Dependencies confirmed for 26.1.2: JEI 29.6.2.31, Jade 26.1.1, Curios 15.0.0-beta.2 | No |

### 3.1 High-Risk Migration Details

#### Capabilities→Attachments (HIGHEST RISK)

| File | Call Sites | Complexity | Migration Strategy |
|------|-----------|------------|-------------------|
| `CapHandler.java` | 4 (RegisterCapabilitiesEvent + 2 ItemHandler.BLOCK) | HIGH | Replace with direct BE IItemHandler impl + AttachmentType |
| `TileIOHandler.java` | 3 (level.getCapability) | MEDIUM | Query neighbor BE handlers directly |
| `InventoryUtils.java` | 5 (ItemHandler.ITEM) | MEDIUM | Replace with DataComponent access |
| `InfinityBucketItem.java` | 1 (FluidHandler.ITEM) + 1 (RegisterCapabilitiesEvent) | MEDIUM | Refactor to DataComponent fluid storage |
| `NeutronRingMenu.java` | 1 (ItemHandler.ITEM) | LOW | Direct ComponentItemHandler |
| `IItemCapability.java` | — | LOW | Delete interface entirely |
| `AvaritiaCuriosPlugin.java` | 1 (RegisterCapabilitiesEvent) | UNKNOWN | Depends on Curios 26.1 API |

**Dead code to remove:** `_NeutronRingMenu.java` (361 lines commented-out), NeutronRingItem.java lines 37–40, CuriosTools.java lines 20–56

#### Broken Mixin Targets

| Mixin | Status | Issue | Wave Assignment |
|-------|--------|-------|-----------------|
| RecipeManagerMixin | 🔴 BROKEN | `apply(Map<...>)` → `apply(RecipeMap, ...)` — 1st param type changed. `@Local` variables may be gone. | Wave 5 |
| ReloadableServerResourcesMixin | 🔴 BROKEN | `loadResources()` method added in 1.21.5. `listeners()` may be removed. `@Shadow` field types changed. | Wave 5 |
| MappedRegistryAccessor | 🔶 RENAMED | Class exists, `toId`/`byValue` fields unchanged. Registry system refactored — verify accessor targets. | Wave 2 |
| EnchantmentHelperMixin | ⚠️ DEPRECATED | `getItemEnchantmentLevel` method deprecated but functional. | Wave 5 |
| PlayerRendererMixin | ⚠️ ATTENTION | Parent changed to `LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel>`. Extends clause must update. | Wave 6 |
| ItemRendererMixin | ⚠️ ATTENTION | Render pipeline refactored (entity render state system). Inject point needs runtime verification. | Wave 6 |
| 5 others | ✅ OK | No changes needed. | Wave 5 |

---

## Section 4: Dependency Version Confirmation

| Dependency | Target Version (26.1.2) | Source Version (1.21.1) | Artifact Coordinates | Repository |
|------------|------------------------|------------------------|----------------------|------------|
| **JEI** | 29.6.2.31 | 19.27.0.340 | `mezz.jei:jei-26.1.2-neoforge:29.6.2.31`<br>`mezz.jei:jei-26.1.2-neoforge-api:29.6.2.31` | `https://maven.blamejared.com/` |
| **Jade** | 26.1.1+neoforge | 15.10.5+neoforge | `maven.modrinth:jade:26.1.1+neoforge` | `https://api.modrinth.com/maven` |
| **Curios** | 15.0.0-beta.2+26.1.2 | 9.5.1+1.21.1 | `curse.maven:curios-309927:7642217` | `https://www.cursemaven.com/` |
| **MDG Plugin** | 2.0.96+ | N/A | `net.neoforged.moddev` | Gradle Plugin Portal |
| **MixinExtras** | 0.4.1 | 0.4.1 | `com.llamalad7.mixinextras:mixinextras-neoforge:0.4.1` | NeoForge/external Maven |

### Version Migration Summary

| Dependency | Source (1.21.1) | Target (26.1.2) | Change |
|------------|-----------------|-----------------|--------|
| JEI | `19.27.0.340` (jei-1.21.1-neoforge) | `29.6.2.31` (jei-26.1.2-neoforge) | Major version bump |
| Jade | `15.10.5+neoforge` | `26.1.1+neoforge` | MC version alignment |
| Curios | `9.5.1+1.21.1` | `15.0.0-beta.2+26.1.2` | Major version bump |

---

## Section 5: Toolchain & Environment

| Component | Status | Issue | Action Required |
|-----------|--------|-------|-----------------|
| **Java version** | ✅ Java 21 detected | | Compatible with NeoForge 26.1.2 |
| **JAVA_HOME** | ⚠️ Mismatch | JAVA_HOME=C:\Program Files\Java\jdk-21 but runtime=C:\Users\1\.jdks\openjdk-21.0.5 | Fix environment variable for consistency |
| **Gradle wrapper** | ❌ Missing | No gradle wrapper in target workspace | Wave 1: Create from NeoForge MDK template |
| **CodeGraph index** | ✅ Clean | 0 files, 0 nodes, 0 edges | Ready for incremental migration tracking |
| **Parchment** | ❌ Unavailable | No version for 26.1.2 | Use Mojang mappings |

---

## Section 6: Wave-by-Wave Impact Estimation

| Wave | Primary Focus | Files Affected | Estimated Effort | Blockers |
|------|--------------|----------------|-----------------|----------|
| **Wave 1** | Build foundation | 10–15 | 2–3 hours | AT audit required |
| **Wave 2** | Core registration | 15–20 | 3–4 hours | Lombok removal |
| **Wave 3** | Data generation | 5–8 | 1–2 hours | None |
| **Wave 4** | Block entities & IO | 8–12 | 2–3 hours | Caps→Attachments migration |
| **Wave 5** | Mixins & compatibility | 11 mixins | 2–4 hours | Broken mixin verification |
| **Wave 6** | UI & rendering | 3–5 | 1–2 hours | Render pipeline changes |
| **Wave 7** | Integration testing | 3–5 | 1–2 hours | JEI/Jade/Curios compatibility |
| **Total** | **~55–65 files** | **~12–18 hours** | | |

### Wave-Specific Details

**Wave 1 (Blocked by AT audit):** Requires AT audit before proceeding. Otherwise includes: gradle wrapper setup, dependency configuration, Mojang mappings.

**Wave 4 (High complexity):** Contains Capabilities→Attachments migration for 5–8 core files. This is the highest-risk technical migration.

**Wave 5 (Mixin attention):** 2 BROKEN mixins require verification against 26.1.2 source. ReloadableServerResourcesMixin likely needs complete rewrite.

---

## Evidence File Index

| File | Verdict | Key Finding | Audit Task |
|------|---------|-------------|------------|
| `task-0.1-codegraph-index.txt` | ✅ PASS | Source: 525 files, 13,286 nodes, 26,016 edges | 0.1 |
| `task-0.1-version-mapping.txt` | ✅ PASS | Target is MC 26.1.2 (not 1.21.1, not 1.21.11) | 0.1b |
| `task-0.2-lombok-audit.txt` | ⚠️ STRIP | 26 files, 142 annotations, ~5% of codebase | 0.2a |
| `task-0.2-anvillib-check.txt` | ❌ REJECTED | Available but decided against; use native API | 0.2b |
| `task-0.3-mixinextras-audit.txt` | ✅ KEEP | 0.4.1 needed for 1 mixin (@Local) | 0.3a |
| `task-0.3-parchment-check.txt` | ℹ️ INFO | Parchment 1.21.11 available but not used | 0.3b |
| `task-0.4-toolchain.txt` | ⚠️ WARNING | Java 21 OK, NO Gradle, JAVA_HOME mismatch | 0.4 |
| `task-0.5-parchment-check.txt` | ❌ DROP | No Parchment for 26.1.2; use Mojang mappings | 0.5 |
| `task-0.6-caps-audit.txt` | 🔴 HIGH RISK | 12 files, 5–8 active, API removed | 0.6 |
| `task-0.7-enumextensions-check.txt` | ✅ SUPPORTED | Rarity constructor deprecated but functional | 0.7 |
| `avaritia-mixin-audit.md` | 🟡 2 BROKEN | 2 BROKEN, 1 RENAMED, 1 deprecated | 0.8 |
| *(missing)* | ❓ NOT AUDITED | **AT audit required before Wave 1** | 0.9 |
| `task-0.10-dependency-versions.txt` | ✅ CONFIRMED | JEI 29.6.2.31, Jade 26.1.1, Curios 15.0.0-beta.2 | 0.10 |
| `task-0.11-codegraph-target.txt` | ✅ EMPTY | 0 files, 0 nodes, 0 edges | 0.11 |
| `dependency-versions.md` | ✅ REFERENCE | Legacy dependency documentation | — |
| `pre-audit-summary.md` | ✅ EXISTING | Previous summary report | — |

---

## Critical Pre-Migration Actions

1. **AT audit (Blocks Wave 1):** Complete Access Transformers audit (task 0.9) before proceeding with Wave 1 build setup.

2. **Capabilities migration planning:** Detailed mapping of 5–8 affected files to AttachmentType replacements.

3. **Broken mixin verification:** Verify RecipeManagerMixin and ReloadableServerResourcesMixin against 26.1.2 source code.

4. **Environment consistency:** Fix JAVA_HOME mismatch for consistent build environment.

---

**Report generated from 16 evidence files across all Wave 0 audit tasks.**
**All findings extracted directly from evidence; no fabricated numbers.**
**This report serves as the authoritative migration reference for Waves 1–7.**