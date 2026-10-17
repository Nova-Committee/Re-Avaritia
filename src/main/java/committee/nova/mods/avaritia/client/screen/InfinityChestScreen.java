package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPack;
import committee.nova.mods.avaritia.core.chest.InfinityChestContainer;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.LOCK_BUTTON_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.LOCK_BUTTON_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.LOCK_TEXTURE_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SCROLLBAR_HEIGHT;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SCROLLBAR_WIDTH;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SCROLLBAR_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SCROLLBAR_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SEARCH_HEIGHT;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SEARCH_WIDTH;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SEARCH_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SEARCH_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_HEIGHT;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_WIDTH;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_TEXTURE_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.STORAGE_HEIGHT;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.TITLE_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.TITLE_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.lockTextureX;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.sortTextureX;

/**
 * 无尽箱界面（15×9 虚拟库存）。
 * <p>26.3 Fabric：假槽位交互（左/右键、Shift、拖拽、Q 丢弃、中键克隆）全部由
 * {@code InfinityChestMenu#clicked} 内部处理并发包，屏幕层只负责布局、搜索、滚动与锁/排序按钮。</p>
 */
public class InfinityChestScreen extends AbstractContainerScreen<InfinityChestMenu> {
    private static final Identifier TEXTURE = Const.rl("textures/gui/chest/infinity_chest1.png");
    private static final int TEXTURE_SIZE = 550;
    private static final int LABEL_COLOR = 0xFF404040;

    private EditBox searchBox;
    private final WidgetSprites legacyButtonSprites = new WidgetSprites(TEXTURE, TEXTURE);
    private boolean draggingScrollbar;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 302, 268);
        titleLabelX = TITLE_X;
        titleLabelY = TITLE_Y;
        inventoryLabelX = 62;
        inventoryLabelY = 180;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setValue(menu.filter);
        searchBox.setResponder(value -> {
            menu.filter = value;
            menu.chestContainer.refreshContainer(true);
            NetworkHandler.CHANNEL.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, value));
        });
        addRenderableWidget(UiInspector.name(searchBox, "chest.search"));

        addRenderableWidget(UiInspector.name(
                new LegacyLockButton(leftPos + LOCK_BUTTON_X, topPos + LOCK_BUTTON_Y), "chest.lock"));

        addRenderableWidget(UiInspector.name(new LegacySortButton(leftPos + SORT_BUTTON_X, topPos + SORT_BUTTON_Y), "chest.sort"));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
                imageWidth, STORAGE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + STORAGE_HEIGHT,
                0.0F, STORAGE_HEIGHT,
                imageWidth, imageHeight - STORAGE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        drawScrollbar(graphics);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (!isFake(slot)) {
            super.extractSlot(graphics, slot, mouseX, mouseY);
            return;
        }
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            return;
        }
        String count = slot.getContainerSlot() < menu.chestContainer.formatCount.size()
                ? menu.chestContainer.formatCount.get(slot.getContainerSlot())
                : null;
        graphics.item(stack, slot.x, slot.y);
        graphics.itemDecorations(font, stack, slot.x, slot.y, count);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && isFake(hoveredSlot) && hoveredSlot.hasItem()
                && hoveredSlot.getContainerSlot() < menu.chestContainer.viewingObject.size()) {
            ItemStack stack = hoveredSlot.getItem();
            String id = menu.chestContainer.viewingObject.get(hoveredSlot.getContainerSlot());
            long amount = menu.chest.getRealItemAmount(id);
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(stack));
            tooltip.add(Component.literal(StorageUtils.DECIMAL_FORMAT.format(amount)));
            graphics.setTooltipForNextFrame(font, tooltip, stack.getTooltipImage(), mouseX, mouseY,
                    (Identifier) stack.get(DataComponents.TOOLTIP_STYLE), true);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && insideScrollbar(event.x(), event.y()) && canScroll()) {
            draggingScrollbar = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && searchBox != null
                && searchBox.isMouseOver(event.x(), event.y())) {
            searchBox.setValue("");
            searchBox.setFocused(true);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 8 && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT
                && canScroll()) {
            menu.chestContainer.onMouseScrolled(scrollY > 0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        NetworkHandler.CHANNEL.sendToServer(new C2SInfinityChestFilterPack(menu.containerId, menu.filter));
        super.onClose();
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int x = leftPos + SCROLLBAR_X;
        int y = topPos + SCROLLBAR_Y;
        graphics.fill(x, y, x + SCROLLBAR_WIDTH, y + SCROLLBAR_HEIGHT, 0x66000000);
        int handleHeight = scrollbarHandleHeight();
        int handleY = y + (int) Math.round(menu.chestContainer.getScrollOn() * (SCROLLBAR_HEIGHT - handleHeight));
        graphics.fill(x + 2, handleY, x + SCROLLBAR_WIDTH - 2, handleY + handleHeight,
                canScroll() ? 0xFFC0C0C0 : 0xFF707070);
        UiInspector.region("chest.scrollbar", x, y, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT, null, canScroll());
    }

    private boolean canScroll() {
        return menu.chestContainer.sortedObject.size() > InfinityChestContainer.SIZE;
    }

    private int scrollbarHandleHeight() {
        int variants = menu.chestContainer.sortedObject.size();
        int rows = Math.max(InfinityChestContainer.HEIGHT,
                (variants + InfinityChestContainer.WIDTH - 1) / InfinityChestContainer.WIDTH);
        return Math.max(16, SCROLLBAR_HEIGHT * InfinityChestContainer.HEIGHT / rows);
    }

    private void updateScrollFromMouse(double mouseY) {
        int handleHeight = scrollbarHandleHeight();
        double value = (mouseY - topPos - SCROLLBAR_Y - handleHeight / 2.0D)
                / (SCROLLBAR_HEIGHT - handleHeight);
        menu.chestContainer.onScrollTo(Mth.clamp(value, 0.0D, 1.0D));
    }

    private boolean insideScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT;
    }

    private boolean isFake(Slot slot) {
        return slot != null && slot.container == menu.chestContainer;
    }

    private void toggleLock() {
        if (minecraft == null || minecraft.player == null || !menu.owner.equals(minecraft.player.getUUID())) {
            return;
        }
        menu.locked = !menu.locked;
        searchBox.setFocused(false);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
    }

    private String ownerDisplayName() {
        if (minecraft != null && minecraft.player != null && menu.owner.equals(minecraft.player.getUUID())) {
            return minecraft.player.getGameProfile().name();
        }
        return menu.owner.toString();
    }

    private void cycleSort() {
        if (isShiftDown()) {
            menu.reverseSort();
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
        } else {
            menu.nextSort();
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }
    }

    private boolean isShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    private final class LegacyLockButton extends ImageButton {
        private LegacyLockButton(int x, int y) {
            super(x, y, SORT_BUTTON_WIDTH, SORT_BUTTON_HEIGHT, legacyButtonSprites,
                    ignored -> toggleLock());
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int textureY = LOCK_TEXTURE_Y + (isHovered() ? SORT_BUTTON_HEIGHT : 0);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(),
                    lockTextureX(menu.locked), textureY,
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
            setTooltip(Tooltip.create(menu.locked
                    ? Component.translatable("gui.avaritia.owner", "§c" + ownerDisplayName())
                    : Component.translatable("gui.avaritia.public")));
        }
    }

    private final class LegacySortButton extends ImageButton {
        private LegacySortButton(int x, int y) {
            super(x, y, SORT_BUTTON_WIDTH, SORT_BUTTON_HEIGHT, legacyButtonSprites,
                    ignored -> cycleSort());
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int textureY = SORT_TEXTURE_Y + (isHovered() ? SORT_BUTTON_HEIGHT : 0);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(),
                    sortTextureX(menu.sortType), textureY,
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
