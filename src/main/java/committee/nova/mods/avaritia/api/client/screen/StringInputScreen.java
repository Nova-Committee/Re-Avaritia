package committee.nova.mods.avaritia.api.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
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

    /**
     * 父级 Screen
     */
    private final Screen previousScreen;
    /**
     * 标题
     */
    private final Text titleText;
    /**
     * 提示
     */
    private final Text messageText;
    /**
     * 输入数据校验
     */
    private final String validator;
    /**
     * 输入数据回调1
     */
    private final Consumer<String> onDataReceived1;
    /**
     * 输入数据回调2
     */
    private final Function<String, String> onDataReceived2;
    /**
     * 是否要显示该界面, 若为false则直接关闭当前界面并返回到调用者的 Screen
     */
    private final Supplier<Boolean> shouldClose;
    /**
     * 输入框
     */
    private EditBox inputField;
    /**
     * 确认按钮
     */
    private Button submitButton;
    /**
     * 输入框默认值
     */
    private final String defaultValue;
    /**
     * 输入错误提示
     */
    private Text errorText;
    private ScreenRectangle panel;
    private ScreenRectangle content;
    private ScreenRectangle errorBounds;
    private ScreenRectangle footer;
    private List<FormattedCharSequence> errorLines = List.of();


    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, Consumer<String> onDataReceived) {
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

    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, String defaultValue, Consumer<String> onDataReceived) {
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

    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, String defaultValue, Consumer<String> onDataReceived, Supplier<Boolean> shouldClose) {
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

    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, Function<String, String> onDataReceived) {
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

    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, String defaultValue, Function<String, String> onDataReceived) {
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

    public StringInputScreen(Screen callbackScreen, Text titleText, Text messageText, String validator, String defaultValue, Function<String, String> onDataReceived, Supplier<Boolean> shouldClose) {
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
                Math.min(20, content.height()), GuiUtils.textToComponent(messageText));
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
                // 关闭当前屏幕并返回到调用者的 Screen
                Minecraft.getInstance().setScreen(previousScreen);
            } else {
                // 获取输入的数据，并执行回调
                if (onDataReceived1 != null) {
                    onDataReceived1.accept(value);
                    // 关闭当前屏幕并返回到调用者的 Screen
                    Minecraft.getInstance().setScreen(previousScreen);
                } else if (onDataReceived2 != null) {
                    String result = onDataReceived2.apply(value);
                    if (StringUtils.isNotNullOrEmpty(result)) {
                        this.errorText = Text.literal(result).setColor(0xFFFF0000);
                        updateErrorLines();
                    } else {
                        // 关闭当前屏幕并返回到调用者的 Screen
                        Minecraft.getInstance().setScreen(previousScreen);
                    }
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
                : font.split(GuiUtils.textToComponent(errorText), errorBounds.width());
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        PortableUi.panel(graphics, panel);
        PortableUi.header(graphics, font, GuiUtils.textToComponent(titleText), panel.left(), panel.top(), panel.width());
        UiInspector.region("input.panel", panel, null, false);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
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
                graphics.renderTooltip(font, GuiUtils.textToComponent(errorText), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /**
     * 重写键盘事件
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.inputField.isFocused()) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
