package committee.nova.mods.avaritia.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.model.ItemQuadBakery;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.item.IItemRenderer;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.client.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.SimpleModelState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Name: Avaritia-forge / CosmicbakeModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 4:41
 * Description:
 */

public class CosmicBakeModel extends WrappedItemModel implements IItemRenderer {
    private final List<BakedQuad> maskQuads;
    private final ItemOverrides overrideList = new ItemOverrides() {
        public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @javax.annotation.Nullable ClientLevel world, @javax.annotation.Nullable LivingEntity entity, int seed) {
            CosmicBakeModel.this.entity = entity;
            CosmicBakeModel.this.world = world == null ? (entity == null ? null : (ClientLevel)entity.level()) : null;
            return CosmicBakeModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
        }
    };


    public CosmicBakeModel(final BakedModel wrapped, final TextureAtlasSprite maskSprite) {
        super(wrapped);
        this.maskQuads = ItemQuadBakery.bakeItem(maskSprite);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() == ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        }
        if (stack.getItem() == ModItems.infinity_bow.get()) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        }
        if (stack.getItem() != ModItems.infinity_bow.get() && stack.getItem() != ModItems.infinity_sword.get()) {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }
        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1.0f;
        if (transformType != ItemDisplayContext.GUI) {
            yaw = (float)((double)(mc.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
            pitch = -((float)((double)(mc.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
        } else {
            scale = 25.0F;
        }
        if (AvaritiaShaders.cosmicOpacity != null) {
            AvaritiaShaders.cosmicOpacity.set(1.0F);
        }
        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);
        final ItemRenderer itemRenderer = mc.getItemRenderer();
        final VertexConsumer cons = source.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
        //this.renderWrapped(stack, pStack, source, light, overlay, true, vertexConsumer -> cons);
        itemRenderer.renderQuadList(pStack, cons, this.maskQuads, stack, light, overlay);
        //itemRenderer.renderModelLists(wrapped, stack, light, overlay, pStack, cons);
    }


    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

}
