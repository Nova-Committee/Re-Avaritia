package committee.nova.mods.avaritia.client.model.bake;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * InfinitySwordBakeModel
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/9 下午1:59
 */
public class InfinitySwordBakeModel implements BakedModel {
    private final BakedModel swordModel;
    public InfinitySwordBakeModel(BakedModel swordModel) {
        this.swordModel = swordModel;
    }
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, @NotNull RandomSource pRandom) {
        return this.swordModel.getQuads(pState, pDirection, pRandom);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        throw new AssertionError("IForgeBakedModel::getQuads should never be called, only IForgeBakedModel::getQuads");
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.swordModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.swordModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.swordModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.swordModel.getParticleIcon();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.swordModel.getOverrides();
    }
    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        return this.swordModel.applyTransform(transformType,poseStack,applyLeftHandTransform);
    }
}
