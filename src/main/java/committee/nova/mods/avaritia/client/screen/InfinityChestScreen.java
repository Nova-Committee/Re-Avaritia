package committee.nova.mods.avaritia.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.SimpleScrollBar;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPack;
import committee.nova.mods.avaritia.core.chest.ClientChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: cnlimiter
 */
public class InfinityChestScreen extends BaseContainerScreen<InfinityChestMenu> {
    @Setter
    @Getter
    private int blitOffset;

    private static final ResourceLocation GUI_IMG = Res.INFINITY_CHEST_TEX;
    private final String ownerName;
    private String lastHoveredObject="";
    private long lastCount = 0;
    private String lastFormatCountTemp = "";
    private SortButton sortButton;
    private ItemScrollBar scrollBar;
    private EditBox searchBox;
    private CraftToChannelButton craftToChannelButton;
    private CraftToInventoryButton craftToInventoryButton;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, null, 218, 256);
        this.ownerName = ClientChestManager.getInstance().getUserName(this.getMenu().owner);
    }

    @Override
    protected void subInit() {
        super.subInit();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;

        this.scrollBar = new ItemScrollBar(leftPos + 198, topPos + 17, 15, 118);
        this.scrollBar.setScrolledOn(menu.chestContainer.getScrollOn());
        this.addRenderableWidget(scrollBar);
        this.addRenderableWidget(new ToggleLockButton(this.leftPos + 185, this.topPos + 202));
        this.sortButton = new SortButton(this.leftPos + 185, this.topPos + 219);
        this.addRenderableWidget(sortButton);

        this.searchBox = new EditBox(this.font, leftPos + 104, topPos + 4, 90, 12, Component.translatable("gui.avaritia.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setBordered(false);
        this.searchBox.setValue(menu.filter);
        this.addRenderableWidget(searchBox);

        this.craftToChannelButton = new CraftToChannelButton(leftPos + 160, topPos + 143);
        this.craftToInventoryButton = new CraftToInventoryButton(leftPos + 160, topPos + 171);
        this.addRenderableWidget(craftToChannelButton);
        this.addRenderableWidget(craftToInventoryButton);
        menu.chestContainer.refreshContainer(true);
    }

    public void blit(GuiGraphics pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        pPoseStack.blit(GUI_IMG, pX, pY, this.blitOffset, (float) pUOffset, (float) pVOffset, pUWidth, pVHeight, 256, 256);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX + 17, this.inventoryLabelY + 122, 4210752, false);
    }

    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        this.blit(pGuiGraphics, this.leftPos, this.topPos, 0, 0, imageWidth, 6);
        this.blit(pGuiGraphics, this.leftPos, this.topPos, 0, 0, imageWidth, 68);//前三行
        this.blit(pGuiGraphics, this.leftPos, this.topPos + 68, 0, 17, imageWidth, 51);//再加三行
        this.blit(pGuiGraphics, this.leftPos, this.topPos + 119, 0, 17, imageWidth, 17);//再加一行
        this.blit(pGuiGraphics, this.leftPos, this.topPos + 136, 0, 69, imageWidth, 141);//物品栏
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderDummyCount(pGuiGraphics);
    }

    public void renderDummyCount(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        for (int i = 0; i < menu.chestContainer.formatCount.size(); i++) {
            Slot slot = menu.slots.get(i + 51);
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
            if (hoveredSlot.index >= 51) {
                if (menu.getCarried().getCount() == 1)
                    renderObjectStorageTooltip(pPoseStack, pX, pY);
                else
                    renderCounterTooltip(pPoseStack, pX, pY);
            } else if (!hoveredSlot.getItem().isEmpty() && menu.getCarried().isEmpty())
                pPoseStack.renderTooltip(font, this.hoveredSlot.getItem(), pX, pY);
        } else {
            if (isInsideEditBox(pX, pY)) {
                List<Component> list = new ArrayList<>();
                list.add(Component.translatable("gui.avaritia.search.tip1"));
                list.add(Component.translatable("gui.avaritia.search.tip2"));
                list.add(Component.translatable("gui.avaritia.search.tip3"));
                pPoseStack.renderComponentTooltip(font, list, pX, pY);
            }
        }
    }

    private void renderCounterTooltip(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
        if ((hoveredSlot.index - 51) >= menu.chestContainer.viewingObject.size()) return;
        String hoveredObject = menu.chestContainer.viewingObject.get(hoveredSlot.index - 51);
        List<Component> components;
        long count;
        components = getTooltipFromItem(minecraft, hoveredSlot.getItem());
        count = menu.channel.getRealItemAmount(hoveredObject);

        if (!hoveredObject.equals(lastHoveredObject)) {
            String formatCount = StorageUtils.DECIMAL_FORMAT.format(count);
            components.add(Component.literal(formatCount));
            this.lastHoveredObject = hoveredObject;
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
        boolean hasCapability = carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()
                || carried.getCapability(ForgeCapabilities.ENERGY).isPresent()
                || carried.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
        if (hasCapability) {
            List<Component> components = Lists.newArrayList();
            if ((hoveredSlot.index - 51) < menu.chestContainer.viewingObject.size()) {
                components.add(Component.translatable("gui.avaritia.capability.tip1", hoveredSlot.getItem().getHoverName()));
            }
            components.add(Component.translatable("gui.avaritia.capability.tip2"));
            components.add(Component.translatable("gui.avaritia.capability.tip3"));
            pPoseStack.renderTooltip(font, components, ItemStack.EMPTY.getTooltipImage(), pMouseX, pMouseY);
        } else renderCounterTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (searchBox.isFocused()) searchBox.tick();
    }

    @Override
    public void onClose() {
        NetworkHandler.CHANNEL.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, menu.filter));
        ((ClientChestHandler) menu.channel).removeListener();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean lshift = InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.KEY_LSHIFT);
        if (pButton == 1) {
            //搜索框
            if (searchBox.isMouseOver(pMouseX, pMouseY)) {
                menu.filter = "";
                searchBox.setValue("");
                menu.chestContainer.refreshContainer(true);
                searchBox.setFocused(true);
                searchBox.setEditable(true);
            } else if (craftToChannelButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 6);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 7);
            } else if (craftToInventoryButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 11);
            }
        } else {
            if (craftToChannelButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 9);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 8);
            } else if (craftToInventoryButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 13);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 12);
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
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pMouseX >= leftPos + 5 && pMouseX <= leftPos + 214 && pMouseY >= topPos + 17 && pMouseY <= topPos + 18 + 119 && scrollBar.canScroll()) {
            if (pDelta <= 0) scrollBar.setScrolledOn(menu.chestContainer.onMouseScrolled(false));
            else scrollBar.setScrolledOn(menu.chestContainer.onMouseScrolled(true));
            return true;
        } else return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double) pX && pMouseX < (double) (pX + pWidth) && pMouseY >= (double) pY && pMouseY < (double) (pY + pHeight);
    }

    private boolean isInsideEditBox(double pMouseX, double pMouseY) {
        return pMouseX >= leftPos + 104 && pMouseX <= leftPos + 194 && pMouseY >= topPos + 4 && pMouseY <= topPos + 16;
    }

    private void toggleLock() {
        if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            this.menu.locked = !this.menu.locked;
            this.searchBox.setFocused(false);
            NetworkHandler.CHANNEL.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, menu.filter));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    private void cycleSort() {
        if (InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            menu.reverseSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
        } else {
            menu.nextSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }
    }

    private String getSortKey(int sortType) {
        return switch (sortType) {
            case SortUtils.Sort.ID_ASCENDING, SortUtils.Sort.ID_DESCENDING -> "gui.avaritia.sort.id";
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
            this.lastObjectListSize = menu.chestContainer.sortedObject.size();
        }

        public void setScrollTagSize() {
            double v = (double) this.height * (7.0D / Math.ceil(menu.chestContainer.sortedObject.size() / 11.0D));
            this.setScrollTagSize(v);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            menu.chestContainer.onScrollTo(scrolledOn);
        }

        @Override
        public void beforeRender() {
            if (menu.chestContainer.sortedObject.size() != lastObjectListSize) {
                setScrollTagSize();
                this.lastObjectListSize = menu.chestContainer.sortedObject.size();
            }
        }
    }



    private class ToggleLockButton extends ImageButton {

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 43, GUI_IMG, pButton -> toggleLock());
            MutableComponent componentB = Component.translatable("gui.avaritia.owner", "§c" + ownerName);
            MutableComponent componentC = Component.translatable("gui.avaritia.public");
            if (menu.locked) setTooltip(Tooltip.create(componentB));
            else setTooltip(Tooltip.create(componentC));
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            int uOffset = menu.locked ? 235 : 219;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), uOffset, this.yTexStart, this.width, this.height, 256, 256);
        }
    }


    private class SortButton extends ImageButton {

        public SortButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 75, GUI_IMG, pButton -> cycleSort());
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            int vOffset = menu.sortType * 16 + this.yTexStart;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, vOffset, this.width, this.height, 256, 256);
            list.add(Component.translatable(getSortKey(menu.sortType)).getVisualOrderText());
            if (menu.sortType % 2 == 0)
                list.add(Component.translatable("gui.avaritia.sort.ascending").getVisualOrderText());
            else list.add(Component.translatable("gui.avaritia.sort.descending").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.line").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip2").getVisualOrderText());
            if (sortButton.isHovered) setTooltipForNextRenderPass(list);
        }
    }

    private class CraftToChannelButton extends ImageButton {

        public CraftToChannelButton(int x, int y) {
            super(x, y, 17, 9, 219, 0, GUI_IMG, pButton -> {
            });

        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            list.add(Component.translatable("gui.avaritia.craft.channel").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip2").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip3").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip4").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }

    private class CraftToInventoryButton extends ImageButton {
        public CraftToInventoryButton(int x, int y) {
            super(x, y, 17, 9, 219, 18, GUI_IMG, pButton -> {
            });
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            list.add(Component.translatable("gui.avaritia.craft.inv").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip2").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip3").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip4").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }
}
