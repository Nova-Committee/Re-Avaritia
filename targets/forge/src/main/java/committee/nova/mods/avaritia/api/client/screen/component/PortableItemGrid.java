package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Native widget for a finite viewport of item choices, without container or packet ownership. */
public final class PortableItemGrid extends AbstractWidget {
    private static final int STEP = 19;
    private final Minecraft minecraft;
    private final int columns;
    private final int rows;
    private final String inspectorPrefix;
    private final Consumer<ItemStack> onSelect;
    private List<ItemStack> items = List.of();
    private ItemStack selected = ItemStack.EMPTY;
    private Function<ItemStack, List<Component>> tooltipProvider;
    private int scrollOffset;
    private int pressedIndex = -1;
    private boolean draggingScrollbar;
    private ScreenRectangle content;
    private ScreenRectangle scrollbar;
    private ScreenRectangle handle;

    public PortableItemGrid(Minecraft minecraft, int x, int y, int columns, int rows,
                            String inspectorPrefix, Consumer<ItemStack> onSelect) {
        super(x, y, columns * STEP + 12, rows * STEP + 3, Component.translatable("gui.avaritia.search"));
        this.minecraft = minecraft;
        this.columns = columns;
        this.rows = rows;
        this.inspectorPrefix = inspectorPrefix;
        this.onSelect = onSelect;
        this.tooltipProvider = stack -> minecraft == null ? List.of() : Screen.getTooltipFromItem(minecraft, stack);
        updateGeometry();
    }

    /** Screen owners call this after every release, including releases outside this widget. */
    public void cancelInteraction() {
        pressedIndex = -1;
        draggingScrollbar = false;
    }

    public void setItems(List<ItemStack> items) {
        this.items = List.copyOf(items);
        cancelInteraction();
        setScrollOffset(scrollOffset);
    }

    public void setSelected(ItemStack selected) {
        this.selected = selected.copy();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int offset) {
        scrollOffset = Mth.clamp(offset, 0, maxScroll());
        pressedIndex = -1;
        updateGeometry();
    }

    public void setTooltipProvider(Function<ItemStack, List<Component>> provider) {
        tooltipProvider = provider;
    }

    private int maxScroll() {
        return Math.max(0, (items.size() + columns - 1) / columns - rows);
    }

    private void updateGeometry() {
        content = new ScreenRectangle(getX(), getY(), columns * STEP + 3, rows * STEP + 3);
        scrollbar = new ScreenRectangle(content.right() + 2, getY() + 1, 5, Math.max(0, getHeight() - 2));
        int totalRows = Math.max(rows, (items.size() + columns - 1) / columns);
        int thumb = Math.min(scrollbar.height(), Math.max(8, scrollbar.height() * rows / totalRows));
        int offset = maxScroll() == 0 ? 0 : scrollOffset * (scrollbar.height() - thumb) / maxScroll();
        handle = new ScreenRectangle(scrollbar.left(), scrollbar.top() + offset, scrollbar.width(), thumb);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        if (columns > 0) {
            updateGeometry();
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (columns > 0) {
            updateGeometry();
        }
    }

    private int indexAt(double mouseX, double mouseY) {
        if (!visible || !active || !PortableLayout.contains(content, mouseX, mouseY)) {
            return -1;
        }
        double x = mouseX - content.left() - 2;
        double y = mouseY - content.top() - 2;
        if (x < 0 || y < 0) {
            return -1;
        }
        int column = (int) (x / STEP);
        int row = (int) (y / STEP);
        if (column >= columns || row >= rows || x % STEP >= 18 || y % STEP >= 18) {
            return -1;
        }
        int index = (scrollOffset + row) * columns + column;
        return index < items.size() && !items.get(index).isEmpty() ? index : -1;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        draggingScrollbar = maxScroll() > 0 && PortableLayout.contains(scrollbar, mouseX, mouseY);
        if (draggingScrollbar) {
            scrollFromMouse(mouseY);
        } else {
            pressedIndex = indexAt(mouseX, mouseY);
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (draggingScrollbar) {
            scrollFromMouse(mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        int index = indexAt(mouseX, mouseY);
        int pressed = pressedIndex;
        pressedIndex = -1;
        boolean dragged = draggingScrollbar;
        draggingScrollbar = false;
        if (!dragged && pressed >= 0 && index == pressed) {
            ItemStack choice = items.get(index).copyWithCount(1);
            setSelected(choice);
            onSelect.accept(choice);
        }
    }

    private void scrollFromMouse(double mouseY) {
        double ratio = (mouseY - scrollbar.top() - handle.height() / 2.0)
                / Math.max(1, scrollbar.height() - handle.height());
        setScrollOffset((int) Math.round(ratio * maxScroll()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible || !active || !isMouseOver(mouseX, mouseY) || delta == 0) {
            return false;
        }
        setScrollOffset(scrollOffset - (int) Math.signum(delta));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused() || !active || !visible) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            setScrollOffset(scrollOffset + (keyCode == GLFW.GLFW_KEY_PAGE_DOWN ? rows : -rows));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PortableUi.inset(graphics, content);
        int hovered = indexAt(mouseX, mouseY);
        graphics.enableScissor(content.left(), content.top(), content.right(), content.bottom());
        for (int cell = 0; cell < columns * rows; cell++) {
            int index = scrollOffset * columns + cell;
            int x = content.left() + 2 + cell % columns * STEP;
            int y = content.top() + 2 + cell / columns * STEP;
            PortableUi.slot(graphics, x, y);
            if (index >= items.size() || items.get(index).isEmpty()) {
                continue;
            }
            ItemStack stack = items.get(index);
            PortableUi.row(graphics, x, y, 18, 18, hovered == index,
                    !selected.isEmpty() && ItemStack.isSameItemSameTags(stack, selected));
            graphics.renderItem(stack, x + 1, y + 1);
            if (UiInspector.enabled()) {
                UiInspector.row(inspectorPrefix + ".grid", null, index, x, y, 18, 18, content, active);
            }
        }
        graphics.disableScissor();
        graphics.fill(scrollbar.left(), scrollbar.top(), scrollbar.right(), scrollbar.bottom(), PortableUi.MUTED);
        graphics.fill(handle.left(), handle.top(), handle.right(), handle.bottom(), 0xFFAAAAAA);
        if (UiInspector.enabled()) {
            UiInspector.region(inspectorPrefix + ".scrollbar", scrollbar, null, maxScroll() > 0);
        }
    }

    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = indexAt(mouseX, mouseY);
        if (index >= 0 && !draggingScrollbar && minecraft != null) {
            ItemStack stack = items.get(index);
            graphics.renderTooltip(minecraft.font, tooltipProvider.apply(stack), stack.getTooltipImage(), stack, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
