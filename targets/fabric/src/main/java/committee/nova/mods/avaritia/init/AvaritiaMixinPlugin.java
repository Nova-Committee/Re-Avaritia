package committee.nova.mods.avaritia.init;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class AvaritiaMixinPlugin implements IMixinConfigPlugin {
    private static final String CREATIVE_INVENTORY_MIXIN =
            "committee.nova.mods.avaritia.init.mixins.ClientCreativeInventoryMixin";
    private static final boolean HAS_FABRIC_ITEM_GROUP_API =
            FabricLoader.getInstance().isModLoaded("fabric-item-group-api-v1");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Fabric Item Group API owns pagination and tab positions when it is installed.
        return !HAS_FABRIC_ITEM_GROUP_API || !CREATIVE_INVENTORY_MIXIN.equals(mixinClassName);
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
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
