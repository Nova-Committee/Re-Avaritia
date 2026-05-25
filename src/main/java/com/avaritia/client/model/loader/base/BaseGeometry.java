package com.avaritia.client.model.loader.base;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author cnlimiter
 */
public abstract class BaseGeometry<U extends IUnbakedGeometry<U>> implements IUnbakedGeometry<U> {
    public final BlockModel baseModel;

    public BaseGeometry(BlockModel baseModel) {
        this.baseModel = baseModel;
    }


    @Override
    public void resolveParents(@NotNull Function<Identifier, UnbakedModel> modelGetter, @NotNull IGeometryBakingContext context) {
        baseModel.resolveParents(modelGetter);
    }
}
