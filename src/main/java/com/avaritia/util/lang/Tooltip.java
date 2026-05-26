package com.avaritia.util.lang;

import net.minecraft.ChatFormatting;

/**
 * Tooltip template with default gray color, extending {@link Localizable}.
 */
public class Tooltip extends Localizable {
    public Tooltip(String key) {
        super(key, ChatFormatting.GRAY);
    }

    public Tooltip(String key, ChatFormatting defaultColor) {
        super(key, defaultColor);
    }
}
