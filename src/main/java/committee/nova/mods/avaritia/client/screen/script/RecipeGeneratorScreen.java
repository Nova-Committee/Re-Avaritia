package committee.nova.mods.avaritia.client.screen.script;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.ItemSelectScreen;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorScreen extends BaseContainerScreen<RecipeGeneratorMenu> {
    // UI状态
    private RecipeType type = RecipeType.VALLIA_SHAPED; // 配方类型
    private int tier = 1; // 等级 (1-4)
    private int outType = 1; // 生成方式
    private boolean selectMode = false; // 模式
    private ItemStack brushItem = ItemStack.EMPTY; // 画刷物品
    private int selectedSlot = -1; // 当前选择的槽位索引

    // 增强功能相关
    private List<ScriptFile> scriptFiles = new ArrayList<>();
    private ScriptFile selectedFile = null;
    private ScriptEntry selectedScript = null;
    private List<ScriptEntry> currentScripts = new ArrayList<>();
    private String currentScriptName = "";
    private String currentScriptContent = "";

    // 滚动相关
    private int fileScrollOffset = 0;
    private int scriptScrollOffset = 0;

    // 按钮和组件
    private Button brushButton; // 画刷按钮
    private CycleButton<String> tierButton; // 等级按钮
    private CycleButton<String> typeButton; // 类型按钮
    private Button saveButton; // 保存按钮
    private Button applyButton; // 应用按钮

    // UI布局参数
    private static final int FILE_LIST_WIDTH = 40;
    private static final int SCRIPT_LIST_WIDTH = 40;
    private static final int EDITOR_WIDTH = 220;
    private static final int EDITOR_HEIGHT = 215;
    private static final int LIST_ITEM_HEIGHT = 15;

    private int bgX;
    private int bgY;
    private int scriptStartX;
    private int editorStartX;

    public RecipeGeneratorScreen(RecipeGeneratorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.RECIPE_GENERATOR_TEX, 300, 215, 384, 384);
        this.loadScriptFiles();
    }

    @Override
    protected void subInit() {
        this.updateLayout();
        // 添加选择模式切换按钮
        this.addRenderableWidget(createModeButton(bgX, bgY));

        // 添加类型选择按钮
        this.typeButton = this.addRenderableWidget(createTypeButton(bgX, bgY));

        // 添加等级选择按钮
        this.tierButton = this.addRenderableWidget(createTierButton(bgX, bgY));

        // 添加格式选择按钮
        this.addRenderableWidget(createFormatButton(bgX, bgY));

        // 添加保存按钮（替代生成按钮）
        this.saveButton = this.addRenderableWidget(createSaveButton(bgX, bgY));

        // 添加应用按钮
        this.applyButton = this.addRenderableWidget(createApplyButton(bgX, bgY));

        // 添加配方选择按钮
        this.addRenderableWidget(createRecipeSelectButton(bgX, bgY));

        this.brushButton = this.addRenderableWidget(createBrushButton(bgX, bgY));
        updateButtonVisibility();
    }

    private void updateLayout() {
        this.bgX = (this.width - this.imageWidth) / 2;
        this.bgY = (this.height - this.imageHeight) / 2;
        this.scriptStartX = this.bgX + FILE_LIST_WIDTH;
        this.editorStartX = this.bgX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH;
    }

    // 创建模式切换按钮
    private Button createModeButton(int centerX, int centerY) {
        return GuiUtils.newButton(editorStartX + 2, centerY +  + 185, 40, 15,
                Component.translatable(this.selectMode ? "gui.avaritia.recipe_generator.select" : "gui.avaritia.recipe_generator.brush"),
                button -> {
                    this.selectMode = !this.selectMode;
                    updateButtonVisibility();
                    button.setMessage(Component.translatable(this.selectMode ? "gui.avaritia.recipe_generator.select" : "gui.avaritia.recipe_generator.brush"));
                });
    }

    // 创建类型选择按钮
    private CycleButton<String> createTypeButton(int centerX, int centerY) {
        return CycleButton.builder(Component::literal)
                .withValues(
                        RecipeType.VALLIA_SHAPED.name(),
                        RecipeType.VALLIA_SHAPELESS.name(),
                        RecipeType.VALLIA_SMITHING.name(),
                        RecipeType.VALLIA_SMELTING.name(),
                        RecipeType.VALLIA_BLASTING.name(),
                        RecipeType.AVARITIA_SHAPED.name(),
                        RecipeType.AVARITIA_SHAPELESS.name(),
                        RecipeType.AVARITIA_SMITHING.name(),
                        RecipeType.AVARITIA_COMPRESSOR.name()
                )
                .withInitialValue(this.type.name())
                .create(editorStartX + 42, centerY + 185, 40, 15,
                        Component.translatable("gui.avaritia.recipe_generator.type"),
                        (button, value) -> {
                            this.type = RecipeType.valueOf(value);
                            updateButtonVisibility();
                        });
    }

    // 创建等级选择按钮
    private CycleButton<String> createTierButton(int centerX, int centerY) {
        return CycleButton.builder(Component::literal)
                .withValues("1", "2", "3", "4")
                .withInitialValue(String.valueOf(this.tier))
                .create(editorStartX + 82, centerY + 185, 40, 15,
                        Component.translatable("gui.avaritia.recipe_generator.tier"),
                        (button, value) -> this.tier = Integer.parseInt(value));
    }

    // 创建格式选择按钮
    private CycleButton<String> createFormatButton(int centerX, int centerY) {
        return CycleButton.builder((String value) -> {
                    if (value.equals("1")) {
                        return Component.literal("KubeJs");
                    } else if (value.equals("2")) {
                        return Component.literal("Crt");
                    } else {
                        return Component.literal("Json");
                    }
                })
                .withValues("1", "2", "3")
                .withInitialValue(String.valueOf(this.outType))
                .create(editorStartX + 122, centerY + 185, 60, 15,
                        Component.translatable("gui.avaritia.recipe_generator.type"),
                        (button, value) -> this.outType = Integer.parseInt(value));
    }

    // 创建保存按钮
    private Button createSaveButton(int centerX, int centerY) {
        return GuiUtils.newButton(editorStartX + 182, centerY + 170, 40, 15,
                Component.translatable("gui.avaritia.save"),
                button -> this.saveScript());
    }

    // 创建应用按钮
    private Button createApplyButton(int centerX, int centerY) {
        return GuiUtils.newButton(editorStartX + 182, centerY + 185, 40, 15,
                Component.translatable("gui.avaritia.apply"),
                button -> this.applyScript());
    }

    // 创建配方选择按钮
    private Button createRecipeSelectButton(int centerX, int centerY) {
        return GuiUtils.newButton(editorStartX + 202, centerY + 10, 20, 20,
                Component.translatable("gui.avaritia.recipe_generator.select_recipe"),
                button -> this.openRecipeSelectScreen());
    }

    // 创建画刷按钮
    private Button createBrushButton(int centerX, int centerY) {
        return GuiUtils.newButton(editorStartX + 202, centerY + 30, 20, 20,
                brushItem.getDisplayName(),
                button -> this.minecraft.setScreen(new ItemSelectScreen(
                        this,
                        (itemStack) -> this.brushItem = itemStack,
                        ItemStack.EMPTY
                )));
    }

    private void updateButtonVisibility() {
        this.brushButton.visible = !this.selectMode;
        // 根据配方类型决定是否显示等级按钮
        if (this.tierButton != null) {
            this.tierButton.visible = (this.type == RecipeType.AVARITIA_SHAPED || this.type == RecipeType.AVARITIA_SHAPELESS);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pX, int pY) {
        if (!this.selectMode)
            pGuiGraphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_generator.brush"), 180, 36, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        // 绘制背景
        //graphics.fill(bgX, bgY, bgX + 500, bgY + 250, 0xFFC6C6C6);
        //graphics.fill(bgX + 1, bgY + 1, bgX + 499, bgY + 249, 0xFFAAAAAA);

        // 绘制分割线
        //graphics.fill(bgX + FILE_LIST_WIDTH, bgY, bgX + FILE_LIST_WIDTH + 1, bgY + 250, 0xFF000000);
        //graphics.fill(bgX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH, bgY, bgX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1, bgY + 250, 0xFF000000);
        //graphics.fill(bgX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + EDITOR_WIDTH, bgY, bgX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + EDITOR_WIDTH + 1, bgY + 250, 0xFF000000);

        // 绘制各区域标题
        graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_generator.files"), bgX + 2, bgY + 5, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_generator.scripts"), this.scriptStartX + 2, bgY + 5, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_generator.editor"), this.editorStartX + 2, bgY + 5, 0x404040, false);

        // 渲染文件列表
        renderFileList(graphics, bgX, bgY, mouseX, mouseY);

        // 渲染脚本列表
        renderScriptList(graphics, bgX, bgY, mouseX, mouseY);

        // 渲染编辑器
        renderEditor(graphics, bgX, bgY, mouseX, mouseY);

        // 渲染槽位选择指示器
        renderSlotSelectionIndicator(graphics);

        // 渲染不可用槽位的遮罩
        renderDisabledSlotsOverlay(graphics);
    }

    // 渲染文件列表
    private void renderFileList(GuiGraphics graphics, int baseX, int baseY, int mouseX, int mouseY) {
        int listStartY = baseY + 20;
        int maxVisibleItems = (230) / LIST_ITEM_HEIGHT;

        for (int i = 0; i < Math.min(maxVisibleItems, this.scriptFiles.size() - this.fileScrollOffset); i++) {
            int index = this.fileScrollOffset + i;
            if (index < this.scriptFiles.size()) {
                ScriptFile file = this.scriptFiles.get(index);
                int itemY = listStartY + i * LIST_ITEM_HEIGHT;

                // 绘制背景
                int bgColor = (file == this.selectedFile) ? 0xFF7CAB7C : 0xFF707070;
                if (mouseX >= baseX && mouseX < baseX + FILE_LIST_WIDTH &&
                        mouseY >= itemY && mouseY < itemY + LIST_ITEM_HEIGHT) {
                    bgColor = 0xFFAAAAAA;
                }
                graphics.fill(baseX + 2, itemY, baseX + FILE_LIST_WIDTH - 2, itemY + LIST_ITEM_HEIGHT, bgColor);

                // 绘制文件名
                String fileName = file.getFile().getName();
                if (fileName.length() > 12) {
                    fileName = fileName.substring(0, 9) + "...";
                }
                graphics.drawString(this.font, fileName, baseX + 5, itemY + 3, 0xFFFFFF, false);
            }
        }
    }

    // 渲染脚本列表
    private void renderScriptList(GuiGraphics graphics, int baseX, int baseY, int mouseX, int mouseY) {
        int listStartX = baseX + FILE_LIST_WIDTH + 1;
        int listStartY = baseY + 20;
        int maxVisibleItems = (230) / LIST_ITEM_HEIGHT;

        for (int i = 0; i < Math.min(maxVisibleItems, this.currentScripts.size() - this.scriptScrollOffset); i++) {
            int index = this.scriptScrollOffset + i;
            if (index < this.currentScripts.size()) {
                ScriptEntry script = this.currentScripts.get(index);
                int itemY = listStartY + i * LIST_ITEM_HEIGHT;

                // 绘制背景
                int bgColor = (script == this.selectedScript) ? 0xFF7CAB7C : 0xFF707070;
                if (mouseX >= listStartX && mouseX < listStartX + SCRIPT_LIST_WIDTH &&
                        mouseY >= itemY && mouseY < itemY + LIST_ITEM_HEIGHT) {
                    bgColor = 0xFFAAAAAA;
                }
                graphics.fill(listStartX + 2, itemY, listStartX + SCRIPT_LIST_WIDTH - 2, itemY + LIST_ITEM_HEIGHT, bgColor);

                // 绘制脚本名
                String scriptName = script.getName();
                if (scriptName.length() > 12) {
                    scriptName = scriptName.substring(0, 9) + "...";
                }
                graphics.drawString(this.font, scriptName, listStartX + 5, itemY + 3, 0xFFFFFF, false);
            }
        }
    }

    // 渲染编辑器
    private void renderEditor(GuiGraphics graphics, int baseX, int baseY, int mouseX, int mouseY) {
        int editorStartX = baseX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1;
        int editorStartY = baseY + 20;

        // 如果有选中的脚本，显示其内容
        if (this.selectedScript != null) {
            // 显示脚本名称
            graphics.drawString(this.font, this.selectedScript.getName(), editorStartX + 5, editorStartY + 5, 0xFFFFFF, false);

            // 显示配方类型
            graphics.drawString(this.font,
                    Component.translatable("gui.avaritia.recipe_generator.recipe_type", this.selectedScript.getRecipeType().name()),
                    editorStartX + 5, editorStartY + 20, 0xFFFFFF, false);

            // 显示等级（如果适用）
            if (isAvaritiaTableRecipe(this.selectedScript.getRecipeType())) {
                graphics.drawString(this.font,
                        Component.translatable("gui.avaritia.recipe_generator.tier", this.selectedScript.getTier()),
                        editorStartX + 5, editorStartY + 35, 0xFFFFFF, false);
            }

            // 显示配方预览
            if (this.selectedScript.getRecipe() != null) {
                renderRecipePreview(graphics, editorStartX + 5, editorStartY + 50, this.selectedScript.getRecipe());
            }
        } else {
            // 显示提示信息
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.avaritia.recipe_generator.select_script"),
                    editorStartX + EDITOR_WIDTH / 2, editorStartY + EDITOR_HEIGHT / 2, 0xAAAAAA);
        }
    }

    // 渲染配方预览
    private void renderRecipePreview(GuiGraphics graphics, int startX, int startY, Recipe<?> recipe) {
        // 绘制输出物品
        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        if (!result.isEmpty()) {
            graphics.renderItem(result, startX, startY);
            graphics.drawString(this.font, result.getDisplayName(), startX + 20, startY + 5, 0xFFFFFF, false);
        }

        // 绘制配方网格预览
        renderRecipeGridPreview(graphics, startX, startY + 25, recipe);
    }

    // 渲染配方网格预览
    private void renderRecipeGridPreview(GuiGraphics graphics, int startX, int startY, Recipe<?> recipe) {
        int gridSize = 3; // 默认3x3网格

        if (recipe instanceof ShapedTableCraftingRecipe) {
            gridSize = getGridSizeForTier(((ShapedTableCraftingRecipe) recipe).getTier());
        } else if (recipe instanceof ShapelessTableCraftingRecipe) {
            gridSize = getGridSizeForTier(((ShapelessTableCraftingRecipe) recipe).getTier());
        }

        // 绘制网格背景
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int cellX = startX + x * 18;
                int cellY = startY + y * 18;
                graphics.fill(cellX, cellY, cellX + 16, cellY + 16, 0xFF555555);
            }
        }

        // 填充配方内容（简化实现）
        // 实际实现中需要根据具体配方类型绘制材料
    }

    // 渲染槽位选择指示器
    private void renderSlotSelectionIndicator(GuiGraphics graphics) {
        if (this.selectedSlot >= 0 && this.selectedSlot < 82) {
            int baseX = (this.width - 500) / 2 + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1;
            int baseY = (this.height - 250) / 2;
            int slotX, slotY;
            if (this.selectedSlot < 81) {
                // 输入槽位
                int row = this.selectedSlot / 9;
                int col = this.selectedSlot % 9;
                slotX = baseX + 8 + col * 18 - 1;
                slotY = baseY + 18 + row * 18 - 1;
            } else {
                // 输出槽位
                slotX = baseX + 202 - 1;
                slotY = baseY + 89 - 1;
            }
            // 绘制选择指示器 (绿色边框)
            graphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF00FF00);
            graphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF00FF00);
            graphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF00FF00);
            graphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF00FF00);
        }
    }

    // 渲染不可用槽位的遮罩
    private void renderDisabledSlotsOverlay(GuiGraphics graphics) {
        int baseX = this.editorStartX - 1;
        int baseY = this.bgY;

        Set<Integer> availableSlots = this.getAvailableSlotsForType(this.type, this.tier);
        for (int i = 0; i < 81; i++) {
            if (!availableSlots.contains(i)) {
                // 槽位不可用，绘制半透明遮罩
                int row = i / 9;
                int col = i % 9;
                int slotX = baseX + 8 + col * 18;
                int slotY = baseY + 18 + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000); // 半透明黑色
            }
        }
    }

    // 根据配方类型和等级获取可用槽位
    private Set<Integer> getAvailableSlotsForType(RecipeType type, int tier) {
        Set<Integer> availableSlots = new HashSet<>();

        switch (type) {
            case VALLIA_SHAPED:
            case VALLIA_SHAPELESS:
                // 原版工作台配方，使用中心3x3区域
                for (int row = 3; row <= 5; row++) {
                    for (int col = 3; col <= 5; col++) {
                        availableSlots.add(row * 9 + col);
                    }
                }
                break;

            case VALLIA_SMITHING:
                // 锻造台配方，使用特定槽位 (例如第4行的3个槽位)
                availableSlots.add(39); // 模板槽位
                availableSlots.add(40); // 基础物品槽位
                availableSlots.add(41); // 添加物品槽位
                break;

            case VALLIA_SMELTING:
            case VALLIA_BLASTING:
                // 熔炉/高炉配方，使用单个槽位 (中心位置)
                availableSlots.add(40);
                break;

            case AVARITIA_SHAPED:
            case AVARITIA_SHAPELESS:
                // 无尽工作台配方，根据等级确定可用槽位
                availableSlots.addAll(this.menu.getAvailableSlotsSetForTier(tier));
                break;

            case AVARITIA_SMITHING:
                // 无尽锻造配方，使用中心5x5区域
                for (int row = 2; row <= 6; row++) {
                    for (int col = 2; col <= 6; col++) {
                        availableSlots.add(row * 9 + col);
                    }
                }
                break;

            case AVARITIA_COMPRESSOR:
                // 压缩机配方，使用单个槽位 (中心位置)
                availableSlots.add(40);
                break;
        }

        // 输出槽位总是可用
        availableSlots.add(81);

        return availableSlots;
    }

    // 检查指定槽位对当前类型是否可用
    private boolean isSlotAvailableForCurrentType(int slotIndex) {
        if (slotIndex >= 81) return slotIndex == 81; // 输出槽位总是可用
        return this.getAvailableSlotsForType(this.type, this.tier).contains(slotIndex);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int baseX = (this.width - 500) / 2;
        int baseY = (this.height - 250) / 2;

        // 检查文件列表点击
        if (mouseX >= baseX && mouseX < baseX + FILE_LIST_WIDTH &&
                mouseY >= baseY + 20 && mouseY < baseY + 250) {
            return handleFileListClick(mouseX, mouseY, button);
        }

        // 检查脚本列表点击
        if (mouseX >= baseX + FILE_LIST_WIDTH + 1 && mouseX < baseX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH &&
                mouseY >= baseY + 20 && mouseY < baseY + 250) {
            return handleScriptListClick(mouseX, mouseY, button);
        }

        // 检查编辑器区域点击
        if (mouseX >= baseX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1 && mouseX < baseX + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + EDITOR_WIDTH &&
                mouseY >= baseY + 20 && mouseY < baseY + 250) {
            return handleEditorClick(mouseX, mouseY, button);
        }

        // 检查是否点击了槽位
        if (selectMode) {
            if (button == 0) { // 左键点击
                return handleLeftClick(mouseX, mouseY);
            } else if (button == 1) { // 右键点击清除选择
                return handleRightClick(mouseX, mouseY);
            }
        } else {
            if (button == 0) {
                return handleLeftClick(mouseX, mouseY);
            } else if (button == 1) {
                return handleRightClick(mouseX, mouseY);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // 处理文件列表点击
    private boolean handleFileListClick(double mouseX, double mouseY, int button) {
        int baseX = (this.width - 500) / 2;
        int baseY = (this.height - 250) / 2;
        int listStartY = baseY + 20;
        int maxVisibleItems = (230) / LIST_ITEM_HEIGHT;

        for (int i = 0; i < Math.min(maxVisibleItems, this.scriptFiles.size() - this.fileScrollOffset); i++) {
            int index = this.fileScrollOffset + i;
            if (index < this.scriptFiles.size()) {
                int itemY = listStartY + i * LIST_ITEM_HEIGHT;
                if (mouseY >= itemY && mouseY < itemY + LIST_ITEM_HEIGHT) {
                    this.selectedFile = this.scriptFiles.get(index);
                    this.currentScripts = this.selectedFile.getScripts();
                    this.selectedScript = null;
                    return true;
                }
            }
        }
        return false;
    }

    // 处理脚本列表点击
    private boolean handleScriptListClick(double mouseX, double mouseY, int button) {
        int baseX = (this.width - 500) / 2;
        int baseY = (this.height - 250) / 2;
        int listStartX = baseX + FILE_LIST_WIDTH + 1;
        int listStartY = baseY + 20;
        int maxVisibleItems = (230) / LIST_ITEM_HEIGHT;

        for (int i = 0; i < Math.min(maxVisibleItems, this.currentScripts.size() - this.scriptScrollOffset); i++) {
            int index = this.scriptScrollOffset + i;
            if (index < this.currentScripts.size()) {
                int itemY = listStartY + i * LIST_ITEM_HEIGHT;
                if (mouseY >= itemY && mouseY < itemY + LIST_ITEM_HEIGHT) {
                    this.selectedScript = this.currentScripts.get(index);
                    // 加载脚本到编辑器
                    loadScriptToEditor(this.selectedScript);
                    return true;
                }
            }
        }
        return false;
    }

    // 处理编辑器点击
    private boolean handleEditorClick(double mouseX, double mouseY, int button) {
        // 编辑器区域的点击处理
        return false;
    }

    // 处理左键点击
    private boolean handleLeftClick(double mouseX, double mouseY) {
        int baseX = (this.width - 500) / 2 + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1;
        int baseY = (this.height - 250) / 2;

        // 检查输入槽位区域 (9x9网格)
        int gridX = (int) ((mouseX - (baseX + 8)) / 18);
        int gridY = (int) ((mouseY - (baseY + 18)) / 18);

        if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
            // 点击了输入槽位
            int slotIndex = gridY * 9 + gridX;
            // 检查槽位是否对当前类型可用
            if (this.isSlotAvailableForCurrentType(slotIndex)) {
                this.selectedSlot = slotIndex;
                if (this.selectMode) {
                    this.openItemSelectScreen(slotIndex);
                } else {
                    this.menu.getSlot(slotIndex).set(this.brushItem.copy());
                }
                return true;
            }
        }

        // 检查输出槽位
        int outputX = (int) ((mouseX - (baseX + 202)) / 18);
        int outputY = (int) ((mouseY - (baseY + 89)) / 18);

        if (outputX == 0 && outputY == 0) {
            // 点击了输出槽位
            this.selectedSlot = 81;
            this.openItemSelectScreen(81);
            return true;
        }

        return false;
    }

    // 处理右键点击
    private boolean handleRightClick(double mouseX, double mouseY) {
        int baseX = (this.width - 500) / 2 + FILE_LIST_WIDTH + SCRIPT_LIST_WIDTH + 1;
        int baseY = (this.height - 250) / 2;

        // 检查输入槽位区域 (9x9网格)
        int gridX = (int) ((mouseX - (baseX + 8)) / 18);
        int gridY = (int) ((mouseY - (baseY + 18)) / 18);

        if (gridX >= 0 && gridX < 9 && gridY >= 0 && gridY < 9) {
            // 点击了输入槽位
            int slotIndex = gridY * 9 + gridX;
            // 检查槽位是否对当前类型可用
            if (this.isSlotAvailableForCurrentType(slotIndex)) {
                if (!this.menu.getSlotItem(slotIndex).isEmpty()) {
                    this.menu.getSlot(slotIndex).set(ItemStack.EMPTY);
                }
                return true;
            }
        }

        // 检查输出槽位
        int outputX = (int) ((mouseX - (baseX + 202)) / 18);
        int outputY = (int) ((mouseY - (baseY + 89)) / 18);

        if (outputX == 0 && outputY == 0) {
            // 点击了输出槽位
            this.selectedSlot = 81;
            if (!this.menu.getSlotItem(81).isEmpty())
                this.menu.getSlot(81).set(ItemStack.EMPTY);
            return true;
        }

        return false;
    }

    private void openItemSelectScreen(int slotIndex) {
        ItemStack defaultItem = this.menu.getSlotItem(slotIndex);
        if (defaultItem.isEmpty()) {
            defaultItem = new ItemStack(Items.AIR);
        }

        this.minecraft.setScreen(new ItemSelectScreen(
                this,
                (itemStack) -> this.menu.getSlot(slotIndex).set(itemStack.copy()),
                defaultItem
        ));
    }

    // 保存脚本
    private void saveScript() {
        if (this.selectedFile != null && this.selectedScript != null) {
            // 生成脚本内容
            String scriptContent = generateScriptContent();
            this.selectedScript.setScriptContent(scriptContent);

            // 保存到文件
            this.selectedFile.save();
        } else if (this.selectedFile != null) {
            // 创建新脚本
            String scriptName = "NewScript_" + System.currentTimeMillis();
            String scriptContent = generateScriptContent();
            ScriptEntry newScript = new ScriptEntry(scriptName, this.type, this.tier, null, scriptContent);
            this.selectedFile.addScript(newScript);
            this.selectedFile.save();
            this.currentScripts = this.selectedFile.getScripts();
        }
    }

    // 应用脚本
    private void applyScript() {
        if (this.selectedScript != null) {
            // 将脚本应用到指定位置
            // 这里应该实现具体的脚本应用逻辑
        }
    }

    // 生成脚本内容
    private String generateScriptContent() {
        StringBuilder content = new StringBuilder();

        switch (this.outType) {
            case 1: // KubeJs
                content.append("// KubeJS Recipe Script\n");
                break;
            case 2: // Crt
                content.append("// CraftTweaker Recipe Script\n");
                break;
            default:
                content.append("// Recipe Script\n");
                break;
        }

        // 添加配方信息
        content.append("// Recipe Type: ").append(this.type.name()).append("\n");
        if (isAvaritiaTableRecipe(this.type)) {
            content.append("// Tier: ").append(this.tier).append("\n");
        }

        // 添加槽位信息
        content.append("// Slots:\n");
        for (int i = 0; i < 82; i++) {
            ItemStack item = this.menu.getSlotItem(i);
            if (!item.isEmpty()) {
                content.append("// Slot ").append(i).append(": ")
                        .append(item.getItem().toString()).append("\n");
            }
        }

        return content.toString();
    }

    // 加载脚本到编辑器
    private void loadScriptToEditor(ScriptEntry script) {
        this.currentScriptName = script.getName();
        this.currentScriptContent = script.getScriptContent();
        this.type = script.getRecipeType();
        this.tier = script.getTier();

        // 更新UI组件
        if (this.typeButton != null) {
            this.typeButton.setValue(this.type.name());
        }
        if (this.tierButton != null && isAvaritiaTableRecipe(this.type)) {
            this.tierButton.setValue(String.valueOf(this.tier));
        }

        // 加载配方到槽位
        if (script.getRecipe() != null) {
            this.fillRecipeIntoSlots(script.getRecipe());
        }
    }

    // 添加配方选择相关方法
    private void openRecipeSelectScreen() {
        this.minecraft.setScreen(new RecipeSelectScreen(this, this::onRecipeSelected));
    }

    private void onRecipeSelected(Recipe<?> recipe) {
        // 将选中的配方填充到输入输出槽中
        this.fillRecipeIntoSlots(recipe);
        // 返回当前界面
        this.minecraft.setScreen(this);
    }

    private void fillRecipeIntoSlots(Recipe<?> recipe) {
        // 清空现有槽位
        for (int i = 0; i < 82; i++) {
            this.menu.getSlot(i).set(ItemStack.EMPTY);
        }

        ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
        if (!result.isEmpty()) {
            // 设置输出槽
            this.menu.getSlot(81).set(result.copy());
        }

        // 根据具体配方类型填充输入槽并更新参数
        if (recipe instanceof ShapedTableCraftingRecipe shapedRecipe) {
            fillShapedTableRecipe(shapedRecipe);
        } else if (recipe instanceof ShapelessTableCraftingRecipe shapelessRecipe) {
            fillShapelessTableRecipe(shapelessRecipe);
        } else if (recipe instanceof ShapedRecipe shapedRecipe) {
            fillVanillaShapedRecipe(shapedRecipe);
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            fillVanillaShapelessRecipe(shapelessRecipe);
        } else if (recipe instanceof ExtremeSmithingRecipe extremeSmithingRecipe) {
            fillExtremeSmithingRecipe(extremeSmithingRecipe);
        } else if (recipe instanceof SmeltingRecipe smeltingRecipe) {
            fillSmeltingRecipe(smeltingRecipe);
        } else if (recipe instanceof BlastingRecipe blastingRecipe) {
            fillBlastingRecipe(blastingRecipe);
        }

        // 更新UI组件状态
        updateButtonVisibility();
        if (this.typeButton != null) {
            this.typeButton.setValue(this.type.name());
        }
        if (this.tierButton != null && isAvaritiaTableRecipe(this.type)) {
            this.tierButton.setValue(String.valueOf(this.tier));
        }
    }

    // 填充无尽有序工作台配方
    private void fillShapedTableRecipe(ShapedTableCraftingRecipe recipe) {
        this.type = RecipeType.AVARITIA_SHAPED;
        this.tier = recipe.getTier();
        fillShapedRecipe(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
    }

    // 填充无尽无序工作台配方
    private void fillShapelessTableRecipe(ShapelessTableCraftingRecipe recipe) {
        this.type = RecipeType.AVARITIA_SHAPELESS;
        this.tier = recipe.getTier();
        fillShapelessRecipe(recipe.getIngredients());
    }

    // 填充原版有序工作台配方
    private void fillVanillaShapedRecipe(ShapedRecipe recipe) {
        this.type = RecipeType.VALLIA_SHAPED;
        this.tier = 1;
        fillShapedRecipe(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
    }

    // 填充原版无序工作台配方
    private void fillVanillaShapelessRecipe(ShapelessRecipe recipe) {
        this.type = RecipeType.VALLIA_SHAPELESS;
        this.tier = 1;
        fillShapelessRecipe(recipe.getIngredients());
    }

    // 填充无尽锻造配方
    private void fillExtremeSmithingRecipe(ExtremeSmithingRecipe recipe) {
        this.type = RecipeType.AVARITIA_SMITHING;
        try {
            this.menu.getSlot(40).set(recipe.template.getItems()[0]); // 模板槽位
            this.menu.getSlot(41).set(recipe.base.getItems()[0]);     // 基础物品槽位
            this.menu.getSlot(42).set(recipe.additions.getItems()[0]); // 添加物品槽位
            this.menu.getSlot(32).set(recipe.additions.getItems()[1]); // 添加物品槽位
            this.menu.getSlot(50).set(recipe.additions.getItems()[2]); // 添加物品槽位
        } catch (Exception e) {
            // 忽略异常
        }
    }

    // 填充熔炉配方
    private void fillSmeltingRecipe(SmeltingRecipe recipe) {
        this.type = RecipeType.VALLIA_SMELTING;
        fillSingleSlotRecipe(recipe.getIngredients().get(0));
    }

    // 填充高炉配方
    private void fillBlastingRecipe(BlastingRecipe recipe) {
        this.type = RecipeType.VALLIA_BLASTING;
        fillSingleSlotRecipe(recipe.getIngredients().get(0));
    }

    // 填充有序配方的通用方法
    private void fillShapedRecipe(NonNullList<Ingredient> ingredients, int width, int height) {
        try {
            // 计算起始位置（居中）
            int startRow = (9 - height) / 2;
            int startCol = (9 - width) / 2;

            int index = 0;
            for (int y = 0; y < height && y < 9; y++) {
                for (int x = 0; x < width && x < 9; x++) {
                    if (index < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(index);
                        if (!ingredient.isEmpty()) {
                            ItemStack[] items = ingredient.getItems();
                            if (items.length > 0) {
                                int slotIndex = (startRow + y) * 9 + (startCol + x);
                                if (this.isSlotAvailableForCurrentType(slotIndex)) {
                                    this.menu.getSlot(slotIndex).set(items[0].copy());
                                }
                            }
                        }
                    }
                    index++;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    // 填充无序配方的通用方法
    private void fillShapelessRecipe(java.util.List<Ingredient> ingredients) {
        try {
            if (isAvaritiaTableRecipe(this.type)) {
                // 无尽工作台配方
                int gridSize = (int) Math.sqrt(this.menu.getAvailableSlotsForTier(this.tier));
                int startRow = (9 - gridSize) / 2;
                int startCol = (9 - gridSize) / 2;

                for (int i = 0; i < Math.min(ingredients.size(), gridSize * gridSize); i++) {
                    Ingredient ingredient = ingredients.get(i);
                    if (!ingredient.isEmpty()) {
                        ItemStack[] items = ingredient.getItems();
                        if (items.length > 0) {
                            int row = i / gridSize;
                            int col = i % gridSize;
                            int slotIndex = (startRow + row) * 9 + (startCol + col);
                            if (this.isSlotAvailableForCurrentType(slotIndex)) {
                                this.menu.getSlot(slotIndex).set(items[0].copy());
                            }
                        }
                    }
                }
            } else {
                // 原版工作台配方，使用中心3x3区域
                int startRow = 3;
                int startCol = 3;

                for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
                    Ingredient ingredient = ingredients.get(i);
                    if (!ingredient.isEmpty()) {
                        ItemStack[] items = ingredient.getItems();
                        if (items.length > 0) {
                            int row = i / 3;
                            int col = i % 3;
                            int slotIndex = (startRow + row) * 9 + (startCol + col);
                            if (this.isSlotAvailableForCurrentType(slotIndex)) {
                                this.menu.getSlot(slotIndex).set(items[0].copy());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    // 填充单槽位配方的通用方法
    private void fillSingleSlotRecipe(Ingredient ingredient) {
        try {
            if (!ingredient.isEmpty()) {
                ItemStack[] items = ingredient.getItems();
                if (items.length > 0) {
                    // 放在中心位置
                    this.menu.getSlot(40).set(items[0].copy());
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    // 判断是否为无尽工作台配方
    private boolean isAvaritiaTableRecipe(RecipeType type) {
        return type == RecipeType.AVARITIA_SHAPED || type == RecipeType.AVARITIA_SHAPELESS;
    }

    // 根据等级获取网格大小
    private int getGridSizeForTier(int tier) {
        return switch (tier) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            case 4 -> 9;
            default -> 3;
        };
    }

    // 加载脚本文件
    private void loadScriptFiles() {
        try {
            Path configPath = Paths.get("config", "avaritia", "recipe");
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }

            // 加载所有脚本文件
            Files.walk(configPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".js") || path.toString().endsWith(".zs"))
                    .forEach(path -> {
                        ScriptFile scriptFile = new ScriptFile(path.toFile());
                        this.scriptFiles.add(scriptFile);
                    });
        } catch (Exception e) {
            // 忽略异常
        }
    }

}
