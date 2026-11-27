package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Name: Avaritia-forge / ModSpriteSource
 * Author: cnlimiter
 * CreateTime: 2023/9/23 2:15
 * Description:
 */

public class ModSpriteSource extends SpriteSourceProvider {
    public ModSpriteSource(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper fileHelper) {
        super(output, future, Const.MOD_ID, fileHelper);
    }

    @Override
    protected void gather() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("misc", "misc/"));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("models", "models/"));
        atlas(SpriteSourceProvider.CHESTS_ATLAS).addSource(new DirectoryLister("block/chest", "block/chest/"));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("mask", "mask/"));
    }
}
