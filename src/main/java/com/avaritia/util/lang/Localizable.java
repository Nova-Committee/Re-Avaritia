package com.avaritia.util.lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Localizable string builder wrapping {@link Component#translatable}.
 */
public class Localizable {
    private final String key;
    private final ChatFormatting defaultColor;

    protected Localizable(String key) {
        this(key, null);
    }

    protected Localizable(String key, ChatFormatting defaultColor) {
        this.key = key;
        this.defaultColor = defaultColor;
    }

    public static Localizable of(String key) {
        return new Localizable(key);
    }

    public static Localizable of(String key, ChatFormatting defaultColor) {
        return new Localizable(key, defaultColor);
    }

    public Builder args(Object... args) {
        return builder().args(args);
    }

    public Builder color(ChatFormatting color) {
        return builder().color(color);
    }

    public Builder prepend(String text) {
        return builder().prepend(text);
    }

    public Builder append(String text) {
        return builder().append(text);
    }

    public MutableComponent build() {
        return builder().build();
    }

    public String string() {
        return builder().buildString();
    }

    private Builder builder() {
        return new Builder(key).color(defaultColor);
    }

    public static class Builder {
        private final String key;
        private Object[] args = new Object[0];
        private ChatFormatting color;
        private String prependText = "";
        private String appendText = "";

        public Builder(String key) {
            this.key = key;
        }

        public Builder args(Object... args) {
            this.args = args;
            return this;
        }

        public Builder color(ChatFormatting color) {
            this.color = color;
            return this;
        }

        public Builder prepend(String text) {
            this.prependText = this.prependText + text;
            return this;
        }

        public Builder append(String text) {
            this.appendText = text + this.appendText;
            return this;
        }

        public MutableComponent build() {
            MutableComponent component = Component.translatable(key, args);
            if (!prependText.isEmpty()) {
                component = Component.literal(prependText).append(component);
            }
            if (!appendText.isEmpty()) {
                component = component.append(Component.literal(appendText));
            }
            if (color != null) {
                component.withStyle(color);
            }
            return component;
        }

        public String buildString() {
            return build().getString();
        }
    }
}
