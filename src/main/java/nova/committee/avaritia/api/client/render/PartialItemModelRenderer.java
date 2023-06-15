package nova.committee.avaritia.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.model.data.ModelData;
import nova.committee.avaritia.util.render.Iterate;

public class PartialItemModelRenderer {
    static PartialItemModelRenderer instance;
    ItemStack stack;
    int overlay;
    PoseStack ms;
    ItemDisplayContext transformType;
    MultiBufferSource buffer;

    static PartialItemModelRenderer get() {
        if (instance == null)
            instance = new PartialItemModelRenderer();
        return instance;
    }

    public static PartialItemModelRenderer of(ItemStack stack, ItemDisplayContext transformType,
                                              PoseStack ms, MultiBufferSource buffer, int overlay) {
        PartialItemModelRenderer instance = get();
        instance.stack = stack;
        instance.buffer = buffer;
        instance.ms = ms;
        instance.transformType = transformType;
        instance.overlay = overlay;
        return instance;
    }

    public void render(BakedModel model, int light) {
        render(model, RenderTypes.getItemPartialTranslucent(), light);
    }

    public void renderSolid(BakedModel model, int light) {
        render(model, RenderTypes.getItemPartialSolid(), light);
    }

    public void renderSolidGlowing(BakedModel model, int light) {
        render(model, RenderTypes.getGlowingSolid(), light);
    }

    public void renderGlowing(BakedModel model, int light) {
        render(model, RenderTypes.getGlowingTranslucent(), light);
    }

    public void render(BakedModel model, RenderType type, int light) {
        if (stack.isEmpty())
            return;

        ms.pushPose();
        ms.translate(-0.5D, -0.5D, -0.5D);

        if (!model.isCustomRenderer())
            renderBakedItemModel(model, light, ms,
                    ItemRenderer.getFoilBufferDirect(buffer, type, true, stack.hasFoil()));
        else
            RenderProperties.get(stack)
                    .getItemStackRenderer()
                    .renderByItem(stack, transformType, ms, buffer, light, overlay);


        ms.popPose();
    }

    private void renderBakedItemModel(BakedModel model, int light, PoseStack ms, VertexConsumer p_229114_6_) {
        ItemRenderer ir = Minecraft.getInstance()
                .getItemRenderer();
        RandomSource random = RandomSource.create();
        ModelData data = ModelData.EMPTY;

        for (Direction direction : Iterate.directions) {
            random.setSeed(42L);
            ir.renderQuadList(ms, p_229114_6_, model.getQuads(null, direction, random, data, null), stack, light,
                    overlay);
        }

        random.setSeed(42L);
        ir.renderQuadList(ms, p_229114_6_, model.getQuads(null, null, random, data, null), stack, light, overlay);
    }
}
