package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the singularity definitions listener on the server reload pipeline.
 *
 * <p>26.3 起 {@code ReloadableServerResources#listeners()} 只包含函数库，{@link RecipeManager}
 * 已不再是重载监听器（配方随动态注册表加载），因此这里不再按 {@code indexOf(recipes)}
 * 排序，直接把奇点监听器追加到列表末尾；配方重建发生在之后的
 * {@code RecipeManager#finalizeRecipeLoading}，时序天然满足。</p>
 */
public final class ResourceReloadHandler {
    private ResourceReloadHandler() {}

    public static List<PreparableReloadListener> listeners(List<PreparableReloadListener> vanilla,
                                                          RecipeManager recipes) {
        List<PreparableReloadListener> listeners = new ArrayList<>(vanilla);
        listeners.add(SingularityReloadListener.INSTANCE);
        return listeners;
    }
}
