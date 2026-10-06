package committee.nova.mods.avaritia.init.registry;

import net.minecraft.world.level.block.Block;

/** Called once after vanilla contents creation, before vanilla registry freeze. */
public final class ModRegistries {
    private static boolean initialized;

    public static void initialize() {
        if (initialized) return;
        ModSounds.SOUNDS.registerAll();
        ModBlocks.BLOCKS.registerAll();
        ModItems.ITEMS.registerAll();
        ModEntities.ENTITIES.registerAll();
        ModTileEntities.BLOCK_ENTITIES.registerAll();
        ModMenus.MENUS.registerAll();
        ModMobEffects.MOB_EFFECTS.registerAll();
        ModEnchants.ENCHANTMENT.registerAll();
        ModParticles.PARTICLE_TYPE.registerAll();
        ModRecipeTypes.RECIPES.registerAll();
        ModRecipeSerializers.SERIALIZERS.registerAll();
        ModCreativeModeTabs.TABS.registerAll();
        // Vanilla Blocks initialized its global wire/palette IDs before these registrations.
        for (var entry : ModBlocks.BLOCKS.getEntries()) {
            for (var state : entry.get().getStateDefinition().getPossibleStates()) {
                if (Block.BLOCK_STATE_REGISTRY.getId(state) < 0) {
                    Block.BLOCK_STATE_REGISTRY.add(state);
                    state.initCache();
                }
            }
        }
        initialized = true;
    }

    private ModRegistries() {}
}
