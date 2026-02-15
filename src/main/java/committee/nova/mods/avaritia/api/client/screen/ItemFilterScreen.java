package committee.nova.mods.avaritia.api.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.api.client.screen.component.KeyEventManager;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButton;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.StringUtils;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.FILTER_KEY;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/12/30 01:06
 * @Description:
 */
@OnlyIn(Dist.CLIENT)
public class ItemFilterScreen extends Screen {
    private final KeyEventManager keyManager = new KeyEventManager();

    // 每行显示数量
    private final int itemPerLine = 9;
    // 每页显示行数
    private final int maxLine = 5;
    private int bgX;
    private int bgY;
    private final double margin = 3;
    private double itemBgX = this.bgX + margin;
    private double itemBgY = this.bgY + 20;

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
        // 创建添加按钮
        this.addRenderableWidget(GuiUtils.newButton((int) (this.bgX + 90 + this.margin), (int) (this.bgY + (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * 5 + margin))
                , (int) (90 - this.margin * 2), 20
                , GuiUtils.textToComponent(Text.i18n("添加")), button -> {
                    Minecraft.getInstance().setScreen(new ItemSelectScreen(this, input -> {
                        NetworkHandler.CHANNEL.sendToServer(new C2SItemFilterPack(0, input));
                        this.itemList.add(input);
                    }, Blocks.DIRT.asItem().getDefaultInstance()));
                }));
        // 创建删除按钮
        this.addRenderableWidget(GuiUtils.newButton((int) (this.bgX + this.margin), (int) (this.bgY + (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * 5 + margin))
                , (int) (90 - this.margin * 2), 20
                , GuiUtils.textToComponent(Text.i18n("删除"))
                , button -> {
                    this.itemList.remove(this.currentItem);
                    if (this.currentItem != null)
                        NetworkHandler.CHANNEL.sendToServer(new C2SItemFilterPack(1, this.currentItem));
                    Minecraft.getInstance().setScreen(null);
                }));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 绘制背景
        this.renderBackground(graphics);
        GuiUtils.fill(graphics, (int) (this.bgX - this.margin), (int) (this.bgY - this.margin), (int) (180 + this.margin * 2), (int) (20 + (GuiUtils.ITEM_ICON_SIZE + 3) * 5 + 20 + margin * 2 + 5), 0xCCC6C6C6, 2);
        GuiUtils.fillOutLine(graphics, (int) (this.itemBgX - this.margin), (int) (this.itemBgY - this.margin), (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.itemPerLine + this.margin), (int) ((GuiUtils.ITEM_ICON_SIZE + this.margin) * this.maxLine + this.margin), 1, 0xFF000000, 1);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderButton(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        keyManager.mouseScrolled(delta, mouseX, mouseY);
        this.setScrollOffset(this.getScrollOffset() - delta);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        keyManager.mouseClicked(button, mouseX, mouseY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            OP_BUTTONS.forEach((key, value) -> {
                if (value.isHovered()) {
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
            ITEM_BUTTONS.forEach(bt -> bt.setPressed(bt.isHovered()));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        keyManager.refresh(mouseX, mouseY);
        AtomicBoolean flag = new AtomicBoolean(false);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            // 物品按钮
            ITEM_BUTTONS.forEach(bt -> {
                if (bt.isHovered() && bt.isPressed()) {
                    this.handleItem(bt, button, flag);
                }
                bt.setPressed(false);
            });
            this.mouseDownX = -1;
            this.mouseDownY = -1;
        }
        keyManager.mouseReleased(button, mouseX, mouseY);
        return flag.get() ? flag.get() : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        keyManager.mouseMoved(mouseX, mouseY);
        // 控制按钮
        OP_BUTTONS.forEach((key, value) -> {
            value.setHovered(value.isMouseOverEx(mouseX, mouseY));
            if (key == OperationButtonType.SLIDER.getCode()) {
                if (value.isPressed() && this.mouseDownX != -1 && this.mouseDownY != -1) {
                    // 一个像素对应多少滚动偏移量
                    double scale = Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine) / (this.outScrollHeight - 2);
                    this.setScrollOffset(this.scrollOffsetOld + (mouseY - this.mouseDownY) * scale);
                }
            }
        });
        // 物品按钮
        ITEM_BUTTONS.forEach(bt -> bt.setHovered(bt.isMouseOverEx(mouseX, mouseY)));
        super.mouseMoved(mouseX, mouseY);
    }
    /**
     * 重写键盘事件
     */
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        keyManager.keyPressed(pKeyCode);
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (FILTER_KEY.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        } else return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        keyManager.keyReleased(keyCode);
        return super.keyReleased(keyCode, scanCode, modifiers);
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
        if (Minecraft.getInstance().player != null) {
            this.itemList.clear();
            CompoundTag filters = Minecraft.getInstance().player.getMainHandItem().getOrCreateTag().getCompound("filters");
            filters.getAllKeys().forEach(key -> {
                this.itemList.add(ItemStack.of((CompoundTag) filters.get(key)));
            });
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

        // 初始化操作按钮

        // 滚动条
        this.OP_BUTTONS.put(OperationButtonType.SLIDER.getCode(), new OperationButton(OperationButtonType.SLIDER.getCode(), context -> {
            // 背景宽高
            double bgWidth = (GuiUtils.ITEM_ICON_SIZE + margin) * itemPerLine;
            double bgHeight = (GuiUtils.ITEM_ICON_SIZE + margin) * maxLine - margin;
            // 绘制滚动条
            this.outScrollX = itemBgX + bgWidth + 2;
            this.outScrollY = itemBgY - this.margin + 1;
            this.outScrollWidth = 5;
            this.outScrollHeight = (int) (bgHeight + this.margin + 1);
            // 滚动条百分比
            double inScrollWidthScale = itemList.size() > itemPerLine * maxLine ? (double) itemPerLine * maxLine / itemList.size() : 1;
            // 多出来的行数
            double outLine = Math.max((int) Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine), 0);
            // 多出来的每行所占的空余条长度
            double outCellHeight = outLine == 0 ? 0 : (1 - inScrollWidthScale) * (outScrollHeight - 2) / outLine;
            // 滚动条上边距长度
            double inScrollTopHeight = this.getScrollOffset() * outCellHeight;
            // 滚动条高度
            this.inScrollHeight = Math.max(2, (outScrollHeight - 2) * inScrollWidthScale);
            this.inScrollY = outScrollY + inScrollTopHeight + 1;
            // 绘制滚动条外层背景
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) this.outScrollY, this.outScrollWidth, this.outScrollHeight, 0xCC232323);
            // 绘制滚动条滑块
            int color = context.button().isHovered() ? 0xCCFFFFFF : 0xCC8B8B8B;
            GuiUtils.fill(context.graphics(), (int) this.outScrollX, (int) Math.ceil(this.inScrollY), this.outScrollWidth, (int) this.inScrollHeight, color);
            context.button().setX(this.outScrollX).setY(this.outScrollY).setWidth(this.outScrollWidth).setHeight(this.outScrollHeight);
        }));

        // 物品列表
        this.ITEM_BUTTONS.clear();
        for (int i = 0; i < maxLine; i++) {
            for (int j = 0; j < itemPerLine; j++) {
                ITEM_BUTTONS.add(new OperationButton(itemPerLine * i + j, context -> {
                    int i1 = context.button().getOperation() / itemPerLine;
                    int j1 = context.button().getOperation() % itemPerLine;
                    int index = ((itemList.size() > itemPerLine * maxLine ? this.getScrollOffset() : 0) + i1) * itemPerLine + j1;
                    if (index >= 0 && index < itemList.size()) {
                        ItemStack itemStack = itemList.get(index);
                        // 物品图标在弹出层中的 x 位置
                        double itemX = itemBgX + j1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                        // 物品图标在弹出层中的 y 位置
                        double itemY = itemBgY + i1 * (GuiUtils.ITEM_ICON_SIZE + margin);
                        // 绘制背景
                        int bgColor;
                        if (context.button().isHovered()
                                || ItemUtils.getId(itemStack).equalsIgnoreCase(this.getSelectedItemId())
                        ) {
                            bgColor = 0xEE7CAB7C;
                        } else {
                            bgColor = 0xEE707070;
                        }
                        context.button().setX(itemX - 1).setY(itemY - 1).setWidth(GuiUtils.ITEM_ICON_SIZE + 2).setHeight(GuiUtils.ITEM_ICON_SIZE + 2)
                                .setId(ItemUtils.getId(itemStack));

                        GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), bgColor);
                        context.graphics().renderItem(itemStack, (int) context.button().getX() + 1, (int) context.button().getY() + 1);
                        // 绘制物品详情悬浮窗
                        context.button().setCustomPopupFunction(() -> {
                            if (context.button().isHovered()) {
                                List<Component> list = itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
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
                                context.graphics().renderTooltip(font, list1, itemStack.getTooltipImage(), itemStack, (int) context.keyManager().getMouseX(), (int) context.keyManager().getMouseY());
                            }
                        });
                    } else {
                        context.button().setX(0).setY(0).setWidth(0).setHeight(0).setId("");
                    }
                }));
            }
        }
    }

    /**
     * 绘制按钮
     */
    private void renderButton(GuiGraphics graphics) {
        for (OperationButton button : OP_BUTTONS.values()) button.render(graphics, keyManager);
        for (OperationButton button : ITEM_BUTTONS) button.render(graphics, keyManager);
        for (OperationButton button : OP_BUTTONS.values())
            button.renderPopup(graphics, this.font, keyManager);
        for (OperationButton button : ITEM_BUTTONS)
            button.renderPopup(graphics, this.font, keyManager);
    }


}
