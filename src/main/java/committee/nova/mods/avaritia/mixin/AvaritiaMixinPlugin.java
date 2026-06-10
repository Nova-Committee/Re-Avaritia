package committee.nova.mods.avaritia.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class AvaritiaMixinPlugin implements IMixinConfigPlugin {
    private static final String JEI_MOD_ID = "jei";
    private static final String JEI_MOD_ID_HELPER_MIXIN = "committee.nova.mods.avaritia.mixin.compat.JeiModIdHelperMixin";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (JEI_MOD_ID_HELPER_MIXIN.equals(mixinClassName)) {
            return isLoadedDuringMixinSelection(JEI_MOD_ID);
        }
        return true;
    }

    private static boolean isLoadedDuringMixinSelection(String modId) {
        var loader = FMLLoader.getCurrentOrNull();
        if (loader == null) {
            return false;
        }

        try {
            return loader.getLoadingModList().getModFileById(modId) != null;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
