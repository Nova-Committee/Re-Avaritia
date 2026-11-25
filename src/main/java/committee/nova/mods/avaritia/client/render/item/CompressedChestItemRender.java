package committee.nova.mods.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.shader.AvaritiaShaders;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public final class CompressedChestItemRender implements NoDataSpecialModelRenderer {
    private static final SpriteId CHEST_SPRITE = Sheets.CHEST_MAPPER.apply(Const.rl("compressed_chest"));

    private final ChestModel model;
    private final SpriteGetter sprites;
    private final RenderType renderType;

    private CompressedChestItemRender(SpecialModelRenderer.BakingContext context, boolean gui) {
        model = new ChestModel(context.entityModelSet().bakeLayer(ModelLayers.CHEST));
        sprites = context.sprites();
        renderType = CHEST_SPRITE.renderType(gui ? CompressedChestItemRender::guiCutout : RenderTypes::entityCutout);
    }

    private static RenderType guiCutout(Identifier atlas) {
        return RenderType.create("avaritia_compressed_chest_gui",
                RenderSetup.builder(AvaritiaShaders.COMPRESSED_CHEST_GUI)
                        .withTexture("Sampler0", atlas)
                        .useLightmap()
                        .useOverlay()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                        .createRenderSetup());
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector output, int packedLight, int packedOverlay,
                       boolean hasFoilType, int outlineColor) {
        output.submitModel(model, 0.0F, poseStack, renderType, packedLight, packedOverlay,
                -1, sprites.get(CHEST_SPRITE), outlineColor);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        model.setupAnim(0.0F);
        model.root().getExtentsForGui(new PoseStack(), output);
    }

    public record Unbaked(boolean gui) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = Codec.BOOL.optionalFieldOf("gui", false)
                .xmap(Unbaked::new, Unbaked::gui);

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new CompressedChestItemRender(context, gui);
        }
    }
}
