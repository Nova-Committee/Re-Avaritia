package committee.nova.mods.avaritia.api.client.screen;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButton;
import committee.nova.mods.avaritia.api.client.screen.component.OperationButtonType;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/25 20:09
 * @Description: from <a href="https://github.com/TinyTsuki/SakuraSignIn_MC">...</a>
 */
public class EntitySelectScreen extends Screen {
    private static final Quaternionf ARMOR_STAND_ANGLE = (new Quaternionf()).rotationXYZ(0.43633232F, 0.0F, (float)Math.PI);
    private static final Logger LOGGER = LogManager.getLogger();
    // 每行显示数量
    private final int itemPerLine = 5;
    // 每页显示行数
    private final int maxLine = 5;

    /**
     * 父级 Screen
     */
    private final Screen previousScreen;
    /**
     * 输入数据回调1
     */
    private final Consumer<EntityType<?>> onDataReceived1;
    /**
     * 是否要显示该界面, 若为false则直接关闭当前界面并返回到调用者的 Screen
     */
    private final Supplier<Boolean> shouldClose;

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
    private final List<EntityType<?>> itemList = new ArrayList<>();
    /**
     * 操作按钮
     */
    private final Map<Integer, OperationButton> OP_BUTTONS = new HashMap<>();
    /**
     * 物品按钮
     */
    private final List<OperationButton> ITEM_BUTTONS = new ArrayList<>();
    /**
     * 当前选择的物品 ID
     */
    @Getter
    private String selectedEntityId;
    /**
     * 当前选择的物品
     */
    private EntityType<?> currentEntity;


    private int bgX;
    private int bgY;
    private final double margin = 3;
    private double itemBgX = this.bgX + margin;
    private double itemBgY = this.bgY + 20;

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

    public EntitySelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<EntityType<?>> onDataReceived
                            ) {
        this(callbackScreen, onDataReceived, null);
    }

    public EntitySelectScreen(@NonNull Screen callbackScreen, @NonNull Consumer<EntityType<?>> onDataReceived,
                             Supplier<Boolean> shouldClose

    ) {
        super(Component.literal("SelectScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.currentEntity = EntityType.BEE;
        this.selectedEntityId = EntityType.BEE.getDescriptionId();
        this.shouldClose = shouldClose;
    }

    @Override
    protected void init() {
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get()))
            Minecraft.getInstance().setScreen(previousScreen);
        this.updateSearchResults();
        this.updateLayout();
        // 创建文本输入框
        this.inputField = GuiUtils.newTextFieldWidget(this.font, bgX, bgY, 180, 15, Component.literal(""));
        this.inputField.setValue(this.inputFieldText);
        this.addRenderableWidget(this.inputField);
        // 创建提交按钮
        this.addRenderableWidget(GuiUtils.newButton((int) (this.bgX + 90 + this.margin), (int) (this.bgY + (20 + (GuiUtils.ENTITY_ICON_SIZE + 3) * 5 + margin))
                , (int) (90 - this.margin * 2), 20
                , GuiUtils.textToComponent(Text.i18n("提交")), button -> {
                    if (this.currentEntity == null) {
                        // 关闭当前屏幕并返回到调用者的 Screen
                        Minecraft.getInstance().setScreen(previousScreen);
                    } else {
                        // 获取选择的数据，并执行回调
                        if (onDataReceived1 != null) {
                            onDataReceived1.accept(this.currentEntity);
                            Minecraft.getInstance().setScreen(previousScreen);
                        }
                    }
                }));
        // 创建取消按钮
        this.addRenderableWidget(GuiUtils.newButton((int) (this.bgX + this.margin), (int) (this.bgY + (20 + (GuiUtils.ENTITY_ICON_SIZE + 3) * 5 + margin))
                , (int) (90 - this.margin * 2), 20
                , GuiUtils.textToComponent(Text.i18n("取消"))
                , button -> Minecraft.getInstance().setScreen(previousScreen)));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 绘制背景
        this.renderBackground(graphics, mouseX, mouseY, delta);
        GuiUtils.fill(graphics, (int) (this.bgX - this.margin), (int) (this.bgY - this.margin), (int) (180 + this.margin * 2), (int) (20 + (GuiUtils.ENTITY_ICON_SIZE + 3) * 5 + 20 + margin * 2 + 5), 0xCCC6C6C6, 2);
        GuiUtils.fillOutLine(graphics, (int) (this.itemBgX - this.margin), (int) (this.itemBgY - this.margin), (int) ((GuiUtils.ENTITY_ICON_SIZE + this.margin) * this.itemPerLine + this.margin), (int) ((GuiUtils.ENTITY_ICON_SIZE + this.margin) * this.maxLine + this.margin), 1, 0xFF000000, 1);
        super.render(graphics, mouseX, mouseY, delta);
        // 保存输入框的文本, 防止窗口重绘时输入框内容丢失
        this.inputFieldText = this.inputField.getValue();

        this.renderButton(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.setScrollOffset(this.getScrollOffset() - scrollY);
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
        return flag.get() ? flag.get() : super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicBoolean updateSearchResults = new AtomicBoolean(false);
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
            if (updateSearchResults.get()) {
                this.updateSearchResults();
            }
        }
        return flag.get() ? flag.get() : super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.inputField.isFocused())) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.inputField.isFocused()) {
            this.updateSearchResults();
            // this.updateLayout();
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

    private List<EntityType<?>> getAllItemList() {
        return new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.stream().toList());
    }

    /**
     * 设置排列方式
     */
    private void updateLayout() {
        this.bgX = this.width / 2 - 92;
        this.bgY = this.height / 2 - 65;
        this.itemBgX = this.bgX + margin;
        this.itemBgY = this.bgY + 20;
        // 滚动条
        this.OP_BUTTONS.put(OperationButtonType.SLIDER.getCode(), new OperationButton(OperationButtonType.SLIDER.getCode(), context -> {
            // 背景宽高
            double bgWidth = (GuiUtils.ENTITY_ICON_SIZE + margin) * itemPerLine;
            double bgHeight = (GuiUtils.ENTITY_ICON_SIZE + margin) * maxLine - margin;
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
                        EntityType<?> entityType = itemList.get(index);
                        // 物品图标在弹出层中的 x 位置
                        double itemX = itemBgX + j1 * (GuiUtils.ENTITY_ICON_SIZE + margin);
                        // 物品图标在弹出层中的 y 位置
                        double itemY = itemBgY + i1 * (GuiUtils.ENTITY_ICON_SIZE + margin);
                        // 绘制背景
                        int bgColor;
                        if (context.button().isHovered() || entityType.getDescriptionId().equalsIgnoreCase(this.getSelectedEntityId().toString())) {
                            bgColor = 0xEE7CAB7C;
                        } else {
                            bgColor = 0xEE707070;
                        }
                        context.button().setX(itemX - 1).setY(itemY - 1).setWidth(GuiUtils.ENTITY_ICON_SIZE + 2).setHeight(GuiUtils.ENTITY_ICON_SIZE + 2)
                                .setId(entityType.getDescriptionId());

                        GuiUtils.fill(context.graphics(), (int) context.button().getX(), (int) context.button().getY(), (int) context.button().getWidth(), (int) context.button().getHeight(), bgColor);
                        //InventoryScreen.renderEntityInInventory(context.graphics(), (int) context.button().getX() + 1, (int) context.button().getY() + 1, 50, ARMOR_STAND_ANGLE, (Quaternionf) null, (LivingEntity) entityType.create(Minecraft.getInstance().level));
                        //context.graphics().renderItem(itemStack, (int) context.button().getX() + 1, (int) context.button().getY() + 1);
                        // 绘制物品详情悬浮窗
                        context.button().setCustomPopupFunction(() -> {
                            if (context.button().isHovered()) {
                                List<Component> list1 = Lists.newArrayList(entityType.getDescription());
                                
                               // context.graphics().renderTooltip(font, list1, itemStack.getTooltipImage(), itemStack, (int) context.mouseX(), (int) context.mouseY());
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
     * 更新搜索结果
     */
    private void updateSearchResults() {
        String s = this.inputField == null ? null : this.inputField.getValue();
        this.itemList.clear();
        if (StringUtils.isNotNullOrEmpty(s)) {
//            SearchTree<EntityType<?>> isearchtree = Minecraft.getInstance().se(ModSearches.LIVING_ENTITY_KEY);
//            this.itemList.addAll(isearchtree.search(s.toLowerCase(Locale.ROOT)));
        } else {
            this.itemList.addAll(new ArrayList<>(this.getAllItemList()));
        }
        this.setScrollOffset(0);
    }


    private void setScrollOffset(double offset) {
        this.scrollOffset = (int) Math.max(Math.min(offset, (int) Math.ceil((double) (itemList.size() - itemPerLine * maxLine) / itemPerLine)), 0);
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
            this.selectedEntityId = bt.getId();
            if (StringUtils.isNotNullOrEmpty(this.selectedEntityId)) {
                //this.currentEntity = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(selectedEntityId));
                //LOGGER.debug("Select item: {}", ItemRewardParser.getDisplayName(this.currentItem));
                flag.set(true);

            }
        }
    }
}
