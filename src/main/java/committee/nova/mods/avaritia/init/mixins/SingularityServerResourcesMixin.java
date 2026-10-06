package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.ResourceReloadHandler;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ReloadableServerResources.class, priority = 900)
public abstract class SingularityServerResourcesMixin {
    @Shadow @Final private TagManager tagManager;
    @Shadow @Final private RecipeManager recipes;

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void avaritia$reloadListeners(CallbackInfoReturnable<List<PreparableReloadListener>> callback) {
        callback.setReturnValue(ResourceReloadHandler.listeners(callback.getReturnValue(), this.tagManager, this.recipes));
    }
}
