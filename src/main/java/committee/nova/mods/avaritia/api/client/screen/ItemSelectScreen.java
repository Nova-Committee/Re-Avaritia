package committee.nova.mods.avaritia.api.client.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButton;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButtonType;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static committee.nova.mods.avaritia.Const.GSON;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/25 20:09
 * @Description: from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
@OnlyIn(Dist.CLIENT)
public class ItemSelectScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();
    // 每行显示数量
    private final int itemPerLine = 9;
    // 每页显示行数
    private final int maxLine = 5;

    /**
     * 父级 Screen
     */
    private final Screen previousScreen;
    /**
     * 输入数据回调1
     */
    private final Consumer<ItemStack> onDataReceived1;
    /**
     * 是否要显示该界面, 若为false则直接关闭当前界面并返回到调用者的 Screen
     */
    private final Supplier<Boolean> shouldClose;
    /**
     * 显示种类
     */
    private final ResourceKey<CreativeModeTab> tabs;
    /**
     * 背包模式
     */
    private final boolean useInventoryMode;
    private boolean inventoryMode = false;
    /**
     * 输入框
     */
    private EditBox inputField;
    /**
     * 输入框文本
     */
    private String inputFieldText = "";
    /**
     * 搜索结果
     */
    private final List<ItemStack> itemList = new ArrayList<>();
    /**
     * 操作按钮
     */
    private final Map<Integer, OperationButton> OP_BUTTONS = new HashMap<>();
    /**
     * 物品按钮
     */
    private final List<OperationButton> ITEM_BUTTONS = new ArrayList<>();
    /**
     * 显示的标签
     */
    private final Set<TagKey<Item>> visibleTags = new HashSet<>();
    /**
     * 当前选择的物品 ID
     */
    @Getter
    private String selectedItemId;
    /**
     * 当前选择的物品
     */
    private ItemStack currentItem;


    private int bgX;
    private int bgY;
    private final double margin = 3;
    private double itemBgX = this.bgX + margin;
    private double itemBgY = this.bgY + 20;
    private ScreenRectangle panel;
    private ScreenRectangle grid;
    private ScreenRectangle footer;
    private ScreenRectangle scrollbar;

    // region 滚动条相关

    /**
     * 当前滚动偏移量
     */
    @Getter
    private int scrollOffset = 0;
    // 鼠标按下时的X坐标
    private double mouseDownX = -1;
    // 鼠标按下时的Y坐标
    private double mouseDownY = -1;

    // Y坐标偏移
    private double scrollOffsetOld;
    private double outScrollX;
    private double outScrollY;
    private int outScrollWidth = 5;
    private int outScrollHeight;
    private double inScrollHeight;
    private double inScrollY;

    // endregion 滚动条相关

    public ItemSelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<ItemStack> onDataReceived,
                            @NonNull ItemStack defaultItem) {
        this(callbackScreen, onDataReceived, defaultItem, null, CreativeModeTabs.SEARCH, true);
    }

    public ItemSelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<ItemStack> onDataReceived,
                            @NonNull ItemStack defaultItem, Supplier<Boolean> shouldClose, ResourceKey<CreativeModeTab> tabs,
                            boolean useInventoryMode
    ) {
        super(Component.literal("SelectScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.currentItem = defaultItem;
        this.selectedItemId = ItemUtils.getId(defaultItem);
        this.shouldClose = shouldClose;
        this.tabs = tabs;
        this.useInventoryMode = useInventoryMode;
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
        int previousScroll = this.scrollOffset;
        this.rebuildItemList();
        this.updateLayout();
        this.setScrollOffset(previousScroll);
        // 创建文本输入框
        this.inputField = new EditBox(this.font, bgX, bgY, 180, 15, Component.literal(""));
        this.inputField.setValue(this.inputFieldText);
        this.addRenderableWidget(UiInspector.name(this.inputField, "selector.search"));
        int footerY = footer.top();
        int buttonWidth = (int) (90 - this.margin * 2);
        // 创建提交按钮
        this.addRenderableWidget(UiInspector.name(PortableUi.button(footer.left() + 90, footerY, buttonWidth, footer.height()
                , GuiUtils.textToComponent(Text.i18n("提交")), button -> {
                    if (this.currentItem == null) {
                        // 关闭当前屏幕并返回到调用者的 Screen
                        Minecraft.getInstance().setScreen(previousScreen);
                    } else {
                        // 获取选择的数据，并执行回调
                        if (onDataReceived1 != null) {
                            onDataReceived1.accept(this.currentItem);
                            Minecraft.getInstance().setScreen(previousScreen);
                        }
                    }
                }), "selector.submit"));
        // 创建取消按钮
        this.addRenderableWidget(UiInspector.name(PortableUi.button(footer.left(), footerY, buttonWidth, footer.height()
                , GuiUtils.textToComponent(Text.i18n("取消"))
                , button -> Minecraft.getInstance().setScreen(previousScreen)), "selector.cancel"));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderTransparentBackground(graphics);
        PortableUi.panel(graphics, panel);
        UiInspector.region("selector.panel", panel, null, false);
        PortableUi.inset(graphics, grid);
        UiInspector.region("selector.grid", grid, grid, true);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.inputFieldText = this.inputField.getValue();

        this.renderButton(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setScrollOffset(this.getScrollOffset() - deltaY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AtomicBoolean flag = new AtomicBoolean(false);
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            flag.set(true);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            OP_BUTTONS.forEach((key, value) -> {
                if (isActiveOperation(value) && value.isMouseOverEx(mouseX, mouseY)) {
                    value.setPressed(true);
                    // 若是滑块
                    if (key == OperationButtonType.SLIDER.getCode()) {
                        this.scrollOffsetOld = this.getScrollOffset();
                        this.mouseDownX = mouseX;
                        this.mouseDownY = mouseY;
                    }
                }
            });
            ITEM_BUTTONS.forEach(bt -> bt.setPressed(isItemCellHit(bt, mouseX, mouseY)));
        }
        return flag.get() ? flag.get() : super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicBoolean updateSearchResults = new AtomicBoolean(false);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            OP_BUTTONS.forEach((key, value) -> {
                if (value.isPressed() && isActiveOperation(value) && value.isMouseOverEx(mouseX, mouseY)) {
                    this.handleOperation(value, button, flag, updateSearchResults);
                }
                value.setPressed(false);
            });
            ITEM_BUTTONS.forEach(bt -> {
                if (bt.isPressed() && isItemCellHit(bt, mouseX, mouseY)) {
                    this.handleItem(bt, button, flag);
                }
                bt.setPressed(false);
            });
            this.mouseDownX = -1;
            this.mouseDownY = -1;
            if (updateSearchResults.get()) {
                this.updateSearchResults();
            }
        }
        return flag.get() ? flag.get() : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        OP_BUTTONS.forEach((key, value) -> {
            value.setHovered(isActiveOperation(value) && value.isMouseOverEx(mouseX, mouseY));
            if (key == OperationButtonType.SLIDER.getCode()) {
                if (value.isPressed() && this.mouseDownX != -1 && this.mouseDownY != -1) {
                    // 一个像素对应多少滚动偏移量
                    double scale = Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine) / (this.outScrollHeight - 2);
                    this.setScrollOffset(this.scrollOffsetOld + (mouseY - this.mouseDownY) * scale);
                }
            }
        });
        ITEM_BUTTONS.forEach(bt -> bt.setHovered(isItemCellHit(bt, mouseX, mouseY)));
        super.mouseMoved(mouseX, mouseY);
    }

    /**
     * 重写键盘事件
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.inputField.isFocused())) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.inputField.isFocused()) {
            this.updateSearchResults();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Collection<ItemStack> getAllItemList() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true, player.level().registryAccess());
        }
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(this.tabs).getDisplayItems();
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

    /**
     * 设置排列方式
     */
    private void updateLayout() {
        this.bgX = this.width / 2 - 92;
        this.bgY = this.height / 2 - 65;
        this.itemBgX = this.bgX + margin;
        this.itemBgY = this.bgY + 20;
        this.panel = new ScreenRectangle((int) (this.bgX - this.margin), (int) (this.bgY - this.margin),
                (int) (180 + this.margin * 2), (int) (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * 5 + 20 + margin * 2 + 5));
        this.grid = new ScreenRectangle((int) (this.itemBgX - this.margin), (int) (this.itemBgY - this.margin),
                (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.itemPerLine + this.margin),
                (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.maxLine + this.margin));
        int footerY = (int) (this.bgY + (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * 5 + margin));
        this.footer = new ScreenRectangle((int) (this.bgX + this.margin), footerY, (int) (180 - this.margin * 2), 20);

        this.OP_BUTTONS.put(OperationButtonType.TYPE.getCode(), new OperationButton(OperationButtonType.TYPE.getCode(), context -> {
            int lineColor = context.button().isHovered() ? 0xEEFFFFFF : 0xEE000000;
            GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 0xEE707070, 2);
            GuiUtils.fillOutLine(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 1, lineColor, 2);
            ItemStack itemStack = new ItemStack(this.inventoryMode ? Items.CHEST : Items.COMPASS);
            context.graphics().renderItem(itemStack, (int) context.button().getX() + 2, (int) context.button().getY() + 2);
            Text text = this.inventoryMode ? Text.i18n("列出模式\n物品栏 (%s)", getPlayerItemList().size()) : Text.i18n("列出模式\n所有物品 (%s)", getAllItemList().size());
            context.button().setTooltip(text);
            inspectOperation("selector.type", context.button());
        }).setX(this.bgX - GuiUtils.ITEM_ICON_SIZE - 2 - margin - 3).setY(this.bgY + margin).setWidth(GuiUtils.ITEM_ICON_SIZE + 4).setHeight(GuiUtils.ITEM_ICON_SIZE + 4));
        this.OP_BUTTONS.put(OperationButtonType.ITEM.getCode(), new OperationButton(OperationButtonType.ITEM.getCode(), context -> {
            int lineColor = context.button().isHovered() ? 0xEEFFFFFF : 0xEE000000;
            GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 0xEE707070, 2);
            GuiUtils.fillOutLine(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 1, lineColor, 2);
            context.graphics().renderItem(this.currentItem, (int) context.button().getX() + 2, (int) context.button().getY() + 2);
            context.button().setTooltip(GuiUtils.componentToText(this.currentItem.getHoverName().copy()));
            inspectOperation("selector.item", context.button());
        }).setX(this.bgX - GuiUtils.ITEM_ICON_SIZE - 2 - margin - 3).setY(this.bgY + margin + GuiUtils.ITEM_ICON_SIZE + 4 + 1).setWidth(GuiUtils.ITEM_ICON_SIZE + 4).setHeight(GuiUtils.ITEM_ICON_SIZE + 4));
        this.OP_BUTTONS.put(OperationButtonType.COUNT.getCode(), new OperationButton(OperationButtonType.COUNT.getCode(), context -> {
            int lineColor = context.button().isHovered() ? 0xEEFFFFFF : 0xEE000000;
            GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 0xEE707070, 2);
            GuiUtils.fillOutLine(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 1, lineColor, 2);
            ItemStack itemStack = new ItemStack(Items.WRITABLE_BOOK);
            context.graphics().renderItem(itemStack, (int) context.button().getX() + 2, (int) context.button().getY() + 2);
            Text text = Text.i18n("设置数量\n当前 %s", this.currentItem.getCount());
            context.button().setTooltip(text);
            inspectOperation("selector.count", context.button());
        }).setX(this.bgX - GuiUtils.ITEM_ICON_SIZE - 2 - margin - 3).setY(this.bgY + margin + (GuiUtils.ITEM_ICON_SIZE + 4 + 1) * 2).setWidth(GuiUtils.ITEM_ICON_SIZE + 4).setHeight(GuiUtils.ITEM_ICON_SIZE + 4));
        this.OP_BUTTONS.put(OperationButtonType.NBT.getCode(), new OperationButton(OperationButtonType.NBT.getCode(), context -> {
            int lineColor = context.button().isHovered() ? 0xEEFFFFFF : 0xEE000000;
            GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 0xEE707070, 2);
            GuiUtils.fillOutLine(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), 1, lineColor, 2);
            ItemStack itemStack = new ItemStack(Items.NAME_TAG);
            context.graphics().renderItem(itemStack, (int) context.button().getX() + 2, (int) context.button().getY() + 2);
            Text text = Text.i18n("编辑NBT");
            context.button().setTooltip(text);
            inspectOperation("selector.nbt", context.button());
        }).setX(this.bgX - GuiUtils.ITEM_ICON_SIZE - 2 - margin - 3).setY(this.bgY + margin + (GuiUtils.ITEM_ICON_SIZE + 4 + 1) * 3).setWidth(GuiUtils.ITEM_ICON_SIZE + 4).setHeight(GuiUtils.ITEM_ICON_SIZE + 4));

        this.OP_BUTTONS.put(OperationButtonType.SLIDER.getCode(), new OperationButton(OperationButtonType.SLIDER.getCode(), context -> {
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) this.outScrollY, this.outScrollWidth, this.outScrollHeight, 0xCC232323);
            int color = context.button().isHovered() ? 0xCCFFFFFF : 0xCC8B8B8B;
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) Math.ceil(this.inScrollY), this.outScrollWidth, (int) this.inScrollHeight, color);
            inspectOperation("selector.scrollbar", context.button());
        }));

        this.ITEM_BUTTONS.clear();
        for (int i = 0; i < maxLine; i++) {
            for (int j = 0; j < itemPerLine; j++) {
                ITEM_BUTTONS.add(new OperationButton(itemPerLine * i + j, context -> {
                    int index = this.itemIndex(context.button());
                    if (index < 0 || index >= itemList.size() || context.button().getWidth() <= 0 || context.button().getHeight() <= 0) {
                        context.button().setCustomPopupFunction(null);
                        return;
                    }
                    ItemStack itemStack = itemList.get(index);
                    int bgColor;
                    if (context.button().isHovered() || ItemUtils.getId(itemStack).equalsIgnoreCase(this.getSelectedItemId())) {
                        bgColor = 0xEE7CAB7C;
                    } else {
                        bgColor = 0xEE707070;
                    }

                    GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), bgColor);
                    context.graphics().renderItem(itemStack, (int) context.button().getX() + 1, (int) context.button().getY() + 1);
                    context.button().setCustomPopupFunction(() -> {
                        if (context.button().isHovered()) {
                            List<Component> list = itemStack.getTooltipLines(Item.TooltipContext.of(minecraft.level.registryAccess()), Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                            List<Component> list1 = Lists.newArrayList(list);
                            this.visibleTags.forEach((itemITag) -> {
                                if (itemStack.is(itemITag)) {
                                    list1.add(1, (Component.literal("#" + itemITag.location())).withStyle(ChatFormatting.DARK_PURPLE));
                                }
                            });
                            for (CreativeModeTab modeTab : CreativeModeTabs.allTabs()) {
                                if (modeTab.contains(itemStack)) {
                                    list1.add(1, modeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                                }
                            }
                            context.graphics().renderTooltip(font, list1, itemStack.getTooltipImage(), itemStack, (int) context.mouseX(), (int) context.mouseY());
                        }
                    });
                    UiInspector.row("selector.grid", null, index,
                            (int) context.button().getRealX(), (int) context.button().getRealY(),
                            (int) context.button().getRealWidth(), (int) context.button().getRealHeight(),
                            grid, true);
                }));
            }
        }
        this.updateScrollbar();
        this.updateItemCells();
    }

    /**
     * 更新搜索结果
     */
    private void updateSearchResults() {
        this.rebuildItemList();
        this.setScrollOffset(0);
    }

    private void rebuildItemList() {
        String s = this.inputField != null ? this.inputField.getValue() : this.inputFieldText;
        this.inputFieldText = s == null ? "" : s;
        this.itemList.clear();
        this.visibleTags.clear();
        if (StringUtils.isNotNullOrEmpty(s)) {
            ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
            if (clientpacketlistener != null) {
                SessionSearchTrees sessionsearchtrees = clientpacketlistener.searchTrees();
                SearchTree<ItemStack> isearchtree;
                if (s.startsWith("#")) {
                    s = s.substring(1);
                    isearchtree = sessionsearchtrees.creativeTagSearch();
                    this.updateVisibleTags(s);
                } else {
                    isearchtree = sessionsearchtrees.creativeNameSearch();
                }
                this.itemList.addAll(isearchtree.search(s.toLowerCase(Locale.ROOT)));
            }
        } else {
            this.itemList.addAll(this.inventoryMode ? this.getPlayerItemList() : this.getAllItemList());
        }
        this.clearItemPressState();
    }

    private void updateVisibleTags(String string) {
        int i = string.indexOf(58);
        Predicate<ResourceLocation> predicate;
        if (i == -1) {
            predicate = (resourceLocation) -> resourceLocation.getPath().contains(string);
        } else {
            String s = string.substring(0, i).trim();
            String s1 = string.substring(i + 1).trim();
            predicate = (resourceLocation) -> resourceLocation.getNamespace().contains(s) && resourceLocation.getPath().contains(s1);
        }
        BuiltInRegistries.ITEM.getTagNames().filter((tagKey) -> predicate.test(tagKey.location())).forEach(this.visibleTags::add);
    }

    private void setScrollOffset(double offset) {
        int next = (int) Math.max(Math.min(offset, (int) Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine)), 0);
        if (next != this.scrollOffset) {
            this.clearItemPressState();
        }
        this.scrollOffset = next;
        this.updateItemCells();
        this.updateScrollbar();
    }

    private void updateScrollbar() {
        double bgWidth = (GuiUtils.ITEM_ICON_SIZE + margin) * itemPerLine;
        double bgHeight = (GuiUtils.ITEM_ICON_SIZE + margin) * maxLine - margin;
        this.outScrollX = itemBgX + bgWidth + 2;
        this.outScrollY = itemBgY - this.margin + 1;
        this.outScrollWidth = 5;
        this.outScrollHeight = (int) (bgHeight + this.margin + 1);
        double inScrollWidthScale = itemList.size() > itemPerLine * maxLine ? (double) itemPerLine * maxLine / itemList.size() : 1;
        double outLine = Math.max((int) Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine), 0);
        double outCellHeight = outLine == 0 ? 0 : (1 - inScrollWidthScale) * (outScrollHeight - 2) / outLine;
        double inScrollTopHeight = this.getScrollOffset() * outCellHeight;
        this.inScrollHeight = Math.max(2, (outScrollHeight - 2) * inScrollWidthScale);
        this.inScrollY = outScrollY + inScrollTopHeight + 1;
        this.scrollbar = new ScreenRectangle((int) this.outScrollX, (int) this.outScrollY, this.outScrollWidth, this.outScrollHeight);
        OperationButton slider = this.OP_BUTTONS.get(OperationButtonType.SLIDER.getCode());
        if (slider != null) {
            slider.setX(this.outScrollX).setY(this.outScrollY).setWidth(this.outScrollWidth).setHeight(this.outScrollHeight);
        }
    }

    private void updateItemCells() {
        for (OperationButton button : ITEM_BUTTONS) {
            int i1 = button.getOperation() / itemPerLine;
            int j1 = button.getOperation() % itemPerLine;
            int index = this.itemIndex(button);
            if (index >= 0 && index < itemList.size()) {
                ItemStack itemStack = itemList.get(index);
                String id = ItemUtils.getId(itemStack);
                if (!id.equals(button.getId())) {
                    button.setPressed(false);
                }
                double itemX = itemBgX + j1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                double itemY = itemBgY + i1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                button.setX(itemX - 1).setY(itemY - 1).setWidth(GuiUtils.ITEM_ICON_SIZE + 2).setHeight(GuiUtils.ITEM_ICON_SIZE + 2)
                        .setId(id);
            } else {
                if (button.isPressed() || StringUtils.isNotNullOrEmpty(button.getId())) {
                    button.setPressed(false);
                }
                button.setHovered(false);
                button.setX(0).setY(0).setWidth(0).setHeight(0).setId("");
                button.setCustomPopupFunction(null);
            }
        }
    }

    private int itemIndex(OperationButton button) {
        int i1 = button.getOperation() / itemPerLine;
        int j1 = button.getOperation() % itemPerLine;
        return ((itemList.size() > itemPerLine * maxLine ? this.getScrollOffset() : 0) + i1) * itemPerLine + j1;
    }

    private boolean isActiveOperation(OperationButton button) {
        return button.getRealWidth() > 0 && button.getRealHeight() > 0;
    }

    private boolean isItemCellHit(OperationButton button, double mouseX, double mouseY) {
        if (button.getRealWidth() <= 0 || button.getRealHeight() <= 0) {
            return false;
        }
        int index = this.itemIndex(button);
        if (index < 0 || index >= this.itemList.size()) {
            return false;
        }
        return PortableLayout.contains(grid, mouseX, mouseY) && button.isMouseOverEx(mouseX, mouseY);
    }

    private void clearItemPressState() {
        for (OperationButton button : ITEM_BUTTONS) {
            button.setPressed(false);
        }
    }

    private static void inspectOperation(String id, OperationButton button) {
        UiInspector.region(id, (int) button.getRealX(), (int) button.getRealY(),
                (int) button.getRealWidth(), (int) button.getRealHeight(), null, true);
    }

    /**
     * 绘制按钮
     */
    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY) {
        for (OperationButton button : OP_BUTTONS.values()) button.render(graphics, mouseX, mouseY);
        for (OperationButton button : ITEM_BUTTONS) button.render(graphics, mouseX, mouseY);
        for (OperationButton button : OP_BUTTONS.values())
            button.renderPopup(graphics, this.font, mouseX, mouseY);
        for (OperationButton button : ITEM_BUTTONS)
            button.renderPopup(graphics, this.font, mouseX, mouseY);
    }

    private void handleItem(OperationButton bt, int button, AtomicBoolean flag) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.selectedItemId = bt.getId();
            if (StringUtils.isNotNullOrEmpty(this.selectedItemId)) {
                this.currentItem = ItemUtils.getItemStack(selectedItemId);
                this.currentItem.setCount(1);
                flag.set(true);

            }
        }
    }

    private void handleOperation(OperationButton bt, int button, AtomicBoolean flag, AtomicBoolean updateSearchResults) {
        if (this.useInventoryMode && bt.getOperation() == OperationButtonType.TYPE.getCode()) {
            this.inventoryMode = !this.inventoryMode;
            updateSearchResults.set(true);
            flag.set(true);
        } else if (bt.getOperation() == OperationButtonType.ITEM.getCode()) {
            String itemRewardJsonString = ItemUtils.serialize(this.currentItem).toString();
            Minecraft.getInstance().setScreen(new StringInputScreen(this, Text.i18n("请输入物品Json").setShadow(true), Text.i18n("请输入"), "", itemRewardJsonString, input -> {
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
        } else if (bt.getOperation() == OperationButtonType.COUNT.getCode()) {
            Minecraft.getInstance().setScreen(new StringInputScreen(this, Text.i18n("请输入物品数量").setShadow(true), Text.i18n("请输入"), "\\d{0,4}", String.valueOf(this.currentItem.getCount()), input -> {
                String result = "";
                if (StringUtils.isNotNullOrEmpty(input)) {
                    int count = StringUtils.toInt(input);
                    if (count > 0 && count <= 64 * 9 * 5) {
                        this.currentItem.setCount(count);
                    } else {
                        result = String.format("物品数量[%s]输入有误", input);
                    }
                }
                return result;
            }));
        } else if (bt.getOperation() == OperationButtonType.NBT.getCode()) {
            String itemNbtJsonString = ItemUtils.getNbtString(this.currentItem);
            Minecraft.getInstance().setScreen(new StringInputScreen(this, Text.i18n("请输入物品NBT").setShadow(true), Text.i18n("请输入"), "", itemNbtJsonString, input -> {
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
                    if (itemStack != null
                            //&& itemStack.hasTag()
                    ) {
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
}
