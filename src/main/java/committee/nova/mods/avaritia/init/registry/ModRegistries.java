package committee.nova.mods.avaritia.init.registry;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.world.level.block.Block;

/** Called after vanilla contents creation; repeated bootstrap calls register nothing twice. */
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
        ModParticles.PARTICLE_TYPE.registerAll();
        ModRecipeTypes.RECIPES.registerAll();
        ModRecipeSerializers.SERIALIZERS.registerAll();
        // 26.3 客户端不再持有完整配方表；Fabric 仅同步显式注册过的序列化器。
        // 不注册则客户端 getSynchronizedRecipes() 查不到任何模组配方（JEI 分类全空、配方书缺失）。
        for (var entry : ModRecipeSerializers.SERIALIZERS.getEntries()) {
            RecipeSynchronization.synchronizeRecipeSerializer(entry.get());
        }
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
