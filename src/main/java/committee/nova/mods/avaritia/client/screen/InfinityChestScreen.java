package committee.nova.mods.avaritia.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.SimpleScrollBar;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPack;
import committee.nova.mods.avaritia.core.chest.ClientChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import committee.nova.mods.avaritia.core.chest.ItemSuper;
import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cnlimiter
 */
public class InfinityChestScreen extends BaseContainerScreen<InfinityChestMenu> {
    @Setter
    @Getter
    private int blitOffset;

    private static final ResourceLocation GUI_IMG = Res.INFINITY_CHEST_TEX;
    private final String ownerName;
    private ItemSuper lastHoveredItem = ItemSuper.EMPTY;
    private long lastCount = 0;
    private String lastFormatCountTemp = "";
    private SortButton sortButton;
    private ItemScrollBar scrollBar;
    private EditBox searchBox;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, null, 302, 274, 550, 550);
        this.ownerName = ClientChestManager.getInstance().getUserName(this.getMenu().owner);
    }

    @Override
    protected void subInit() {
        super.subInit();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.scrollBar = new ItemScrollBar(leftPos + 282, topPos + 16, 12, 160);
        this.scrollBar.setScrolledOn(menu.chestContainer.getScrollOn());
        this.addRenderableWidget(scrollBar);
        this.addRenderableWidget(new ToggleLockButton(this.leftPos + 231, this.topPos + 187));
        this.sortButton = new SortButton(this.leftPos + 249, this.topPos + 187);
        this.addRenderableWidget(sortButton);

        this.searchBox = new EditBox(this.font, leftPos + 187, topPos + 4, 89, 10, Component.translatable("gui.avaritia.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setBordered(false);
        this.searchBox.setValue(menu.filter);
        this.addRenderableWidget(searchBox);
        menu.chestContainer.refreshContainer(true);

        // 创建两侧避让区
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX + 50, this.inventoryLabelY + 108, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        pGuiGraphics.blit(GUI_IMG, x, y, this.blitOffset, 0, 0,  this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderDummyCount(pGuiGraphics);
    }

    @Override
    public void renderSlot(@NotNull GuiGraphics guiGraphics, Slot slot) {
        // 如果是InfinityChest的虚拟物品槽，不渲染原版的数量（避免与自定义数量显示冲突）
        if (slot.index >= InfinityChestMenu.CONTAINER_SLOT_START) {
            // 临时修改数量为1以隐藏原版数量渲染，然后恢复正常
            ItemStack stack = slot.getItem();
            int originalCount = stack.getCount();
            if (stack.getCount() > 1) {
                stack.setCount(1);
                super.renderSlot(guiGraphics, slot);
                stack.setCount(originalCount);
            } else {
                super.renderSlot(guiGraphics, slot);
            }
            return;
        }
        // 其他槽正常渲染
        super.renderSlot(guiGraphics, slot);
    }

    public void renderDummyCount(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        for (int i = 0; i < menu.chestContainer.formatCount.size(); i++) {
            Slot slot = menu.slots.get(i + InfinityChestMenu.CONTAINER_SLOT_START);
            String count = menu.chestContainer.formatCount.get(i);
            this.setBlitOffset(100);
            RenderSystem.enableDepthTest();
            float fontSize = 0.5F;
            poseStack.pushPose();
            poseStack.translate(leftPos + slot.x, topPos + slot.y, 300.0D);
            poseStack.scale(fontSize, fontSize, 1.0F);
            guiGraphics.drawString(font, count,
                    (16 - this.font.width(count) * fontSize) / fontSize,
                    (16 - this.font.lineHeight * fontSize) / fontSize,
                    16777215, false);
            poseStack.popPose();
            this.setBlitOffset(0);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void renderTooltip(GuiGraphics pPoseStack, int pX, int pY) {
        if (this.hoveredSlot != null) {
            if (hoveredSlot.index >= InfinityChestMenu.CONTAINER_SLOT_START) {
                if (menu.getCarried().getCount() == 1)
                    renderObjectStorageTooltip(pPoseStack, pX, pY);
                else
                    renderCounterTooltip(pPoseStack, pX, pY);
            } else if (!hoveredSlot.getItem().isEmpty() && menu.getCarried().isEmpty())
                pPoseStack.renderTooltip(font, this.hoveredSlot.getItem(), pX, pY);
        } else {
            if (searchBox.isHovered()) {
                List<Component> list = new ArrayList<>();
                list.add(Component.translatable("gui.avaritia.search.tip1"));
                list.add(Component.translatable("gui.avaritia.search.tip2"));
                list.add(Component.translatable("gui.avaritia.search.tip3"));
                pPoseStack.renderComponentTooltip(font, list, pX, pY);
            }
        }
    }

    private void renderCounterTooltip(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
        if ((hoveredSlot.index - InfinityChestMenu.CONTAINER_SLOT_START) >= menu.chestContainer.viewingObject.size()) return;
        var hoveredObject = menu.chestContainer.viewingObject.get(hoveredSlot.index - InfinityChestMenu.CONTAINER_SLOT_START);
        List<Component> components;
        long count;
        components = getTooltipFromItem(minecraft, hoveredSlot.getItem());
        count = menu.chest.getRealItemAmount(hoveredObject);

        if (!hoveredObject.equals(lastHoveredItem)) {
            String formatCount = StorageUtils.DECIMAL_FORMAT.format(count);
            components.add(Component.literal(formatCount));
            this.lastHoveredItem = hoveredObject;
            this.lastCount = count;
            this.lastFormatCountTemp = formatCount;
        } else if (count == lastCount) {
            components.add(Component.literal(lastFormatCountTemp));
        } else {
            String formatCount = StorageUtils.DECIMAL_FORMAT.format(count);
            long count2 = count - lastCount;
            String formatCount2 = StorageUtils.DECIMAL_FORMAT.format(count2);
            if (count2 >= 0) formatCount += "  |  +§a" + formatCount2;
            else formatCount += "  |  §c" + formatCount2;
            components.add(Component.literal(formatCount));
            lastCount = count;
            lastFormatCountTemp = formatCount;
        }
        pPoseStack.renderTooltip(font, components, hoveredSlot.getItem().getTooltipImage(), pMouseX, pMouseY);
    }


    private void renderObjectStorageTooltip(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
        ItemStack carried = menu.getCarried();
        boolean hasCapability = carried.getCapability(Capabilities.ItemHandler.ITEM) != null;
        if (hasCapability) {
            List<Component> components = Lists.newArrayList();
            if ((hoveredSlot.index - InfinityChestMenu.CONTAINER_SLOT_START) < menu.chestContainer.viewingObject.size()) {
                components.add(Component.translatable("gui.avaritia.capability.tip1", hoveredSlot.getItem().getHoverName()));
            }
            components.add(Component.translatable("gui.avaritia.capability.tip2"));
            components.add(Component.translatable("gui.avaritia.capability.tip3"));
            pPoseStack.renderTooltip(font, components, ItemStack.EMPTY.getTooltipImage(), pMouseX, pMouseY);
        } else renderCounterTooltip(pPoseStack, pMouseX, pMouseY);
    }


    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, menu.filter));
        ((ClientChestHandler) menu.chest).removeListener();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1) {
            //搜索框
            if (searchBox.isMouseOver(pMouseX, pMouseY)) {
                menu.filter = "";
                searchBox.setValue("");
                menu.chestContainer.refreshContainer(true);
                searchBox.setFocused(true);
                searchBox.setEditable(true);
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (scrollBar.isScrolling()) scrollBar.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        scrollBar.mouseReleased(pMouseX, pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (searchBox.isFocused()) {
            if (pKeyCode >= InputConstants.KEY_0 && pKeyCode <= InputConstants.KEY_Z) return true;
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) menu.LShifting = true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (searchBox.isFocused()) {
            String s = searchBox.getValue().toLowerCase();
            if (!s.equals(menu.filter)) {
                menu.filter = s;
                menu.chestContainer.refreshContainer(true);
            }
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) {
            menu.LShifting = false;
            menu.chestContainer.refreshContainer(true);
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 5 && mouseX <= leftPos + 214 && mouseY >= topPos + 17 && mouseY <= topPos + 18 + 119 && scrollBar.canScroll()) {
            if (scrollY <= 0) scrollBar.setScrolledOn(menu.chestContainer.onMouseScrolled(false));
            else scrollBar.setScrolledOn(menu.chestContainer.onMouseScrolled(true));
            return true;
        } else return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double) pX && pMouseX < (double) (pX + pWidth) && pMouseY >= (double) pY && pMouseY < (double) (pY + pHeight);
    }

    private void toggleLock() {
        if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            this.menu.locked = !this.menu.locked;
            this.searchBox.setFocused(false);
            PacketDistributor.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, menu.filter));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    private void cycleSort() {
        if (InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            menu.reverseSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        } else {
            menu.nextSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }
    }

    private String getSortKey(int sortType) {
        return switch (sortType) {
            case SortUtils.Sort.ID_ASCENDING, SortUtils.Sort.ID_DESCENDING -> "gui.avaritia.sort.itemSuper";
            case SortUtils.Sort.NAMESPACE_ID_ASCENDING, SortUtils.Sort.NAMESPACE_ID_DESCENDING ->
                    "gui.avaritia.sort.nid";
            case SortUtils.Sort.MIRROR_ID_ASCENDING, SortUtils.Sort.MIRROR_ID_DESCENDING ->
                    "gui.avaritia.sort.mirror_id";
            case SortUtils.Sort.COUNT_ASCENDING, SortUtils.Sort.COUNT_DESCENDING -> "gui.avaritia.sort.count";
            default -> "";
        };
    }


    private class ItemScrollBar extends SimpleScrollBar {

        private int lastObjectListSize;

        public ItemScrollBar(int x, int y, int weight, int height) {
            super(x, y, weight, height);
            this.setScrollTagSize();
            this.lastObjectListSize = menu.chestContainer.sortedItems.size();
        }

        public void setScrollTagSize() {
            double v = (double) this.height * (9.0D / Math.ceil(menu.chestContainer.sortedItems.size() / 15.0D));
            this.setScrollTagSize(v);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            menu.chestContainer.onScrollTo(scrolledOn);
        }

        @Override
        public void beforeRender() {
            if (menu.chestContainer.sortedItems.size() != lastObjectListSize) {
                setScrollTagSize();
                this.lastObjectListSize = menu.chestContainer.sortedItems.size();
            }
        }
    }



    private class ToggleLockButton extends ImageButton {

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 17, 18, new WidgetSprites(GUI_IMG, GUI_IMG), pButton -> toggleLock());
            MutableComponent componentB = Component.translatable("gui.avaritia.owner", "§c" + ownerName);
            MutableComponent componentC = Component.translatable("gui.avaritia.public");
            if (menu.locked) setTooltip(Tooltip.create(componentB));
            else setTooltip(Tooltip.create(componentC));
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            int uOffset = menu.locked ? 303 : 320;
            var yTexStart = 36;
            if (this.isHovered) {
                pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), uOffset, yTexStart + 18, this.width, this.height, 550, 550);
            } else pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), uOffset, yTexStart, this.width, this.height, 550, 550);
        }
    }


    private class SortButton extends ImageButton {

        public SortButton(int pX, int pY) {
            super(pX, pY, 17, 18, new WidgetSprites(GUI_IMG, GUI_IMG), pButton -> cycleSort());
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            var xTexStart = 303;
            var yTexStart = 0;
            List<FormattedCharSequence> list = new ArrayList<>();
            int xOffset = menu.sortType * 17 + xTexStart;
            if (this.isHovered) {
                pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), xOffset, yTexStart + 18, this.width, this.height, 550, 550);

            } else pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), xOffset, yTexStart, this.width, this.height, 550, 550);
            list.add(Component.translatable(getSortKey(menu.sortType)).getVisualOrderText());
            if (menu.sortType % 2 == 0)
                list.add(Component.translatable("gui.avaritia.sort.ascending").getVisualOrderText());
            else list.add(Component.translatable("gui.avaritia.sort.descending").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.line").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip2").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }
}