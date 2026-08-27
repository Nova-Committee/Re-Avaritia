package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestActionPacket;
import committee.nova.mods.avaritia.common.net.chest.C2SInfinityChestFilterPacket;
import committee.nova.mods.avaritia.common.net.chest.ChannelAction;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_HEIGHT;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_WIDTH;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_X;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_BUTTON_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.SORT_TEXTURE_Y;
import static committee.nova.mods.avaritia.client.screen.InfinityChestScreenLayout.sortTextureX;

/** 无尽箱 15×7 虚拟库存界面。 */
public class InfinityChestScreen extends AbstractContainerScreen<InfinityChestMenu> {
    private static final Identifier TEXTURE = Const.rl("textures/gui/chest/infinity_chest1.png");
    private static final int TEXTURE_SIZE = 550;
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int SCROLLBAR_X = 282;
    private static final int SCROLLBAR_Y = 16;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 124;
    private static final int STORAGE_HEIGHT = 142;
    private static final int LEGACY_PLAYER_SECTION_Y = 178;

    private EditBox searchBox;
    private Button lockButton;
    private final WidgetSprites legacyButtonSprites = new WidgetSprites(TEXTURE, TEXTURE);
    private boolean draggingScrollbar;
    private ItemResource lastDragged = ItemResource.EMPTY;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 302, 238);
        inventoryLabelX = 62;
        inventoryLabelY = 144;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + 187, topPos + 4, 89, 10,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setValue(menu.getFilter());
        searchBox.setResponder(value -> {
            menu.setFilterFromClient(value);
            NetworkHandler.sendToServer(new C2SInfinityChestFilterPacket(menu.containerId, value));
        });
        addRenderableWidget(searchBox);

        lockButton = addRenderableWidget(Button.builder(lockText(), button -> {
            menu.toggleLockClient();
            button.setMessage(lockText());
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }).bounds(leftPos + 231, topPos + 151, 17, 18).build());
        lockButton.active = menu.getOwner().equals(minecraft.player.getUUID());

        addRenderableWidget(new LegacySortButton(leftPos + SORT_BUTTON_X, topPos + SORT_BUTTON_Y));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
                imageWidth, STORAGE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + STORAGE_HEIGHT,
                0.0F, LEGACY_PLAYER_SECTION_Y,
                imageWidth, imageHeight - STORAGE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        drawScrollbar(graphics);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack stack, Slot slot, String itemCount) {
        int virtualIndex = menu.virtualIndex(slot);
        String count = virtualIndex >= 0 ? menu.getChestContainer().formattedAmount(virtualIndex) : itemCount;
        super.renderSlotContents(graphics, stack, slot, count);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && isVirtual(hoveredSlot) && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            long amount = menu.getChestContainer().amount(menu.virtualIndex(hoveredSlot));
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(stack));
            tooltip.add(Component.literal(InfinityChestContainer.formatExactAmount(amount)));
            graphics.setTooltipForNextFrame(font, tooltip, stack.getTooltipImage(), stack, mouseX, mouseY,
                    stack.get(DataComponents.TOOLTIP_STYLE));
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isVirtual(hoveredSlot) && (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                || event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                || event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
            ChannelAction action;
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                action = ChannelAction.CLONE;
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                action = isShiftDown() ? ChannelAction.LEFT_SHIFT : ChannelAction.LEFT_CLICK;
            } else {
                action = isShiftDown() ? ChannelAction.RIGHT_SHIFT : ChannelAction.RIGHT_CLICK;
            }
            sendAction(action, hoveredSlot);
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && insideScrollbar(event.x(), event.y())
                && menu.getChestContainer().canScroll()) {
            draggingScrollbar = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(event.x(), event.y())) {
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
        if (isVirtual(hoveredSlot)) {
            ItemResource resource = menu.getChestContainer().resource(menu.virtualIndex(hoveredSlot));
            if (!resource.isEmpty() && !resource.equals(lastDragged)) {
                ChannelAction action = switch (event.button()) {
                    case GLFW.GLFW_MOUSE_BUTTON_LEFT -> ChannelAction.LEFT_DRAG;
                    case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> ChannelAction.RIGHT_DRAG;
                    case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> ChannelAction.DRAG_CLONE;
                    default -> null;
                };
                if (action != null) {
                    lastDragged = resource;
                    NetworkHandler.sendToServer(new C2SInfinityChestActionPacket(menu.containerId, action, resource));
                    return true;
                }
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        lastDragged = ItemResource.EMPTY;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 5 && mouseX < leftPos + 278
                && mouseY >= topPos + 16 && mouseY < topPos + STORAGE_HEIGHT
                && menu.getChestContainer().canScroll()) {
            menu.getChestContainer().scrollRows(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isVirtual(hoveredSlot) && minecraft.options.keyDrop.matches(event)) {
            sendAction(isControlDown() ? ChannelAction.THROW_STACK : ChannelAction.THROW_ONE, hoveredSlot);
            return true;
        }
        if (isVirtual(hoveredSlot) && minecraft.options.keyPickItem.matches(event)) {
            sendAction(ChannelAction.CLONE, hoveredSlot);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        NetworkHandler.sendToServer(new C2SInfinityChestFilterPacket(menu.containerId, menu.getFilter()));
        super.onClose();
    }

    private void sendAction(ChannelAction action, Slot slot) {
        ItemResource resource = menu.getChestContainer().resource(menu.virtualIndex(slot));
        if (!resource.isEmpty() || !menu.getCarried().isEmpty()) {
            NetworkHandler.sendToServer(new C2SInfinityChestActionPacket(menu.containerId, action, resource));
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int x = leftPos + SCROLLBAR_X;
        int y = topPos + SCROLLBAR_Y;
        graphics.fill(x, y, x + SCROLLBAR_WIDTH, y + SCROLLBAR_HEIGHT, 0x66000000);
        int handleHeight = scrollbarHandleHeight();
        int handleY = y + (int) Math.round(menu.getChestContainer().getScroll() * (SCROLLBAR_HEIGHT - handleHeight));
        graphics.fill(x + 2, handleY, x + SCROLLBAR_WIDTH - 2, handleY + handleHeight,
                menu.getChestContainer().canScroll() ? 0xFFC0C0C0 : 0xFF707070);
    }

    private int scrollbarHandleHeight() {
        int variants = menu.getChestContainer().totalVariants();
        int rows = Math.max(InfinityChestContainer.HEIGHT,
                (variants + InfinityChestContainer.WIDTH - 1) / InfinityChestContainer.WIDTH);
        return Math.max(16, SCROLLBAR_HEIGHT * InfinityChestContainer.HEIGHT / rows);
    }

    private void updateScrollFromMouse(double mouseY) {
        int handleHeight = scrollbarHandleHeight();
        double value = (mouseY - topPos - SCROLLBAR_Y - handleHeight / 2.0D)
                / (SCROLLBAR_HEIGHT - handleHeight);
        menu.getChestContainer().scrollTo(value);
    }

    private boolean insideScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT;
    }

    private boolean isVirtual(Slot slot) {
        return slot != null && menu.virtualIndex(slot) >= 0;
    }

    private Component lockText() {
        return Component.literal(menu.isLocked() ? "L" : "U");
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
        var window = minecraft.getWindow();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT);
    }

    private boolean isControlDown() {
        var window = minecraft.getWindow();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_RCONTROL);
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
                    sortTextureX(menu.getSortType()), textureY,
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
