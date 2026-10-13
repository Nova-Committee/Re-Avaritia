package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.client.render.entity.InfinityArmorRender;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Attaches the infinity armor/wings layer to the player renderer (PlayerRenderer 已更名为 AvatarRenderer）。 */
@Mixin(AvatarRenderer.class)
public abstract class ClientPlayerRendererMixin extends LivingEntityRenderer<Avatar, AvatarRenderState, PlayerModel> {
    public ClientPlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void avaritia$armor(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        this.addLayer(new InfinityArmorRender<>(this, context.getModelSet(), slim));
    }
}
