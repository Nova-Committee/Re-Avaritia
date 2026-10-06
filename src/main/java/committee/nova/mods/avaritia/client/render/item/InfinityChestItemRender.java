package committee.nova.mods.avaritia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.tile.InfinityChestBlockRender;
import committee.nova.mods.avaritia.client.tint.RainbowTintSource;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.joml.Vector3fc;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class InfinityChestItemRender implements NoDataSpecialModelRenderer {
    private static final SpriteId INFINITY_CHEST_SPRITE = Sheets.CHEST_MAPPER.apply(Const.rl("infinity_chest"));

    private final ChestModel model;
    private final SpriteGetter sprites;

    public InfinityChestItemRender(EntityModelSet entityModelSet, SpriteGetter sprites) {
        this.model = new ChestModel(entityModelSet.bakeLayer(InfinityChestBlockRender.INFINITY_CHEST));
        this.sprites = sprites;
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector output, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        output.submitModel(this.model, 0.0F, poseStack, packedLight, packedOverlay,
                RainbowTintSource.currentColor(), INFINITY_CHEST_SPRITE, this.sprites, outlineColor, null);
    }

    @Override
    public void getExtents(@NotNull Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(0.0F);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<InfinityChestItemRender.Unbaked> MAP_CODEC = MapCodec.unit(new InfinityChestItemRender.Unbaked());

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Void>> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new InfinityChestItemRender(context.entityModelSet(), context.sprites());
        }
    }
}
