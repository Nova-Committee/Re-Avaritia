package nova.committee.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.api.client.render.CustomRenderedItemModelRenderer;
import nova.committee.avaritia.api.client.render.PartialItemModelRenderer;
import nova.committee.avaritia.client.render.shader.CosmicShaderHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/4 10:49
 * Version: 1.0
 */
public class PerspectiveItemRender extends CustomRenderedItemModelRenderer<PerspectiveItemModel> {
    private final String name;

    public PerspectiveItemRender(String name) {
        super();
        this.name = name;
    }


    @Override
    protected void render(ItemStack stack, PerspectiveItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ms.pushPose();
        CosmicShaderHelper.inventoryRender = true;
        CosmicShaderHelper.useShader();

        renderer.render(model.getOriginalModel(), light);

        CosmicShaderHelper.releaseShader();
        CosmicShaderHelper.inventoryRender = false;
        ms.popPose();
    }

    @Override
    public @NotNull PerspectiveItemModel createModel(BakedModel originalModel) {
        return new PerspectiveItemModel(originalModel, name);
    }
}
