package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/** Shared portable framing; selection, scrolling and keyboard navigation remain vanilla-owned. */
public abstract class PortableSelectionList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    private final int rowInset;
    private final int scrollbarInset;
    private final boolean drawInset;

    protected PortableSelectionList(Minecraft minecraft, int width, int height, int y, int rowHeight,
                                    int rowInset, int scrollbarInset, boolean drawInset) {
        super(minecraft, width, height, y, rowHeight);
        this.rowInset = rowInset;
        this.scrollbarInset = scrollbarInset;
        this.drawInset = drawInset;
    }

    @Override
    public int getRowWidth() {
        return Math.max(0, getWidth() - 2 * rowInset);
    }

    @Override
    protected int getScrollbarPosition() {
        return getX() + getWidth() - scrollbarInset;
    }

    @Override
    protected boolean isValidMouseClick(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected void renderListBackground(@NotNull GuiGraphics graphics) {
        if (drawInset) {
            PortableUi.inset(graphics, getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    protected void renderListSeparators(@NotNull GuiGraphics graphics) {
        // The portable inset owns its border.
    }

    @Override
    protected void renderSelection(@NotNull GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
        // Entries draw their own hover and selection state with PortableUi.row.
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return isMouseOver(mouseX, mouseY) && super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
