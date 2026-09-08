package committee.nova.mods.avaritia.api.client.screen.component;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Const;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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
    private static final ResourceLocation PANEL = Const.rl("textures/gui/portable/panel.png");
    private static final ResourceLocation INSET = Const.rl("textures/gui/portable/inset.png");
    private static final ResourceLocation SLOT = Const.rl("textures/gui/portable/slot.png");

    private PortableUi() {
    }

    private static void nineSlice(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                  int border, int textureSize) {
        if (width <= 0 || height <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        graphics.blitNineSlicedSized(texture, x, y, width, height, border, textureSize, textureSize, 0, 0,
                textureSize, textureSize);
        RenderSystem.disableBlend();
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        nineSlice(graphics, PANEL, x, y, width, height, 4, 128);
    }

    public static void panel(GuiGraphics graphics, ScreenRectangle bounds) {
        panel(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height());
    }

    public static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        nineSlice(graphics, INSET, x, y, width, height, 3, 128);
    }

    public static void inset(GuiGraphics graphics, ScreenRectangle bounds) {
        inset(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height());
    }

    public static void slot(GuiGraphics graphics, int x, int y) {
        RenderSystem.enableBlend();
        graphics.blit(SLOT, x, y, 0, 0, 18, 18, 18, 18);
        RenderSystem.disableBlend();
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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == parent) {
            minecraft.pushGuiLayer(new Confirmation(parent, title, message, confirmed));
        }
    }

    public static void prompt(Screen parent, Component title, String initial, int maxLength,
                              boolean allowBlank, Consumer<String> confirmed) {
        prompt(parent, title, initial, maxLength, allowBlank, List.of(), confirmed);
    }

    public static void prompt(Screen parent, Component title, String initial, int maxLength,
                              boolean allowBlank, List<String> suggestions, Consumer<String> confirmed) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == parent) {
            minecraft.pushGuiLayer(new Prompt(parent, title, initial, maxLength, allowBlank, suggestions, confirmed));
        }
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
        private boolean finished;

        Dialog(Screen parent, Component title) {
            super(title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            finish();
        }

        protected final boolean finish() {
            Minecraft minecraft = Minecraft.getInstance();
            if (finished || minecraft.screen != this) {
                return false;
            }
            finished = true;
            minecraft.popGuiLayer();
            return true;
        }

        @Override
        public void removed() {
            finished = true;
            super.removed();
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }

    private static final class Confirmation extends Dialog {
        private final Component message;
        private final Runnable confirmed;
        private ScreenRectangle panel;
        private ScreenRectangle content;

        Confirmation(Screen parent, Component title, Component message, Runnable confirmed) {
            super(parent, title);
            this.message = message;
            this.confirmed = confirmed;
        }

        @Override
        protected void init() {
            int preferredHeight = 76 + font.split(message, Math.max(1, Math.min(300, width - 16) - 28)).size() * font.lineHeight;
            panel = PortableLayout.centered(width, height, 300, preferredHeight, 8);
            content = PortableLayout.inset(panel, 12, 28, 12, 36);
            int panelX = panel.left();
            int panelY = panel.top();
            int panelWidth = panel.width();
            int panelHeight = panel.height();
            int buttonWidth = Math.max(0, (panelWidth - 32) / 2);
            Button cancel = addRenderableWidget(UiInspector.name(button(panelX + 12, panelY + panelHeight - 30, buttonWidth, 20,
                    CommonComponents.GUI_CANCEL, button -> onClose()), "dialog.cancel"));
            setInitialFocus(cancel);
            addRenderableWidget(UiInspector.name(dangerButton(panelX + panelWidth - buttonWidth - 12, panelY + panelHeight - 30,
                    buttonWidth, 20, Component.translatable("gui.avaritia.portable.confirm"), button -> {
                        if (finish()) {
                            confirmed.run();
                        }
                    }), "dialog.confirm"));
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xA0000000);
            panel(graphics, panel);
            UiInspector.region("dialog.panel", panel, null, false);
            header(graphics, font, title, panel.left() + 4, panel.top() + 4, panel.width() - 8);
            if (content.width() > 0 && content.height() > 0) {
                graphics.enableScissor(content.left(), content.top(), content.right(), content.bottom());
                graphics.drawWordWrap(font, message, content.left() + 2, content.top() + 1,
                        Math.max(1, content.width() - 4), TEXT);
                graphics.disableScissor();
            }
            super.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private static final class Prompt extends Dialog {
        private final int maxLength;
        private final boolean allowBlank;
        private final List<String> candidates;
        private final Consumer<String> confirmed;
        private final OperationMenu suggestions = new OperationMenu("dialog.suggestions");
        private String value;
        private EditBox input;
        private Button accept;
        private ScreenRectangle panel;

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
            panel = PortableLayout.centered(width, height, 300, 108, 8);
            ScreenRectangle inputBounds = PortableLayout.inset(panel, 12, 32, 12, 56);
            ScreenRectangle footer = PortableLayout.inset(panel, 12, Math.max(0, panel.height() - 32), 12, 12);
            input = addRenderableWidget(UiInspector.name(new EditBox(font, inputBounds.left(), inputBounds.top(),
                    inputBounds.width(), inputBounds.height(), title), "dialog.input"));
            input.setMaxLength(maxLength);
            input.setValue(value);
            int buttonWidth = Math.max(0, (footer.width() - 8) / 2);
            addRenderableWidget(UiInspector.name(button(footer.left(), footer.top(), buttonWidth, footer.height(),
                    CommonComponents.GUI_CANCEL, button -> onClose()), "dialog.cancel"));
            accept = addRenderableWidget(UiInspector.name(button(footer.right() - buttonWidth, footer.top(),
                    buttonWidth, footer.height(), Component.translatable("gui.avaritia.portable.confirm"), button -> submit()), "dialog.confirm"));
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
                if (finish()) {
                    confirmed.accept(result);
                }
            }
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xA0000000);
            panel(graphics, panel);
            UiInspector.region("dialog.panel", panel, null, false);
            header(graphics, font, title, panel.left() + 4, panel.top() + 4, panel.width() - 8);
            super.render(graphics, mouseX, mouseY, partialTick);
            suggestions.render(graphics, font, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return suggestions.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            return suggestions.mouseScrolled(delta) || super.mouseScrolled(mouseX, mouseY, delta);
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
