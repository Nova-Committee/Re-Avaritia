package committee.nova.mods.avaritia.api.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
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
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get()))
            Minecraft.getInstance().setScreen(previousScreen);
        // 创建文本输入框
        this.inputField = GuiUtils.newTextFieldWidget(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20
                , GuiUtils.textToComponent(this.messageText));
        this.inputField.setMaxLength(Integer.MAX_VALUE);
        if (StringUtils.isNotNullOrEmpty(validator)) {
            this.inputField.setFilter(s -> s.matches(validator));
        }
        this.inputField.setValue(defaultValue);
        this.addRenderableWidget(this.inputField);
        // 创建提交按钮
        this.submitButton = GuiUtils.newButton(this.width / 2 + 5, this.height / 2 + 10, 95, 20, Component.literal(("取消")), button -> {
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
                    } else {
                        // 关闭当前屏幕并返回到调用者的 Screen
                        Minecraft.getInstance().setScreen(previousScreen);
                    }
                }
            }
        });
        this.addRenderableWidget(this.submitButton);
        // 创建取消按钮
        this.addRenderableWidget(GuiUtils.newButton(this.width / 2 - 100, this.height / 2 + 10, 95, 20, Component.literal(("取消")), button -> {
            // 关闭当前屏幕并返回到调用者的 Screen
            Minecraft.getInstance().setScreen(previousScreen);
        }));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        // 绘制背景
        super.render(graphics, mouseX, mouseY, delta);
        // 绘制标题
        GuiUtils.drawString(titleText.setGraphics(graphics), this.width / 2.0f - 100, this.height / 2.0f - 33);
        // 绘制错误提示
        if (this.errorText != null) {
            GuiUtils.drawLimitedText(errorText.setGraphics(graphics), this.width / 2.0f - 100, this.height / 2.0f + 2, 200, GuiUtils.EllipsisPosition.MIDDLE);
        }
        if (StringUtils.isNotNullOrEmpty(this.inputField.getValue())) {
            this.submitButton.setMessage(Component.literal(("提交")));
        } else {
            this.submitButton.setMessage(Component.literal(("取消")));
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
