package committee.nova.mods.avaritia.client.render.entity;

import committee.nova.mods.avaritia.Static;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import org.jetbrains.annotations.NotNull;

/**
 * InfinityGolemRenderer
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 11:35
 */
public class InfinityGolemRenderer extends IronGolemRenderer {
    private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation(Static.MOD_ID,"textures/entity/iron_golem.png");
    public InfinityGolemRenderer(EntityRendererProvider.Context p_174188_) {
        super(p_174188_);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull IronGolem pEntity) {
        return GOLEM_LOCATION;
    }
}
