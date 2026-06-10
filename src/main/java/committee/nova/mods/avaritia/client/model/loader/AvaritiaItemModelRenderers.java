package committee.nova.mods.avaritia.client.model.loader;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloSetting;
import committee.nova.mods.avaritia.client.model.loader.utils.halo.HaloUtils;
import committee.nova.mods.avaritia.client.render.mesh.SimpleMesh;
import committee.nova.mods.avaritia.client.render.util.ArcRender;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaderUniforms;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Avaritia 物品模型使用的特殊渲染器集合。
 * <p>
 * {@link committee.nova.mods.avaritia.client.model.item.LayeredEffectItemModel} 只负责追加 LayerRenderState；
 * 真正把 halo、pulse、effect、三叉戟和弧光提交到 {@link SubmitNodeCollector} 的逻辑集中放在这里。
 */
public final class AvaritiaItemModelRenderers {
    public static final SpecialModelRenderer<EffectLayerArgument> EFFECT = new EffectSpecialRenderer();
    public static final SpecialModelRenderer<HaloLayerArgument> HALO = new HaloSpecialRenderer();
    public static final SpecialModelRenderer<PulseLayerArgument> PULSE = new PulseSpecialRenderer();
    public static final SpecialModelRenderer<TridentLayerArgument> TRIDENT = new TridentSpecialRenderer();
    public static final SpecialModelRenderer<ArcLayerArgument> ARC = new ArcSpecialRenderer();

    private static final int PULSE_ALPHA_COLOR = 0x99FFFFFF;
    // effect 覆盖层要排在基础物品层之后，才能让星空遮罩覆盖在原模型上而不是抢先写入。
    private static final int ITEM_EFFECT_OVERLAY_SUBMIT_ORDER = 1;

    private AvaritiaItemModelRenderers() {
    }

    public record EffectLayerArgument(List<BakedQuad> quads, RenderType renderType, AvaritiaShaderUniforms.Effect effect,
                               float time, float yaw, float pitch, float scale,
                               float opacity, float[] uvs) {
        /**
         * 在提交几何前绑定当前物品自己的 shader 参数。
         */
        void applyUniforms() {
            AvaritiaShaderUniforms.set(this.renderType, this.effect, this.time, this.yaw, this.pitch, this.scale, this.opacity, this.uvs);
        }
    }

    public record HaloLayerArgument(Identifier texture, HaloSetting setting) {
    }

    public record PulseLayerArgument(List<BakedQuad> quads) {
    }

    public record TridentLayerArgument(Map<String, SimpleMesh> models, ItemDisplayContext displayContext) {
    }

    public record ArcLayerArgument(long time) {
    }

    private static final class HaloSpecialRenderer implements SpecialModelRenderer<HaloLayerArgument> {
        @Override
        public void submit(@Nullable HaloLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null) {
                return;
            }

            // halo 使用圆形自定义几何，避免旧式单张方形透明贴图在 JEI/创造栏中露出方边。
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                    .get(new SpriteId(Const.HALO_ATLAS_LOCATION, argument.texture()));
            RenderType renderType = NeoForgeRenderTypes.getItemLayeredTranslucent(sprite.atlasLocation());
            submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                renderCircularHalo(pose, buffer, sprite, argument.setting(), lightCoords, overlayCoords);
            });
        }

        private static void renderCircularHalo(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
                                               HaloSetting setting, int lightCoords, int overlayCoords) {
            int segments = HaloUtils.circleSegments(setting.size());
            double radius = HaloUtils.haloRadius(setting.size());
            double min = 0.5 - radius;
            double diameter = radius * 2.0;
            int color = setting.color();
            int alpha = color >>> 24 & 255;
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;

            for (int i = 0; i < segments; i++) {
                double current = Math.PI * 2.0 * i / segments;
                double next = Math.PI * 2.0 * (i + 1) / segments;
                double currentX = 0.5 + Math.cos(current) * radius;
                double currentY = 0.5 + Math.sin(current) * radius;
                double nextX = 0.5 + Math.cos(next) * radius;
                double nextY = 0.5 + Math.sin(next) * radius;

                putHaloVertex(pose, buffer, sprite, 0.5, 0.5, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, currentX, currentY, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, nextX, nextY, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
                putHaloVertex(pose, buffer, sprite, 0.5, 0.5, min, diameter, red, green, blue, alpha, lightCoords, overlayCoords);
            }
        }

        private static void putHaloVertex(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite,
                                          double x, double y, double min, double diameter,
                                          int red, int green, int blue, int alpha, int lightCoords, int overlayCoords) {
            double u = (x - min) / diameter;
            double v = (y - min) / diameter;
            float atlasU = sprite.getU0() + (float) u * (sprite.getU1() - sprite.getU0());
            float atlasV = sprite.getV1() - (float) v * (sprite.getV1() - sprite.getV0());
            buffer.addVertex(pose, (float) x, (float) y, 0.0F)
                    .setColor(red, green, blue, alpha)
                    .setUv(atlasU, atlasV)
                    .setOverlay(overlayCoords)
                    .setLight(lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable HaloLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class PulseSpecialRenderer implements SpecialModelRenderer<PulseLayerArgument> {
        @Override
        public void submit(@Nullable PulseLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.quads().isEmpty()) {
                return;
            }

            submitNodeCollector.submitCustomGeometry(poseStack, NeoForgeRenderTypes.BLOCK_ITEM_LAYERED_TRANSLUCENT.get(), (pose, buffer) -> {
                QuadInstance instance = new QuadInstance();
                instance.setColor(PULSE_ALPHA_COLOR);
                instance.setLightCoords(lightCoords);
                instance.setOverlayCoords(overlayCoords);

                for (BakedQuad quad : argument.quads()) {
                    buffer.putBakedQuad(pose, quad, instance);
                }
            });
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable PulseLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class TridentSpecialRenderer implements SpecialModelRenderer<TridentLayerArgument> {
        @Override
        public void submit(@Nullable TridentLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.models().isEmpty()) {
                return;
            }

            poseStack.pushPose();
            try {
                transformTrident(argument.displayContext(), poseStack);
                submitNodeCollector.submitCustomGeometry(poseStack, AvaritiaRenderTypes.TRIDENT,
                        (poseState, vertexConsumer) -> argument.models().values().forEach(model ->
                                model.render(poseState, vertexConsumer, 0xFFFFFFFF, lightCoords, overlayCoords)));
            } finally {
                poseStack.popPose();
            }
        }

        private static void transformTrident(ItemDisplayContext displayContext, PoseStack poseStack) {
            switch (displayContext) {
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.translate(0.2D, -0.2D, -1.3D);
                }
                case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.translate(0.0D, 0.0D, -1.5D);
                }
                default -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable TridentLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class ArcSpecialRenderer implements SpecialModelRenderer<ArcLayerArgument> {
        @Override
        public void submit(@Nullable ArcLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null) {
                return;
            }

            poseStack.pushPose();
            try {
                poseStack.mulPose(Axis.YP.rotationDegrees((argument.time() * 8L) % 360L));
                submitNodeCollector.submitCustomGeometry(poseStack, ArcRender.ARC_RENDER_TYPE, (pose, buffer) ->
                        ArcRender.renderArc(pose, buffer, argument.time(),
                                -0.5F, 0.0F, -0.5F,
                                0.5F, 0.0F, 0.5F,
                                0.05F, 8));
            } finally {
                poseStack.popPose();
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable ArcLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }

    private static final class EffectSpecialRenderer implements SpecialModelRenderer<EffectLayerArgument> {
        @Override
        public void submit(@Nullable EffectLayerArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                           int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            if (argument == null || argument.quads().isEmpty()) {
                return;
            }

            argument.applyUniforms();
            // order(1) 与无深度写入的 effect RenderType 配合，保证基础模型和遮罩效果同时可见。
            submitNodeCollector.order(ITEM_EFFECT_OVERLAY_SUBMIT_ORDER)
                    .submitCustomGeometry(poseStack, argument.renderType(), (pose, buffer) -> {
                        QuadInstance instance = new QuadInstance();
                        instance.setColor(-1);
                        instance.setLightCoords(lightCoords);
                        instance.setOverlayCoords(overlayCoords);

                        for (BakedQuad quad : argument.quads()) {
                            buffer.putBakedQuad(pose, quad, instance);
                        }
                    });
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable EffectLayerArgument extractArgument(ItemStack stack) {
            return null;
        }
    }
}
