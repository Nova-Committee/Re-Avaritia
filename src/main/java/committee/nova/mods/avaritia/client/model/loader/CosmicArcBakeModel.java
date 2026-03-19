package committee.nova.mods.avaritia.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.avaritia.api.client.render.CCModel;
import committee.nova.mods.avaritia.api.client.render.CCRenderState;
import committee.nova.mods.avaritia.api.client.render.model.OBJParser;
import committee.nova.mods.avaritia.api.client.util.TransformUtils;
import committee.nova.mods.avaritia.client.AvaritiaForgeClient;
import committee.nova.mods.avaritia.client.model.entity.InfinityTridentModel;
import committee.nova.mods.avaritia.client.render.util.ArcRender;
import committee.nova.mods.avaritia.client.shader.AvaritiaRenderTypes;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static committee.nova.mods.avaritia.client.shader.AvaritiaShaders.COSMIC_UVS;

/**
 * @author cnlimiter
 */
public class CosmicArcBakeModel extends WrappedItemModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmicArcBakeModel.class);
    private final List<ResourceLocation> maskSprite;
    private Map<String, CCModel> tridentObjModel;

    public CosmicArcBakeModel(BakedModel wrapped, List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.cosmic = true;

        try {
            this.tridentObjModel = new OBJParser(Const.rl("models/infinity_trident.obj"))
                    .swapYZ()
                    .parse();
            LOGGER.info("Loaded trident OBJ models for item rendering: {}", tridentObjModel.keySet());
        } catch (Exception e) {
            LOGGER.error("Failed to load trident OBJ model, will use default model", e);
            this.tridentObjModel = Map.of();
        }
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source,
                           int packedLight, int packedOverlay) {


        // 渲染基础模型
        if (stack.is(ModItems.infinity_trident.get())) {
            this.parentState = TransformUtils.DEFAULT_TRIDENT;
            if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.GROUND || transformType == ItemDisplayContext.FIXED) {
                this.cosmic = true;
                this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
            } else {
                this.cosmic = false;

                if (!tridentObjModel.isEmpty()) {
                    pStack.pushPose();
                    try {

                        switch (transformType) {

                            // 第一人称
                            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                                pStack.scale(1F, 1F, 1F);

                                pStack.mulPose(Axis.XP.rotationDegrees(90));
                                pStack.mulPose(Axis.YP.rotationDegrees(180));

                                pStack.translate(0.2D, -0.2D, -1.3D);
                            }

                            // 第三人称
                            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                                pStack.scale(1.0F, 1.0F, 1.0F);

                                pStack.mulPose(Axis.XP.rotationDegrees(90));
                                pStack.mulPose(Axis.YP.rotationDegrees(180));

                                pStack.translate(0.0D, 0.0D, -1.5D);
                            }

                            // GUI
                            default -> {
                                pStack.scale(1.0F, 1.0F, 1.0F);
                                pStack.mulPose(Axis.XP.rotationDegrees(90));
                            }
                        }

                        CCRenderState cc = CCRenderState.instance();
                        cc.reset();
                        cc.bind(AvaritiaRenderTypes.TRIDENT, source, pStack);

                        for (CCModel model : tridentObjModel.values()) {
                            model.render(cc);
                        }

                    } finally {
                        pStack.popPose();
                    }
                } else {
                    // 如果 OBJ 模型加载失败，回退到原代码生成的模型
                    var tridentModel = new InfinityTridentModel();
                    pStack.pushPose();
                    pStack.scale(1.0F, -1.0F, -1.0F);
                    VertexConsumer vertexconsumer1 = ItemRenderer.getFoilBufferDirect(source, tridentModel.renderType(Res.TRIDENT_TEX), false, stack.hasFoil());
                    tridentModel.renderToBuffer(pStack, vertexconsumer1, packedLight, packedOverlay);
                    pStack.popPose();
                }
            }
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
            this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
        }


        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        if (transformType != ItemDisplayContext.GUI && transformType != ItemDisplayContext.GROUND) {
            // 保存当前变换矩阵
            pStack.pushPose();

            // 定义电弧起点和终点（相对于物品中心）
            float startX = -0.5f;
            float startY = 0.0f;
            float startZ = -0.5f;
            float endX = 0.5f;
            float endY = 0.0f;
            float endZ = 0.5f;

            // 设置电弧参数
            long seed = System.currentTimeMillis(); // 使用当前时间作为种子，使电弧随时间变化
            float thickness = 0.05f; // 电弧粗细
            int segments = 8; // 电弧分段数

            // 可选：添加一些偏移或旋转来增强视觉效果
//        pStack.translate(0.5, 0.5, 0.5); // 移动到物品中心
            pStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 10 % 360))); // 旋转

            // 渲染电弧
            ArcRender.renderArc(
                    pStack,
                    source,
                    seed,
                    startX, startY, startZ,
                    endX, endY, endZ,
                    thickness,
                    segments
            );

            // 恢复变换矩阵
            pStack.popPose();
        }

        // 渲染 Cosmic 效果
        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;
        if (AvaritiaForgeClient.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }
        AvaritiaShaders.cosmicTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        AvaritiaShaders.cosmicYaw.set(yaw);
        AvaritiaShaders.cosmicPitch.set(pitch);
        AvaritiaShaders.cosmicExternalScale.set(scale);
        AvaritiaShaders.cosmicUVs.set(COSMIC_UVS);
        final VertexConsumer cons = source.getBuffer(AvaritiaRenderTypes.COSMIC);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, packedLight, packedOverlay);
    }
}
