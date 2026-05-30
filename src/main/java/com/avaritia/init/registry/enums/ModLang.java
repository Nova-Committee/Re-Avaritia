package com.avaritia.init.registry.enums;

import com.avaritia.Avaritia;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

/**
 * 物品模式相关语言键。
 */
public enum ModLang {
    CURRENT_MODE("mode", "current"),
    DEFAULT_MODE("mode", "default"),
    ADVANCE_MODE("mode", "advance"),
    RANGE_MODE("mode", "range"),
    MODE_SWITCH("mode", "switch");

    private final String key;

    ModLang(String type, String path) {
        this(Util.makeDescriptionId(type, Identifier.fromNamespaceAndPath(Const.MOD_ID, path)));
    }

    ModLang(String key) {
        this.key = key;
    }

    public String getTranslationKey() {
        return key;
    }

    public MutableComponent translate(Object... args) {
        return Component.translatable(getTranslationKey(), args);
    }

    public MutableComponent translate() {
        return Component.translatable(getTranslationKey());
    }
}
