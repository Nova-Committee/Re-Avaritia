package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SAddChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRemoveChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRenameChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SSetChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
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

    public TesseractChannelScreen(TesseractChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, Res.BLACK_HOLE_CHANNEL_SELECT, WIDTH, HEIGHT, 256, 256);
        legacySprites = new WidgetSprites(Res.BLACK_HOLE_CHANNEL_SELECT, Res.BLACK_HOLE_CHANNEL_SELECT);
    }

    @Override
    protected void subInit() {
        selectedType = channelManager.selectedType();
        selectedId = channelManager.selectedId();

        search = new EditBox(font, leftPos + 7, topPos + 118, 76, 12,
                Component.translatable("gui.avaritia.search"));
        search.setBordered(false);
        search.setMaxLength(64);
        search.setHint(Component.translatable("gui.avaritia.search"));
        search.setResponder(ignored -> refreshChannels());
        addRenderableWidget(search);

        rowButtons.clear();
        for (int row = 0; row < ROWS; row++) {
            ChannelRowButton button = new ChannelRowButton(leftPos + ROW_X,
                    topPos + ROW_Y + row * ROW_HEIGHT, row);
            rowButtons.add(button);
            addRenderableWidget(button);
        }

        addRenderableWidget(new LegacyIconButton(leftPos + 7, topPos + 131, 18, 18, 202, 0, 18,
                () -> addChannel(isShiftDown())));
        addRenderableWidget(new LegacyIconButton(leftPos + 27, topPos + 131, 16, 16, 202, 34, 16,
                this::renameSelected));
        addRenderableWidget(new LegacyIconButton(leftPos + 47, topPos + 131, 16, 16, 202, 18, 16,
                this::removeSelected));
        addRenderableWidget(new LegacyIconButton(leftPos + 67, topPos + 131, 16, 16, 202, 50, 16,
                this::openSelected));

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

    private void addChannel(boolean shared) {
        String name = search.getValue().strip();
        if (!name.isEmpty()) {
            send(new C2SAddChannelPack(menu.containerId, name, shared));
        }
    }

    private void renameSelected() {
        String name = search.getValue().strip();
        if (hasSelection() && !name.isEmpty()) {
            send(new C2SRenameChannelPack(menu.containerId, selectedType, selectedId, name));
        }
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
    }

    private int scrollbarHandleHeight() {
        if (channels.size() <= ROWS) return SCROLLBAR_HEIGHT;
        return Math.max(8, ROWS * SCROLLBAR_HEIGHT / channels.size());
    }

    private int scrollbarHandleY() {
        int maximumOffset = maxScrollOffset(channels.size());
        int travel = SCROLLBAR_HEIGHT - scrollbarHandleHeight();
        return topPos + SCROLLBAR_Y + (maximumOffset == 0 ? 0 : scrollOffset * travel / maximumOffset);
    }

    private void updateScrollFromMouse(double mouseY) {
        int maximumOffset = maxScrollOffset(channels.size());
        if (maximumOffset == 0) {
            setScrollOffset(0);
            return;
        }
        int handleHeight = scrollbarHandleHeight();
        double ratio = (mouseY - topPos - SCROLLBAR_Y - handleHeight / 2.0D)
                / (SCROLLBAR_HEIGHT - handleHeight);
        setScrollOffset((int) Math.round(Math.max(0.0D, Math.min(1.0D, ratio)) * maximumOffset));
    }

    @Override
    protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        search.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (channels.size() > ROWS) {
            int handleY = scrollbarHandleY();
            int handleHeight = scrollbarHandleHeight();
            graphics.fill(leftPos + SCROLLBAR_X + 2, handleY,
                    leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH - 2, handleY + handleHeight, 0xFF777777);
            graphics.outline(leftPos + SCROLLBAR_X + 1, handleY - 1,
                    SCROLLBAR_WIDTH - 2, handleHeight + 2, 0xFF202020);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D && mouseX >= leftPos && mouseX < leftPos + WIDTH
                && mouseY >= topPos && mouseY < topPos + HEIGHT) {
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
        if (event.button() == 0 && event.x() >= leftPos + SCROLLBAR_X
                && event.x() < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && event.y() >= topPos + SCROLLBAR_Y
                && event.y() < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT) {
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
