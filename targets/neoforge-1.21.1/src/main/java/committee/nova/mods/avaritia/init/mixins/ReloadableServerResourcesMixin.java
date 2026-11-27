package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/19 00:22
 * @Description:
 */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin{

    @Mutable
    @Shadow
    @Final
    private RecipeManager recipes;

    @Shadow
    @Final
    private TagManager tagManager;

    @Shadow
    @Final
    private ServerFunctionLibrary functionLibrary;

    @Shadow
    @Final
    private ServerAdvancementManager advancements;

    public ReloadableServerResourcesMixin() {
    }

    @Inject(
            at = {@At(value = "RETURN")},
            method = {"listeners"},
            cancellable = true)
    public void avaritia$listeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        cir.setReturnValue(List.of(this.tagManager, SingularityReloadListener.INSTANCE, this.recipes, this.functionLibrary, this.advancements));
    }

}
