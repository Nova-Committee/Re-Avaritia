package committee.nova.mods.avaritia.client.model.loader.base;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * @author: cnlimiter
 */
public record HaloSetting(IntArrayList layerColors, String texture,
                          int color, int size, boolean pulse
) {
}
