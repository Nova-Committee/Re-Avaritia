package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.StringInputScreen;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.api.client.widget.SimpleScrollBar;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SAddChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRenameChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SSetChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TesseractChannelScreen extends AbstractContainerScreen<TesseractChannelMenu> {
    private static final ResourceLocation GUI_IMG = Res.BLACK_HOLE_CHANNEL_SELECT;
    private static final int VISIBLE_CHANNELS = 9;

    private final ClientChannelManager channelManager = ClientChannelManager.getInstance();
    private final List<ChannelEntry> filteredChannels = new ArrayList<>();
    private final List<Button> channelButtons = new ArrayList<>();
    private EditBox searchBox;
    private ChannelScrollBar scrollBar;
    private String previousFilter = "";
    private int scrollOffset;

    public TesseractChannelScreen(TesseractChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 88;
        imageHeight = 154;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        searchBox = new EditBox(font, leftPos + 7, topPos + 116, 76, 12,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        addRenderableWidget(searchBox);

        channelButtons.clear();
        for (int index = 0; index < VISIBLE_CHANNELS; index++) {
            final int buttonIndex = index;
            Button button = Button.builder(Component.empty(), ignored -> selectChannel(buttonIndex))
                    .bounds(leftPos + 7, topPos + 7 + index * 12, 66, 12).build();
            channelButtons.add(button);
            addRenderableWidget(button);
        }

        scrollBar = new ChannelScrollBar(leftPos + 75, topPos + 7, 8, 108);
        addRenderableWidget(scrollBar);

        addRenderableWidget(Button.builder(Component.literal("+"), ignored -> openAddDialog())
                .bounds(leftPos + 7, topPos + 132, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("R"), ignored -> openRenameDialog())
                .bounds(leftPos + 27, topPos + 132, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("-"), ignored -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }).bounds(leftPos + 47, topPos + 132, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("↩"), ignored -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + 67, topPos + 132, 18, 18).build());

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

    private void selectChannel(int buttonIndex) {
        int index = scrollOffset + buttonIndex;
        if (index < 0 || index >= filteredChannels.size()) return;
        ChannelEntry entry = filteredChannels.get(index);
        PacketDistributor.sendToServer(new C2SSetChannelPack(menu.containerId, entry.type, entry.id));
    }

    public void updateChannelList() {
        if (searchBox == null) return;
        String filter = searchBox.getValue().toLowerCase(Locale.ROOT);
        filteredChannels.clear();
        addChannels((byte) 0, channelManager.myChannels, filter);
        addChannels((byte) 1, channelManager.otherChannels, filter);
        addChannels((byte) 2, channelManager.publicChannels, filter);
        filteredChannels.sort(Comparator.comparingInt(ChannelEntry::type).thenComparingInt(ChannelEntry::id));
        setScrollOffset(scrollOffset);
        refreshButtons();
    }

    private void addChannels(byte type, Map<Integer, String> channels, String filter) {
        channels.forEach((id, name) -> {
            if (name.toLowerCase(Locale.ROOT).contains(filter)) {
                filteredChannels.add(new ChannelEntry(type, id, name));
            }
        });
    }

    private void setScrollOffset(int value) {
        scrollOffset = Math.max(0, Math.min(Math.max(0, filteredChannels.size() - VISIBLE_CHANNELS), value));
    }

    private void refreshButtons() {
        for (int i = 0; i < channelButtons.size(); i++) {
            int index = scrollOffset + i;
            Button button = channelButtons.get(i);
            button.visible = index < filteredChannels.size();
            if (button.visible) {
                ChannelEntry entry = filteredChannels.get(index);
                String prefix = entry.type == 0 ? "§a" : entry.type == 1 ? "§c" : "§e";
                String selected = entry.type == channelManager.selectedChannelType
                        && entry.id == channelManager.selectedChannelID ? "▶ " : "";
                button.setMessage(Component.literal(selected + prefix + entry.name));
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!previousFilter.equals(searchBox.getValue())) {
            previousFilter = searchBox.getValue();
            updateChannelList();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (filteredChannels.size() > VISIBLE_CHANNELS) {
            setScrollOffset(scrollOffset + (scrollY < 0 ? 1 : -1));
            refreshButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI_IMG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        channelManager.onScreenClose();
        super.onClose();
    }

    private final class ChannelScrollBar extends SimpleScrollBar {
        private ChannelScrollBar(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            int max = Math.max(0, filteredChannels.size() - VISIBLE_CHANNELS);
            setScrollOffset((int) Math.round(max * scrolledOn));
            refreshButtons();
        }

        @Override
        public void beforeRender() {
            setScrollTagSize(filteredChannels.isEmpty() ? getHeight()
                    : Math.max(getWidth(), getHeight() * Math.min(1.0D,
                    VISIBLE_CHANNELS / (double) filteredChannels.size())));
            visible = filteredChannels.size() > VISIBLE_CHANNELS;
        }
    }

    private record ChannelEntry(byte type, int id, String name) {
    }
}
