# Gameplay and server configuration

## Infinity Sword

Set an array of entity type IDs in `config/avaritia-local.toml` under `[tools]` with the key `sword_attack_entity_blacklist`. The default is `[]`. For example, use `["minecraft:zombie", "minecraft:chest_minecart"]` and restart the server. This affects the sword’s two right-click area attacks. The code separately excludes Lootr’s `lootr:lootr_minecart` and `lootr:chest_minecart` IDs. Direct left-click damage, the Infinity Totem, and block chests have their own behavior. [ModConfig.java](../../../src/main/java/committee/nova/mods/avaritia/init/config/ModConfig.java#L77) · [InfinitySwordItem.java](../../../src/main/java/committee/nova/mods/avaritia/common/item/tools/infinity/InfinitySwordItem.java#L122)

## Infinity Shield

Sneak and use to cycle modes. Use normally to raise the shield. Normal mode provides active blocking. Defending reflects blocked hits while raised and deflects projectiles while held. Ultimate Defense provides passive protection while held, with exceptions for Infinity damage and damage that bypasses invulnerability. Floating provides buoyancy, a slower fall, sneaking hover and fall damage protection when the mode permits it. These modes are item behavior in Java. See the shield code for the branch’s exact handling of piercing arrows, swimming, flying, and bypass effects. [InfinityShieldItem.java](../../../src/main/java/committee/nova/mods/avaritia/common/item/tools/infinity/InfinityShieldItem.java#L80)

## Compressor automation and the Infinity Ring

Recipes define outputs and materials. The compressor’s side input/output rules and the Infinity Ring’s personal-dimension travel are implemented in Java. A recipe can supply the compressor’s ingredient, result, input count and processing time. Side modes are changed in the machine’s controls. [SideConfiguration.java](../../../src/main/java/committee/nova/mods/avaritia/core/io/SideConfiguration.java#L45) · [InfinityRingDimensions.java](../../../src/main/java/committee/nova/mods/avaritia/common/dimension/InfinityRingDimensions.java#L129)
