package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.menu.InfiniteChestMenu2;
import committee.nova.mods.avaritia.common.wrappers.InfiniteItemHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * @author: cnlimiter
 */
public class InfiniteChestScreen2 extends AbstractContainerScreen<InfiniteChestMenu2> {
    private static final ResourceLocation CONTAINER_LOCATION =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private static final ResourceLocation SCROLLBAR_LOCATION =
            new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");


    private EditBox searchBox;
    private Button sortButton;
    private Button modCategoryButton;
    private Scrollbar scrollbar;


    public InfiniteChestScreen2(InfiniteChestMenu2 menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }


    @Override
    protected void init() {
        super.init();

        // 添加搜索框
        searchBox = new EditBox(font, leftPos + 78, topPos + 3, 90, 14, Component.literal("搜索..."));
        searchBox.setMaxLength(50);
        searchBox.setValue(menu.getSearchQuery());
        searchBox.setResponder(this::onSearchChanged);
        addWidget(searchBox);

        // 添加排序按钮
        sortButton = addRenderableWidget(
                Button.builder(Component.literal(getSortButtonText()), button -> cycleSortType())
                        .bounds(leftPos - 50, topPos + 56, 50, 20)
                        .build()
        );

        // 添加模组分类按钮
        modCategoryButton = addRenderableWidget(
                Button.builder(Component.literal("按模组分类"), button -> {
                            showModCategories();
                        })
                        .bounds(leftPos - 50, topPos + 116, 50, 20)
                        .build()
        );
        // 添加滚动条
        scrollbar = new Scrollbar(leftPos + leftPos + 100, topPos + 1, imageHeight - 36, menu);
        addRenderableWidget(scrollbar);
    }

    private void onSearchChanged(String newText) {
        menu.setSearchQuery(newText);
    }

    private String getSortButtonText() {
        return switch (menu.getSortType()) {
            case NAME -> "按名称";
            case COUNT -> "按数量";
            case MOD -> "按模组";
            case CATEGORY -> "按类别";
            default -> "不排序";
        };
    }

    private void cycleSortType() {
        switch (menu.getSortType()) {
            case NONE:
                menu.setSortType(InfiniteItemHandler.SortType.NAME);
                break;
            case NAME:
                menu.setSortType(InfiniteItemHandler.SortType.COUNT);
                break;
            case COUNT:
                menu.setSortType(InfiniteItemHandler.SortType.MOD);
                break;
            case MOD:
                menu.setSortType(InfiniteItemHandler.SortType.CATEGORY);
                break;
            case CATEGORY:
                menu.setSortType(InfiniteItemHandler.SortType.NONE);
                break;
        }
        sortButton.setMessage(Component.literal(getSortButtonText()));
    }

    private void showModCategories() {
        // 这里可以打开一个新的界面显示按模组分类的物品
        // 为了简化，我们只在控制台打印
        Map<String, List<ItemStack>> modItems = menu.getItemsByMod();
        System.out.println("按模组分类的物品:");
        for (Map.Entry<String, List<ItemStack>> entry : modItems.entrySet()) {
            System.out.println("模组: " + entry.getKey() + ", 物品数量: " + entry.getValue().size());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制背景
        graphics.blit(CONTAINER_LOCATION, x, y, 0, 0, imageWidth, 6 * 18 + 17);// 顶部
        graphics.blit(CONTAINER_LOCATION, x, y + 6 * 18 + 17, 0, 126, imageWidth, 96); // 底部和玩家物品栏
        // 绘制滚动信息
        String scrollInfo = String.format("%d/%d", menu.getScrollPosition() + 1, menu.getMaxScrollPosition() + 1);
        graphics.drawString(font, scrollInfo, x + imageWidth - 50, y + 6, 0x404040, false);

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 不绘制标题，因为我们在背景中绘制了页码信息
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        searchBox.render(graphics, mouseX, mouseY, partialTick);
    }


    private static class Scrollbar extends AbstractScrollWidget implements GuiEventListener {
        private final InfiniteChestMenu2 menu;
        private boolean isDragging;

        public Scrollbar(int x, int y, int height, InfiniteChestMenu2 menu) {
            super(x, y, 12, height, Component.literal(""));
            this.menu = menu;
        }

        @Override
        protected int getInnerHeight() {
            return menu.getMaxScrollPosition();
        }

        @Override
        protected double scrollRate() {
            return 3;
        }

        @Override
        protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            // 计算滚动条位置和高度
            int maxScroll = menu.getMaxScrollPosition();
            int scrollPos = menu.getScrollPosition();

            // 如果没有足够的物品需要滚动，不显示滚动条
            if (maxScroll <= 0) {
                return;
            }

            // 计算滚动条高度（最小高度为10）
            int scrollbarHeight = Math.max(10, height * height / (height + maxScroll));

            // 计算滚动条位置
            int scrollbarY = getY() + (height - scrollbarHeight) * scrollPos / maxScroll;
            //绘制滚动条背景
            pGuiGraphics.blit(SCROLLBAR_LOCATION, getX(), getY(), 232, 0, 12, height);
            // 绘制滚动条
            pGuiGraphics.blit(SCROLLBAR_LOCATION, getX(), scrollbarY, 244, 0, 12, scrollbarHeight);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 0) {
                isDragging = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) {
                isDragging = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (isDragging && button == 0) {
                // 计算新的滚动位置
                int maxScroll = menu.getMaxScrollPosition();
                if (maxScroll > 0) {
                    double relativeY = mouseY - getY();
                    double scrollRatio = relativeY / height;
                    int newScrollPos = (int) (scrollRatio * maxScroll);

                    // 限制滚动范围
                    newScrollPos = Math.max(0, Math.min(newScrollPos, maxScroll));

                    // 更新滚动位置
                    menu.scroll(newScrollPos - menu.getScrollPosition());
                }
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (isMouseOver(mouseX, mouseY)) {
                // 处理鼠标滚轮
                menu.scroll(delta > 0 ? -1 : 1);
                return true;
            }
            return false;
        }
    }
}
