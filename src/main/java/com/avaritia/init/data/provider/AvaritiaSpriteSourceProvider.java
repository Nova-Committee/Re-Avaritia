package com.avaritia.init.data.provider;

import com.avaritia.Const;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

public class AvaritiaSpriteSourceProvider extends SpriteSourceProvider {
    private static final Identifier BLOCKS_ATLAS = Identifier.withDefaultNamespace("blocks");
    private static final Identifier CHESTS_ATLAS = Identifier.withDefaultNamespace("chests");

    public AvaritiaSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Const.MOD_ID);
    }

    @Override
    protected void gather() {
        atlas(BLOCKS_ATLAS).addSource(new DirectoryLister("misc", "misc/"));
        atlas(BLOCKS_ATLAS).addSource(new DirectoryLister("models", "models/"));
        atlas(BLOCKS_ATLAS).addSource(new DirectoryLister("mask", "mask/"));
        atlas(CHESTS_ATLAS).addSource(new DirectoryLister("block/chest", "block/chest/"));
    }
}
