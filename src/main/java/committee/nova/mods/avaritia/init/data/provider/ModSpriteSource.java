package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import java.util.Optional;

/**
 * Name: Avaritia-forge / ModSpriteSource
 * Author: cnlimiter
 * CreateTime: 2023/9/23 2:15
 * Description:
 */

public class ModSpriteSource extends SpriteSourceProvider {
    public ModSpriteSource(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper, Static.MOD_ID);
    }

    @Override
    protected void addSources() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("misc", "misc/"));
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("models", "models/"));
        atlas(SpriteSourceProvider.CHESTS_ATLAS).addSource(new DirectoryLister("block/chest", "block/chest/"));
    }
}
