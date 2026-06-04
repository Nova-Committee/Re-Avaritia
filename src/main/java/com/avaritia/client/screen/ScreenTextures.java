package com.avaritia.client.screen;

import com.avaritia.Const;

import com.avaritia.Avaritia;
import net.minecraft.resources.Identifier;

/**
 * Client-side texture identifiers used by migrated screens.
 */
public final class ScreenTextures {
    private ScreenTextures() {
    }

    public static final Identifier NEUTRON_COMPRESSOR = texture("textures/gui/machine/neutron_compressor.png");
    public static final Identifier EXTREME_ANVIL = texture("textures/gui/machine/extreme_anvil_gui.png");
    public static final Identifier EXTREME_SMITHING = texture("textures/gui/machine/extreme_smithing_table_gui.png");
    public static final Identifier GENERIC_243 = texture("textures/gui/chest/generic_243.png");
    public static final Identifier INFINITY_CHEST = texture("textures/gui/chest/infinity_chest_gui.png");
    public static final Identifier INFINITY_CLOCK_TIME = texture("textures/gui/machine/infinity_clock_time.png");

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, path);
    }
}
