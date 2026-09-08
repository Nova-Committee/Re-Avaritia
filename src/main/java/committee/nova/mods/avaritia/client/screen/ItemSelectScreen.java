package committee.nova.mods.avaritia.client.screen;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.StringInputScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableItemGrid;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Native item picker with JSON/count/NBT editors and SEARCH-tab catalog. */
public class ItemSelectScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int GRID_COLUMNS = 9;
    private static final int GRID_ROWS = 5;
    private static final int GRID_WIDTH = GRID_COLUMNS * 19 + 12;
    private static final int GRID_HEIGHT = GRID_ROWS * 19 + 3;
    private static final int SIDE_WIDTH = 64;
    private static final int BUTTON = 20;
    private static final int GAP = 4;
    private static final int PAD = 12;
    private static final int PANEL_WIDTH = PAD + SIDE_WIDTH + GAP + GRID_WIDTH + PAD;
    private static final int PANEL_HEIGHT = PAD + BUTTON + GAP + GRID_HEIGHT + GAP + BUTTON + PAD;
    private static final int MAX_COUNT = 64 * 9 * 5;

    private final Screen previousScreen;
    private final Consumer<ItemStack> onDataReceived1;
    private final Supplier<Boolean> shouldClose;
    private final ResourceKey<CreativeModeTab> tabs;
    private final boolean useInventoryMode;
    private boolean inventoryMode = false;
    private EditBox inputField;
    private String inputFieldText = "";
    private final List<ItemStack> itemList = new ArrayList<>();
    private final Set<TagKey<Item>> visibleTags = new HashSet<>();
    private ItemStack currentItem;
    private int scrollOffset = 0;
    private ScreenRectangle panel;
    private PortableItemGrid itemGrid;
    private Button typeButton;
    private Button itemButton;
    private Button countButton;
    private Button nbtButton;

    public ItemSelectScreen(Screen callbackScreen, Consumer<ItemStack> onDataReceived, ItemStack defaultItem) {
        this(callbackScreen, onDataReceived, defaultItem, null, CreativeModeTabs.SEARCH, true);
    }

    public ItemSelectScreen(Screen callbackScreen, Consumer<ItemStack> onDataReceived, ItemStack defaultItem,
                            Supplier<Boolean> shouldClose, ResourceKey<CreativeModeTab> tabs, boolean useInventoryMode) {
        super(Component.translatable("title.avaritia.item_select"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.currentItem = defaultItem.copy();
        this.shouldClose = shouldClose;
        this.tabs = tabs;
        this.useInventoryMode = useInventoryMode;
    }

    public int getScrollOffset() {
        return itemGrid != null ? itemGrid.getScrollOffset() : scrollOffset;
    }

    @Override
    protected void init() {
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get())) {
            Minecraft.getInstance().setScreen(previousScreen);
            return;
        }
        if (this.inputField != null) {
            this.inputFieldText = this.inputField.getValue();
        }
        this.scrollOffset = getScrollOffset();
        this.rebuildItemList();
        this.panel = PortableLayout.centered(this.width, this.height, PANEL_WIDTH, PANEL_HEIGHT, 8);
        ScreenRectangle inner = PortableLayout.inset(this.panel, PAD, PAD, PAD, PAD);
        int gridX = inner.left() + SIDE_WIDTH + GAP;
        int gridY = inner.top() + BUTTON + GAP;
        ScreenRectangle footer = new ScreenRectangle(inner.left(), inner.bottom() - BUTTON, inner.width(), BUTTON);

        this.inputField = new EditBox(this.font, gridX, inner.top(), GRID_WIDTH, BUTTON, Component.translatable("gui.avaritia.search"));
        this.inputField.setMaxLength(256);
        this.inputField.setValue(this.inputFieldText);
        this.inputField.setResponder(text -> this.inputFieldText = text);
        this.addRenderableWidget(UiInspector.name(this.inputField, "selector.search"));

        GridLayout side = new GridLayout(inner.left(), inner.top()).rowSpacing(2);
        this.typeButton = UiInspector.name(PortableUi.button(0, 0, SIDE_WIDTH, BUTTON, typeLabel(),
                button -> toggleInventoryMode()), "selector.type");
        this.typeButton.active = this.useInventoryMode;
        this.itemButton = UiInspector.name(PortableUi.button(0, 0, SIDE_WIDTH, BUTTON, Component.literal("JSON"),
                button -> openJsonEditor()), "selector.item");
        this.countButton = UiInspector.name(PortableUi.button(0, 0, SIDE_WIDTH, BUTTON, Component.literal(""),
                button -> openCountEditor()), "selector.count");
        this.nbtButton = UiInspector.name(PortableUi.button(0, 0, SIDE_WIDTH, BUTTON, Component.literal("NBT"),
                button -> openNbtEditor()), "selector.nbt");
        side.addChild(this.typeButton, 0, 0);
        side.addChild(this.itemButton, 1, 0);
        side.addChild(this.countButton, 2, 0);
        side.addChild(this.nbtButton, 3, 0);
        side.arrangeElements();
        side.visitWidgets(this::addRenderableWidget);

        this.itemGrid = this.addRenderableWidget(UiInspector.name(
                new PortableItemGrid(this.minecraft, gridX, gridY, GRID_COLUMNS, GRID_ROWS, "selector", this::onItemSelected),
                "selector.grid"));
        this.itemGrid.setTooltipProvider(this::tooltipFor);
        this.itemGrid.setItems(this.itemList);
        this.itemGrid.setSelected(this.currentItem);
        this.itemGrid.setScrollOffset(this.scrollOffset);

        int buttonWidth = Math.max(0, (footer.width() - GAP) / 2);
        this.addRenderableWidget(UiInspector.name(PortableUi.button(footer.left(), footer.top(), buttonWidth, footer.height(),
                CommonComponents.GUI_CANCEL, button -> Minecraft.getInstance().setScreen(previousScreen)), "selector.cancel"));
        this.addRenderableWidget(UiInspector.name(PortableUi.button(footer.right() - buttonWidth, footer.top(), buttonWidth, footer.height(),
                Component.translatable("gui.avaritia.confirm"), button -> submit()), "selector.submit"));
        this.refreshActionButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        PortableUi.panel(graphics, panel);
        UiInspector.region("selector.panel", panel, null, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (this.itemGrid != null) {
            this.itemGrid.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = super.mouseReleased(event);
        if (itemGrid != null) {
            itemGrid.cancelInteraction();
        }
        return handled;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || (event.key() == GLFW.GLFW_KEY_BACKSPACE && (this.inputField == null || !this.inputField.isFocused()))) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else if ((event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) && this.inputField != null && this.inputField.isFocused()) {
            this.updateSearchResults();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void submit() {
        if (this.currentItem == null || this.currentItem.isEmpty()) {
            Minecraft.getInstance().setScreen(previousScreen);
        } else if (onDataReceived1 != null) {
            onDataReceived1.accept(this.currentItem);
            Minecraft.getInstance().setScreen(previousScreen);
        }
    }

    private void onItemSelected(ItemStack stack) {
        this.currentItem = stack;
        this.refreshActionButtons();
    }

    private void toggleInventoryMode() {
        if (!this.useInventoryMode) {
            return;
        }
        this.inventoryMode = !this.inventoryMode;
        this.updateSearchResults();
    }

    private void updateSearchResults() {
        this.rebuildItemList();
        this.scrollOffset = 0;
        if (this.itemGrid != null) {
            this.itemGrid.setItems(this.itemList);
            this.itemGrid.setSelected(this.currentItem);
            this.itemGrid.setScrollOffset(0);
        }
        this.refreshActionButtons();
    }

    private Collection<ItemStack> getAllItemList() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true, player.level().registryAccess());
        }
        if (this.tabs == CreativeModeTabs.SEARCH) {
            return CreativeModeTabs.searchTab().getDisplayItems();
        }
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(this.tabs);
        return tab == null ? List.of() : tab.getDisplayItems();
    }

    private List<ItemStack> getPlayerItemList() {
        List<ItemStack> result = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
                    result.add(stack);
                }
            }
        }
        return result;
    }

    private void rebuildItemList() {
        String s = this.inputField != null ? this.inputField.getValue() : this.inputFieldText;
        this.inputFieldText = s == null ? "" : s;
        this.itemList.clear();
        this.visibleTags.clear();
        if (StringUtils.isNotNullOrEmpty(s)) {
            ClientPacketListener connection = this.minecraft.getConnection();
            if (connection != null) {
                SessionSearchTrees trees = connection.searchTrees();
                SearchTree<ItemStack> searchTree;
                if (s.startsWith("#")) {
                    s = s.substring(1);
                    searchTree = trees.creativeTagSearch();
                    this.updateVisibleTags(s);
                } else {
                    searchTree = trees.creativeNameSearch();
                }
                List<ItemStack> found = searchTree.search(s.toLowerCase(Locale.ROOT));
                if (this.inventoryMode) {
                    Set<Item> matching = new HashSet<>();
                    for (ItemStack stack : found) {
                        matching.add(stack.getItem());
                    }
                    for (ItemStack owned : this.getPlayerItemList()) {
                        if (matching.contains(owned.getItem())) {
                            this.itemList.add(owned);
                        }
                    }
                } else {
                    this.itemList.addAll(found);
                }
            }
        } else {
            this.itemList.addAll(this.inventoryMode ? this.getPlayerItemList() : this.getAllItemList());
        }
    }

    private void updateVisibleTags(String string) {
        int i = string.indexOf(':');
        Predicate<Identifier> predicate;
        if (i == -1) {
            predicate = id -> id.getPath().contains(string);
        } else {
            String namespace = string.substring(0, i).trim();
            String path = string.substring(i + 1).trim();
            predicate = id -> id.getNamespace().contains(namespace) && id.getPath().contains(path);
        }
        BuiltInRegistries.ITEM.getTags().forEach(named -> {
            if (predicate.test(named.key().location())) {
                this.visibleTags.add(named.key());
            }
        });
    }

    private List<Component> tooltipFor(ItemStack itemStack) {
        List<Component> list = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, itemStack));
        this.visibleTags.forEach(tag -> {
            if (itemStack.is(tag)) {
                list.add(1, Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
        for (CreativeModeTab modeTab : CreativeModeTabs.allTabs()) {
            if (modeTab.contains(itemStack)) {
                list.add(1, modeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
        }
        return list;
    }

    private Component typeLabel() {
        return Component.translatable(this.inventoryMode ? "gui.avaritia.item_select.inventory" : "gui.avaritia.item_select.all");
    }

    private void refreshActionButtons() {
        if (this.typeButton != null) {
            this.typeButton.setMessage(typeLabel());
            int size = this.inventoryMode ? this.getPlayerItemList().size() : this.getAllItemList().size();
            this.typeButton.setTooltip(Tooltip.create(Component.literal(typeLabel().getString() + " (" + size + ")")));
        }
        if (this.itemButton != null && this.currentItem != null && !this.currentItem.isEmpty()) {
            this.itemButton.setTooltip(Tooltip.create(this.currentItem.getHoverName().copy()));
        }
        if (this.countButton != null && this.currentItem != null) {
            this.countButton.setMessage(Component.literal(String.valueOf(this.currentItem.getCount())));
            this.countButton.setTooltip(Tooltip.create(Component.literal(String.valueOf(this.currentItem.getCount()))));
        }
        if (this.nbtButton != null) {
            this.nbtButton.setTooltip(Tooltip.create(Component.literal("NBT")));
        }
    }

    private void openJsonEditor() {
        String json = ItemUtils.serialize(this.currentItem).toString();
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Component.literal("请输入物品Json"), Component.literal("请输入"), "", json, input -> {
            String result = "";
            if (StringUtils.isNotNullOrEmpty(input)) {
                ItemStack itemStack;
                try {
                    JsonObject jsonObject = Const.GSON.fromJson(input, JsonObject.class);
                    itemStack = ItemUtils.deserialize(jsonObject);
                } catch (Exception e) {
                    LOGGER.error("Invalid Json: {}", input);
                    itemStack = null;
                }
                if (itemStack != null && itemStack.getItem() != Items.AIR) {
                    this.currentItem = itemStack;
                } else {
                    result = String.format("物品Json[%s]输入有误", input);
                }
            }
            return result;
        }));
    }

    private void openCountEditor() {
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Component.literal("请输入物品数量"), Component.literal("请输入"), "\\d{0,4}",
                String.valueOf(this.currentItem.getCount()), input -> {
            String result = "";
            if (StringUtils.isNotNullOrEmpty(input)) {
                int count = StringUtils.toInt(input);
                if (count > 0 && count <= MAX_COUNT) {
                    this.currentItem.setCount(count);
                } else {
                    result = String.format("物品数量[%s]输入有误", input);
                }
            }
            return result;
        }));
    }

    private void openNbtEditor() {
        String nbt = ItemUtils.getNbtString(this.currentItem);
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Component.literal("请输入物品NBT"), Component.literal("请输入"), "", nbt, input -> {
            String result = "";
            if (StringUtils.isNotNullOrEmpty(input)) {
                ItemStack itemStack;
                try {
                    itemStack = ItemUtils.getItemStack(ItemUtils.getId(this.currentItem.getItem()) + input, true);
                    itemStack.setCount(this.currentItem.getCount());
                } catch (Exception e) {
                    LOGGER.error("Invalid NBT: {}", input);
                    itemStack = null;
                }
                if (itemStack != null) {
                    this.currentItem = itemStack;
                } else {
                    result = String.format("物品NBT[%s]输入有误", input);
                }
            }
            return result;
        }));
    }
}
