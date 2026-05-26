package com.avaritia.client.model.loader.base;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4fc;

import java.util.List;
import java.util.Optional;

public abstract class Unbaked implements ItemModel.Unbaked {
    protected final Identifier baseModel;
    protected final AvaritiaCustomItemModel.Factory factory;

    protected Unbaked(Identifier baseModel, AvaritiaCustomItemModel.Factory factory) {
        this.baseModel = baseModel;
        this.factory = factory;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(baseModel);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
        ModelBaker baker = context.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(baseModel);
        TextureSlots slots = resolvedModel.getTopTextureSlots();
        ItemModel wrapped = new CuboidItemModelWrapper.Unbaked(baseModel, Optional.empty(), List.of()).bake(context, transformation);
        return factory.create(wrapped, baker, resolvedModel, slots, transformation);
    }

    public Identifier baseModel() {
        return baseModel;
    }
}
