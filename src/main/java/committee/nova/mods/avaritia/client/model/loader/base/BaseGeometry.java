package committee.nova.mods.avaritia.client.model.loader.base;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

/** Custom art decorates the vanilla-generated mesh, including its override transforms. */
public abstract class BaseGeometry<U extends BaseGeometry<U>> {
    public final BlockModel baseModel;
    protected BaseGeometry(BlockModel baseModel) { this.baseModel = baseModel; }
    public abstract BakedModel bake(BakedModel nativeBase, ModelBaker baker,
        Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
        ItemOverrides overrides, ResourceLocation location);
}
