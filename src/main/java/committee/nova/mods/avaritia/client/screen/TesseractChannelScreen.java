package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SAddChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRemoveChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SRenameChannelPack;
import committee.nova.mods.avaritia.common.net.channel.C2SSetChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Paginated, server-authoritative Tesseract channel selector. */
public final class TesseractChannelScreen extends BaseContainerScreen<TesseractChannelMenu> {
    private static final int WIDTH = 330;
    private static final int HEIGHT = 220;
    private static final int ROWS = 7;
    private EditBox name;
    private int page;
    private byte selectedType = -1;
    private int selectedId = -1;

    public TesseractChannelScreen(TesseractChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    protected void subInit() {
        ClientChannelManager.getInstance().listenSelector(this::rebuildChannelWidgets);
        rebuildChannelWidgets();
    }

    private void rebuildChannelWidgets() {
        clearWidgets();
        name = new EditBox(font, leftPos + 8, topPos + 178, 150, 16, Component.translatable("gui.avaritia.name"));
        name.setMaxLength(64);
        addRenderableWidget(name);

        ClientChannelManager manager = ClientChannelManager.getInstance();
        int maximumPage = maximumPage(manager.mine(), manager.terminalOwner(), manager.shared());
        page = Math.min(page, maximumPage);
        addColumn(manager.mine(), (byte) 0, 8);
        addColumn(manager.terminalOwner(), (byte) 1, 115);
        addColumn(manager.shared(), (byte) 2, 222);

        addButton("+", 164, 178, 22, () -> send(new C2SAddChannelPack(menu.containerId, name.getValue(), false)));
        addButton("+P", 188, 178, 30, () -> send(new C2SAddChannelPack(menu.containerId, name.getValue(), true)));
        addButton("✎", 220, 178, 22, () -> {
            if (hasSelection()) send(new C2SRenameChannelPack(menu.containerId, selectedType, selectedId, name.getValue()));
        });
        addButton("×", 244, 178, 22, () -> {
            if (hasSelection()) send(new C2SRemoveChannelPack(menu.containerId, selectedType, selectedId));
        });
        addButton("Open", 268, 178, 54, () -> {
            if (hasSelection()) send(new C2SSetChannelPack(menu.containerId, selectedType, selectedId));
        });
        addButton("<", 8, 198, 28, () -> { if (page > 0) { page--; rebuildChannelWidgets(); } });
        addButton(">", 294, 198, 28, () -> {
            if (page < maximumPage) { page++; rebuildChannelWidgets(); }
        });
    }

    private void addColumn(Map<Integer, String> values, byte type, int x) {
        List<Map.Entry<Integer, String>> entries = new ArrayList<>(values.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getKey));
        int start = page * ROWS;
        for (int row = 0; row < ROWS && start + row < entries.size(); row++) {
            Map.Entry<Integer, String> entry = entries.get(start + row);
            String label = entry.getKey() + " " + entry.getValue();
            addButton(label, x, 25 + row * 21, 100, () -> {
                selectedType = type;
                selectedId = entry.getKey();
                name.setValue(entry.getValue());
            });
        }
    }

    private void addButton(String text, int x, int y, int width, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(text), ignored -> action.run())
                .bounds(leftPos + x, topPos + y, width, 18).build());
    }

    private static void send(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        NetworkHandler.sendToServer(payload);
    }

    private boolean hasSelection() { return selectedType >= 0 && selectedType <= 2 && selectedId >= 0; }

    static int maximumPage(Map<Integer, String> mine, Map<Integer, String> owner, Map<Integer, String> shared) {
        int maximumSize = Math.max(mine.size(), Math.max(owner.size(), shared.size()));
        return Math.max(0, (maximumSize - 1) / ROWS);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE171717);
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        name.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(font, Component.translatable("gui.avaritia.tesseract.channels.mine"), 8, 10, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("gui.avaritia.tesseract.channels.owner"), 115, 10, 0xFFFFFFFF, false);
        graphics.text(font, Component.translatable("gui.avaritia.tesseract.channels.public"), 222, 10, 0xFFFFFFFF, false);
        graphics.text(font, Component.literal(Integer.toString(page + 1)), 160, 202, 0xFFFFFFFF, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return name.keyPressed(event) || name.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void onClose() {
        ClientChannelManager.getInstance().closeSelector();
        super.onClose();
    }
}
