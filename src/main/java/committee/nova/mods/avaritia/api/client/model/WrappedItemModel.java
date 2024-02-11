package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.util.client.TransformUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Name: Avaritia-forge / WrappedItemModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 5:17
 * Description:
 */

public abstract class WrappedItemModel implements BakedModel {
    protected final BakedModel wrapped;
    protected final ModelState parentState;
    @Nullable
    protected LivingEntity entity;
    @Nullable
    protected ClientLevel world;

    private final ItemOverrides overrideList = new ItemOverrides() {
        @Override
        public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            WrappedItemModel.this.entity = entity;
            WrappedItemModel.this.world = world == null ? entity == null ? null : (ClientLevel) entity.level() : null;
            return originalModel;
        }
    };

    public WrappedItemModel(BakedModel wrapped) {
        this.wrapped = wrapped;
        parentState = TransformUtil.stateFromItemTransforms(wrapped.getTransforms());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrideList;
    }


    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, boolean fabulous) {
        renderWrapped(stack, pStack, buffers, packedLight, packedOverlay, fabulous, Function.identity());
    }


    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, boolean fabulous, Function<VertexConsumer, VertexConsumer> consOverride) {
        BakedModel model = wrapped.getOverrides().resolve(wrapped, stack, world, entity, 0);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        RenderType rType = ItemBlockRenderTypes.getRenderType(stack, fabulous);
        VertexConsumer builder = ItemRenderer.getFoilBuffer(buffers, rType, true, stack.hasFoil());
        itemRenderer.renderModelLists(model, stack, packedLight, packedOverlay, pStack, consOverride.apply(builder));
    }
}
