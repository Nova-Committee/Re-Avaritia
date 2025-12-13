package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/5/19 00:22
 * @Description:
 */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
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

    @Shadow
    @Final
    private ICondition.IContext context;

    @Shadow
    @Final
    private LootDataManager lootData;

    public ReloadableServerResourcesMixin() {
    }

    @Inject(
            at = {@At("RETURN")},
            method = {"<init>"}
    )
    public void avaritia$constructor(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, CallbackInfo ci) {
        SingularityReloadListener.INSTANCE = new SingularityReloadListener(this.context, this.recipes);
    }

    @Inject(
            at = {@At(value = "RETURN")},
            method = {"listeners"},
            cancellable = true)
    public void avaritia$listeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        cir.setReturnValue(List.of(this.tagManager, this.lootData, SingularityReloadListener.INSTANCE, this.recipes, this.functionLibrary, this.advancements));
    }
}
