## BLOCKER: Missing entity/tile/menu classes (not a migration issue)

76 compile errors remain in `./gradlew compileJava`. ALL errors reference classes from `com.avaritia.common.*` packages that **do not exist in the target project**:

- `com.avaritia.common.entity.*` / `.entity.ball.*` / `.entity.arrow.*` — entity classes
- `com.avaritia.common.tile.*` — tile entity classes
- `com.avaritia.common.menu.*` — menu classes
- `com.avaritia.common.block.chest.*` — block classes

These classes exist only in the original 1.21 source at `D:\workspace\minecraft\mods\3Nova\Avaritia\Avaritia-1.21` and need to be PORTED (with 26.1.2 API updates).

## Files that need these classes:
### Entity renderers (10+ files):
- BurningBallRender, HeavenArrowRender, HeavenSubArrowRender, NeutronArrowRender, 
  TracerArrowRender, ExplosionsArrowRender, BurningArrowRender, BladeSlashRender,
  FireBallRender, GapingVoidRender, SunProRender, RainProRender, StormProRender,
  TNTProEntityRender, InfinityThrownTridentRender, AcceleratorDisplayRender

### Tile renderers (4+ files):
- CompressedChestRenderer, InfinityChestBlockRender, InfinitatoTileRender, CompressorRenderer

### Screen files (10+ files):
- NeutronCollectorScreen, NeutronCompressorScreen, NeutronRingScreen, 
  ExtremeCraftScreen, NetherCraftScreen, EndCraftScreen, SculkCraftScreen,
  CompressedChestScreen, InfinityChestScreen, ExtremeSmithingScreen, ...etc.

### Particle files (2 files):
- ChargeParticle, ShockwaveParticle — both reference removed TextureSheetParticle

## Registration files that REFERENCE these classes (already exist):
- ModEntityTypes.java (imports com.avaritia.common.entity.*)
- ModTileEntities.java (imports com.avaritia.common.tile.*)
- ModMenus.java (imports com.avaritia.common.menu.*)

## Recommended approach:
1. Copy entity classes from `Avaritia-1.21` to `Avaritia-26/src/main/java/com/avaritia/common/entity/`
2. Update each entity class to use MC 1.21.5 APIs (EntityType.Builder changes, etc.)
3. Repeat for tile entities, menus, block classes
4. Fix ChargeParticle/ShockwaveParticle for MC 1.21.5 particle system changes
