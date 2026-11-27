package committee.nova.mods.avaritia.api.client.model.bakedmodels;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Const;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/14 22:58
 * @Description:
 */
public abstract class WrappedItemModel implements PerspectiveModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Map<List<TextureAtlasSprite>, List<BakedQuad>> MASK_QUADS = new java.util.HashMap<>();
    private static final Map<List<ResourceLocation>, List<BakedQuad>> RESOURCE_MASKS = new java.util.HashMap<>();
    public static void clearMaskCache() { MASK_QUADS.clear(); RESOURCE_MASKS.clear(); }
    public static List<BakedQuad> maskQuads(List<ResourceLocation> resources) {
        return RESOURCE_MASKS.computeIfAbsent(resources, locations -> {
            var atlas = Minecraft.getInstance().getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
            return bakeItem(locations.stream().map(atlas).toList());
        });
    }
    @Override
    public @NotNull ItemTransforms getTransforms() { return this.wrapped.getTransforms(); }
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
                WrappedItemModel.this.world = world != null ? world : entity != null && entity.level() instanceof ClientLevel level ? level : null;
                BakedModel resolved = WrappedItemModel.this.wrapped.getOverrides().resolve(WrappedItemModel.this.wrapped, stack, world, entity, seed);
                if (resolved instanceof WrappedItemModel custom && custom != WrappedItemModel.this) {
                    custom.entity = entity;
                    custom.world = WrappedItemModel.this.world;
                    return custom;
                }
                return originalModel;
            }
        };
        this.wrapped = wrapped;
        this.parentState = TransformUtils.stateFromItemTransforms(wrapped.getTransforms());
    }


    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        return MASK_QUADS.computeIfAbsent(List.copyOf(sprites), key -> {
            final List<BakedQuad> quads = new java.util.ArrayList<>();
            for (int i = 0; i < key.size(); i++) {
                TextureAtlasSprite sprite = key.get(i);
                for (BlockElement element : ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite.contents())) {
                    for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                        quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(),
                                PerspectiveModelState.IDENTITY, element.rotation, element.shade, Const.rl("dynamic")));
                    }
                }
            }
            return List.copyOf(quads);
        });
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
        if (model == null) model = this.wrapped;
        if (model instanceof WrappedItemModel custom) model = custom.wrapped;
        RenderType renderType = net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderType(stack, fabulous);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffers, renderType, true, stack.hasFoil());
        itemRenderer.renderModelLists(model, stack, packedLight, packedOverlay, pStack, consOverride.apply(consumer));
    }
}
