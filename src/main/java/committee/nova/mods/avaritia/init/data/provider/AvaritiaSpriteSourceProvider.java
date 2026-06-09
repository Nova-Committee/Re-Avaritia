package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AvaritiaSpriteSourceProvider extends SpriteSourceProvider {
    public AvaritiaSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Const.MOD_ID);
    }

    @Override
    protected void gather() {
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("block/chest", "block/chest/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("misc", "misc/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("models", "models/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("mask", "mask/"));
        // ChestModel 遵循原版箱子的专用 chest atlas；源文件仍复用现有 block/chest 贴图。
        atlas(AtlasIds.CHESTS).addSource(new SingleFile(
                Const.rl("block/chest/infinity_chest"),
                Optional.of(Const.rl("entity/chest/infinity_chest"))
        ));
    }
}
