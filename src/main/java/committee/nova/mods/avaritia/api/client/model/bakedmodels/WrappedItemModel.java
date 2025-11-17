package committee.nova.mods.avaritia.api.client.model.bakedmodels;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModel;
import committee.nova.mods.avaritia.api.client.model.PerspectiveModelState;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public abstract class WrappedItemModel implements PerspectiveModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    protected BakedModel wrapped;
    protected PerspectiveModelState parentState;
    protected boolean cosmic = false;
    @Nullable
    protected LivingEntity entity;
    @Nullable
    protected ClientLevel world;
    protected ItemOverrides overrideList;

    public WrappedItemModel(BakedModel wrapped) {
        this.overrideList = new ItemOverrides() {
            @Override
            public BakedModel resolve(final @NotNull BakedModel originalModel, final @NotNull ItemStack stack, final ClientLevel world, final LivingEntity entity, final int seed) {
                WrappedItemModel.this.entity = entity;
                WrappedItemModel.this.world = ((world == null) ? ((entity == null) ? null : ((ClientLevel) entity.level())) : null);
                if (WrappedItemModel.this.cosmic) {
                    return WrappedItemModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
                }
                return originalModel;
            }
        };
        this.wrapped = wrapped;
        this.parentState = TransformUtils.stateFromItemTransforms(wrapped.getTransforms());
    }


    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        final LinkedList<BakedQuad> quads = new LinkedList<>();
        for (final TextureAtlasSprite sprite : sprites) {
            final List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(sprites.indexOf(sprite), "layer" + sprites.indexOf(sprite), sprite.contents());
            for (final BlockElement element : unbaked) {
                for (final Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(), new PerspectiveModelState(ImmutableMap.of()), element.rotation, element.shade));
                }
            }
        }
        return quads;
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return this.parentState;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    /**
     * Render the wrapped model.
     * <p>
     * This does not take into account all the special edge cases hardcoded into
     * {@link ItemRenderer#render(ItemStack, ItemDisplayContext, boolean, PoseStack, MultiBufferSource, int, int, BakedModel)}.
     *
     * @param stack         The stack.
     * @param pStack        The pose stack.
     * @param buffers       The {@link MultiBufferSource}.
     * @param packedLight   The packed light coords. See {@link LightTexture}.
     * @param packedOverlay The packed Overlay coords. See {@link OverlayTexture}.
     * @param fabulous      If fabulous is required. (not sure on this desc, might be inaccurate as its value in vanilla
     *                      is mixed with the aforementioned hardcoded edge cases.)
     */
    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, boolean fabulous) {
        renderWrapped(stack, pStack, buffers, packedLight, packedOverlay, fabulous, Function.identity());
    }

    /**
     * Overload of {@link #renderWrapped(ItemStack, PoseStack, MultiBufferSource, int, int, boolean)}.
     * <p>
     * Except, with a callback to wrap the {@link VertexConsumer} used.
     *
     * @param stack         The stack.
     * @param pStack        The pose stack.
     * @param buffers       The {@link MultiBufferSource}.
     * @param packedLight   The packed light coords. See {@link LightTexture}.
     * @param packedOverlay The packed Overlay coords. See {@link OverlayTexture}.
     * @param fabulous      If fabulous is required. (not sure on this desc, might be inaccurate as its value in vanilla
     *                      is mixed with the aforementioned hardcoded edge cases.)
     */
    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, boolean fabulous, Function<VertexConsumer, VertexConsumer> consOverride) {
        BakedModel model = this.wrapped.getOverrides().resolve(this.wrapped, stack, this.world, this.entity, 0);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel bakedModel : model.getRenderPasses(stack, true)) {
            for (RenderType rendertype : bakedModel.getRenderTypes(stack, true)) {
                itemRenderer.renderModelLists(bakedModel, stack, packedLight, packedOverlay, pStack,
                        consOverride.apply(buffers.getBuffer(rendertype)));
            }
        }
    }
}
