package committee.nova.mods.avaritia.api.client.screen.component;

import committee.nova.mods.avaritia.api.client.util.key.GLFWKey;
import committee.nova.mods.avaritia.api.client.util.key.GLFWKeyHelper;
import committee.nova.mods.avaritia.api.util.StringUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
@OnlyIn(Dist.CLIENT)
public class KeyEventManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Set<Integer> pressedKeys = new LinkedHashSet<>();
    @Deprecated
    private int modifiers = GLFWKey.GLFW_KEY_UNKNOWN;
    private final Set<Integer> pressedMouses = new LinkedHashSet<>();
    @Getter
    private double mousedScroll, mouseDownX, mouseDownY, mouseX, mouseY;

    @Deprecated
    public void keyPressed(int keyCode, int modifiers) {
        this.pressedKeys.add(keyCode);
        this.modifiers = modifiers;
    }

    @Deprecated
    public void keyReleased(int keyCode, int modifiers) {
        this.pressedKeys.remove(keyCode);
        this.modifiers = GLFWKey.GLFW_KEY_UNKNOWN;
    }

    public void keyPressed(int keyCode) {
        this.pressedKeys.add(keyCode);
    }

    public void keyReleased(int keyCode) {
        this.pressedKeys.remove(keyCode);
    }

    public void mouseClicked(int mouseButton, double mouseX, double mouseY) {
        this.pressedMouses.add(mouseButton);
        this.mouseDownX = mouseX;
        this.mouseDownY = mouseY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void mouseReleased(int mouseButton, double mouseX, double mouseY) {
        this.pressedMouses.remove(mouseButton);
        this.mouseDownX = -1;
        this.mouseDownY = -1;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void mouseMoved(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void mouseScrolled(double mousedScroll, double mouseX, double mouseY) {
        this.mousedScroll = mousedScroll;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    private boolean active = false;

    public void refresh(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        if (!Minecraft.getInstance().isWindowActive()) {
            if (this.active) LOGGER.debug("Window is not active, clear all pressed keys and mouses");
            this.active = false;
            this.pressedKeys.clear();
            this.pressedMouses.clear();
            this.mouseDownX = -1;
            this.mouseDownY = -1;
            this.mousedScroll = 0;
            this.modifiers = GLFWKey.GLFW_KEY_UNKNOWN;
        } else {
            this.active = true;
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return this.pressedKeys.contains(keyCode);
    }

    public boolean isCtrlPressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_LEFT_CONTROL) || this.isKeyPressed(GLFWKey.GLFW_KEY_RIGHT_CONTROL);
    }

    public boolean isShiftPressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_LEFT_SHIFT) || this.isKeyPressed(GLFWKey.GLFW_KEY_RIGHT_SHIFT);
    }

    public boolean isAltPressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_LEFT_ALT) || this.isKeyPressed(GLFWKey.GLFW_KEY_RIGHT_ALT);
    }

    public boolean isSuperPressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_LEFT_SUPER) || this.isKeyPressed(GLFWKey.GLFW_KEY_RIGHT_SUPER);
    }

    public boolean onlyCtrlPressed() {
        return this.pressedKeys.size() == 1 && this.isCtrlPressed();
    }

    public boolean onlyShiftPressed() {
        return this.pressedKeys.size() == 1 && this.isShiftPressed();
    }

    public boolean onlyAltPressed() {
        return this.pressedKeys.size() == 1 && this.isAltPressed();
    }

    public boolean onlySuperPressed() {
        return this.pressedKeys.size() == 1 && this.isSuperPressed();
    }

    public boolean onlyCtrlShiftPressed() {
        return this.pressedKeys.size() == 2 && this.isCtrlPressed() && this.isShiftPressed();
    }

    public boolean onlyCtrlAltPressed() {
        return this.pressedKeys.size() == 2 && this.isCtrlPressed() && this.isAltPressed();
    }

    public boolean onlyCtrlSuperPressed() {
        return this.pressedKeys.size() == 2 && this.isCtrlPressed() && this.isSuperPressed();
    }

    public boolean onlyShiftAltPressed() {
        return this.pressedKeys.size() == 2 && this.isShiftPressed() && this.isAltPressed();
    }

    public boolean onlyShiftSuperPressed() {
        return this.pressedKeys.size() == 2 && this.isShiftPressed() && this.isSuperPressed();
    }

    public boolean onlyAltSuperPressed() {
        return this.pressedKeys.size() == 2 && this.isAltPressed() && this.isSuperPressed();
    }

    public boolean onlyCtrlShiftAltPressed() {
        return this.pressedKeys.size() == 3 && this.isCtrlPressed() && this.isShiftPressed() && this.isAltPressed();
    }

    public boolean ieEscapePressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_ESCAPE);
    }

    public boolean ieEnterPressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_ENTER) || this.isKeyPressed(GLFWKey.GLFW_KEY_KP_ENTER);
    }

    public boolean ieBackspacePressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_BACKSPACE);
    }

    public boolean ieDeletePressed() {
        return this.isKeyPressed(GLFWKey.GLFW_KEY_DELETE);
    }

    public boolean onlyEscapePressed() {
        return this.pressedKeys.size() == 1 && this.ieEscapePressed();
    }

    public boolean onlyEnterPressed() {
        return this.pressedKeys.size() == 1 && this.ieEnterPressed();
    }

    public boolean onlyBackspacePressed() {
        return this.pressedKeys.size() == 1 && this.ieBackspacePressed();
    }

    public boolean onlyDeletePressed() {
        return this.pressedKeys.size() == 1 && this.ieDeletePressed();
    }

    public boolean isMousePressed(int mouseButton) {
        return this.pressedMouses.contains(mouseButton);
    }

    public boolean isMouseLeftPressed() {
        return this.isMousePressed(GLFWKey.GLFW_MOUSE_BUTTON_LEFT);
    }

    public boolean isMouseRightPressed() {
        return this.isMousePressed(GLFWKey.GLFW_MOUSE_BUTTON_RIGHT);
    }

    public boolean isMouseMiddlePressed() {
        return this.isMousePressed(GLFWKey.GLFW_MOUSE_BUTTON_MIDDLE);
    }

    public boolean onlyMouseLeftPressed() {
        return this.pressedMouses.size() == 1 && this.isMouseLeftPressed();
    }

    public boolean onlyMouseRightPressed() {
        return this.pressedMouses.size() == 1 && this.isMouseRightPressed();
    }

    public boolean onlyMouseMiddlePressed() {
        return this.pressedMouses.size() == 1 && this.isMouseMiddlePressed();
    }

    public boolean onlyMouseLeftRightPressed() {
        return this.pressedMouses.size() == 2 && this.isMouseLeftPressed() && this.isMouseRightPressed();
    }

    public boolean isMouseDragged() {
        return this.mouseDownX != GLFWKey.GLFW_KEY_UNKNOWN && this.mouseDownY != GLFWKey.GLFW_KEY_UNKNOWN;
    }

    public boolean isMouseDragged(int mouseButton) {
        return this.isMouseDragged() && this.pressedMouses.contains(mouseButton);
    }

    public boolean isMouseMoved() {
        return Math.abs(this.mouseX - this.mouseDownX) > 1 || Math.abs(this.mouseY - this.mouseDownY) > 1;
    }

    /**
     * 按键是否按下
     */
    public boolean isKeyPressed(String keyNames) {
        if (StringUtils.isNullOrEmptyEx(keyNames)) return false;
        return GLFWKeyHelper.matchKey(keyNames, this.pressedKeys.stream().mapToInt(i -> i).toArray());
    }

    /**
     * 按键是否按顺序按下
     */
    public boolean isKeyPressedInOrder(String keyNames) {
        if (StringUtils.isNullOrEmptyEx(keyNames)) return false;
        return GLFWKeyHelper.matchKeyInOrder(keyNames, this.pressedKeys.stream().mapToInt(i -> i).toArray());
    }

    /**
     * 鼠标是否按下
     */
    public boolean isMousePressed(String mouseNames) {
        if (StringUtils.isNullOrEmptyEx(mouseNames)) return false;
        return GLFWKeyHelper.matchMouse(mouseNames, this.pressedMouses.stream().mapToInt(i -> i).toArray());
    }

    /**
     * 鼠标是否按顺序按下
     */
    public boolean isMousePressedInOrder(String mouseNames) {
        if (StringUtils.isNullOrEmptyEx(mouseNames)) return false;
        return GLFWKeyHelper.matchMouseInOrder(mouseNames, this.pressedMouses.stream().mapToInt(i -> i).toArray());
    }

    public boolean isKeyAndMousePressed(String names) {
        if (StringUtils.isNullOrEmptyEx(names)) return false;
        StringBuilder keyNames = new StringBuilder();
        StringBuilder mouseNames = new StringBuilder();
        String[] parts = names.split("\\+");
        for (String part : parts) {
            if (part.startsWith("Mouse")) {
                mouseNames.append(part).append("+");
            } else {
                keyNames.append(part).append("+");
            }
        }
        return (StringUtils.isNullOrEmptyEx(keyNames.toString()) || this.isKeyPressed(keyNames.toString()))
                && (StringUtils.isNullOrEmptyEx(mouseNames.toString()) || this.isMousePressed(mouseNames.toString()));
    }

}
