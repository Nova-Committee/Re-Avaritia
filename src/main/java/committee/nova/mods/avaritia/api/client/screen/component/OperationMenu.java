package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Viewport-clamped modal popup shared by portable-item actions and name suggestions. */
@OnlyIn(Dist.CLIENT)
public final class OperationMenu {
    public record Entry(Component label, Runnable action) {
        public static Entry of(String translationKey, Runnable action) {
            return new Entry(Component.translatable(translationKey), action);
        }

        public static Entry danger(String translationKey, Runnable action) {
            return new Entry(Component.translatable(translationKey).withStyle(ChatFormatting.DARK_RED), action);
        }
    }

    private static final int ITEM_HEIGHT = 18;
    private static final int PAD = 4;
    private List<Entry> entries = List.of();
    private int x;
    private int y;
    private int width;
    private int height;
    private int visibleRows;
    private int firstRow;
    private int selected = -1;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;

    public boolean isOpen() {
        return !entries.isEmpty();
    }

    public boolean hasSelection() {
        return selected >= 0;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return isOpen() && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public void close() {
        entries = List.of();
        selected = -1;
        firstRow = 0;
    }

    public void open(int mouseX, int mouseY, int screenWidth, int screenHeight, Font font, List<Entry> choices) {
        close();
        if (choices.isEmpty()) {
            return;
        }
        entries = List.copyOf(choices);
        int labelWidth = 72;
        for (Entry entry : entries) {
            labelWidth = Math.max(labelWidth, font.width(entry.label()) + PAD * 2 + 8);
        }
        width = Math.min(labelWidth, Math.max(24, screenWidth - PAD * 2));
        visibleRows = Math.min(entries.size(), Math.max(1, (screenHeight - PAD * 4) / ITEM_HEIGHT));
        height = visibleRows * ITEM_HEIGHT + PAD * 2;
        x = Mth.clamp(mouseX, PAD, Math.max(PAD, screenWidth - width - PAD));
        y = Mth.clamp(mouseY, PAD, Math.max(PAD, screenHeight - height - PAD));
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        if (!isOpen()) {
            return;
        }
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            selected = rowAt(mouseX, mouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 600);
        PortableUi.panel(graphics, x, y, width, height);
        for (int row = 0; row < visibleRows; row++) {
            int index = firstRow + row;
            int top = y + PAD + row * ITEM_HEIGHT;
            PortableUi.row(graphics, x + PAD, top, width - PAD * 2, ITEM_HEIGHT, index == selected, false);
            PortableUi.text(graphics, font, entries.get(index).label(), x + PAD + 4, top + 5,
                    width - PAD * 2 - 8, PortableUi.TEXT);
        }
        if (firstRow > 0) {
            graphics.fill(x + PAD, y + 1, x + width - PAD, y + 2, PortableUi.ACCENT);
        }
        if (firstRow + visibleRows < entries.size()) {
            graphics.fill(x + PAD, y + height - 2, x + width - PAD, y + height - 1, PortableUi.ACCENT);
        }
        int hovered = rowAt(mouseX, mouseY);
        if (hovered >= 0 && font.width(entries.get(hovered).label()) > width - PAD * 2 - 8) {
            graphics.renderTooltip(font, entries.get(hovered).label(), mouseX, mouseY);
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < x + PAD || mouseX >= x + width - PAD || mouseY < y + PAD || mouseY >= y + height - PAD) {
            return -1;
        }
        return firstRow + (int) ((mouseY - y - PAD) / ITEM_HEIGHT);
    }

    private void activate(int index) {
        Runnable action = entries.get(index).action();
        close();
        action.run();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOpen()) {
            return false;
        }
        int row = rowAt(mouseX, mouseY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && row >= 0) {
            activate(row);
        } else {
            close();
        }
        // Dismissal belongs to the popup, never the slot or button underneath it.
        return true;
    }

    public boolean mouseScrolled(double scrollY) {
        if (!isOpen()) {
            return false;
        }
        firstRow = Mth.clamp(firstRow - (int) Math.signum(scrollY), 0, entries.size() - visibleRows);
        selected = -1;
        return true;
    }

    public boolean keyPressed(int keyCode) {
        if (!isOpen()) {
            return false;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> close();
            case GLFW.GLFW_KEY_UP -> selected = selected < 0 ? entries.size() - 1 : Math.floorMod(selected - 1, entries.size());
            case GLFW.GLFW_KEY_DOWN -> selected = (selected + 1) % entries.size();
            case GLFW.GLFW_KEY_HOME -> selected = 0;
            case GLFW.GLFW_KEY_END -> selected = entries.size() - 1;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (selected >= 0) {
                    activate(selected);
                }
            }
            default -> {
                return false;
            }
        }
        if (selected >= 0) {
            firstRow = Mth.clamp(firstRow, Math.max(0, selected - visibleRows + 1), selected);
        }
        return true;
    }
}
