package committee.nova.mods.avaritia.api.client.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Const;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Vanilla container framing and right-click-initiated dialogs for portable storage. */
public final class PortableUi {
    public static final int TEXT = 0xFF202020;
    public static final int MUTED = 0xFF404040;
    public static final int ACCENT = 0xFF555555;
    public static final int DANGER = 0xFFAA0000;
    public static final int INSET_BG = 0xFF8B8B8B;
    private static final ResourceLocation PANEL = Const.rl("portable/panel");
    private static final ResourceLocation INSET = Const.rl("portable/inset");
    private static final ResourceLocation SLOT = Const.rl("portable/slot");

    private PortableUi() {
    }

    private static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (width > 0 && height > 0) {
            RenderSystem.enableBlend();
            graphics.blitSprite(sprite, x, y, width, height);
            RenderSystem.disableBlend();
        }
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        sprite(graphics, PANEL, x, y, width, height);
    }

    public static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        sprite(graphics, INSET, x, y, width, height);
    }

    public static void slot(GuiGraphics graphics, int x, int y) {
        sprite(graphics, SLOT, x, y, 18, 18);
    }

    public static void header(GuiGraphics graphics, Font font, Component title, int x, int y, int width) {
        text(graphics, font, title, x + 8, y + 6, width - 16, TEXT);
    }

    public static void text(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        if (maxWidth <= 0) {
            return;
        }
        Component shown = text;
        if (font.width(text) > maxWidth) {
            shown = Component.literal(font.plainSubstrByWidth(text.getString(), Math.max(0, maxWidth - font.width("…"))) + "…")
                    .setStyle(text.getStyle());
        }
        graphics.drawString(font, shown, x, y, color, false);
    }

    public static void row(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered, boolean selected) {
        if (selected || hovered) {
            graphics.fill(x, y, x + width, y + height, selected ? 0xFFD8D8D8 : 0xFFC8C8C8);
        }
        if (selected) {
            graphics.renderOutline(x, y, width, height, 0xFFFFFFFF);
        }
    }

    public static Button button(int x, int y, int width, int height, Component label, Button.OnPress action) {
        return Button.builder(label, action).bounds(x, y, width, height).build();
    }

    public static Button dangerButton(int x, int y, int width, int height, Component label, Button.OnPress action) {
        return button(x, y, width, height, label.copy().withStyle(ChatFormatting.RED), action);
    }

    public static void confirm(Screen parent, Component title, Component message, Runnable confirmed) {
        Minecraft.getInstance().setScreen(new Confirmation(parent, title, message, confirmed));
    }

    public static void prompt(Screen parent, Component title, String initial, int maxLength,
                              boolean allowBlank, Consumer<String> confirmed) {
        prompt(parent, title, initial, maxLength, allowBlank, List.of(), confirmed);
    }

    public static void prompt(Screen parent, Component title, String initial, int maxLength,
                              boolean allowBlank, List<String> suggestions, Consumer<String> confirmed) {
        Minecraft.getInstance().setScreen(new Prompt(parent, title, initial, maxLength, allowBlank, suggestions, confirmed));
    }

    @Nullable
    public static Screen root(@Nullable Screen screen) {
        while (screen instanceof Dialog dialog) {
            screen = dialog.parent;
        }
        return screen;
    }

    private abstract static class Dialog extends Screen {
        protected final Screen parent;

        Dialog(Screen parent, Component title) {
            super(title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    private static final class Confirmation extends Dialog {
        private final Component message;
        private final Runnable confirmed;
        private int panelX;
        private int panelY;
        private int panelWidth;
        private int panelHeight;

        Confirmation(Screen parent, Component title, Component message, Runnable confirmed) {
            super(parent, title);
            this.message = message;
            this.confirmed = confirmed;
        }

        @Override
        protected void init() {
            panelWidth = Math.min(300, width - 16);
            panelHeight = Math.min(height - 16, 76 + font.split(message, panelWidth - 28).size() * font.lineHeight);
            panelX = (width - panelWidth) / 2;
            panelY = (height - panelHeight) / 2;
            int buttonWidth = (panelWidth - 32) / 2;
            addRenderableWidget(button(panelX + 12, panelY + panelHeight - 30, buttonWidth, 20,
                    CommonComponents.GUI_CANCEL, button -> onClose()));
            addRenderableWidget(dangerButton(panelX + panelWidth - buttonWidth - 12, panelY + panelHeight - 30,
                    buttonWidth, 20, Component.translatable("gui.avaritia.portable.confirm"), button -> {
                        onClose();
                        confirmed.run();
                    }));
        }

        @Override
        protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
            graphics.fill(0, 0, width, height, 0xA0000000);
            panel(graphics, panelX, panelY, panelWidth, panelHeight);
            header(graphics, font, title, panelX + 4, panelY + 4, panelWidth - 8);
            graphics.enableScissor(panelX + 12, panelY + 28, panelX + panelWidth - 12, panelY + panelHeight - 36);
            graphics.drawWordWrap(font, message, panelX + 14, panelY + 29, panelWidth - 28, TEXT);
            graphics.disableScissor();
        }
    }

    private static final class Prompt extends Dialog {
        private final int maxLength;
        private final boolean allowBlank;
        private final List<String> candidates;
        private final Consumer<String> confirmed;
        private final OperationMenu suggestions = new OperationMenu();
        private String value;
        private EditBox input;
        private Button accept;
        private int panelX;
        private int panelY;
        private int panelWidth;

        Prompt(Screen parent, Component title, String initial, int maxLength, boolean allowBlank,
               List<String> candidates, Consumer<String> confirmed) {
            super(parent, title);
            this.value = initial;
            this.maxLength = maxLength;
            this.allowBlank = allowBlank;
            this.candidates = List.copyOf(candidates);
            this.confirmed = confirmed;
        }

        @Override
        protected void init() {
            suggestions.close();
            panelWidth = Math.min(300, width - 16);
            panelX = (width - panelWidth) / 2;
            panelY = (height - 108) / 2;
            input = addRenderableWidget(new EditBox(font, panelX + 12, panelY + 32, panelWidth - 24, 20, title));
            input.setMaxLength(maxLength);
            input.setValue(value);
            int buttonWidth = (panelWidth - 32) / 2;
            addRenderableWidget(button(panelX + 12, panelY + 76, buttonWidth, 20,
                    CommonComponents.GUI_CANCEL, button -> onClose()));
            accept = addRenderableWidget(button(panelX + panelWidth - buttonWidth - 12, panelY + 76,
                    buttonWidth, 20, Component.translatable("gui.avaritia.portable.confirm"), button -> submit()));
            accept.active = allowBlank || !value.isBlank();
            input.setResponder(text -> {
                value = text;
                accept.active = allowBlank || !text.isBlank();
                updateSuggestions(text);
            });
            setInitialFocus(input);
        }

        private void updateSuggestions(String text) {
            if (text.isBlank() || candidates.isEmpty()) {
                suggestions.close();
                return;
            }
            String prefix = text.toLowerCase(Locale.ROOT);
            List<OperationMenu.Entry> entries = new ArrayList<>();
            for (String name : candidates) {
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix) && !name.equalsIgnoreCase(text)) {
                    entries.add(new OperationMenu.Entry(Component.literal(name), () -> {
                        input.setValue(name);
                        suggestions.close();
                        setFocused(input);
                        input.setFocused(true);
                    }));
                    if (entries.size() == 8) {
                        break;
                    }
                }
            }
            suggestions.open(input.getX(), input.getY() + input.getHeight(), width, height, font, entries);
        }

        private void submit() {
            if (allowBlank || !value.isBlank()) {
                suggestions.close();
                String result = value.trim();
                onClose();
                confirmed.accept(result);
            }
        }

        @Override
        protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
            graphics.fill(0, 0, width, height, 0xA0000000);
            panel(graphics, panelX, panelY, panelWidth, 108);
            header(graphics, font, title, panelX + 4, panelY + 4, panelWidth - 8);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics, mouseX, mouseY, partialTick);
            suggestions.render(graphics, font, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (suggestions.isOpen() && !suggestions.isMouseOver(mouseX, mouseY)) {
                suggestions.close();
            }
            return suggestions.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return suggestions.mouseScrolled(scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean enter = keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
            if (input.isFocused()) {
                if (enter && !suggestions.hasSelection()) {
                    submit();
                    return true;
                }
                if (suggestions.keyPressed(keyCode)) {
                    return true;
                }
            }
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                suggestions.close();
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
