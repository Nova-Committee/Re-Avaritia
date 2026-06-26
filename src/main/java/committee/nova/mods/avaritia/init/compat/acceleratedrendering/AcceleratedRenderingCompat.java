package committee.nova.mods.avaritia.init.compat.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;

public final class AcceleratedRenderingCompat {
    private AcceleratedRenderingCompat() {
    }

    public static boolean beginHaloRender() {
        if (       !CoreFeature.isLoaded()
                || !AcceleratedItemRenderingFeature.isEnabled()
                || !AcceleratedItemRenderingFeature.shouldUseAcceleratedPipeline()) {
            return false;
        }

        AcceleratedItemRenderingFeature.useVanillaPipeline();
        return true;
    }

    public static void endHaloRender() {
        AcceleratedItemRenderingFeature.resetPipeline();
    }
}