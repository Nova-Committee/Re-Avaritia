package com.avaritia.client.model.loader.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

public abstract class AvaritiaCustomItemModel implements ItemModel {
    protected final ItemModel wrapped;

    protected AvaritiaCustomItemModel(ItemModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, net.minecraft.client.multiplayer.ClientLevel level,
                       ItemOwner owner, int seed) {
        wrapped.update(renderState, stack, resolver, displayContext, level, owner, seed);
    }

    public abstract void renderCustom(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                                      MultiBufferSource source, int packedLight, int packedOverlay);

    public interface Factory {
        AvaritiaCustomItemModel create(ItemModel wrapped, ModelBaker baker, ResolvedModel resolvedModel,
                                       TextureSlots textureSlots, Matrix4fc transform);
    }

    public interface LoaderFactory {
        Unbaked create(Identifier baseModel);

        MapCodec<? extends ItemModel.Unbaked> codec();
    }
}
