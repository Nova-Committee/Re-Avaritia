package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
import io.github.fabricators_of_create.porting_lib.data.SpriteSourceProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

/**
 * Name: Avaritia-forge / ModSpriteSource
 * Author: cnlimiter
 * CreateTime: 2023/9/23 2:15
 * Description:
 */

public class ModSpriteSource extends SpriteSourceProvider {
    public ModSpriteSource(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, Static.MOD_ID);
    }

    @Override
    protected void addSources() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("shader", ""));
    }
}
