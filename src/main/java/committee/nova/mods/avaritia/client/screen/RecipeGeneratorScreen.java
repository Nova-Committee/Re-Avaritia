package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.ItemSelectScreen;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import committee.nova.mods.avaritia.util.KubeJsUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorScreen extends BaseContainerScreen<RecipeGeneratorMenu> {
    private boolean shaped = false; // 无序有序
    private int tier = 1; // 等级
    private int outType = 1; // 生成方式
    private boolean selectMode = false; // 模式
    private ItemStack brushItem = ItemStack.EMPTY; // 画刷物品
    private int selectedSlot = -1; // 当前选择的槽位索引

    private Button brushButton; // 当前选择的槽位索引

    public RecipeGeneratorScreen(RecipeGeneratorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.RECIPE_GENERATOR_TEX, 223, 234);
    }


    @Override
    protected void subInit() {
        super.subInit();
        int centerX = (this.width - this.imageWidth) / 2;
        int centerY = (this.height - this.imageHeight) / 2;
        // 添加选择模式切换按钮
        this.addRenderableWidget(
                GuiUtils.newButton(centerX + 5, centerY + 185, 40, 15,
                        Component.literal(this.selectMode ? "选择" : "画刷"),
                        button -> {
                            this.selectMode = !this.selectMode;
                            updateButtonVisibility();
                            button.setMessage(Component.literal(this.selectMode ? "选择" : "画刷"));
                        })
        );
        // 添加有序无序按钮
        this.addRenderableWidget(
                GuiUtils
                        .newButton(centerX + 45, centerY + 185, 40, 15,
                                Component.literal(this.shaped ? "有序" : "无序"),
                                button -> {
                                    this.shaped = !this.shaped;
                                    button.setMessage(Component.literal(this.shaped ? "有序" : "无序"));
                                })
        );
        // 添加等级选择按钮
        this.addRenderableWidget(
                CycleButton.builder(Component::literal)
                        .withValues("1", "2", "3", "4")
                        .withInitialValue(String.valueOf(this.tier + 1))
                        .create(centerX + 85, centerY + 185, 40, 15,
                                Component.literal("等级"),
                                (button, value) -> {
                                    this.tier = Integer.parseInt(value) - 1;
                                })
        );
        // 添加生成方式选择按钮
        this.addRenderableWidget(
                CycleButton.builder((String value) -> {
                            if (value.equals("1")) {
                                return Component.literal("KubeJs");
                            } else if (value.equals("2")) {
                                return Component.literal("Crt");
                            } else {
                                return Component.literal("Json");
                            }
                        })
                        .withValues("1", "2", "3")
                        .withInitialValue(String.valueOf(this.outType + 1))
                        .create(centerX + 125, centerY + 185, 60, 15,
                                Component.literal("方式"),
                                (button, value) -> {
                                    this.outType = Integer.parseInt(value) - 1;
                                })
        );
        // 添加生成按钮
        this.addRenderableWidget(
                GuiUtils
                        .newButton(centerX + 185, centerY + 185, 40, 15,
                                Component.literal("生成"),
                                button -> this.generateKubeJSRecipe())
        );

        this.brushButton = GuiUtils.newButton(centerX + 182, centerY + 69, 20, 20,
                brushItem.getDisplayName(),
                button -> {
                    this.minecraft.setScreen(new ItemSelectScreen(
                            this,
                            (itemStack) -> {
                                // 将选中的物品放入对应的槽位
                                this.brushItem = itemStack;
                            },
                            ItemStack.EMPTY
                    ));
                });
        this.addRenderableWidget(this.brushButton);
    }

    private void updateButtonVisibility() {
        this.brushButton.visible = !this.selectMode;
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pX, int pY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        // 渲染槽位选择指示器
        if (this.selectedSlot >= 0 && this.selectedSlot < 82) {
            int slotX, slotY;
            if (this.selectedSlot < 81) {
                // 输入槽位
                int row = this.selectedSlot / 9;
                int col = this.selectedSlot % 9;
                slotX = this.leftPos + 8 + col * 18 - 1;
                slotY = this.topPos + 18 + row * 18 - 1;
            } else {
                // 输出槽位
                slotX = this.leftPos + 202 - 1;
                slotY = this.topPos + 89 - 1;
            }
            // 绘制选择指示器 (绿色边框)
            pGuiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF00FF00);
            pGuiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF00FF00);
            pGuiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF00FF00);
            pGuiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF00FF00);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否点击了槽位
        if (selectMode) {
            if (button == 0) { // 左键点击
                // 检查输入槽位区域 (9x9网格)
                int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
                int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

                if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
                    // 点击了输入槽位
                    int slotIndex = gridY * 9 + gridX;
                    this.selectedSlot = slotIndex;
                    this.openItemSelectScreen(slotIndex);
                    return true;
                }

                // 检查输出槽位
                int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
                int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

                if (outputX == 0 && outputY == 0) {
                    // 点击了输出槽位
                    this.selectedSlot = 81;
                    this.openItemSelectScreen(81);
                    return true;
                }
            } else if (button == 1) { // 右键点击  清除选择
                // 检查输入槽位区域 (9x9网格)
                int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
                int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

                if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
                    // 点击了输入槽位
                    int slotIndex = gridY * 9 + gridX;
                    if (!this.menu.getSlotItem(slotIndex).isEmpty()) this.menu.getSlot(slotIndex).set(ItemStack.EMPTY);
                    return true;
                }
                // 检查输出槽位
                int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
                int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

                if (outputX == 0 && outputY == 0) {
                    // 点击了输出槽位
                    this.selectedSlot = 81;
                    if (!this.menu.getSlotItem(81).isEmpty()) this.menu.getSlot(81).set(ItemStack.EMPTY);
                    return true;
                }
            }
        } else {
            if (button == 0) {
                // 检查输入槽位区域 (9x9网格)
                int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
                int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

                if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
                    // 点击了输入槽位
                    int slotIndex = gridY * 9 + gridX;
                    this.selectedSlot = slotIndex;
                    this.menu.getSlot(slotIndex).set(this.brushItem.copy());
                    return true;
                }

                // 检查输出槽位
                int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
                int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

                if (outputX == 0 && outputY == 0) {
                    // 点击了输出槽位
                    this.selectedSlot = 81;
                    this.openItemSelectScreen(81);
                    return true;
                }
            } else if (button == 1) {
                // 检查输入槽位区域 (9x9网格)
                int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
                int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

                if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
                    // 点击了输入槽位
                    int slotIndex = gridY * 9 + gridX;
                    if (!this.menu.getSlotItem(slotIndex).isEmpty()) this.menu.getSlot(slotIndex).set(ItemStack.EMPTY);
                    return true;
                }
                // 检查输出槽位
                int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
                int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

                if (outputX == 0 && outputY == 0) {
                    // 点击了输出槽位
                    this.selectedSlot = 81;
                    if (!this.menu.getSlotItem(81).isEmpty()) this.menu.getSlot(81).set(ItemStack.EMPTY);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openItemSelectScreen(int slotIndex) {
        ItemStack defaultItem = this.menu.getSlotItem(slotIndex);
        if (defaultItem.isEmpty()) {
            defaultItem = new ItemStack(Items.AIR);
        }

        this.minecraft.setScreen(new ItemSelectScreen(
                this,
                (itemStack) -> {
                    // 将选中的物品放入对应的槽位
                    this.menu.getSlot(slotIndex).set(itemStack.copy());
                },
                defaultItem
        ));
    }

    private void generateKubeJSRecipe() {
        // 使用KubeJsUtils生成代码
        if (!this.menu.slots.isEmpty()
                && !this.menu.getSlotItem(81).isEmpty()
        ) KubeJsUtils.exportJSRecipe(this.menu, this.shaped, this.tier, true, "generated_recipe");
    }

}
