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
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("misc/cosmic", "misc/cosmic/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("misc/eternal", "misc/eternal/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("models", "models/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("mask", "mask/"));
        atlas(Const.HALO_ATLAS_INFO)
                .addSource(new SingleFile(Const.rl("misc/halo"), Optional.empty()))
                .addSource(new SingleFile(Const.rl("misc/halo_noise"), Optional.empty()));
        atlas(AtlasIds.GUI).addSource(new SingleFile(
                Const.rl("block/resource/neutron"),
                Optional.of(Const.rl("gui/advancements/backgrounds/neutron"))
        ));
        addChestSprite("compressed_chest");
        addChestSprite("infinity_chest");
    }

    private void addChestSprite(String name) {
        atlas(AtlasIds.CHESTS).addSource(new SingleFile(
                Const.rl("block/chest/" + name),
                Optional.of(Const.rl("entity/chest/" + name))
        ));
    }
}
