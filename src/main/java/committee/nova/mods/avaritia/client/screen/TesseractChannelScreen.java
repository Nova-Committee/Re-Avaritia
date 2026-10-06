package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SAddChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRemoveChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRenameChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SSetChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Server-authoritative Tesseract channel selector using the legacy 1.20.1 panel. */
public final class TesseractChannelScreen extends BaseContainerScreen<TesseractChannelMenu> {
    static final int WIDTH = 88;
    static final int HEIGHT = 154;
    static final int ROWS = 9;
    private static final int ROW_X = 7;
    private static final int ROW_Y = 8;
    private static final int ROW_WIDTH = 64;
    private static final int ROW_HEIGHT = 12;
    private static final int SCROLLBAR_X = 74;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_WIDTH = 9;
    private static final int SCROLLBAR_HEIGHT = 90;
    private final ClientChannelManager channelManager = ClientChannelManager.getInstance();
    private final WidgetSprites legacySprites;
    private final List<ChannelRowButton> rowButtons = new ArrayList<>();
    private List<ChannelEntry> channels = List.of();
    private EditBox search;
    private int scrollOffset;
    private byte selectedType = -1;
    private int selectedId = -1;
    private boolean draggingScrollbar;
    private ScreenRectangle panel = ScreenRectangle.empty();
    private ScreenRectangle listBounds = ScreenRectangle.empty();
    private ScreenRectangle scrollbar = ScreenRectangle.empty();
    private ScreenRectangle scrollbarHandle = ScreenRectangle.empty();
    public TesseractChannelScreen(TesseractChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Res.BLACK_HOLE_CHANNEL_SELECT, WIDTH, HEIGHT, 256, 256);
        legacySprites = new WidgetSprites(Res.BLACK_HOLE_CHANNEL_SELECT, Res.BLACK_HOLE_CHANNEL_SELECT);
    }

    @Override
    protected void subInit() {
        selectedType = channelManager.selectedType();
        selectedId = channelManager.selectedId();
        panel = new ScreenRectangle(leftPos, topPos, imageWidth, imageHeight);
        listBounds = PortableLayout.translate(new ScreenRectangle(ROW_X, ROW_Y, ROW_WIDTH, ROW_HEIGHT * ROWS), panel.left(), panel.top());
        scrollbar = PortableLayout.translate(new ScreenRectangle(SCROLLBAR_X, SCROLLBAR_Y, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT), panel.left(), panel.top());

        search = new EditBox(font, leftPos + 7, topPos + 118, 76, 12,
                Component.translatable("gui.avaritia.search"));
        search.setBordered(false);
        search.setMaxLength(64);
        search.setHint(Component.translatable("gui.avaritia.search"));
        search.setResponder(ignored -> refreshChannels());
        addRenderableWidget(UiInspector.name(search, "channels.search"));

        rowButtons.clear();
        for (int row = 0; row < ROWS; row++) {
            ChannelRowButton button = new ChannelRowButton(listBounds.left(),
                    listBounds.top() + row * ROW_HEIGHT, row);
            rowButtons.add(button);
            addRenderableWidget(button);
        }

        var addButton = new LegacyIconButton(leftPos + 7, topPos + 131, 18, 18, 202, 0, 18, this::openAddDialog);
        addButton.setTooltip(Tooltip.create(Component.translatable("gui.avaritia.addChannel.tip2")));
        addRenderableWidget(UiInspector.name(addButton, "channels.add"));
        addRenderableWidget(UiInspector.name(new LegacyIconButton(leftPos + 27, topPos + 131, 16, 16, 202, 34, 16,
                this::openRenameDialog), "channels.rename"));
        addRenderableWidget(UiInspector.name(new LegacyIconButton(leftPos + 47, topPos + 131, 16, 16, 202, 18, 16,
                this::removeSelected), "channels.delete"));
        addRenderableWidget(UiInspector.name(new LegacyIconButton(leftPos + 67, topPos + 131, 16, 16, 202, 50, 16,
                this::openSelected), "channels.back"));

        channelManager.listenSelector(this::refreshChannels);
        refreshChannels();
    }


    private void refreshChannels() {
        String filter = search == null ? "" : search.getValue().strip().toLowerCase(Locale.ROOT);
        List<ChannelEntry> refreshed = new ArrayList<>();
        appendChannels(refreshed, channelManager.mine(), (byte) 0, filter);
        appendChannels(refreshed, channelManager.terminalOwner(), (byte) 1, filter);
        appendChannels(refreshed, channelManager.shared(), (byte) 2, filter);
        channels = List.copyOf(refreshed);
        setScrollOffset(scrollOffset);
    }

    private static void appendChannels(List<ChannelEntry> destination, Map<Integer, String> source,
                                       byte type, String filter) {
        source.entrySet().stream()
                .filter(entry -> filter.isEmpty() || entry.getValue().toLowerCase(Locale.ROOT).contains(filter))
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new ChannelEntry(type, entry.getKey(), entry.getValue()))
                .forEach(destination::add);
    }

    private void openAddDialog() {
        PortableUi.prompt(this, Component.literal("Channel name"), "Channel", 64, false,
                input -> send(new C2SAddChannelPack(menu.containerId, input, isShiftDown())));
    }

    private void openRenameDialog() {
        if (!hasSelection()) {
            return;
        }
        byte type = selectedType;
        int id = selectedId;
        String current = search.getValue().strip();
        PortableUi.prompt(this, Component.literal("Rename channel"), current.isEmpty() ? "Channel" : current, 64, false, input -> {
            if (selectedType == type && selectedId == id) {
                send(new C2SRenameChannelPack(menu.containerId, type, id, input));
            }
        });
    }


    private void removeSelected() {
        if (hasSelection()) {
            send(new C2SRemoveChannelPack(menu.containerId, selectedType, selectedId));
        }
    }

    private void openSelected() {
        if (hasSelection()) {
            send(new C2SSetChannelPack(menu.containerId, selectedType, selectedId));
        }
    }

    private void selectRow(int row) {
        ChannelEntry entry = rowEntry(row);
        if (entry != null) {
            selectedType = entry.type();
            selectedId = entry.id();
        }
    }

    private ChannelEntry rowEntry(int row) {
        int index = scrollOffset + row;
        return index >= 0 && index < channels.size() ? channels.get(index) : null;
    }

    private boolean hasSelection() {
        return selectedType >= 0 && selectedType <= 2 && selectedId >= 0;
    }

    private boolean isSelected(ChannelEntry entry) {
        return entry.type() == selectedType && entry.id() == selectedId;
    }

    private boolean isShiftDown() {
        var window = minecraft.getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private static void send(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        NetworkHandler.sendToServer(payload);
    }

    static int maxScrollOffset(int channelCount) {
        return Math.max(0, channelCount - ROWS);
    }

    private void setScrollOffset(int requestedOffset) {
        scrollOffset = Math.max(0, Math.min(maxScrollOffset(channels.size()), requestedOffset));
        for (int row = 0; row < rowButtons.size(); row++) {
            rowButtons.get(row).visible = rowEntry(row) != null;
        }
        updateScrollbar();
    }

    private void updateScrollbar() {
        int handleHeight = channels.size() <= ROWS ? scrollbar.height()
                : Math.max(8, ROWS * scrollbar.height() / Math.max(1, channels.size()));
        int maximumOffset = maxScrollOffset(channels.size());
        int travel = scrollbar.height() - handleHeight;
        int handleY = scrollbar.top() + (maximumOffset == 0 ? 0 : scrollOffset * travel / maximumOffset);
        scrollbarHandle = new ScreenRectangle(scrollbar.left(), handleY, scrollbar.width(), handleHeight);
    }

    private void updateScrollFromMouse(double mouseY) {
        int maximumOffset = maxScrollOffset(channels.size());
        if (maximumOffset == 0) {
            setScrollOffset(0);
            return;
        }
        int handleHeight = scrollbarHandle.height();
        double ratio = (mouseY - scrollbar.top() - handleHeight / 2.0D)
                / Math.max(1, scrollbar.height() - handleHeight);
        setScrollOffset((int) Math.round(Math.max(0.0D, Math.min(1.0D, ratio)) * maximumOffset));
    }

    @Override
    protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        search.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (channels.size() > ROWS) {
            graphics.fill(scrollbarHandle.left() + 2, scrollbarHandle.top(),
                    scrollbarHandle.right() - 2, scrollbarHandle.bottom(), 0xFF777777);
            graphics.outline(scrollbarHandle.left() + 1, scrollbarHandle.top() - 1,
                    scrollbarHandle.width() - 2, scrollbarHandle.height() + 2, 0xFF202020);
        }
        UiInspector.region("channels.list", listBounds, listBounds, true);
        UiInspector.region("channels.scrollbar", scrollbar, null, channels.size() > ROWS);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D && PortableLayout.contains(panel, mouseX, mouseY)) {
            setScrollOffset(scrollOffset + (scrollY > 0.0D ? -1 : 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && search.isMouseOver(event.x(), event.y())) {
            search.setValue("");
            search.setFocused(true);
            return true;
        }
        if (event.button() == 0 && PortableLayout.contains(scrollbar, event.x(), event.y())) {
            draggingScrollbar = true;
            updateScrollFromMouse(event.y());
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
        if (event.button() == 0) draggingScrollbar = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return search.keyPressed(event) || search.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void onClose() {
        channelManager.closeSelector();
        super.onClose();
    }

    record ChannelEntry(byte type, int id, String name) {
    }

    private final class ChannelRowButton extends ImageButton {
        private final int row;

        private ChannelRowButton(int x, int y, int row) {
            super(x, y, ROW_WIDTH, ROW_HEIGHT, legacySprites, ignored -> selectRow(row));
            this.row = row;
        }

        @Override
        public void extractContents(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            ChannelEntry entry = rowEntry(row);
            if (entry == null) return;
            int vOffset = 154;
            if (isHoveredOrFocused()) vOffset += 12;
            if (isSelected(entry)) vOffset += 24;
            graphics.blit(RenderPipelines.GUI_TEXTURED, Res.BLACK_HOLE_CHANNEL_SELECT,
                    getX(), getY(), 0, vOffset, width, height, 256, 256);
            int color = switch (entry.type()) {
                case 0 -> 0xFF55FF55;
                case 1 -> 0xFFFF5555;
                default -> 0xFFFFFFFF;
            };
            String label = font.plainSubstrByWidth(entry.name(), ROW_WIDTH - 8);
            graphics.text(font, Component.literal(label), getX() + 4, getY() + 2, color, false);
        }
    }

    private final class LegacyIconButton extends ImageButton {
        private final int u;
        private final int v;
        private final int hoverOffset;

        private LegacyIconButton(int x, int y, int width, int height, int u, int v, int hoverOffset,
                                 Runnable action) {
            super(x, y, width, height, legacySprites, ignored -> action.run());
            this.u = u;
            this.v = v;
            this.hoverOffset = hoverOffset;
        }

        @Override
        public void extractContents(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int uOffset = isHoveredOrFocused() ? u + hoverOffset : u;
            graphics.blit(RenderPipelines.GUI_TEXTURED, Res.BLACK_HOLE_CHANNEL_SELECT,
                    getX(), getY(), uOffset, v, width, height, 256, 256);
        }
    }
}
