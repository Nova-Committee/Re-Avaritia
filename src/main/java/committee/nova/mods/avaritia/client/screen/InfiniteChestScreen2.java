package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.common.menu.InfiniteChestMenu2;
import committee.nova.mods.avaritia.common.tile.InfiniteChestBlockEntity;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import committee.nova.mods.avaritia.common.wrappers.InfiniteItemHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
/**
 * @author: cnlimiter
 */
public class InfiniteChestScreen2 extends AbstractContainerScreen<InfiniteChestMenu2> {
    private static final ResourceLocation CONTAINER_LOCATION =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    private EditBox searchBox;
    private Button sortButton;
    private Button autoOrganizeButton;
    private Button organizeButton;
    private Button modCategoryButton;


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

        // 添加上一页按钮
        addRenderableWidget(
                Button.builder(Component.literal("上一页"), button -> {
                            menu.previousPage();
                            //init();
                        })
                        .bounds(leftPos - 50, topPos + 16, 50, 20)
                        .build()

        );

        // 添加下一页按钮
        addRenderableWidget(
                Button.builder(Component.literal("下一页"), button -> {
                            menu.nextPage();
                            //init();
                        })
                        .bounds(leftPos - 50, topPos + 36, 50, 20)
                        .build()
        );

        // 添加排序按钮
        sortButton = addRenderableWidget(
                Button.builder(Component.literal(getSortButtonText()), button -> cycleSortType())
                        .bounds(leftPos - 50, topPos + 56, 50, 20)
                        .build()
        );


        // 添加自动整理按钮
        autoOrganizeButton = addRenderableWidget(
                Button.builder(Component.literal(menu.isAutoOrganize() ? "自动:开" : "自动:关"), button -> {
                            menu.toggleAutoOrganize();
                            autoOrganizeButton.setMessage(Component.literal(menu.isAutoOrganize() ? "自动:开" : "自动:关"));
                        })
                        .bounds(leftPos - 50, topPos + 76, 50, 20)
                        .build()
        );

        // 添加手动整理按钮
        organizeButton = addRenderableWidget(
                Button.builder(Component.literal("整理"), button -> {
                            menu.organizeItems();
                        })
                        .bounds(leftPos - 50, topPos + 96, 50, 20)
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
    }

    private void onSearchChanged(String newText) {
        menu.setSearchQuery(newText);
    }

    private String getSortButtonText() {
        return switch (menu.getSortType()) {
            case NAME -> "按名称";
            case COUNT -> "按数量";
            case MOD -> "按模组";
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
                menu.setSortType(InfiniteItemHandler.SortType.NONE);
                break;
        }
        sortButton.setMessage(Component.literal(getSortButtonText()));
    }

    private void showModCategories() {
        // 这里可以打开一个新的界面显示按模组分类的物品
        // 为了简化，我们只是在控制台打印
        Map<String, List<ItemStack>> modItems = this.getMenu().getTileEntity().getItemHandler().getItemsByMod();
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
        //minecraft.getTextureManager().bindForSetup(CONTAINER_LOCATION);
        graphics.blit(CONTAINER_LOCATION, x, y, 0, 0, imageWidth, 6 * 18 + 17);// 顶部
        graphics.blit(CONTAINER_LOCATION, x, y + 6 * 18 + 17, 0, 126, imageWidth, 96); // 底部和玩家物品栏


        // 绘制页码信息
        String pageInfo = String.format("第 %d/%d 页", menu.getCurrentPage() + 1, menu.getTotalPages());
        graphics.drawString(font, pageInfo, x - 60 + imageWidth / 2 - font.width(pageInfo) / 2, y + 5, 0x404040, false);
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
}
