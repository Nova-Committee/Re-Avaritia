package nova.committee.avaritia.init.mixin.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/22 21:14
 * Version: 1.0
 */

@Mixin(ModelManager.class)
public interface AccessorModelManager {
    @Accessor
    Map<ResourceLocation, BakedModel> getBakedRegistry();
}
