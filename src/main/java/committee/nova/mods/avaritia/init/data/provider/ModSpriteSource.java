package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

/**
 * Name: Avaritia-forge / ModSpriteSource
 * Author: cnlimiter
 * CreateTime: 2023/9/23 2:15
 * Description:
 */

public class ModSpriteSource extends SpriteSourceProvider {
    public ModSpriteSource(DataGenerator generator, ExistingFileHelper fileHelper) {
        super(generator.getPackOutput(), fileHelper, Static.MOD_ID);
    }

    @Override
    protected void addSources() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new DirectoryLister("shader", ""));
    }
}
