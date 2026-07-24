package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.StringInputScreen;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SAddChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRenameChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SSetChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 使用 1.20.1 材质图集的超立方体频道选择界面。 */
public class TesseractChannelScreen extends AbstractContainerScreen<TesseractChannelMenu> {
    static final int WIDTH = 88;
    static final int HEIGHT = 154;
    static final int ROWS = 9;

    private static final ResourceLocation GUI_IMG = Res.BLACK_HOLE_CHANNEL_SELECT;
    private static final int TEXTURE_SIZE = 256;
    private static final int ROW_X = 7;
    private static final int ROW_Y = 8;
    private static final int ROW_WIDTH = 64;
    private static final int ROW_HEIGHT = 12;
    private static final int SCROLLBAR_X = 74;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_WIDTH = 9;
    private static final int SCROLLBAR_HEIGHT = 90;

    private final ClientChannelManager channelManager = ClientChannelManager.getInstance();
    private final WidgetSprites legacySprites = new WidgetSprites(GUI_IMG, GUI_IMG);
    private final List<ChannelEntry> filteredChannels = new ArrayList<>();
    private final List<ChannelRowButton> channelButtons = new ArrayList<>();
    private EditBox searchBox;
    private int scrollOffset;
    private boolean draggingScrollbar;

    public TesseractChannelScreen(TesseractChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        searchBox = new EditBox(font, leftPos + 7, topPos + 118, 76, 12,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setHint(Component.translatable("gui.avaritia.search"));
        searchBox.setResponder(ignored -> updateChannelList());
        addRenderableWidget(searchBox);

        channelButtons.clear();
        for (int row = 0; row < ROWS; row++) {
            ChannelRowButton button = new ChannelRowButton(leftPos + ROW_X,
                    topPos + ROW_Y + row * ROW_HEIGHT, row);
            channelButtons.add(button);
            addRenderableWidget(button);
        }

        LegacyIconButton addButton = new LegacyIconButton(leftPos + 7, topPos + 131,
                18, 18, 202, 0, 18, this::openAddDialog);
        addButton.setTooltip(Tooltip.create(Component.translatable("gui.avaritia.addChannel.tip2")
                .append("\n").append(Component.translatable("gui.avaritia.addChannel.tip3"))));
        addRenderableWidget(addButton);
        addRenderableWidget(new LegacyIconButton(leftPos + 27, topPos + 131,
                16, 16, 202, 34, 16, this::openRenameDialog));
        addRenderableWidget(new LegacyIconButton(leftPos + 47, topPos + 131,
                16, 16, 202, 18, 16, () -> sendMenuButton(0)));
        LegacyIconButton backButton = new LegacyIconButton(leftPos + 67, topPos + 131,
                16, 16, 202, 50, 16, () -> sendMenuButton(1));
        backButton.setTooltip(Tooltip.create(Component.translatable("gui.avaritia.backChannel.tip1")));
        addRenderableWidget(backButton);

        channelManager.addScreenRefresh(this::updateChannelList);
        updateChannelList();
    }

    private void openAddDialog() {
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Text.literal("Channel name").setShadow(true),
                Text.literal("Enter a channel name"), ".{0,64}", "Channel", input -> {
            if (!input.isBlank()) {
                PacketDistributor.sendToServer(new C2SAddChannelPack(menu.containerId, input, hasShiftDown()));
            }
        }));
    }

    private void openRenameDialog() {
        if (channelManager.selectedChannelID < 0) return;
        Minecraft.getInstance().setScreen(new StringInputScreen(this,
                Text.literal("Rename channel").setShadow(true),
                Text.literal("Enter a new channel name"), ".{0,64}",
                channelManager.selectedChannelName, input -> {
            if (!input.isBlank()) {
                PacketDistributor.sendToServer(new C2SRenameChannelPack(menu.containerId, input));
            }
        }));
    }

    private void sendMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void selectChannel(int row) {
        ChannelEntry entry = rowEntry(row);
        if (entry != null) {
            PacketDistributor.sendToServer(new C2SSetChannelPack(menu.containerId, entry.type, entry.id));
        }
    }

    private ChannelEntry rowEntry(int row) {
        int index = scrollOffset + row;
        return index >= 0 && index < filteredChannels.size() ? filteredChannels.get(index) : null;
    }

    public void updateChannelList() {
        if (searchBox == null) return;
        String filter = searchBox.getValue().strip().toLowerCase(Locale.ROOT);
        filteredChannels.clear();
        appendChannels((byte) 0, channelManager.myChannels, filter);
        appendChannels((byte) 1, channelManager.otherChannels, filter);
        appendChannels((byte) 2, channelManager.publicChannels, filter);
        setScrollOffset(scrollOffset);
    }

    private void appendChannels(byte type, Map<Integer, String> channels, String filter) {
        channels.entrySet().stream()
                .filter(entry -> filter.isEmpty() || entry.getValue().toLowerCase(Locale.ROOT).contains(filter))
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new ChannelEntry(type, entry.getKey(), entry.getValue()))
                .forEach(filteredChannels::add);
    }

    static int maxScrollOffset(int channelCount) {
        return Math.max(0, channelCount - ROWS);
    }

    private void setScrollOffset(int value) {
        scrollOffset = Math.max(0, Math.min(maxScrollOffset(filteredChannels.size()), value));
        for (int row = 0; row < channelButtons.size(); row++) {
            channelButtons.get(row).visible = rowEntry(row) != null;
        }
    }

    private int scrollbarHandleHeight() {
        if (filteredChannels.size() <= ROWS) return SCROLLBAR_HEIGHT;
        return Math.max(8, ROWS * SCROLLBAR_HEIGHT / filteredChannels.size());
    }

    private int scrollbarHandleY() {
        int maximumOffset = maxScrollOffset(filteredChannels.size());
        int travel = SCROLLBAR_HEIGHT - scrollbarHandleHeight();
        return topPos + SCROLLBAR_Y + (maximumOffset == 0 ? 0 : scrollOffset * travel / maximumOffset);
    }

    private void updateScrollFromMouse(double mouseY) {
        int maximumOffset = maxScrollOffset(filteredChannels.size());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D && mouseX >= leftPos && mouseX < leftPos + WIDTH
                && mouseY >= topPos && mouseY < topPos + HEIGHT) {
            setScrollOffset(scrollOffset + (scrollY > 0.0D ? -1 : 1));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setValue("");
            searchBox.setFocused(true);
            return true;
        }
        if (button == 0 && mouseX >= leftPos + SCROLLBAR_X
                && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && mouseY >= topPos + SCROLLBAR_Y
                && mouseY < topPos + SCROLLBAR_Y + SCROLLBAR_HEIGHT) {
            draggingScrollbar = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI_IMG, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderScrollbar(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (filteredChannels.size() <= ROWS) return;
        int handleY = scrollbarHandleY();
        int handleHeight = scrollbarHandleHeight();
        graphics.fill(leftPos + SCROLLBAR_X + 2, handleY,
                leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH - 2, handleY + handleHeight, 0xFF777777);
        graphics.renderOutline(leftPos + SCROLLBAR_X + 1, handleY - 1,
                SCROLLBAR_WIDTH - 2, handleHeight + 2, 0xFF202020);
    }

    @Override
    public void onClose() {
        channelManager.onScreenClose();
        super.onClose();
    }

    private record ChannelEntry(byte type, int id, String name) {
    }

    private final class ChannelRowButton extends ImageButton {
        private final int row;

        private ChannelRowButton(int x, int y, int row) {
            super(x, y, ROW_WIDTH, ROW_HEIGHT, legacySprites, ignored -> selectChannel(row));
            this.row = row;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ChannelEntry entry = rowEntry(row);
            if (entry == null) return;
            int vOffset = 154;
            if (isHoveredOrFocused()) vOffset += 12;
            if (entry.type == channelManager.selectedChannelType && entry.id == channelManager.selectedChannelID) {
                vOffset += 24;
            }
            graphics.blit(GUI_IMG, getX(), getY(), 0, vOffset,
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
            int color = switch (entry.type) {
                case 0 -> 0x55FF55;
                case 1 -> 0xFF5555;
                default -> 0xFFFFFF;
            };
            String label = font.plainSubstrByWidth(entry.name, ROW_WIDTH - 8);
            graphics.drawString(font, label, getX() + 4, getY() + 2, color, false);
        }
    }

    private final class LegacyIconButton extends ImageButton {
        private final int u;
        private final int v;
        private final int hoverOffset;

        private LegacyIconButton(int x, int y, int buttonWidth, int buttonHeight,
                                 int u, int v, int hoverOffset, Runnable action) {
            super(x, y, buttonWidth, buttonHeight, legacySprites, ignored -> action.run());
            this.u = u;
            this.v = v;
            this.hoverOffset = hoverOffset;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int uOffset = isHoveredOrFocused() ? u + hoverOffset : u;
            graphics.blit(GUI_IMG, getX(), getY(), uOffset, v,
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
