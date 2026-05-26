package com.avaritia.api.utils.text;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * @project: Avaritia
 * @author: cnlimiter
 * @createTime: 2025/5/24 19:52
 * @apiNote: 
 */
public interface ILangEntry extends IHasTranslationKey {

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place
     */
    default MutableComponent translate(Object... args) {
        return TextComponentUtils.smartTranslate(getTranslationKey(), args);
    }

    /**
     * Translates this {@link ILangEntry} using a "smart" replacement scheme to allow for automatic replacements, and coloring to take place.
     */
    default MutableComponent translate() {
        return TextComponentUtils.translate(getTranslationKey());
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} of the given {@link ChatFormatting} to the {@link
     * net.minecraft.network.chat.Component}
     */
    default MutableComponent translateColored(ChatFormatting color, Object... args) {
        return TextComponentUtils.build(color, translate(args));
    }

    /**
     * Translates this {@link ILangEntry} and applies the {@link net.minecraft.network.chat.TextColor} to the {@link Component}.
     *
     * @since 10.4.0
     */
    default MutableComponent translateColored(ChatFormatting color) {
        return TextComponentUtils.build(color, translate());
    }
}
