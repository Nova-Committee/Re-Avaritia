package committee.nova.mods.avaritia.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.api.client.screen.ItemSelectScreen;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButton;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPack;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.FILTER_KEY;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/30 01:06
 * @Description:
 */
public class ItemFilterScreen extends Screen {
    // 每行显示数量
    private final int itemPerLine = 9;
    // 每页显示行数
    private final int maxLine = 5;
    private int bgX;
    private int bgY;
    private final double margin = 3;
    private double itemBgX = this.bgX + margin;
    private double itemBgY = this.bgY + 20;
    private ScreenRectangle panel = new ScreenRectangle(0, 0, 0, 0);
    private ScreenRectangle grid = new ScreenRectangle(0, 0, 0, 0);
    private ScreenRectangle footer = new ScreenRectangle(0, 0, 0, 0);
    private ScreenRectangle scrollbar = new ScreenRectangle(0, 0, 0, 0);

    // region 滚动条相关

    /**
     * 储存的物品
     */
    private List<ItemStack> itemList = new ArrayList<>();
    /**
     * 显示的标签
     */
    private final Set<TagKey<Item>> visibleTags = new HashSet<>();
    /**
     * 当前选择的物品 ID
     */
    @Getter
    private String selectedItemId = "";
    /**
     * 当前选择的物品
     */
    private ItemStack currentItem = new ItemStack(Items.AIR);
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
    /**
     * 操作按钮
     */
    private final Map<Integer, OperationButton> OP_BUTTONS = new HashMap<>();
    /**
     * 物品按钮
     */
    private final List<OperationButton> ITEM_BUTTONS = new ArrayList<>();

    /**
     * 操作按钮类型
     */
    @Getter
    enum OperationButtonType {

        SLIDER(1),
        ;

        final int code;

        OperationButtonType(int code) {
            this.code = code;
        }

        static OperationButtonType valueOf(int code) {
            return Arrays.stream(values()).filter(v -> v.getCode() == code).findFirst().orElse(null);
        }
    }

    private void setScrollOffset(double offset) {
        this.scrollOffset = (int) Math.max(Math.min(offset, (int) Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine)), 0);
        this.updateMapping();
    }

    public ItemFilterScreen() {
        super(Component.literal("ItemFilterScreen"));
        this.updateItems();
    }

    @Override
    protected void init() {
        this.updateVisibleTags();
        //this.updateItems();
        this.updateLayout();
        int buttonWidth = (int) (90 - this.margin * 2);
        this.addRenderableWidget(UiInspector.name(PortableUi.button(this.footer.right() - buttonWidth, this.footer.top(), buttonWidth, this.footer.height()
                , GuiUtils.textToComponent(Text.i18n("添加")), button -> {
                    Minecraft.getInstance().setScreen(new ItemSelectScreen(this, input -> {
                        PacketDistributor.sendToServer(new C2SItemFilterPack(input,0));
                        if (itemList.stream().noneMatch(existing -> existing.is(input.getItem()))) {
                            this.itemList.add(input.copyWithCount(1));
                        }
                        this.updateMapping();
                    }, Blocks.DIRT.asItem().getDefaultInstance()));
                }), "filter.add"));
        this.addRenderableWidget(UiInspector.name(PortableUi.button(this.footer.left(), this.footer.top(), buttonWidth, this.footer.height()
                , GuiUtils.textToComponent(Text.i18n("删除"))
                , button -> {
                    this.itemList.remove(this.currentItem);
                    if (this.currentItem != null) PacketDistributor.sendToServer(new C2SItemFilterPack(this.currentItem, 1));
                    Minecraft.getInstance().setScreen(null);
                }), "filter.remove"));
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderTransparentBackground(graphics);
        if (this.panel.width() > 0 && this.panel.height() > 0) {
            PortableUi.panel(graphics, this.panel);
            UiInspector.region("filter.panel", this.panel, null, false);
        }
        if (this.grid.width() > 0 && this.grid.height() > 0) {
            PortableUi.inset(graphics, this.grid);
            UiInspector.region("filter.grid", this.grid, this.grid, true);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 绘制背景
        super.render(graphics, mouseX, mouseY, delta);
        this.renderButton(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.setScrollOffset(this.getScrollOffset() - scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            OP_BUTTONS.forEach((key, value) -> {
                if (value.getRealWidth() > 0 && value.getRealHeight() > 0 && value.isMouseOverEx(mouseX, mouseY)) {
                    value.setPressed(true);
                    // 若是滑块
                    if (key == OperationButtonType.SLIDER.getCode()) {
                        this.scrollOffsetOld = this.getScrollOffset();
                        this.mouseDownX = mouseX;
                        this.mouseDownY = mouseY;
                    }
                }
            });
            // 物品按钮
            ITEM_BUTTONS.forEach(bt -> bt.setPressed(this.isLiveItemCell(bt, mouseX, mouseY)));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        AtomicBoolean flag = new AtomicBoolean(false);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            // 物品按钮
            ITEM_BUTTONS.forEach(bt -> {
                if (bt.isPressed() && this.isLiveItemCell(bt, mouseX, mouseY)) {
                    this.handleItem(bt, button, flag);
                }
                bt.setPressed(false);
            });
            OP_BUTTONS.values().forEach(bt -> bt.setPressed(false));
            this.mouseDownX = -1;
            this.mouseDownY = -1;
        }
        return flag.get() ? flag.get() : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // 控制按钮
        OP_BUTTONS.forEach((key, value) -> {
            value.setHovered(value.getRealWidth() > 0 && value.getRealHeight() > 0 && value.isMouseOverEx(mouseX, mouseY));
            if (key == OperationButtonType.SLIDER.getCode()) {
                if (value.isPressed() && this.mouseDownX != -1 && this.mouseDownY != -1) {
                    // 一个像素对应多少滚动偏移量
                    double scale = Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine) / (this.outScrollHeight - 2);
                    this.setScrollOffset(this.scrollOffsetOld + (mouseY - this.mouseDownY) * scale);
                }
            }
        });
        // 物品按钮
        ITEM_BUTTONS.forEach(bt -> bt.setHovered(this.isLiveItemCell(bt, mouseX, mouseY)));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void handleItem(OperationButton bt, int button, AtomicBoolean flag) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.selectedItemId = bt.getId();
            if (StringUtils.isNotNullOrEmpty(this.selectedItemId)) {
                this.currentItem = ItemUtils.getItemStack(selectedItemId);
                this.currentItem.setCount(1);
                //LOGGER.debug("Select item: {}", ItemRewardParser.getDisplayName(this.currentItem));
                flag.set(true);
            }
        }
    }


        /**
         * 更新物品列表
         */
    private void updateItems() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            this.itemList.clear();
            CompoundTag filters = player.getMainHandItem().get(ModDataComponents.TOOL_FILTERS.get());
            if (filters != null) {
                for (String key : filters.getAllKeys()) {
                    ResourceLocation id = ResourceLocation.tryParse(key);
                    if (id == null) {
                        continue;
                    }
                    BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                        if (item != Items.AIR) {
                            ItemStack stack = new ItemStack(item);
                            CompoundTag data = filters.getCompound(key);
                            if (!data.isEmpty()) {
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                            }
                            this.itemList.add(stack);
                        }
                    });
                }
            }
        }
        setScrollOffset(0);
    }

    private void updateVisibleTags() {
        BuiltInRegistries.ITEM.getTagNames().forEach(this.visibleTags::add);
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
                (int) (180 + this.margin * 2),
                (int) (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * this.maxLine + 20 + margin * 2 + 5));
        this.grid = new ScreenRectangle((int) (this.itemBgX - this.margin), (int) (this.itemBgY - this.margin),
                (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.itemPerLine + this.margin),
                (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.maxLine + this.margin));
        int footerY = (int) (this.bgY + (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * this.maxLine + margin));
        int buttonWidth = (int) (90 - this.margin * 2);
        int removeX = (int) (this.bgX + this.margin);
        int addX = (int) (this.bgX + 90 + this.margin);
        this.footer = new ScreenRectangle(removeX, footerY, (addX + buttonWidth) - removeX, 20);

        // 初始化操作按钮

        // 滚动条
        this.OP_BUTTONS.put(OperationButtonType.SLIDER.getCode(), new OperationButton(OperationButtonType.SLIDER.getCode(), context -> {
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) this.outScrollY, this.outScrollWidth, this.outScrollHeight, 0xCC232323);
            int color = context.button().getRealWidth() > 0 && context.button().isMouseOverEx(context.mouseX(), context.mouseY()) ? 0xCCFFFFFF : 0xCC8B8B8B;
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) Math.ceil(this.inScrollY), this.outScrollWidth, (int) this.inScrollHeight, color);
            UiInspector.region("filter.scrollbar", this.scrollbar, null, true);
        }));

        // 物品列表
        this.ITEM_BUTTONS.clear();
        for (int i = 0; i < maxLine; i++) {
            for (int j = 0; j < itemPerLine; j++) {
                ITEM_BUTTONS.add(new OperationButton(itemPerLine * i + j, context -> {
                    OperationButton button = context.button();
                    int index = this.sourceIndex(button);
                    if (button.getRealWidth() <= 0 || button.getRealHeight() <= 0 || index < 0 || index >= itemList.size()) {
                        button.setCustomPopupFunction(null);
                        return;
                    }
                    ItemStack itemStack = itemList.get(index);
                    int bgColor;
                    if (button.isMouseOverEx(context.mouseX(), context.mouseY())
                            || ItemUtils.getId(itemStack).equalsIgnoreCase(this.getSelectedItemId())
                    ) {
                        bgColor = 0xEE7CAB7C;
                    } else {
                        bgColor = 0xEE707070;
                    }

                    GuiUtils.fill(context.graphics(), (int) button.getX(), (int) button.getY(), (int) button.getWidth(), (int) button.getHeight(), bgColor);
                    context.graphics().renderItem(itemStack, (int) button.getX() + 1, (int) button.getY() + 1);
                    if (UiInspector.enabled()) {
                        UiInspector.row("filter.grid", null, index,
                                (int) button.getRealX(), (int) button.getRealY(),
                                (int) button.getRealWidth(), (int) button.getRealHeight(),
                                this.grid, true);
                    }
                    button.setCustomPopupFunction(() -> {
                        if (!this.isLiveItemCell(button, context.mouseX(), context.mouseY())) {
                            return;
                        }
                        int liveIndex = this.sourceIndex(button);
                        if (liveIndex < 0 || liveIndex >= itemList.size()) {
                            return;
                        }
                        ItemStack liveStack = itemList.get(liveIndex);
                        Item.TooltipContext tooltipContext = Item.TooltipContext.of(Minecraft.getInstance().level);
                        List<Component> list = liveStack.getTooltipLines(tooltipContext, Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                        List<Component> list1 = Lists.newArrayList(list);
                        this.visibleTags.forEach((itemITag) -> {
                            if (liveStack.is(itemITag)) {
                                list1.add(1, (Component.literal("#" + itemITag.location())).withStyle(ChatFormatting.DARK_PURPLE));
                            }
                        });
                        for (CreativeModeTab modeTab : CreativeModeTabs.allTabs()) {
                            if (modeTab.contains(liveStack)) {
                                list1.add(1, modeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                            }
                        }
                        context.graphics().renderTooltip(font, list1, liveStack.getTooltipImage(), liveStack, (int) context.mouseX(), (int) context.mouseY());
                    });
                }));
            }
        }
        this.updateMapping();
    }

    private void updateMapping() {
        for (OperationButton button : ITEM_BUTTONS) {
            button.setPressed(false);
            button.setHovered(false);
            button.setCustomPopupFunction(null);
        }
        this.updateScrollbar();
        this.updateItemMapping();
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

    private void updateItemMapping() {
        for (OperationButton button : this.ITEM_BUTTONS) {
            int j1 = button.getOperation() % itemPerLine;
            int i1 = button.getOperation() / itemPerLine;
            int index = this.sourceIndex(button);
            if (index >= 0 && index < itemList.size()) {
                ItemStack itemStack = itemList.get(index);
                double itemX = itemBgX + j1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                double itemY = itemBgY + i1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                String id = ItemUtils.getId(itemStack);
                if (!id.equals(button.getId())) {
                    button.setPressed(false);
                    button.setHovered(false);
                    button.setCustomPopupFunction(null);
                }
                button.setX(itemX - 1).setY(itemY - 1).setWidth(GuiUtils.ITEM_ICON_SIZE + 2).setHeight(GuiUtils.ITEM_ICON_SIZE + 2)
                        .setId(id);
            } else {
                button.setPressed(false);
                button.setHovered(false);
                button.setCustomPopupFunction(null);
                button.setX(0).setY(0).setWidth(0).setHeight(0).setId("");
            }
        }
    }

    private int sourceIndex(OperationButton button) {
        int i1 = button.getOperation() / itemPerLine;
        int j1 = button.getOperation() % itemPerLine;
        return ((itemList.size() > itemPerLine * maxLine ? this.getScrollOffset() : 0) + i1) * itemPerLine + j1;
    }

    private boolean isLiveItemCell(OperationButton button, double mouseX, double mouseY) {
        int index = this.sourceIndex(button);
        return button.getRealWidth() > 0 && button.getRealHeight() > 0
                && index >= 0 && index < itemList.size()
                && PortableLayout.contains(this.grid, mouseX, mouseY)
                && button.isMouseOverEx(mouseX, mouseY);
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


    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (FILTER_KEY.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        } else return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
