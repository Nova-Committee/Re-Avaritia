package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.client.screen.component.PortableSelectionList;
import committee.nova.mods.avaritia.client.render.NeutronSpacePreviewRenderer;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.net.C2SNeutronRingPack;
import committee.nova.mods.avaritia.common.net.S2CNeutronRingOpenPack;
import committee.nova.mods.avaritia.common.net.S2CNeutronRingPreviewPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Player-bound space library. Browsing never changes the server's selected placement by itself. */
public final class NeutronRingManageScreen extends Screen {
    private final List<S2CNeutronRingOpenPack.Entry> spaces = new ArrayList<>();
    private final Map<String, NeutronSpacePreview> previews = new HashMap<>();
    private final NeutronSpacePreviewRenderer renderer = new NeutronSpacePreviewRenderer();
    private final UUID storageId;
    private int hand;
    @Nullable
    private String selectedId;
    @Nullable
    private S2CNeutronRingOpenPack.Entry previewEntry;
    @Nullable
    private String pendingId;
    @Nullable
    private PreviewAssembly assembly;
    private boolean previewMissing;
    private SpaceList list;
    private String query = "";
    private ScreenRectangle panel;
    private ScreenRectangle library;
    private ScreenRectangle previewOuter;
    private ScreenRectangle previewContent;
    private ScreenRectangle previewHint;
    private static final float DEFAULT_YAW = 35.0F;
    private static final float DEFAULT_PITCH = 25.0F;
    private static final float DEFAULT_ZOOM = 1.0F;
    private static final float PITCH_LIMIT = 85.0F;
    private static final float ZOOM_MIN = 0.4F;
    private static final float ZOOM_MAX = 2.5F;
    private static final float ZOOM_STEP = 1.15F;
    private float yaw = DEFAULT_YAW;
    private float pitch = DEFAULT_PITCH;
    private float zoom = DEFAULT_ZOOM;
    private float panX;
    private float panY;
    private Drag drag = Drag.NONE;
    private boolean panMoved;
    private enum Drag { NONE, ORBIT, PAN }
    private final OperationMenu menu = new OperationMenu();

    public NeutronRingManageScreen(S2CNeutronRingOpenPack packet) {
        super(Component.translatable("gui.avaritia.neutron_ring.title"));
        storageId = packet.storageId();
        accept(packet);
    }

    public static void open(S2CNeutronRingOpenPack packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (PortableUi.root(minecraft.screen) instanceof NeutronRingManageScreen current
                && current.storageId.equals(packet.storageId())) {
            current.accept(packet);
        } else {
            minecraft.setScreen(new NeutronRingManageScreen(packet));
        }
    }

    public static void acceptPreview(S2CNeutronRingPreviewPack packet) {
        if (PortableUi.root(Minecraft.getInstance().screen) instanceof NeutronRingManageScreen screen) {
            screen.handlePreview(packet);
        }
    }

    private void accept(S2CNeutronRingOpenPack packet) {
        String previewId = previewEntry == null ? null : previewEntry.id();
        spaces.clear();
        spaces.addAll(packet.spaces());
        hand = packet.hand();
        selectedId = packet.selectedId().orElse(null);
        previewEntry = find(previewId);
        if (previewEntry == null) {
            previewEntry = find(selectedId);
        }
        if (previewEntry == null && !spaces.isEmpty()) {
            previewEntry = spaces.getFirst();
        }
        previews.keySet().removeIf(id -> find(id) == null);
        if (assembly != null && find(assembly.id) == null) {
            assembly = null;
        }
        menu.close();
        if (list != null) {
            double scroll = list.getScrollAmount();
            list.populate();
            list.setScrollAmount(scroll);
        }
        if (previewEntry != null) {
            loadPreview(previewEntry.id());
        } else {
            renderer.setPreview(null);
            pendingId = null;
            previewMissing = false;
        }
    }

    private void handlePreview(S2CNeutronRingPreviewPack packet) {
        if (!matchesPreview(packet.id())) {
            return;
        }
        if (!packet.available() || packet.chunkCount() <= 0) {
            previewMissing = true;
            previews.remove(packet.id());
            renderer.setPreview(null);
            assembly = null;
            if (packet.id().equals(pendingId)) {
                pendingId = null;
            }
            return;
        }
        int maxChunks = (NeutronSpacePreview.MAX_CELLS + NeutronSpacePreview.CELLS_PER_CHUNK - 1)
                / NeutronSpacePreview.CELLS_PER_CHUNK;
        if (packet.chunkCount() > maxChunks || packet.chunkIndex() < 0 || packet.chunkIndex() >= packet.chunkCount()) {
            return;
        }
        if (assembly == null || !assembly.id.equals(packet.id()) || assembly.parts.length != packet.chunkCount()) {
            assembly = new PreviewAssembly(packet.id(), packet.chunkCount());
        }
        assembly.parts[packet.chunkIndex()] = packet.preview();
        if (!assembly.complete()) {
            return;
        }
        NeutronSpacePreview preview = NeutronSpacePreview.assemble(assembly.parts[0], Arrays.asList(assembly.parts));
        previews.put(packet.id(), preview);
        assembly = null;
        if (packet.id().equals(pendingId)) {
            pendingId = null;
        }
        if (previewEntry != null && previewEntry.id().equals(packet.id())) {
            previewMissing = false;
            renderer.setPreview(preview);
        }
    }

    private boolean matchesPreview(String id) {
        return id.equals(pendingId) || previewEntry != null && previewEntry.id().equals(id);
    }

    private void loadPreview(String id) {
        NeutronSpacePreview preview = previews.get(id);
        if (preview != null) {
            previewMissing = false;
            pendingId = null;
            renderer.setPreview(preview);
            return;
        }
        renderer.setPreview(null);
        previewMissing = false;
        if (id.equals(pendingId)) {
            return;
        }
        pendingId = id;
        assembly = null;
        send(C2SNeutronRingPack.PREVIEW, id, "");
    }

    @Nullable
    private S2CNeutronRingOpenPack.Entry find(@Nullable String id) {
        if (id != null) {
            for (S2CNeutronRingOpenPack.Entry entry : spaces) {
                if (entry.id().equals(id)) {
                    return entry;
                }
            }
        }
        return null;
    }

    @Override
    protected void init() {
        double scroll = list == null ? 0 : list.getScrollAmount();
        menu.close();
        stopDrag();
        panel = PortableLayout.centered(width, height, 540, 330, 8);
        int listWidth = Math.min(172, Math.max(112, panel.width() / 3));
        library = PortableLayout.inset(panel, 12, 47, panel.width() - listWidth - 12, 36);
        ScreenRectangle preview = PortableLayout.inset(panel, listWidth + 20, 47, 12, 36);
        previewOuter = PortableLayout.inset(preview, -2, -2, -2, -2);
        previewContent = PortableLayout.inset(preview, 4, 4, 4, 4);
        previewHint = new ScreenRectangle(previewOuter.left() + 2, panel.bottom() - 24,
                Math.max(40, previewOuter.width() - 90), font.lineHeight);
        list = addRenderableWidget(new SpaceList(library.width(), library.height(), library.top()));
        UiInspector.name(list, "neutron.library");
        list.setX(library.left());
        list.populate();
        list.setScrollAmount(scroll);
        addRenderableWidget(UiInspector.name(PortableUi.button(panel.right() - 82, panel.bottom() - 29, 70, 20,
                CommonComponents.GUI_DONE, button -> onClose()), "neutron.done"));
        if (previewEntry != null) {
            loadPreview(previewEntry.id());
        }
    }

    @Override
    public void removed() {
        super.removed();
        renderer.close();
        pendingId = null;
        assembly = null;
    }

    @Override
    public void tick() {
        if (drag != Drag.NONE && !mouseHeld(GLFW.GLFW_MOUSE_BUTTON_LEFT) && !mouseHeld(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            finishDrag();
        }
    }

    private void resetView() {
        yaw = DEFAULT_YAW;
        pitch = DEFAULT_PITCH;
        zoom = DEFAULT_ZOOM;
        panX = 0.0F;
        panY = 0.0F;
    }

    private void applyQuery(String value) {
        query = value;
        menu.close();
        if (list != null) {
            list.populate();
            list.setScrollAmount(0);
        }
    }

    private void promptSearch() {
        PortableUi.prompt(this, Component.translatable("gui.avaritia.portable.search"), query, 64, true, this::applyQuery);
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        PortableUi.panel(graphics, panel);
        UiInspector.region("neutron.panel", panel, null, false);
        PortableUi.header(graphics, font, title, panel.left() + 4, panel.top() + 4, panel.width() - 8);
        PortableUi.inset(graphics, previewOuter);
        UiInspector.region("neutron.preview.outer", previewOuter, null, previewEntry != null);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        PortableUi.text(graphics, font, Component.translatable("gui.avaritia.neutron_ring.library", spaces.size()),
                library.left(), panel.top() + 33, library.width(), PortableUi.MUTED);
        UiInspector.region("neutron.preview.content", previewContent, previewContent, previewEntry != null);
        if (previewEntry == null) {
            graphics.drawWordWrap(font, Component.translatable("gui.avaritia.neutron_ring.empty"),
                    previewContent.left() + 4, previewContent.top() + 4, Math.max(1, previewContent.width() - 8), PortableUi.MUTED);
        } else {
            PortableUi.text(graphics, font, Component.literal(previewEntry.name() + " · "
                            + previewEntry.sizeX() + "×" + previewEntry.sizeY() + "×" + previewEntry.sizeZ()),
                    previewOuter.left() + 2, panel.top() + 33, previewOuter.width() - 4, PortableUi.TEXT);
            NeutronSpacePreview preview = previews.get(previewEntry.id());
            if (previewMissing && preview == null) {
                graphics.fill(previewContent.left(), previewContent.top(), previewContent.right(), previewContent.bottom(), PortableUi.INSET_BG);
                graphics.drawWordWrap(font, Component.translatable("gui.avaritia.neutron_ring.preview_missing"),
                        previewContent.left() + 4, previewContent.top() + 4, Math.max(1, previewContent.width() - 8), PortableUi.MUTED);
            } else if (preview == null) {
                graphics.fill(previewContent.left(), previewContent.top(), previewContent.right(), previewContent.bottom(), PortableUi.INSET_BG);
                graphics.drawWordWrap(font, Component.translatable("gui.avaritia.neutron_ring.preview_loading"),
                        previewContent.left() + 4, previewContent.top() + 4, Math.max(1, previewContent.width() - 8), PortableUi.MUTED);
            } else {
                renderer.draw(graphics, previewContent.left(), previewContent.top(), previewContent.width(), previewContent.height(),
                        yaw, pitch, zoom, panX, panY);
            }
            PortableUi.text(graphics, font, Component.translatable("gui.avaritia.neutron_ring.preview_controls"),
                    previewHint.left(), previewHint.top(), previewHint.width(), PortableUi.MUTED);
            UiInspector.region("neutron.preview.hint", previewHint, null, false);
        }
        if (list.children().isEmpty()) {
            graphics.drawWordWrap(font, Component.translatable(query.isBlank()
                            ? "gui.avaritia.neutron_ring.empty_list" : "gui.avaritia.portable.no_results"),
                    list.getX() + 8, list.getY() + 12, list.getWidth() - 16, PortableUi.MUTED);
        }
        if (!menu.isOpen()) {
            list.renderNameTooltip(graphics, mouseX, mouseY);
        }
        menu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.mouseClicked(mouseX, mouseY, button)) {
            stopDrag();
            return true;
        }
        if (previewEntry != null && insidePreviewChrome(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                startDrag(hasShiftDown() ? Drag.PAN : Drag.ORBIT);
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                startDrag(Drag.PAN);
                return true;
            }
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && insidePanel(mouseX, mouseY)) {
            stopDrag();
            openBlankMenu(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (drag != Drag.NONE && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            finishDrag();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.isOpen()) {
            return true;
        }
        if (drag == Drag.ORBIT && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            orbitPreview(dragX, dragY);
            return true;
        }
        if (drag == Drag.PAN && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            panPreview(dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.isOpen()) {
            return menu.mouseScrolled(scrollY);
        }
        if (previewEntry != null && (drag != Drag.NONE || insidePreviewChrome(mouseX, mouseY))) {
            zoomPreview(scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (menu.keyPressed(keyCode)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_MENU || (keyCode == GLFW.GLFW_KEY_F10 && hasShiftDown())) {
            openKeyboardMenu();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    private boolean insidePreviewChrome(double mouseX, double mouseY) {
        return PortableLayout.contains(previewOuter, mouseX, mouseY);
    }

    private boolean insidePanel(double mouseX, double mouseY) {
        return PortableLayout.contains(panel, mouseX, mouseY);
    }

    private void startDrag(Drag next) {
        drag = next;
        panMoved = false;
        setFocused(null);
        setDragging(true);
    }

    private void finishDrag() {
        boolean openReset = drag == Drag.PAN && !panMoved;
        stopDrag();
        if (openReset && previewEntry != null) {
            openPreviewMenu(minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth(),
                    minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight());
        }
    }

    private void stopDrag() {
        drag = Drag.NONE;
        panMoved = false;
        setDragging(false);
    }

    private boolean mouseHeld(int button) {
        return GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), button) == GLFW.GLFW_PRESS;
    }

    private void orbitPreview(double dragX, double dragY) {
        float viewW = Math.max(1.0F, previewContent.width());
        float viewH = Math.max(1.0F, previewOuter.height() - 4);
        yaw = Mth.wrapDegrees(yaw + (float) (dragX * 180.0 / viewW));
        pitch = Mth.clamp(pitch + (float) (dragY * 90.0 / viewH), -PITCH_LIMIT, PITCH_LIMIT);
    }

    private void panPreview(double dragX, double dragY) {
        if (Math.abs(dragX) + Math.abs(dragY) > 0.5) {
            panMoved = true;
        }
        float limitX = Math.max(8.0F, (previewOuter.width() - 4) * zoom);
        float limitY = Math.max(8.0F, (previewOuter.height() - 4) * zoom);
        panX = Mth.clamp(panX + (float) dragX, -limitX, limitX);
        panY = Mth.clamp(panY + (float) dragY, -limitY, limitY);
    }

    private void zoomPreview(double scrollY) {
        if (scrollY == 0.0) {
            return;
        }
        zoom = Mth.clamp(zoom * (float) Math.pow(ZOOM_STEP, scrollY), ZOOM_MIN, ZOOM_MAX);
    }

    private void send(int action, String id, String name) {
        PacketDistributor.sendToServer(new C2SNeutronRingPack(action, id, name, hand, storageId,
                NeutronRingContents.Size.DEFAULT));
    }
    private void focus(S2CNeutronRingOpenPack.Entry space) {
        if (previewEntry == null || !previewEntry.id().equals(space.id())) {
            panX = 0.0F;
            panY = 0.0F;
        }
        previewEntry = space;
        loadPreview(space.id());
    }

    private void select(S2CNeutronRingOpenPack.Entry space) {
        focus(space);
        send(C2SNeutronRingPack.SELECT, space.id(), "");
    }

    private void openKeyboardMenu() {
        if (list == null) {
            return;
        }
        SpaceList.SpaceEntry focused = list.getSelected();
        int x = list.getX() + 8;
        int y = list.getY() + 8;
        if (focused != null) {
            openSpaceMenu(focused.space, x, y);
        } else {
            openBlankMenu(x, y);
        }
    }

    private void openSpaceMenu(S2CNeutronRingOpenPack.Entry space, double mouseX, double mouseY) {
        List<OperationMenu.Entry> entries = new ArrayList<>();
        if (!space.id().equals(selectedId)) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.select", () -> select(space)));
        }
        entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.rename", () ->
                PortableUi.prompt(this, Component.translatable("gui.avaritia.neutron_ring.rename"), space.name(),
                        128, false, value -> {
                            if (!value.equals(space.name())) {
                                send(C2SNeutronRingPack.RENAME, space.id(), value.trim());
                            }
                        })));
        if (space.id().equals(selectedId)) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.deselect",
                    () -> send(C2SNeutronRingPack.DESELECT, "", "")));
        }
        entries.add(OperationMenu.Entry.danger("gui.avaritia.neutron_ring.delete", () -> PortableUi.confirm(this,
                Component.translatable("gui.avaritia.neutron_ring.delete"),
                Component.translatable("gui.avaritia.neutron_ring.delete_message", space.name()),
                () -> send(C2SNeutronRingPack.DELETE, space.id(), ""))));
        menu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private void openBlankMenu(double mouseX, double mouseY) {
        List<OperationMenu.Entry> entries = new ArrayList<>();
        entries.add(OperationMenu.Entry.of("gui.avaritia.portable.search", this::promptSearch));
        if (!query.isBlank()) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.clear_filter", () -> applyQuery("")));
        }
        if (selectedId != null) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.deselect",
                    () -> send(C2SNeutronRingPack.DESELECT, "", "")));
        }
        menu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private void openPreviewMenu(double mouseX, double mouseY) {
        menu.open((int) mouseX, (int) mouseY, width, height, font,
                List.of(OperationMenu.Entry.of("gui.avaritia.neutron_ring.reset_view", this::resetView)));
    }

    private static final class PreviewAssembly {
        private final String id;
        private final NeutronSpacePreview[] parts;

        private PreviewAssembly(String id, int chunkCount) {
            this.id = id;
            this.parts = new NeutronSpacePreview[chunkCount];
        }

        private boolean complete() {
            for (NeutronSpacePreview part : parts) {
                if (part == null) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class SpaceList extends PortableSelectionList<SpaceList.SpaceEntry> {
        SpaceList(int width, int height, int y) {
            super(NeutronRingManageScreen.this.minecraft, width, height, y, 28, 10, 8, true);
        }

        void populate() {
            clearEntries();
            String filter = query.trim().toLowerCase(Locale.ROOT);
            for (S2CNeutronRingOpenPack.Entry space : spaces) {
                if (space.name().toLowerCase(Locale.ROOT).contains(filter)) {
                    SpaceEntry entry = new SpaceEntry(space);
                    addEntry(entry);
                    if (previewEntry != null && previewEntry.id().equals(space.id())) {
                        setSelected(entry);
                    }
                }
            }
        }

        @Override
        public void setSelected(@Nullable SpaceEntry entry) {
            super.setSelected(entry);
            if (entry != null) {
                focus(entry.space);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isMouseOver(mouseX, mouseY)
                    && getEntryAtPosition(mouseX, mouseY) == null) {
                openBlankMenu(mouseX, mouseY);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        void renderNameTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            SpaceEntry hovered = getHovered();
            if (hovered != null && font.width(hovered.space.name()) > getRowWidth() - 8) {
                graphics.renderTooltip(font, Component.literal(hovered.space.name()), mouseX, mouseY);
            }
        }

        private final class SpaceEntry extends ObjectSelectionList.Entry<SpaceEntry> {
            private final S2CNeutronRingOpenPack.Entry space;
            private final Component dimensions;

            SpaceEntry(S2CNeutronRingOpenPack.Entry space) {
                this.space = space;
                dimensions = Component.literal(space.sizeX() + "×" + space.sizeY() + "×" + space.sizeZ());
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                boolean active = space.id().equals(selectedId);
                PortableUi.row(graphics, left, top, width, height, hovering, getSelected() == this);
                if (UiInspector.enabled()) {
                    UiInspector.row("neutron.library", space.id(), index, left, top, width, height, library, true);
                }
                PortableUi.text(graphics, font, Component.literal(space.name()), left + 4, top + 3, width - 8, PortableUi.TEXT);
                PortableUi.text(graphics, font, active ? Component.translatable("gui.avaritia.neutron_ring.active") : dimensions,
                        left + 4, top + 14, width - 8, active ? PortableUi.ACCENT : PortableUi.MUTED);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                focus(space);
                SpaceList.this.setSelected(this);
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    openSpaceMenu(space, mouseX, mouseY);
                }
                return true;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(space.name());
            }
        }
    }
}
