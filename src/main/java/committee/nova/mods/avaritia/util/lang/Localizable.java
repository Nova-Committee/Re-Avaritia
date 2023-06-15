package committee.nova.mods.avaritia.util.lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:44
 * Version: 1.0
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

    public String getKey() {
        return this.key;
    }

    public ChatFormatting getDefaultColor() {
        return this.defaultColor;
    }

    public Localizable.LocalizableBuilder args(Object... args) {
        return this.builder().args(args);
    }

    public Localizable.LocalizableBuilder color(ChatFormatting color) {
        return this.builder().color(color);
    }

    public Localizable.LocalizableBuilder prepend(String text) {
        return this.builder().prepend(text);
    }

    public MutableComponent build() {
        return this.builder().build();
    }

    public String buildString() {
        return this.builder().buildString();
    }

    private Localizable.LocalizableBuilder builder() {
        return (new Localizable.LocalizableBuilder(this.key)).color(this.defaultColor);
    }

    public static class LocalizableBuilder {
        private final String key;
        private Object[] args = new Object[0];
        private ChatFormatting color;
        private String prependText = "";

        public LocalizableBuilder(String key) {
            this.key = key;
        }

        public Localizable.LocalizableBuilder args(Object... args) {
            this.args = args;
            return this;
        }

        public Localizable.LocalizableBuilder color(ChatFormatting color) {
            this.color = color;
            return this;
        }

        public Localizable.LocalizableBuilder prepend(String text) {
            this.prependText = this.prependText + text;
            return this;
        }

        public MutableComponent build() {
            MutableComponent component = Component.translatable(this.key, this.args);
            if (!this.prependText.equals("")) {
                component = (Component.literal(this.prependText)).append(component);
            }

            if (this.color != null) {
                component.withStyle(this.color);
            }

            return component;
        }

        public String buildString() {
            return this.build().getString();
        }
    }
}
