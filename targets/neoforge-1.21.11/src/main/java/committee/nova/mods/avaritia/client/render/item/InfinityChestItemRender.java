package committee.nova.mods.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.tile.InfinityChestBlockRender;
import committee.nova.mods.avaritia.client.tint.RainbowTintSource;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.Material;
import org.joml.Vector3fc;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * @author cnlimiter
 */
public class InfinityChestItemRender implements NoDataSpecialModelRenderer {
    private static final Material INFINITY_CHEST_SPRITE = Sheets.CHEST_MAPPER.apply(Const.rl("infinity_chest"));

    private final ChestModel model;
    private final MaterialSet sprites;

    public InfinityChestItemRender(EntityModelSet entityModelSet, MaterialSet sprites) {
        this.model = new ChestModel(entityModelSet.bakeLayer(InfinityChestBlockRender.INFINITY_CHEST));
        this.sprites = sprites;
    }

    @Override
    public void submit(@NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        // 1.21.11's submitModel takes an explicit RenderType and resolved sprite instead of
        // the 26.1 Material + MaterialSet pair.
        output.submitModel(this.model, 0.0F, poseStack, INFINITY_CHEST_SPRITE.renderType(RenderTypes::entityCutout),
                packedLight, packedOverlay, RainbowTintSource.currentColor(),
                this.sprites.get(INFINITY_CHEST_SPRITE), outlineColor, null);
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(0.0F);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<InfinityChestItemRender.Unbaked> MAP_CODEC = MapCodec.unit(new InfinityChestItemRender.Unbaked());

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new InfinityChestItemRender(context.entityModelSet(), context.materials());
        }
    }
}
