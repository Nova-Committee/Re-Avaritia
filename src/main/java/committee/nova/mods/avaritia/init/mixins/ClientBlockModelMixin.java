package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.client.model.loader.base.BaseGeometry;
import committee.nova.mods.avaritia.client.model.loader.base.NativeModelGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Geometry lives with its vanilla model and is released with the resource-reload generation. */
@Mixin(BlockModel.class)
public abstract class ClientBlockModelMixin implements NativeModelGeometry.GeometryHolder {
    @Unique private BaseGeometry<?> avaritia$geometry;
    @Override public BaseGeometry<?> avaritia$getGeometry() { return avaritia$geometry; }
    @Override public void avaritia$setGeometry(BaseGeometry<?> geometry) { avaritia$geometry = geometry; }
}
