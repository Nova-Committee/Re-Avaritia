package committee.nova.mods.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * InfinitySwordRender
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/9 下午1:11
 */
public class InfinitySwordRender extends BlockEntityWithoutLevelRenderer {
    public InfinitySwordRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pDisplayContext, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel bakedModel1 = minecraft.getModelManager().getModel(new ModelResourceLocation(Static.rl("infinity_sword"), "inventory"));
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1.0f;
        if (pDisplayContext != ItemDisplayContext.GUI) {
            yaw = (float) ((double) (minecraft.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
            pitch = -((float) ((double) (minecraft.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
        } else {
            scale = 25f;
        }
        if (AvaritiaShaders.cosmicOpacity != null) {
            AvaritiaShaders.cosmicOpacity.glUniform1f(1.0F);
        }
        AvaritiaShaders.cosmicYaw.glUniform1f(yaw);
        AvaritiaShaders.cosmicPitch.glUniform1f(pitch);
        AvaritiaShaders.cosmicExternalScale.glUniform1f(scale);
        final VertexConsumer cons = pBuffer.getBuffer(AvaritiaShaders.COSMIC_RENDER_TYPE);
        itemRenderer.renderModelLists(bakedModel1, pStack, pPackedLight, pPackedOverlay, pPoseStack, cons);
        pPoseStack.popPose();
    }
}
