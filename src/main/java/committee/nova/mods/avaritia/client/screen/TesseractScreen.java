package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.widget.SimpleScrollBar;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SChannelFilterPack;
import committee.nova.mods.avaritia.core.channel.ClientChannel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class TesseractScreen extends AbstractContainerScreen<TesseractMenu> {
    private static final ResourceLocation GUI_IMG = Res.BLACK_HOLE_CHANNEL_PANEL;
    private ItemScrollBar scrollBar;
    private EditBox searchBox;
    private String previousFilter = "";

    public TesseractScreen(TesseractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 218;
        imageHeight = 276;
        inventoryLabelY = 181;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - imageWidth) / 2;
        topPos = Math.max(0, (height - imageHeight) / 2);

        searchBox = new EditBox(font, leftPos + 104, topPos + 4, 90, 12,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setValue(menu.filter);
        previousFilter = menu.filter;
        addRenderableWidget(searchBox);

        scrollBar = new ItemScrollBar(leftPos + 199, topPos + 17, 12, menu.craftingMode ? 118 : 152);
        scrollBar.setScrolledOn(menu.dummyChannelContainer.getScrollOn());
        addRenderableWidget(scrollBar);

        addRenderableWidget(sideButton("C", 195, 1, () -> {
            menu.craftingMode = !menu.craftingMode;
            menu.dummyChannelContainer.refreshContainer(true);
            scrollBar.setSize(12, menu.craftingMode ? 118 : 152);
        }));
        addRenderableWidget(sideButton("L", 211, 0, () -> {
            if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
                menu.locked = !menu.locked;
            }
        }));
        addRenderableWidget(sideButton("Ch", 227, 5, () -> {
        }));
        addRenderableWidget(sideButton("S", 243, -1, () -> {
            int id;
            if (hasShiftDown()) {
                menu.reverseSort();
                id = 3;
            } else {
                menu.nextSort();
                id = 2;
            }
            sendMenuButton(id);
            menu.dummyChannelContainer.refreshContainer(true);
        }));
        addRenderableWidget(sideButton("V", 259, 4, () -> {
            menu.changeViewType();
            menu.dummyChannelContainer.refreshContainer(true);
        }));

        addRenderableWidget(craftButton("→C", 78, 6));
        addRenderableWidget(craftButton("→I", 96, 10));
        addRenderableWidget(craftButton("Drop", 114, 14));

        menu.dummyChannelContainer.refreshContainer(true);
    }

    private Button sideButton(String text, int yOffset, int serverId, Runnable localAction) {
        return Button.builder(Component.literal(text), ignored -> {
            localAction.run();
            if (serverId >= 0) sendMenuButton(serverId);
        }).bounds(leftPos + 197, topPos + yOffset, 21, 15).build();
    }

    private Button craftButton(String text, int xOffset, int baseId) {
        Button button = Button.builder(Component.literal(text), ignored -> {
            int id = hasShiftDown() ? baseId + 3 : baseId;
            sendMenuButton(id);
        }).bounds(leftPos + xOffset, topPos + 174, 38, 16).build();
        return button;
    }

    private void sendMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        String filter = searchBox.getValue().toLowerCase(java.util.Locale.ROOT);
        if (!previousFilter.equals(filter)) {
            previousFilter = filter;
            menu.filter = filter;
            menu.dummyChannelContainer.refreshContainer(true);
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC151515);
        graphics.blit(GUI_IMG, leftPos, topPos, 0, 0, imageWidth, Math.min(256, imageHeight), 256, 256);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 23, 184, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderStoredCounts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderStoredCounts(GuiGraphics graphics) {
        int count = Math.min(menu.dummyChannelContainer.formatCount.size(), menu.slots.size() - 51);
        for (int index = 0; index < count; index++) {
            Slot slot = menu.slots.get(51 + index);
            if (!slot.isActive() || slot.getItem().isEmpty()) continue;
            String value = menu.dummyChannelContainer.formatCount.get(index);
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + slot.x + 16, topPos + slot.y + 11, 300);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(font, value, -font.width(value), 0, 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setValue("");
            searchBox.setFocused(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + 5 && mouseX <= leftPos + 214
                && mouseY >= topPos + 17 && mouseY <= topPos + 170 && scrollBar.canScroll()) {
            scrollBar.setScrolledOn(menu.dummyChannelContainer.onMouseScrolled(scrollY > 0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new C2SChannelFilterPack(menu.containerId, menu.filter));
        if (menu.channel instanceof ClientChannel channel) {
            channel.removeListener();
        }
        super.onClose();
    }

    private final class ItemScrollBar extends SimpleScrollBar {
        private int lastSize = -1;

        private ItemScrollBar(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            menu.dummyChannelContainer.onScrollTo(scrolledOn);
        }

        @Override
        public void beforeRender() {
            int size = menu.dummyChannelContainer.sortedObject.size();
            int visibleRows = menu.craftingMode ? 7 : 9;
            int totalRows = Math.max(1, (int) Math.ceil(size / 11.0D));
            if (lastSize != size) {
                setScrollTagSize(getHeight() * Math.min(1.0D, visibleRows / (double) totalRows));
                lastSize = size;
            }
            visible = totalRows > visibleRows;
        }
    }
}
