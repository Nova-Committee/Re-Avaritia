package committee.nova.mods.avaritia.api.client.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.api.client.screen.component.PortableItemGrid;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static committee.nova.mods.avaritia.Const.GSON;

@OnlyIn(Dist.CLIENT)
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
    private final boolean useInventoryMode;
    private boolean inventoryMode = false;
    private EditBox inputField;
    private String inputFieldText = "";
    private final List<ItemStack> itemList = new ArrayList<>();
    private final Set<TagKey<Item>> visibleTags = new HashSet<>();
    @Getter
    private String selectedItemId;
    private ItemStack currentItem;
    private int scrollOffset = 0;
    private ScreenRectangle panel;
    private PortableItemGrid itemGrid;
    private Button typeButton;
    private Button itemButton;
    private Button countButton;
    private Button nbtButton;

    public ItemSelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<ItemStack> onDataReceived,
                            @NonNull ItemStack defaultItem) {
        this(callbackScreen, onDataReceived, defaultItem, null, true);
    }

    public ItemSelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<ItemStack> onDataReceived,
                            @NonNull ItemStack defaultItem, Supplier<Boolean> shouldClose,
                            boolean useInventoryMode
    ) {
        super(Component.literal("SelectScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.currentItem = defaultItem;
        this.selectedItemId = ItemUtils.getId(defaultItem);
        this.shouldClose = shouldClose;
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

        this.inputField = new EditBox(this.font, gridX, inner.top(), GRID_WIDTH, BUTTON, Component.literal(""));
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
                Component.literal("提交"), button -> submit()), "selector.submit"));
        this.refreshActionButtons();
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        PortableUi.panel(graphics, panel);
        UiInspector.region("selector.panel", panel, null, false);
        super.render(graphics, mouseX, mouseY, delta);
        if (this.itemGrid != null) {
            this.itemGrid.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        if (this.itemGrid != null) {
            this.itemGrid.cancelInteraction();
        }
        return handled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_BACKSPACE && (this.inputField == null || !this.inputField.isFocused()))) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.inputField != null && this.inputField.isFocused()) {
            this.updateSearchResults();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        if (this.currentItem == null) {
            Minecraft.getInstance().setScreen(previousScreen);
        } else if (onDataReceived1 != null) {
            onDataReceived1.accept(this.currentItem);
            Minecraft.getInstance().setScreen(previousScreen);
        }
    }

    private void onItemSelected(ItemStack stack) {
        this.currentItem = stack;
        this.selectedItemId = ItemUtils.getId(stack);
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
            CreativeModeTabs.tryRebuildTabContents(player.level().enabledFeatures(), true, player.level().registryAccess());
        }
        return CreativeModeTabs.searchTab().getDisplayItems();
    }

    private List<ItemStack> getPlayerItemList() {
        List<ItemStack> result = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            result.addAll(player.getInventory().items);
            result.addAll(player.getInventory().armor);
            result.addAll(player.getInventory().offhand);
            result = result.stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR).collect(Collectors.toList());
        }
        return result;
    }

    private void rebuildItemList() {
        String s = this.inputField != null ? this.inputField.getValue() : this.inputFieldText;
        this.inputFieldText = s == null ? "" : s;
        this.itemList.clear();
        this.visibleTags.clear();
        if (StringUtils.isNotNullOrEmpty(s) && this.minecraft != null) {
            if (s.startsWith("#")) {
                s = s.substring(1);
                this.updateVisibleTags(s);
                this.itemList.addAll(this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS).search(s.toLowerCase(Locale.ROOT)));
            } else {
                this.itemList.addAll(this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES).search(s.toLowerCase(Locale.ROOT)));
            }
            if (this.inventoryMode) {
                Set<Item> matching = new HashSet<>();
                for (ItemStack stack : List.copyOf(this.itemList)) {
                    matching.add(stack.getItem());
                }
                this.itemList.clear();
                for (ItemStack owned : this.getPlayerItemList()) {
                    if (matching.contains(owned.getItem())) {
                        this.itemList.add(owned);
                    }
                }
            }
        } else {
            this.itemList.addAll(this.inventoryMode ? this.getPlayerItemList() : this.getAllItemList());
        }
    }

    private void updateVisibleTags(String string) {
        int i = string.indexOf(58);
        Predicate<ResourceLocation> predicate;
        if (i == -1) {
            predicate = (resourceLocation) -> resourceLocation.getPath().contains(string);
        } else {
            String namespace = string.substring(0, i).trim();
            String path = string.substring(i + 1).trim();
            predicate = (resourceLocation) -> resourceLocation.getNamespace().contains(namespace) && resourceLocation.getPath().contains(path);
        }
        BuiltInRegistries.ITEM.getTagNames().filter((tagKey) -> predicate.test(tagKey.location())).forEach(this.visibleTags::add);
    }

    private List<Component> tooltipFor(ItemStack itemStack) {
        List<Component> list = itemStack.getTooltipLines(this.minecraft.player,
                this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        List<Component> list1 = Lists.newArrayList(list);
        this.visibleTags.forEach((itemITag) -> {
            if (itemStack.is(itemITag)) {
                list1.add(1, Component.literal("#" + itemITag.location()).withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
        for (CreativeModeTab modeTab : CreativeModeTabs.allTabs()) {
            if (modeTab.contains(itemStack)) {
                list1.add(1, modeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
        }
        return list1;
    }

    private Component typeLabel() {
        return this.inventoryMode ? Component.literal("物品栏") : Component.literal("所有物品");
    }

    private void refreshActionButtons() {
        if (this.typeButton != null) {
            this.typeButton.setMessage(typeLabel());
            int size = this.inventoryMode ? this.getPlayerItemList().size() : this.getAllItemList().size();
            Component tip = this.inventoryMode
                    ? Component.literal("列出模式\n物品栏 (" + size + ")")
                    : Component.literal("列出模式\n所有物品 (" + size + ")");
            this.typeButton.setTooltip(Tooltip.create(tip));
        }
        if (this.itemButton != null && this.currentItem != null) {
            this.itemButton.setTooltip(Tooltip.create(this.currentItem.getHoverName().copy()));
        }
        if (this.countButton != null && this.currentItem != null) {
            this.countButton.setMessage(Component.literal(String.valueOf(this.currentItem.getCount())));
            this.countButton.setTooltip(Tooltip.create(Component.literal("设置数量\n当前 " + this.currentItem.getCount())));
        }
        if (this.nbtButton != null) {
            this.nbtButton.setTooltip(Tooltip.create(Component.literal("编辑NBT")));
        }
    }

    private void openJsonEditor() {
        String itemRewardJsonString = ItemUtils.serialize(this.currentItem).toString();
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Component.literal("请输入物品Json"), Component.literal("请输入"), "", itemRewardJsonString, input -> {
            String result = "";
            if (StringUtils.isNotNullOrEmpty(input)) {
                ItemStack itemStack;
                try {
                    JsonObject jsonObject = GSON.fromJson(input, JsonObject.class);
                    itemStack = ItemUtils.deserialize(jsonObject);
                } catch (Exception e) {
                    LOGGER.error("Invalid Json: {}", input);
                    itemStack = null;
                }
                if (itemStack != null && itemStack.getItem() != Items.AIR) {
                    this.currentItem = itemStack;
                    this.selectedItemId = ItemUtils.getId(this.currentItem);
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
        String itemNbtJsonString = ItemUtils.getNbtString(this.currentItem);
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Component.literal("请输入物品NBT"), Component.literal("请输入"), "", itemNbtJsonString, input -> {
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
                    this.selectedItemId = ItemUtils.getId(this.currentItem);
                } else {
                    result = String.format("物品NBT[%s]输入有误", input);
                }
            }
            return result;
        }));
    }
}
