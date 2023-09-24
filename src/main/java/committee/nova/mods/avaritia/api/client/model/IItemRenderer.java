package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.util.client.TextureUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Author cnlimiter
 * CreateTime 2023/9/24 5:16
 * Name IItemRenderer
 * Description
 */

public interface IItemRenderer extends PerspectiveModel {
    void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay);

    //Useless methods for IItemRenderer.
    //@formatter:off
    @Override default@NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) { return Collections.emptyList(); }
    @Override default boolean isCustomRenderer() { return true; }
    @Override default@NotNull TextureAtlasSprite getParticleIcon() { return TextureUtil.getMissingSprite(); }
    @Override default@NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}
