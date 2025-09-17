package committee.nova.mods.avaritia.client.screen.script;

import committee.nova.mods.avaritia.api.client.util.GuiUtils;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.StringUtils;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author: cnlimiter
 */
public class RecipeSelectScreen extends Screen {
    private static final int LIST_WIDTH = 150;
    private static final int LIST_HEIGHT = 180;
    private static final int LIST_ITEM_HEIGHT = 20;
    private static final int MAX_LIST_ITEMS = LIST_HEIGHT / LIST_ITEM_HEIGHT;

    private final Screen previousScreen;
    private final Consumer<Recipe<?>> onRecipeSelected;
    private final List<Recipe<?>> allRecipes = new ArrayList<>();
    private final List<Recipe<?>> filteredRecipes = new ArrayList<>();

    private EditBox searchBox;
    private String searchText = "";
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    // 界面布局参数
    private int bgX;
    private int bgY;
    private int listStartX;
    private int listStartY;
    private int previewStartX;
    private int previewStartY;

    public RecipeSelectScreen(Screen previousScreen, Consumer<Recipe<?>> onRecipeSelected) {
        super(Component.translatable("gui.avaritia.recipe_select.title"));
        this.previousScreen = previousScreen;
        this.onRecipeSelected = onRecipeSelected;
        this.loadAllRecipes();
    }

    @Override
    protected void init() {
        this.updateLayout();

        // 创建搜索框
        this.searchBox = GuiUtils.newTextFieldWidget(this.font, this.listStartX, this.bgY + 8, LIST_WIDTH - 5, 15,
                Component.translatable("gui.avaritia.search"));
        this.searchBox.setValue(this.searchText);
        this.addRenderableWidget(this.searchBox);

        // 创建确定按钮
        this.addRenderableWidget(GuiUtils.newButton(
                this.listStartX,
                this.bgY + 220,
                60,
                20,
                Component.translatable("gui.avaritia.confirm"),
                button -> {
                    if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredRecipes.size()) {
                        Recipe<?> selectedRecipe = this.filteredRecipes.get(this.selectedIndex);
                        this.onRecipeSelected.accept(selectedRecipe);
                        Minecraft.getInstance().setScreen(this.previousScreen);
                    }
                }
        ));

        // 创建取消按钮
        this.addRenderableWidget(GuiUtils.newButton(
                this.listStartX + 90,
                this.bgY + 220,
                60,
                20,
                Component.translatable("gui.avaritia.cancel"),
                button -> Minecraft.getInstance().setScreen(this.previousScreen)
        ));

        this.updateFilteredRecipes();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);

        // 绘制背景
        GuiUtils.fill(graphics, this.bgX, this.bgY, 360, 250, 0xFFC6C6C6, 10);
        GuiUtils.fillOutLine(graphics, this.bgX, this.bgY, 360, 250, 1, 0xFF000000, 9);

        // 绘制列表区域背景
        GuiUtils.fill(graphics, this.listStartX, this.listStartY, LIST_WIDTH, LIST_HEIGHT, 0xFFAAAAAA, 3);
        GuiUtils.fillOutLine(graphics, this.listStartX, this.listStartY, LIST_WIDTH, LIST_HEIGHT, 1, 0xFF000000, 3);

        // 绘制预览区域背景
        GuiUtils.fill(graphics, this.previewStartX, this.previewStartY, 180, 210, 0xFFAAAAAA, 3);
        GuiUtils.fillOutLine(graphics, this.previewStartX, this.previewStartY, 180, 210, 1, 0xFF000000, 3);

        super.render(graphics, mouseX, mouseY, delta);

        // 保存搜索框文本
        this.searchText = this.searchBox.getValue();

        // 渲染列表项
        this.renderListItems(graphics, mouseX, mouseY);

        // 渲染预览
        this.renderPreview(graphics);

        // 渲染标题
        graphics.drawString(this.font, this.title, this.previewStartX, this.bgY + 10, 0x404040, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= this.listStartX && mouseX <= this.listStartX + LIST_WIDTH &&
                mouseY >= this.listStartY && mouseY <= this.listStartY + LIST_HEIGHT) {
            this.setScrollOffset(this.getScrollOffset() - (int) delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AtomicBoolean flag = new AtomicBoolean(false);

        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            Minecraft.getInstance().setScreen(previousScreen);
            flag.set(true);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // 检查是否点击了列表项
            if (mouseX >= this.listStartX && mouseX <= this.listStartX + LIST_WIDTH &&
                    mouseY >= this.listStartY && mouseY <= this.listStartY + LIST_HEIGHT) {

                int listItemIndex = (int) ((mouseY - this.listStartY) / LIST_ITEM_HEIGHT);
                int actualIndex = this.scrollOffset + listItemIndex;

                if (actualIndex >= 0 && actualIndex < this.filteredRecipes.size()) {
                    this.selectedIndex = actualIndex;
                    return true;
                }
            }
        }

        return flag.get() ? flag.get() : super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(previousScreen);
            return true;
        } else if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.searchBox.isFocused()) {
            this.updateFilteredRecipes();
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

    private void loadAllRecipes() {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        Collection<Recipe<?>> recipes = recipeManager.getRecipes();

        // 过滤出有效的配方（有输出的配方）
        this.allRecipes.addAll(recipes.stream()
                .filter(recipe -> !recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).isEmpty())
                .collect(Collectors.toList()));

        this.filteredRecipes.addAll(this.allRecipes);
    }

    private void updateFilteredRecipes() {
        String search = this.searchBox.getValue().toLowerCase();
        if (StringUtils.isNotNullOrEmpty(search)) {
            this.filteredRecipes.clear();
            this.filteredRecipes.addAll(this.allRecipes.stream()
                    .filter(recipe -> {
                        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                        if (result.isEmpty()) return false;

                        // 检查物品名称
                        String itemName = ItemUtils.getId(result);
                        if (itemName != null && itemName.toLowerCase().contains(search)) {
                            return true;
                        }

                        // 检查显示名称
                        String displayName = result.getDisplayName().getString().toLowerCase();
                        if (displayName.contains(search)) {
                            return true;
                        }

                        // 检查模组ID
                        ResourceLocation recipeId = recipe.getId();
                        if (recipeId != null && recipeId.getNamespace().toLowerCase().contains(search)) {
                            return true;
                        }

                        return false;
                    })
                    .toList());
        } else {
            this.filteredRecipes.clear();
            this.filteredRecipes.addAll(this.allRecipes);
        }
        this.setScrollOffset(0);
        this.selectedIndex = -1;
    }

    private void updateLayout() {
        this.bgX = (this.width - 350) / 2;
        this.bgY = (this.height - 220) / 2;
        this.listStartX = this.bgX + 10;
        this.listStartY = this.bgY + 30;
        this.previewStartX = this.listStartX + LIST_WIDTH + 10;
        this.previewStartY = this.bgY + 30;
    }

    private void renderListItems(GuiGraphics graphics, int mouseX, int mouseY) {
        int visibleItems = Math.min(MAX_LIST_ITEMS, this.filteredRecipes.size());

        for (int i = 0; i < visibleItems; i++) {
            int index = this.scrollOffset + i;
            if (index >= 0 && index < this.filteredRecipes.size()) {
                Recipe<?> recipe = this.filteredRecipes.get(index);
                int itemY = this.listStartY + i * LIST_ITEM_HEIGHT;

                // 绘制背景
                int bgColor = (index == this.selectedIndex) ? 0xFF7CAB7C : 0xFF707070;
                if (mouseX >= this.listStartX && mouseX <= this.listStartX + LIST_WIDTH &&
                        mouseY >= itemY && mouseY <= itemY + LIST_ITEM_HEIGHT) {
                    bgColor = 0xFFAAAAAA;
                }
                GuiUtils.fill(graphics, this.listStartX, itemY, LIST_WIDTH, LIST_ITEM_HEIGHT, bgColor, 1);

                // 绘制配方信息
                ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                if (!result.isEmpty()) {
                    // 绘制物品图标
                    graphics.renderItem(result, this.listStartX + 2, itemY + 2);

                    // 绘制物品名称
                    String itemName = result.getDisplayName().getString();
                    if (itemName.length() > 15) {
                        itemName = itemName.substring(0, 12) + "...";
                    }
                    graphics.drawString(this.font, itemName, this.listStartX + 22, itemY + 3, 0xFFFFFF, false);

                    // 绘制模组名称
                    String modName = recipe.getId().getNamespace();
                    if (modName.length() > 12) {
                        modName = modName.substring(0, 9) + "...";
                    }
                    graphics.drawString(this.font, modName, this.listStartX + 22, itemY + 12, 0xAAAAAA, false);
                }
            }
        }

        // 绘制滚动条
        if (this.filteredRecipes.size() > MAX_LIST_ITEMS) {
            int scrollbarX = this.listStartX + LIST_WIDTH - 6;
            int scrollbarHeight = LIST_HEIGHT;
            int thumbHeight = Math.max(10, (int) ((double) MAX_LIST_ITEMS / this.filteredRecipes.size() * scrollbarHeight));
            int thumbY = this.listStartY + (int) ((double) this.scrollOffset / (this.filteredRecipes.size() - MAX_LIST_ITEMS) * (scrollbarHeight - thumbHeight));

            // 绘制滚动条背景
            GuiUtils.fill(graphics, scrollbarX, this.listStartY, 6, scrollbarHeight, 0xFF232323, 1);
            // 绘制滚动条滑块
            GuiUtils.fill(graphics, scrollbarX, thumbY, 6, thumbHeight, 0xFF8B8B8B, 1);
        }
    }

    private void renderPreview(GuiGraphics graphics) {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.filteredRecipes.size()) {
            Recipe<?> recipe = this.filteredRecipes.get(this.selectedIndex);
            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());

            // 绘制输出物品
            if (!result.isEmpty()) {
                graphics.renderItem(result, this.previewStartX + 10, this.previewStartY + 5);
                graphics.drawString(this.font, result.getDisplayName(), this.previewStartX + 30, this.previewStartY + 10, 0xFFFFFF, false);
            }

            // 绘制配方类型信息
            String recipeType = "Unknown";
            int tier = 1;

            if (recipe instanceof ShapedTableCraftingRecipe) {
                recipeType = "Shaped Table";
                tier = ((ShapedTableCraftingRecipe) recipe).getTier();
            } else if (recipe instanceof ShapelessTableCraftingRecipe) {
                recipeType = "Shapeless Table";
                tier = ((ShapelessTableCraftingRecipe) recipe).getTier();
            } else if (recipe instanceof ShapedRecipe) {
                recipeType = "Shaped";
            } else if (recipe instanceof ShapelessRecipe) {
                recipeType = "Shapeless";
            }

            graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_select.type", recipeType),
                    this.previewStartX + 10, this.previewStartY + 20, 0xFFFFFF, false);
            graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_select.tier", tier),
                    this.previewStartX + 10, this.previewStartY + 28, 0xFFFFFF, false);

            // 绘制配方ID
            String idString = recipe.getId().toString();
            if (idString.length() > 25) {
                idString = idString.substring(0, 22) + "...";
            }
            graphics.drawString(this.font, Component.translatable("gui.avaritia.recipe_select.id", idString),
                    this.previewStartX + 10, this.previewStartY + 69, 0xAAAAAA, false);

            // 绘制配方预览网格（简化版）
            this.renderRecipeGridPreview(graphics, recipe);
        } else {
            // 显示选择提示
            graphics.drawCenteredString(this.font, Component.translatable("gui.avaritia.recipe_select.select_prompt"),
                    this.previewStartX + 85, this.previewStartY + 85, 0xAAAAAA);
        }
    }

    private void renderRecipeGridPreview(GuiGraphics graphics, Recipe<?> recipe) {
        int gridStartX = this.previewStartX + 10;
        int gridStartY = this.previewStartY + 40;

        // 根据配方类型确定网格大小
        int actualGridSize = 3; // 实际配方大小
        int displayGridSize = 9; // 显示网格大小（固定为9x9）

        if (recipe instanceof ShapedTableCraftingRecipe) {
            int tier = ((ShapedTableCraftingRecipe) recipe).getTier();
            actualGridSize = getGridSizeForTier(tier);
        } else if (recipe instanceof ShapelessTableCraftingRecipe) {
            int tier = ((ShapelessTableCraftingRecipe) recipe).getTier();
            actualGridSize = getGridSizeForTier(tier);
        }  // 原版配方使用3x3网格


        // 绘制完整的9x9网格背景
        for (int y = 0; y < displayGridSize; y++) {
            for (int x = 0; x < displayGridSize; x++) {
                int cellX = gridStartX + x * 18;
                int cellY = gridStartY + y * 18;
                GuiUtils.fill(graphics, cellX, cellY, 16, 16, 0xFF555555, 1);
            }
        }

        // 绘制遮罩，突出显示有效区域
        this.renderGridMask(graphics, gridStartX, gridStartY, actualGridSize, displayGridSize);

        // 填充配方内容
        if (recipe instanceof ShapedTableCraftingRecipe shapedRecipe) {
            this.renderShapedRecipePreview(graphics, shapedRecipe, gridStartX, gridStartY, actualGridSize);
        } else if (recipe instanceof ShapelessTableCraftingRecipe shapelessRecipe) {
            this.renderShapelessRecipePreview(graphics, shapelessRecipe, gridStartX, gridStartY, actualGridSize);
        } else if (recipe instanceof ShapedRecipe shapedRecipe) {
            this.renderVanillaShapedRecipePreview(graphics, shapedRecipe, gridStartX, gridStartY);
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            this.renderVanillaShapelessRecipePreview(graphics, shapelessRecipe, gridStartX, gridStartY);
        }
    }

    // 根据等级获取网格大小的辅助方法
    private int getGridSizeForTier(int tier) {
        return switch (tier) {
            case 1 -> 3; // 3x3
            case 2 -> 5; // 5x5
            case 3 -> 7; // 7x7
            case 4 -> 9; // 9x9
            default -> 3;
        };
    }

    // 绘制网格遮罩，突出显示有效区域
    private void renderGridMask(GuiGraphics graphics, int startX, int startY, int actualSize, int totalSize) {
        int offset = (totalSize - actualSize) / 2; // 绘制上方遮罩
        if (offset > 0) {
            graphics.fill(startX, startY, startX + 18 * totalSize, startY + 18 * offset, 0x80000000); // 半透明黑色
        }

        // 绘制下方遮罩
        if (offset > 0) {
            graphics.fill(startX, startY + 18 * (offset + actualSize), startX + 18 * totalSize, startY + 18 * totalSize, 0x80000000);
        }

        // 绘制左侧遮罩
        if (offset > 0) {
            graphics.fill(startX, startY + 18 * offset, startX + 18 * offset, startY + 18 * (offset + actualSize), 0x80000000);
        }

        // 绘制右侧遮罩
        if (offset > 0) {
            graphics.fill(startX + 18 * (offset + actualSize), startY + 18 * offset, startX + 18 * totalSize, startY + 18 * (offset + actualSize), 0x80000000);
        }

        // 绘制有效区域边框（使用更细的边框）
        if (actualSize < totalSize) {
            int validAreaX = startX + 18 * offset;
            int validAreaY = startY + 18 * offset;
            int validAreaWidth = 18 * actualSize;
            int validAreaHeight = 18 * actualSize;

            // 绘制边框（1像素宽）
            graphics.fill(validAreaX - 1, validAreaY - 1, validAreaX + validAreaWidth + 1, validAreaY, 0xFFFFD700); // 上边框 (金色)
            graphics.fill(validAreaX - 1, validAreaY + validAreaHeight, validAreaX + validAreaWidth + 1, validAreaY + validAreaHeight + 1, 0xFFFFD700); // 下边框
            graphics.fill(validAreaX - 1, validAreaY, validAreaX, validAreaY + validAreaHeight, 0xFFFFD700); // 左边框
            graphics.fill(validAreaX + validAreaWidth, validAreaY, validAreaX + validAreaWidth + 1, validAreaY + validAreaHeight, 0xFFFFD700); // 右边框
        }
    }

    private void renderShapedRecipePreview(GuiGraphics graphics, ShapedTableCraftingRecipe recipe, int startX, int startY, int actualGridSize) {
        try {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            int width = recipe.getWidth();
            int height = recipe.getHeight();

            // 限制网格大小不超过实际网格
            width = Math.min(width, actualGridSize);
            height = Math.min(height, actualGridSize);

            // 计算居中偏移
            int offset = (9 - actualGridSize) / 2;
            int offsetX = offset + Math.max(0, (actualGridSize - width) / 2);
            int offsetY = offset + Math.max(0, (actualGridSize - height) / 2);

            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (index < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(index);
                        if (!ingredient.isEmpty()) {
                            ItemStack[] items = ingredient.getItems();
                            if (items.length > 0) {
                                int cellX = startX + (offsetX + x) * 18;
                                int cellY = startY + (offsetY + y) * 18;
                                graphics.renderItem(items[0], cellX, cellY);
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

    private void renderShapelessRecipePreview(GuiGraphics graphics, ShapelessTableCraftingRecipe recipe, int startX, int startY, int actualGridSize) {
        try {
            List<Ingredient> ingredients = recipe.getIngredients();
            int maxItems = Math.min(ingredients.size(), actualGridSize * actualGridSize);

            // 计算居中偏移
            int offset = (9 - actualGridSize) / 2;

            // 根据网格大小排列物品
            for (int i = 0; i < maxItems; i++) {
                Ingredient ingredient = ingredients.get(i);
                if (ingredient != null && !ingredient.isEmpty()) {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length > 0) {
                        int x = i % actualGridSize;
                        int y = i / actualGridSize;
                        int cellX = startX + (offset + x) * 18;
                        int cellY = startY + (offset + y) * 18;
                        graphics.renderItem(items[0], cellX, cellY);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void renderVanillaShapedRecipePreview(GuiGraphics graphics, ShapedRecipe recipe, int startX, int startY) {
        try {
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            // 原版配方使用3x3网格
            int width = 3;
            int height = Math.min(3, (ingredients.size() + width - 1) / width); // 计算实际需要的行数

            // 计算居中偏移（在9x9网格中居中显示3x3）
            int offset = 3; // (9-3)/2 = 3

            int index = 0;
            for (int y = 0; y < height && y < 3; y++) {
                for (int x = 0; x < width && x < 3; x++) {
                    if (index < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(index);
                        if (!ingredient.isEmpty()) {
                            ItemStack[] items = ingredient.getItems();
                            if (items.length > 0) {
                                int cellX = startX + (offset + x) * 18;
                                int cellY = startY + (offset + y) * 18;
                                graphics.renderItem(items[0], cellX, cellY);
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

    private void renderVanillaShapelessRecipePreview(GuiGraphics graphics, ShapelessRecipe recipe, int startX, int startY) {
        try {
            List<Ingredient> ingredients = recipe.getIngredients();
            int maxItems = Math.min(ingredients.size(), 9); // 原版最多9个物品

            // 在9x9网格的中心3x3区域中排列
            int offset = 3; // (9-3)/2 = 3

            for (int i = 0; i < maxItems; i++) {
                Ingredient ingredient = ingredients.get(i);
                if (!ingredient.isEmpty()) {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length > 0) {
                        int x = i % 3;
                        int y = i / 3;
                        int cellX = startX + (offset + x) * 18;
                        int cellY = startY + (offset + y) * 18;
                        graphics.renderItem(items[0], cellX, cellY);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private int getScrollOffset() {
        return this.scrollOffset;
    }

    private void setScrollOffset(int offset) {
        int maxOffset = Math.max(0, this.filteredRecipes.size() - MAX_LIST_ITEMS);
        this.scrollOffset = Math.max(0, Math.min(offset, maxOffset));
    }
}
