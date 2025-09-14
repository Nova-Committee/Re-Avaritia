package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.ItemSelectScreen;
import committee.nova.mods.avaritia.api.client.screen.StringInputScreen;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import committee.nova.mods.avaritia.util.CrtUtils;
import committee.nova.mods.avaritia.util.KubeJsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorScreen extends BaseContainerScreen<RecipeGeneratorMenu> {
    // 配方类型枚举
    public enum RecipeType {
        VALLIA_SHAPED,      // 原版有序工作台
        VALLIA_SHAPELESS,   // 原版无序工作台
        VALLIA_SMITHING,    // 原版锻造台
        VALLIA_SMELTING,    // 原版熔炉
        VALLIA_BLASTING,    // 原版高炉
        AVARITIA_SHAPED,    // 无尽有序工作台
        AVARITIA_SHAPELESS, // 无尽无序工作台
        AVARITIA_SMITHING,  // 无尽锻造台
        AVARITIA_COMPRESSOR;// 无尽压缩机

        RecipeType() {
        }
    }

    private RecipeType type = RecipeType.VALLIA_SHAPED; // 配方类型
    private int tier = 1; // 等级 (1-4)
    private int outType = 1; // 生成方式
    private boolean selectMode = false; // 模式
    private ItemStack brushItem = ItemStack.EMPTY; // 画刷物品
    private int selectedSlot = -1; // 当前选择的槽位索引

    private Button brushButton; // 画刷按钮
    private CycleButton<String> tierButton; // 等级按钮
    private CycleButton<String> typeButton; // 类型按钮

    public RecipeGeneratorScreen(RecipeGeneratorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.RECIPE_GENERATOR_TEX, 223, 234);
    }


    @Override
    protected void subInit() {
        super.subInit();
        int centerX = (this.width - this.imageWidth) / 2;
        int centerY = (this.height - this.imageHeight) / 2;

        // 添加选择模式切换按钮
        this.addRenderableWidget(createModeButton(centerX, centerY));

        // 添加类型选择按钮
        this.typeButton = this.addRenderableWidget(createTypeButton(centerX, centerY));

        // 添加等级选择按钮
        this.tierButton = this.addRenderableWidget(createTierButton(centerX, centerY));

        // 添加生成方式选择按钮
        this.addRenderableWidget(createFormatButton(centerX, centerY));

        // 添加生成按钮
        this.addRenderableWidget(createGenerateButton(centerX, centerY));

        // 添加配方选择按钮
        this.addRenderableWidget(createRecipeSelectButton(centerX, centerY));

        this.brushButton = this.addRenderableWidget(createBrushButton(centerX, centerY));
        updateButtonVisibility();
    }

    // 创建模式切换按钮
    private Button createModeButton(int centerX, int centerY) {
        return GuiUtils.newButton(centerX + 2, centerY + 185, 40, 15,
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
                .create(centerX + 42, centerY + 185, 40, 15,
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
                .create(centerX + 82, centerY + 185, 40, 15,
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
                .create(centerX + 122, centerY + 185, 60, 15,
                        Component.translatable("gui.avaritia.recipe_generator.type"),
                        (button, value) -> this.outType = Integer.parseInt(value));
    }

    // 创建生成按钮
    private Button createGenerateButton(int centerX, int centerY) {
        return GuiUtils.newButton(centerX + 182, centerY + 185, 40, 15,
                Component.translatable("gui.avaritia.recipe_generator.generate"),
                button -> {
                    switch (this.outType) {
                        case 1 -> generateKubeJSRecipe();
                        case 2 -> generateZSRecipe();
                        default -> {
                        }
                    }
                });
    }

    // 创建配方选择按钮
    private Button createRecipeSelectButton(int centerX, int centerY) {
        return GuiUtils.newButton(centerX + 202, centerY + 10, 20, 20,
                Component.translatable("gui.avaritia.recipe_generator.select_recipe"),
                button -> this.openRecipeSelectScreen());
    }

    // 创建画刷按钮
    private Button createBrushButton(int centerX, int centerY) {
        return GuiUtils.newButton(centerX + 202, centerY + 30, 20, 20,
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
    protected void renderLabels(GuiGraphics pGuiGraphics, int pX, int pY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        if (!this.selectMode)
            pGuiGraphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_generator.brush"), 180, 36, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        // 渲染槽位选择指示器
        renderSlotSelectionIndicator(pGuiGraphics);
        // 渲染不可用槽位的遮罩
        renderDisabledSlotsOverlay(pGuiGraphics);
    }

    // 渲染槽位选择指示器
    private void renderSlotSelectionIndicator(GuiGraphics graphics) {
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
            graphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF00FF00);
            graphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF00FF00);
            graphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF00FF00);
            graphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF00FF00);
        }
    }

    // 渲染不可用槽位的遮罩
    private void renderDisabledSlotsOverlay(GuiGraphics graphics) {
        Set<Integer> availableSlots = this.getAvailableSlotsForType(this.type, this.tier);
        for (int i = 0; i < 81; i++) {
            if (!availableSlots.contains(i)) {
                // 槽位不可用，绘制半透明遮罩
                int row = i / 9;
                int col = i % 9;
                int slotX = this.leftPos + 8 + col * 18;
                int slotY = this.topPos + 18 + row * 18;
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

    // 处理左键点击
    private boolean handleLeftClick(double mouseX, double mouseY) {
        // 检查输入槽位区域 (9x9网格)
        int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
        int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

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
        int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
        int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

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
        // 检查输入槽位区域 (9x9网格)
        int gridX = (int) ((mouseX - (this.leftPos + 8)) / 18);
        int gridY = (int) ((mouseY - (this.topPos + 18)) / 18);

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
        int outputX = (int) ((mouseX - (this.leftPos + 202)) / 18);
        int outputY = (int) ((mouseY - (this.topPos + 89)) / 18);

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

    private void generateKubeJSRecipe() {
        // 使用KubeJsUtils生成代码
        if (!this.menu.slots.isEmpty() && !this.menu.getSlotItem(81).isEmpty()) {
            if (Screen.hasShiftDown()) {
                Minecraft.getInstance().setScreen(new StringInputScreen(this,
                        Text.i18n("请输入自定义文件名").setShadow(true),
                        Text.i18n("请输入"), "", "generated_recipe", input -> {
                    if (!input.isEmpty()) {
                        KubeJsUtils.exportTableJS(this.menu, this.type == RecipeType.AVARITIA_SHAPED, this.tier, true, input);
                    }
                }));
            } else {
                KubeJsUtils.exportTableJS(this.menu, this.type == RecipeType.AVARITIA_SHAPED, this.tier, true, "generated_recipe");
            }
        }
    }

    private void generateZSRecipe() {
        // 使用CrtUtils生成代码
        if (!this.menu.slots.isEmpty() && !this.menu.getSlotItem(81).isEmpty()) {
            CrtUtils.exportTableZS(this.menu, this.type == RecipeType.AVARITIA_SHAPED, this.tier, true, "generated_recipe");
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
        if (this.tierButton != null && isAvaritiaTableRecipe()) {
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
            if (isAvaritiaTableRecipe()) {
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
    private boolean isAvaritiaTableRecipe() {
        return this.type == RecipeType.AVARITIA_SHAPED || this.type == RecipeType.AVARITIA_SHAPELESS;
    }

}
