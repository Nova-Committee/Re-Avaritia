package committee.nova.mods.avaritia.api.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 字符串输入 Screen
 */
public class StringInputScreen extends Screen {

    private final Screen previousScreen;
    private final Component titleText;
    private final Component messageText;
    private final String validator;
    private final Consumer<String> onDataReceived1;
    private final Function<String, String> onDataReceived2;
    private final Supplier<Boolean> shouldClose;
    private EditBox inputField;
    private Button submitButton;
    private final String defaultValue;
    private Component errorText;
    private ScreenRectangle panel;
    private ScreenRectangle content;
    private ScreenRectangle errorBounds;
    private ScreenRectangle footer;
    private List<FormattedCharSequence> errorLines = List.of();

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, Consumer<String> onDataReceived) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = "";
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, String defaultValue, Consumer<String> onDataReceived) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, String defaultValue, Consumer<String> onDataReceived, Supplier<Boolean> shouldClose) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = shouldClose;
    }

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, Function<String, String> onDataReceived) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = "";
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, String defaultValue, Function<String, String> onDataReceived) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, Component titleText, Component messageText, String validator, String defaultValue, Function<String, String> onDataReceived, Supplier<Boolean> shouldClose) {
        super(Component.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = shouldClose;
    }

    @Override
    protected void init() {
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get())) {
            Minecraft.getInstance().setScreen(previousScreen);
            return;
        }
        String retainedValue = inputField == null ? defaultValue : inputField.getValue();
        panel = PortableLayout.centered(width, height, 320, 180, 8);
        content = PortableLayout.inset(panel, 12, 28, 12, 40);
        errorBounds = PortableLayout.inset(content, 0, 28, 0, 0);
        footer = PortableLayout.inset(panel, 12, Math.max(0, panel.height() - 32), 12, 12);
        this.inputField = new EditBox(font, content.left(), content.top(), content.width(),
                Math.min(20, content.height()), messageText);
        this.inputField.setMaxLength(Integer.MAX_VALUE);
        if (StringUtils.isNotNullOrEmpty(validator)) {
            this.inputField.setFilter(s -> s.matches(validator));
        }
        this.inputField.setValue(retainedValue);
        this.addRenderableWidget(UiInspector.name(this.inputField, "input.value"));
        int buttonWidth = Math.max(0, (footer.width() - 8) / 2);
        this.submitButton = PortableUi.button(footer.right() - buttonWidth, footer.top(), buttonWidth, footer.height(), CommonComponents.GUI_CANCEL, button -> {
            String value = this.inputField.getValue();
            if (StringUtils.isNullOrEmpty(value)) {
                Minecraft.getInstance().setScreen(previousScreen);
            } else if (onDataReceived1 != null) {
                onDataReceived1.accept(value);
                Minecraft.getInstance().setScreen(previousScreen);
            } else if (onDataReceived2 != null) {
                String result = onDataReceived2.apply(value);
                if (StringUtils.isNotNullOrEmpty(result)) {
                    this.errorText = Component.literal(result).withStyle(net.minecraft.ChatFormatting.RED);
                    updateErrorLines();
                } else {
                    Minecraft.getInstance().setScreen(previousScreen);
                }
            }
        });
        this.addRenderableWidget(UiInspector.name(this.submitButton, "input.submit"));
        this.addRenderableWidget(UiInspector.name(PortableUi.button(footer.left(), footer.top(), buttonWidth, footer.height(),
                CommonComponents.GUI_CANCEL, button -> Minecraft.getInstance().setScreen(previousScreen)), "input.cancel"));
        inputField.setResponder(text -> submitButton.setMessage(text.isEmpty() ? CommonComponents.GUI_CANCEL : Component.literal("提交")));
        submitButton.setMessage(retainedValue.isEmpty() ? CommonComponents.GUI_CANCEL : Component.literal("提交"));
        updateErrorLines();
    }

    private void updateErrorLines() {
        errorLines = errorText == null || errorBounds.width() == 0 ? List.of()
                : font.split(errorText, errorBounds.width());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        PortableUi.panel(graphics, panel);
        PortableUi.header(graphics, font, titleText, panel.left(), panel.top(), panel.width());
        UiInspector.region("input.panel", panel, null, false);
        super.render(graphics, mouseX, mouseY, delta);
        if (errorText != null && errorBounds.width() > 0 && errorBounds.height() > 0) {
            graphics.enableScissor(errorBounds.left(), errorBounds.top(), errorBounds.right(), errorBounds.bottom());
            int rows = Math.min(errorLines.size(), errorBounds.height() / font.lineHeight);
            for (int i = 0; i < rows; i++) {
                graphics.drawString(font, errorLines.get(i), errorBounds.left(), errorBounds.top() + i * font.lineHeight, PortableUi.DANGER, false);
            }
            graphics.disableScissor();
            UiInspector.region("input.error", errorBounds, errorBounds, false);
            if (PortableLayout.contains(errorBounds, mouseX, mouseY)) {
                graphics.renderTooltip(font, errorText, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.inputField.isFocused()) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
