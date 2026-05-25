# Wave 3 Learnings

## Task 3.6 — AvaritiaBlockStateProvider

### NeoForge BlockStateProvider API (MC 1.21 / NeoForge 26.1.2)

- **Package**: `net.neoforged.neoforge.client.model.generators.BlockStateProvider`
- **Constructor**: `BlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper)`
  - Requires `ExistingFileHelper` — simple dummy can be created with:
    ```java
    new ExistingFileHelper(List.of(), Set.of(MOD_ID), false, null, null)
    ```
- **Key methods**:
  - `simpleBlock(Block, ModelFile)` — blockstate with single variant
  - `simpleBlockWithItem(Block, ModelFile)` — blockstate + item model
  - `simpleBlockItem(Block, ModelFile)` — item model only (for blocks with custom blockstates)
  - `horizontalBlock(Block, ModelFile)` — 4-direction rotation via `HORIZONTAL_FACING`
  - `cubeAll(Block)` — cube_all model using `block/<name>` texture
  - `blockTexture(Block)` — returns `block/<name>` ResourceLocation
  - `models().cubeAll(name, texture)` — explicit cube_all model
- **Blockstate output path**: `assets/<modid>/blockstates/<block>.json` (auto-managed)
- **Model output path**: `assets/<modid>/models/block/<block>.json` (auto-managed)

### Block List (30 blocks in ModBlocks)
- 7 simple resource blocks (cube_all)
- 3 fake blocks (no BlockItem)
- 6 crafting tables (horizontal facing)
- 4 neutron collectors (horizontal facing)
- 4 neutron compressors (horizontal facing)
- 2 special directional (smithing table, anvil)
- 2 chests
- 2 special functional (soul_farmland, endless_cake)
