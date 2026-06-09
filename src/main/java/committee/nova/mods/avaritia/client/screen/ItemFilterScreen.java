package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.client.AvaritiaClient;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPacket;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ItemFilterScreen extends Screen {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int SLOT_SIZE = 18;
    private static final int GAP = 3;
    private static final int PANEL_WIDTH = 218;
    private static final int PANEL_HEIGHT = 166;

    private final List<ItemStack> filterItems = new ArrayList<>();
    private int panelX;
    private int panelY;
    private int gridX;
    private int gridY;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private int scrollbarX;
    private int scrollbarY;
    private int scrollbarHeight;
    private int scrollbarHandleY;
    private int scrollbarHandleHeight;
    private boolean draggingScrollbar = false;
    private Button removeButton;
    private Button clearButton;

    public ItemFilterScreen() {
        super(Component.translatable("title.avaritia.item_filter"));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        updateLayout();
        refreshFilters();

        addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.add"), button ->
                minecraft.setScreen(new ItemSelectScreen(this, stack -> sendFilterUpdate(stack, 0), Items.DIRT.getDefaultInstance()))
        ).bounds(panelX + 8, panelY + PANEL_HEIGHT - 28, 64, 20).build());

        removeButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.remove"), button -> {
            ItemStack selected = getSelectedStack();
            if (!selected.isEmpty()) {
                sendFilterUpdate(selected, 1);
            }
        }).bounds(panelX + 77, panelY + PANEL_HEIGHT - 28, 64, 20).build());

        clearButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.clear"), button ->
                sendFilterUpdate(ItemStack.EMPTY, 2)
        ).bounds(panelX + 146, panelY + PANEL_HEIGHT - 28, 64, 20).build());

        refreshButtonStates();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractTransparentBackground(graphics);
        updateLayout();
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xDD202124);
        graphics.outline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF6E6E6E);
        graphics.fill(gridX - 4, gridY - 4, gridX + gridWidth() + 4, gridY + gridHeight() + 4, 0xAA111214);
        graphics.outline(gridX - 4, gridY - 4, gridWidth() + 8, gridHeight() + 8, 0xFF000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        updateScrollbarMetrics();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, panelY + 8, 0xFFFFFF);
        graphics.text(font, Component.translatable("gui.avaritia.item_filter.count", filterItems.size()), panelX + 8, panelY + PANEL_HEIGHT - 48, 0xCFCFCF);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = (scrollOffset + row) * COLUMNS + column;
                int x = gridX + column * (SLOT_SIZE + GAP);
                int y = gridY + row * (SLOT_SIZE + GAP);
                int color = index == selectedIndex ? 0xFF7CAB7C : isInside(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE) ? 0xFF5E6670 : 0xFF3B3F45;
                graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
                graphics.outline(x, y, SLOT_SIZE, SLOT_SIZE, 0xFF111111);
                if (index >= 0 && index < filterItems.size()) {
                    ItemStack stack = filterItems.get(index);
                    graphics.item(stack, x + 1, y + 1);
                    graphics.itemDecorations(font, stack, x + 1, y + 1);
                    if (isInside(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
                        graphics.setTooltipForNextFrame(font, buildTooltip(stack), stack.getTooltipImage(), stack, mouseX, mouseY);
                    }
                }
            }
        }

        if (maxScrollOffset() > 0) {
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 5, scrollbarY + scrollbarHeight, 0xAA111111);
            graphics.fill(scrollbarX, scrollbarHandleY, scrollbarX + 5, scrollbarHandleY + scrollbarHandleHeight, 0xFFB6B6B6);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isInside(mouseX, mouseY, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        setScrollOffset(scrollOffset - scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            if (maxScrollOffset() > 0 && isInside(event.x(), event.y(), scrollbarX, scrollbarY, 5, scrollbarHeight)) {
                draggingScrollbar = true;
                updateScrollFromMouse(event.y());
                return true;
            }

            int hoveredIndex = getHoveredItemIndex(event.x(), event.y());
            if (hoveredIndex >= 0 && hoveredIndex < filterItems.size()) {
                selectedIndex = hoveredIndex;
                refreshButtonStates();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            draggingScrollbar = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (AvaritiaClient.FILTER_KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void updateLayout() {
        this.panelX = (this.width - PANEL_WIDTH) / 2;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;
        this.gridX = panelX + 8;
        this.gridY = panelY + 28;
    }

    private void refreshFilters() {
        filterItems.clear();
        ItemStack toolStack = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
        CompoundTag filters = toolStack.getOrDefault(ModDataComponents.TOOL_FILTERS.get(), new CompoundTag());
        filters.keySet().stream().sorted().forEach(key -> stackFromFilterEntry(key, filters.getCompoundOrEmpty(key)).ifPresent(filterItems::add));
        setScrollOffset(scrollOffset);
        if (selectedIndex >= filterItems.size()) {
            selectedIndex = filterItems.isEmpty() ? -1 : filterItems.size() - 1;
        }
        refreshButtonStates();
    }

    private Optional<ItemStack> stackFromFilterEntry(String key, CompoundTag customData) {
        Identifier id = Identifier.tryParse(key);
        if (id == null) {
            return Optional.empty();
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(item);
        if (!customData.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        }
        return Optional.of(stack);
    }

    private void sendFilterUpdate(ItemStack stack, int action) {
        ItemStack filterStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        NetworkHandler.sendToServer(new C2SItemFilterPacket(filterStack, action));
        if (minecraft.player != null) {
            ItemStack toolStack = minecraft.player.getMainHandItem();
            CompoundTag filters = toolStack.getOrDefault(ModDataComponents.TOOL_FILTERS.get(), new CompoundTag());
            toolStack.set(ModDataComponents.TOOL_FILTERS.get(), C2SItemFilterPacket.mutateFilterTag(filters, filterStack, action));
        }
        refreshFilters();
    }

    private ItemStack getSelectedStack() {
        return selectedIndex >= 0 && selectedIndex < filterItems.size() ? filterItems.get(selectedIndex) : ItemStack.EMPTY;
    }

    private void refreshButtonStates() {
        if (removeButton != null) {
            removeButton.active = !getSelectedStack().isEmpty();
        }
        if (clearButton != null) {
            clearButton.active = !filterItems.isEmpty();
        }
    }

    private List<Component> buildTooltip(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
        stack.typeHolder().tags()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .limit(8)
                .forEach(tag -> tooltip.add(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)));
        return tooltip;
    }

    private int getHoveredItemIndex(double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, gridX, gridY, gridWidth(), gridHeight())) {
            return -1;
        }
        int column = (int) ((mouseX - gridX) / (SLOT_SIZE + GAP));
        int row = (int) ((mouseY - gridY) / (SLOT_SIZE + GAP));
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) {
            return -1;
        }
        return (scrollOffset + row) * COLUMNS + column;
    }

    private void setScrollOffset(double value) {
        scrollOffset = (int) Math.max(0, Math.min(maxScrollOffset(), Math.round(value)));
    }

    private int maxScrollOffset() {
        return Math.max(0, (int) Math.ceil((double) filterItems.size() / COLUMNS) - ROWS);
    }

    private void updateScrollbarMetrics() {
        scrollbarX = gridX + gridWidth() + 7;
        scrollbarY = gridY - 4;
        scrollbarHeight = gridHeight() + 8;
        int maxOffset = maxScrollOffset();
        if (maxOffset <= 0) {
            scrollbarHandleY = scrollbarY;
            scrollbarHandleHeight = scrollbarHeight;
            return;
        }
        int totalRows = (int) Math.ceil((double) filterItems.size() / COLUMNS);
        scrollbarHandleHeight = Math.max(16, scrollbarHeight * ROWS / totalRows);
        scrollbarHandleY = scrollbarY + (int) ((double) scrollOffset / maxOffset * (scrollbarHeight - scrollbarHandleHeight));
    }

    private void updateScrollFromMouse(double mouseY) {
        int maxOffset = maxScrollOffset();
        if (maxOffset <= 0) return;
        double ratio = (mouseY - scrollbarY - (double) scrollbarHandleHeight / 2) / (scrollbarHeight - scrollbarHandleHeight);
        setScrollOffset(Math.round(ratio * maxOffset));
    }

    private static int gridWidth() {
        return COLUMNS * SLOT_SIZE + (COLUMNS - 1) * GAP;
    }

    private static int gridHeight() {
        return ROWS * SLOT_SIZE + (ROWS - 1) * GAP;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
