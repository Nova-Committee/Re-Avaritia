package committee.nova.mods.avaritia.api.client.screen.component;

import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Right-click / suggestion menu built from {@link OperationButton} + {@link GuiUtils}. */
@OnlyIn(Dist.CLIENT)
public final class OperationMenu {
    public record Entry(Component label, Runnable action) {
        public static Entry of(String translationKey, Runnable action) {
            return new Entry(Component.translatable(translationKey), action);
        }
    }

    private static final int ITEM_HEIGHT = 16;
    private static final int PAD = 3;

    private final List<OperationButton> buttons = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private boolean open;
    private int x;
    private int y;
    private int width;
    private int height;

    public boolean isOpen() {
        return open;
    }

    public void close() {
        open = false;
        buttons.clear();
        actions.clear();
    }

    public void open(int mouseX, int mouseY, int screenWidth, int screenHeight, Font font, List<Entry> entries) {
        close();
        if (entries.isEmpty()) {
            return;
        }
        int maxLabel = 40;
        for (Entry entry : entries) {
            maxLabel = Math.max(maxLabel, font.width(entry.label()) + 12);
        }
        width = maxLabel;
        height = entries.size() * ITEM_HEIGHT + PAD * 2;
        x = Math.min(mouseX, Math.max(0, screenWidth - width));
        y = Math.min(mouseY, Math.max(0, screenHeight - height));
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int top = y + PAD + i * ITEM_HEIGHT;
            Component label = entry.label();
            OperationButton button = new OperationButton(i, context -> {
                int bg = context.button().isHovered() ? 0xEE585858 : 0x00000000;
                if (bg != 0) {
                    GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(),
                            (int) context.button().getWidth(), (int) context.button().getHeight(), bg);
                }
                GuiUtils.drawString(context.graphics(), font, label.getString(),
                        (float) (context.button().getX() + 4), (float) (context.button().getY() + 4), 0xFFFFFFFF, false);
            }).setX(x + PAD).setY(top).setWidth(width - PAD * 2).setHeight(ITEM_HEIGHT);
            buttons.add(button);
            actions.add(entry.action());
        }
        open = true;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        if (!open) {
            return;
        }
        GuiUtils.fill(graphics, x, y, width, height, 0xF0101010, 2);
        GuiUtils.fillOutLine(graphics, x, y, width, height, 1, 0xFF8A8A8A, 2);
        for (OperationButton button : buttons) {
            button.setHovered(button.isMouseOverEx(mouseX, mouseY));
            button.render(graphics, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < buttons.size(); i++) {
                OperationButton item = buttons.get(i);
                if (item.isMouseOverEx(mouseX, mouseY)) {
                    Runnable action = actions.get(i);
                    close();
                    action.run();
                    return true;
                }
            }
        }
        boolean inside = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        close();
        return inside;
    }
}
