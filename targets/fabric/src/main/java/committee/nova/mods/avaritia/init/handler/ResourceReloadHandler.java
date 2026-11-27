package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.init.data.ResourceConditions;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

/** Orders definitions after tag preparation; script commits follow the complete reload future. */
public final class ResourceReloadHandler {
    private ResourceReloadHandler() {}

    public static List<PreparableReloadListener> listeners(List<PreparableReloadListener> vanilla,
                                                          TagManager tags, RecipeManager recipes) {
        ResourceConditions.setTagManager(tags);
        List<PreparableReloadListener> listeners = new ArrayList<>(vanilla);
        listeners.add(listeners.indexOf(recipes), SingularityReloadListener.INSTANCE);
        return listeners;
    }
}
