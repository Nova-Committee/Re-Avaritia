package com.avaritia.init.data.provider;

import com.avaritia.Const;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

public class AvaritiaSpriteSourceProvider extends SpriteSourceProvider {
    public AvaritiaSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Const.MOD_ID);
    }

    @Override
    protected void gather() {
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("misc", "misc/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("models", "models/"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryLister("mask", "mask/"));
        atlas(AtlasIds.CHESTS);
    }
}
