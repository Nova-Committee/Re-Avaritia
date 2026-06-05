package com.avaritia.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ItemSelectScreen extends Screen {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int SLOT_SIZE = 18;
    private static final int GAP = 3;
    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 178;

    private final Screen previousScreen;
    private final Consumer<ItemStack> onSelected;
    private final List<ItemStack> results = new ArrayList<>();
    private ItemStack selectedStack;
    private EditBox searchBox;
    private Button modeButton;
    private Button selectButton;
    private boolean inventoryMode = false;
    private String searchText = "";
    private int selectedIndex = -1;
    private int panelX;
    private int panelY;
    private int gridX;
    private int gridY;
    private int scrollOffset = 0;
    private int scrollbarX;
    private int scrollbarY;
    private int scrollbarHeight;
    private int scrollbarHandleY;
    private int scrollbarHandleHeight;
    private boolean draggingScrollbar = false;

    public ItemSelectScreen(Screen previousScreen, Consumer<ItemStack> onSelected, ItemStack defaultItem) {
        super(Component.translatable("title.avaritia.item_select"));
        this.previousScreen = previousScreen;
        this.onSelected = onSelected;
        this.selectedStack = defaultItem.isEmpty() ? ItemStack.EMPTY : defaultItem.copyWithCount(1);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        updateLayout();
        refreshResults();

        searchBox = new EditBox(font, panelX + 10, panelY + 20, 160, 18, Component.translatable("gui.avaritia.search"));
        searchBox.setValue(searchText);
        searchBox.setHint(Component.translatable("gui.avaritia.search"));
        searchBox.setResponder(value -> {
            searchText = value;
            refreshResults();
        });
        addRenderableWidget(searchBox);

        modeButton = addRenderableWidget(Button.builder(modeText(), button -> {
            inventoryMode = !inventoryMode;
            modeButton.setMessage(modeText());
            refreshResults();
        }).bounds(panelX + PANEL_WIDTH - 82, panelY + 19, 72, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.cancel"), button ->
                minecraft.setScreen(previousScreen)
        ).bounds(panelX + 52, panelY + PANEL_HEIGHT - 28, 74, 20).build());

        selectButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.confirm"), button -> confirmSelection()
        ).bounds(panelX + 134, panelY + PANEL_HEIGHT - 28, 74, 20).build());
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
        graphics.centeredText(font, title, width / 2, panelY + 7, 0xFFFFFF);
        graphics.text(font, Component.translatable("gui.avaritia.item_select.count", results.size()), panelX + 10, panelY + PANEL_HEIGHT - 48, 0xCFCFCF);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = (scrollOffset + row) * COLUMNS + column;
                int x = gridX + column * (SLOT_SIZE + GAP);
                int y = gridY + row * (SLOT_SIZE + GAP);
                int color = index == selectedIndex ? 0xFF7CAB7C : isInside(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE) ? 0xFF5E6670 : 0xFF3B3F45;
                graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
                graphics.outline(x, y, SLOT_SIZE, SLOT_SIZE, 0xFF111111);
                if (index >= 0 && index < results.size()) {
                    ItemStack stack = results.get(index);
                    graphics.item(stack, x + 1, y + 1);
                    graphics.itemDecorations(font, stack, x + 1, y + 1);
                    if (isInside(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
                        graphics.setTooltipForNextFrame(font, buildTooltip(stack), stack.getTooltipImage(), stack, mouseX, mouseY);
                    }
                }
            }
        }

        int previewX = panelX + PANEL_WIDTH - 33;
        int previewY = gridY + 28;
        graphics.fill(previewX - 2, previewY - 2, previewX + 20, previewY + 20, 0xFF3B3F45);
        graphics.outline(previewX - 2, previewY - 2, 22, 22, 0xFF111111);
        if (!selectedStack.isEmpty()) {
            graphics.item(selectedStack, previewX, previewY);
            graphics.itemDecorations(font, selectedStack, previewX, previewY);
            if (isInside(mouseX, mouseY, previewX - 2, previewY - 2, 22, 22)) {
                graphics.setTooltipForNextFrame(font, buildTooltip(selectedStack), selectedStack.getTooltipImage(), selectedStack, mouseX, mouseY);
            }
        }

        if (maxScrollOffset() > 0) {
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 5, scrollbarY + scrollbarHeight, 0xAA111111);
            graphics.fill(scrollbarX, scrollbarHandleY, scrollbarX + 5, scrollbarHandleY + scrollbarHandleHeight, 0xFFB6B6B6);
        }
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
            if (hoveredIndex >= 0 && hoveredIndex < results.size()) {
                selectedIndex = hoveredIndex;
                selectedStack = results.get(hoveredIndex).copyWithCount(1);
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
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(previousScreen);
            return true;
        }
        if ((event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && searchBox != null && !searchBox.isFocused()) {
            confirmSelection();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousScreen);
    }

    private void confirmSelection() {
        if (!selectedStack.isEmpty()) {
            onSelected.accept(selectedStack.copyWithCount(1));
        }
        minecraft.setScreen(previousScreen);
    }

    private void updateLayout() {
        this.panelX = (this.width - PANEL_WIDTH) / 2;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;
        this.gridX = panelX + 10;
        this.gridY = panelY + 48;
    }

    private void refreshResults() {
        results.clear();
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        boolean tagSearch = query.startsWith("#");
        String tagQuery = tagSearch ? query.substring(1) : query;
        for (ItemStack stack : sourceItems()) {
            if (matches(stack, query, tagSearch, tagQuery)) {
                results.add(stack);
            }
        }
        setScrollOffset(0);
        selectedIndex = findSelectedIndex();
        refreshButtonStates();
    }

    private List<ItemStack> sourceItems() {
        if (inventoryMode && minecraft.player != null) {
            return inventoryItems(minecraft.player.getInventory());
        }
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .map(item -> new ItemStack(item, 1))
                .sorted(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
                .toList();
    }

    private List<ItemStack> inventoryItems(Inventory inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copyWithCount(1));
            }
        }
        stacks.sort(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
        return stacks;
    }

    private boolean matches(ItemStack stack, String query, boolean tagSearch, String tagQuery) {
        if (query.isEmpty()) {
            return true;
        }
        if (tagSearch) {
            return stack.typeHolder().tags().anyMatch(tag -> matchesIdentifier(tag.location(), tagQuery));
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.toString().toLowerCase(Locale.ROOT).contains(query)
                || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query);
    }

    private static boolean matchesIdentifier(Identifier id, String query) {
        String value = id.toString().toLowerCase(Locale.ROOT);
        return query.isEmpty() || value.contains(query);
    }

    private int findSelectedIndex() {
        if (selectedStack.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < results.size(); i++) {
            if (ItemStack.isSameItemSameComponents(selectedStack, results.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private List<Component> buildTooltip(ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
        stack.typeHolder().tags()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .limit(8)
                .forEach(tag -> tooltip.add(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)));
        return tooltip;
    }

    private Component modeText() {
        return Component.translatable(inventoryMode ? "gui.avaritia.item_select.inventory" : "gui.avaritia.item_select.all");
    }

    private void refreshButtonStates() {
        if (selectButton != null) {
            selectButton.active = !selectedStack.isEmpty();
        }
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
        return Math.max(0, (int) Math.ceil((double) results.size() / COLUMNS) - ROWS);
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
        int totalRows = (int) Math.ceil((double) results.size() / COLUMNS);
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
