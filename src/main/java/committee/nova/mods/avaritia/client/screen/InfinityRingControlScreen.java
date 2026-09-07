package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableSelectionList;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingOpenPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/** Owner / admin control panel: settings and actions on the left, player roster on the right. */
public final class InfinityRingControlScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HEADER_H = 20;
    private static final int FOOTER_H = 24;
    private static final int BTN_H = 20;
    private static final int GAP = 3;

    private int time;
    private int weather;
    private int access;
    private final List<String> friends;
    private final UUID owner;
    private boolean canDelete;
    private String nameInput = "";
    private String filter = "";
    @Nullable
    private String selectedName;
    private double listScroll;
    private ScreenRectangle panel;
    private ScreenRectangle roster;

    @Nullable
    private PlayerList playerList;
    @Nullable
    private EditBox searchBox;
    @Nullable
    private String menuName;
    @Nullable
    private String menuRole;

    private final OperationMenu menu = new OperationMenu();

    public InfinityRingControlScreen(int time, int weather, int access, List<String> friends,
                                     UUID owner, boolean canDelete) {
        super(Component.translatable("gui.avaritia.infinity_ring.control"));
        this.time = time;
        this.weather = weather;
        this.access = access;
        this.friends = new ArrayList<>(friends);
        this.owner = owner;
        this.canDelete = canDelete;
    }

    public static void open(S2CInfinityRingOpenPack packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (packet.create()) {
            minecraft.setScreen(new InfinityRingCreateScreen(
                    packet.terrain(), packet.time(), packet.weather(), packet.access()));
            return;
        }
        Screen root = PortableUi.root(minecraft.screen);
        if (root instanceof InfinityRingControlScreen screen && screen.owner.equals(packet.owner())) {
            screen.apply(packet);
            return;
        }
        minecraft.setScreen(new InfinityRingControlScreen(
                packet.time(), packet.weather(), packet.access(), packet.friends(),
                packet.owner(), packet.canDelete()));
    }

    private void apply(S2CInfinityRingOpenPack packet) {
        captureTransientState();
        time = packet.time();
        weather = packet.weather();
        access = packet.access();
        friends.clear();
        friends.addAll(packet.friends());
        canDelete = packet.canDelete();
        if (selectedName != null && findRaw(selectedName) == null) {
            selectedName = null;
        }
        if (menu.isOpen() && (menuName == null || !roleMatches(menuName, menuRole))) {
            menu.close();
            menuName = null;
            menuRole = null;
        }
        rebuildWidgets();
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        captureTransientState();
        menu.close();
        super.resize(minecraft, width, height);
    }

    private void captureTransientState() {
        if (searchBox != null) {
            filter = searchBox.getValue().trim();
        }
        if (playerList != null) {
            listScroll = playerList.getScrollAmount();
            PlayerList.PlayerEntry selected = playerList.getSelected();
            if (selected != null) {
                selectedName = selected.name;
            }
        }
    }

    @Override
    protected void init() {
        playerList = null;
        panel = PortableLayout.centered(width, height, 400, 224, MARGIN);
        int panelX = panel.left();
        int panelY = panel.top();
        int panelW = panel.width();
        int panelH = panel.height();
        int innerX = panelX + 8;
        int innerW = panelW - 16;
        int contentY = panelY + HEADER_H + 2;
        int footerY = panelY + panelH - FOOTER_H;
        int contentH = Math.max(48, footerY - contentY - GAP);
        int colGap = 6;
        int leftW = Math.min(168, Math.max(120, (innerW - colGap) / 2));
        int listW = Math.max(80, innerW - leftW - colGap);
        leftW = innerW - listW - colGap;
        int listX = innerX + leftW + colGap;
        int rows = 4;
        int rowGap = Math.max(1, Math.min(GAP, (contentH - rows * BTN_H) / Math.max(1, rows - 1)));
        GridLayout left = new GridLayout(innerX, contentY);
        left.rowSpacing(rowGap);
        left.addChild(addCycle(leftW,
                "gui.avaritia.infinity_ring.option.time", "gui.avaritia.infinity_ring.time.",
                InfinityRingSettings.TimeMode.values(), InfinityRingSettings.TimeMode.byId(time), value -> {
                    time = value.ordinal();
                    sendModes();
                }), 0, 0);
        left.addChild(addCycle(leftW,
                "gui.avaritia.infinity_ring.option.weather", "gui.avaritia.infinity_ring.weather.",
                InfinityRingSettings.WeatherMode.values(), InfinityRingSettings.WeatherMode.byId(weather), value -> {
                    weather = value.ordinal();
                    sendModes();
                }), 1, 0);
        left.addChild(addCycle(leftW,
                "gui.avaritia.infinity_ring.option.access", "gui.avaritia.infinity_ring.access.",
                InfinityRingSettings.Access.values(), InfinityRingSettings.Access.byId(access), value -> {
                    access = value.ordinal();
                    sendModes();
                }), 2, 0);
        Button delete = PortableUi.dangerButton(0, 0, leftW, BTN_H,
                Component.translatable("gui.avaritia.infinity_ring.delete"),
                button -> confirmDelete());
        delete.active = canDelete;
        left.addChild(UiInspector.name(delete, "ring.control.delete"), 3, 0);
        left.arrangeElements();
        left.visitWidgets(this::addRenderableWidget);

        int doneW = 80;
        addRenderableWidget(UiInspector.name(PortableUi.button(panelX + panelW - 8 - doneW, footerY, doneW, BTN_H,
                CommonComponents.GUI_DONE, button -> onClose()), "ring.control.done"));

        int searchH = BTN_H;
        int listY = contentY + searchH + GAP;
        int listH = Math.max(16, contentH - searchH - GAP);
        Component searchTitle = Component.translatable("gui.avaritia.infinity_ring.search_players");
        searchBox = addRenderableWidget(new EditBox(font, listX, contentY, listW, searchH, searchTitle));
        UiInspector.name(searchBox, "ring.control.search");
        searchBox.setMaxLength(32);
        searchBox.setHint(searchTitle);
        searchBox.setValue(filter);
        searchBox.setResponder(value -> {
            String next = value.trim();
            if (filter.equals(next)) {
                return;
            }
            filter = next;
            listScroll = 0;
            if (playerList != null) {
                playerList.populate();
                playerList.setScrollAmount(0);
            }
        });
        playerList = addRenderableWidget(new PlayerList(listW, listH, listY));
        UiInspector.name(playerList, "ring.control.players");
        playerList.setX(listX);
        playerList.setScrollAmount(listScroll);
        roster = new ScreenRectangle(listX - 2, contentY - 2, listW + 4, listY + listH - contentY + 4);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (playerList != null && playerList.children().isEmpty()) {
            int textY = playerList.getY() + 6;
            int textW = Math.max(40, playerList.getWidth() - 8);
            PortableUi.text(graphics, font, Component.translatable(friends.isEmpty()
                            ? "gui.avaritia.infinity_ring.empty_players"
                            : "gui.avaritia.infinity_ring.no_matching_players"),
                    playerList.getX() + 4, textY, textW, PortableUi.MUTED);
        }
        menu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        PortableUi.panel(graphics, panel);
        UiInspector.region("ring.control.panel", panel, null, false);
        PortableUi.header(graphics, font, title, panel.left(), panel.top(), panel.width());
        PortableUi.inset(graphics, roster);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox != null && searchBox.isFocused() && !searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
            if (getFocused() == searchBox) {
                setFocused(null);
            }
        }
        if (menu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.isOpen() && menu.mouseScrolled(scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (menu.isOpen()) {
            if (menu.keyPressed(keyCode)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                menu.close();
                return true;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_MENU || (keyCode == GLFW.GLFW_KEY_F10 && Screen.hasShiftDown())) {
            PlayerList.PlayerEntry selected = playerList == null ? null : playerList.getSelected();
            if (selected != null) {
                if (selected.addCandidate) {
                    addFriend(selected.name);
                } else {
                    openPlayerMenu(selected.name, selected.role, selected.menuX(), selected.menuY());
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void sendModes() {
        PacketDistributor.sendToServer(pack(C2SInfinityRingPack.SET_MODES, access, ""));
    }

    private C2SInfinityRingPack pack(int action, int extra, String name) {
        return new C2SInfinityRingPack(action, 0, time, weather, extra, name, owner);
    }



    private void addFriend(String name) {
        if (name.isEmpty()) {
            return;
        }
        nameInput = name;
        PacketDistributor.sendToServer(pack(C2SInfinityRingPack.ADD_FRIEND, access, name));
    }

    private void visitNamed(String name) {
        if (name.isEmpty()) {
            return;
        }
        nameInput = name;
        PacketDistributor.sendToServer(pack(C2SInfinityRingPack.VISIT, access, name));
        onClose();
    }

    private void confirmDelete() {
        if (!canDelete) {
            return;
        }
        PortableUi.confirm(this,
                Component.translatable("gui.avaritia.infinity_ring.delete"),
                Component.translatable("gui.avaritia.infinity_ring.delete_message"),
                () -> {
                    PacketDistributor.sendToServer(pack(C2SInfinityRingPack.DELETE, access, ""));
                    onClose();
                });
    }

    private void confirmBan(String name) {
        if (name.isEmpty()) {
            return;
        }
        nameInput = name;
        PortableUi.confirm(this,
                Component.translatable("gui.avaritia.infinity_ring.ban"),
                Component.translatable("gui.avaritia.infinity_ring.ban_confirm", name),
                () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.BAN, access, name)));
    }

    private void confirmRemove(String name) {
        PortableUi.confirm(this,
                Component.translatable("gui.avaritia.infinity_ring.remove_friend"),
                Component.translatable("gui.avaritia.infinity_ring.remove_confirm", name),
                () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.REMOVE_FRIEND, access, name)));
    }


    private void selectPlayer(PlayerList.PlayerEntry entry) {
        selectedName = entry.name;
        playerList.setSelected(entry);
    }

    private void openPlayerMenu(String name, String role, double mouseX, double mouseY) {
        menuName = name;
        menuRole = role;
        UUID expectedOwner = owner;
        List<OperationMenu.Entry> entries = new ArrayList<>();
        if ("BANNED".equals(role)) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.unban",
                    () -> runIfCurrent(expectedOwner, name, role,
                            () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.UNBAN, access, name)))));
        } else {
            for (InfinityRingSettings.Role next : InfinityRingSettings.Role.values()) {
                if (next.name().equals(role)) {
                    continue;
                }
                entries.add(new OperationMenu.Entry(
                        Component.translatable("gui.avaritia.infinity_ring.role." + next.name().toLowerCase(Locale.ROOT)),
                        () -> runIfCurrent(expectedOwner, name, role,
                                () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.SET_ROLE, next.ordinal(), name)))));
            }
            entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_ring.remove_friend",
                    () -> runIfCurrent(expectedOwner, name, role, () -> confirmRemove(name))));
            entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_ring.ban",
                    () -> runIfCurrent(expectedOwner, name, role, () -> confirmBan(name))));
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.visit", () -> runIfCurrent(expectedOwner, name, role, () -> {
                PacketDistributor.sendToServer(pack(C2SInfinityRingPack.VISIT, access, name));
                onClose();
            })));
        }
        menu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private void runIfCurrent(UUID expectedOwner, String name, String expectedRole, Runnable action) {
        if (!owner.equals(expectedOwner) || !roleMatches(name, expectedRole)) {
            return;
        }
        action.run();
    }

    private boolean roleMatches(String name, @Nullable String role) {
        String raw = findRaw(name);
        return raw != null && roleOf(raw).equals(role);
    }

    @Nullable
    private String findRaw(String name) {
        for (String raw : friends) {
            if (nameOf(raw).equals(name)) {
                return raw;
            }
        }
        return null;
    }

    private boolean matchesFilter(String name) {
        return filter.isBlank() || name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private static String nameOf(String raw) {
        int split = raw.lastIndexOf('|');
        return split < 0 ? raw : raw.substring(0, split);
    }

    private static String roleOf(String raw) {
        int split = raw.lastIndexOf('|');
        return split < 0 ? "MEMBER" : raw.substring(split + 1);
    }

    private static Component roleLabel(String role) {
        return Component.translatable("gui.avaritia.infinity_ring.role." + role.toLowerCase(Locale.ROOT));
    }

    private <T extends Enum<T>> CycleButton<T> addCycle(int w, String optionKey, String valuePrefix,
                                              T[] values, T current, Consumer<T> changed) {
        CycleButton<T> cycle = CycleButton.builder((T value) -> Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT)))
                .withValues(values)
                .withInitialValue(current)
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT) + ".info")))
                .create(0, 0, w, BTN_H, Component.translatable(optionKey),
                        (ignored, value) -> changed.accept(value));
        if (UiInspector.enabled()) {
            UiInspector.name(cycle, "ring.control." + optionKey.substring(optionKey.lastIndexOf('.') + 1));
        }
        return cycle;
    }
    private final class PlayerList extends PortableSelectionList<PlayerList.PlayerEntry> {
        PlayerList(int width, int height, int y) {
            super(InfinityRingControlScreen.this.minecraft, width, height, y, 16, 4, 6, false);
            this.centerListVertically = false;
            populate();
        }

        void populate() {
            clearEntries();
            boolean exact = false;
            for (String raw : friends) {
                String name = nameOf(raw);
                if (matchesFilter(name)) {
                    PlayerEntry entry = new PlayerEntry(raw, false);
                    addEntry(entry);
                    if (entry.name.equals(selectedName) && !entry.addCandidate) {
                        setSelected(entry);
                    }
                }
                if (!filter.isBlank() && name.equalsIgnoreCase(filter)) {
                    exact = true;
                }
            }
            if (!filter.isBlank() && !exact) {
                addEntry(new PlayerEntry(filter, true));
            }
        }

        private final class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {
            private final String name;
            private final String role;
            private final boolean addCandidate;
            private int lastLeft;
            private int lastTop;

            private PlayerEntry(String raw, boolean addCandidate) {
                this.addCandidate = addCandidate;
                this.name = addCandidate ? raw : nameOf(raw);
                this.role = addCandidate ? "ADD" : roleOf(raw);
            }

            private int menuX() {
                return lastLeft + 8;
            }

            private int menuY() {
                return lastTop + 8;
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                lastLeft = left;
                lastTop = top;
                boolean selected = !addCandidate && name.equals(selectedName);
                PortableUi.row(graphics, left, top, width, height, hovering, selected);
                if (UiInspector.enabled()) {
                    UiInspector.row("ring.control.players", name.toLowerCase(Locale.ROOT), index,
                            left, top, width, height, PlayerList.this.getRectangle(), true);
                }
                if (addCandidate) {
                    PortableUi.text(graphics, font,
                            Component.translatable("gui.avaritia.infinity_ring.add_candidate", name),
                            left + 4, top + 4, Math.max(20, width - 8), PortableUi.TEXT);
                    return;
                }
                int nameW = Math.max(20, width - 8 - font.width(roleLabel(role)) - 6);
                PortableUi.text(graphics, font, Component.literal(name), left + 4, top + 4, nameW, PortableUi.TEXT);
                PortableUi.text(graphics, font, roleLabel(role),
                        left + 4 + nameW + 2, top + 4, Math.max(16, width - nameW - 8),
                        "BANNED".equals(role) ? PortableUi.DANGER : PortableUi.MUTED);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (addCandidate) {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        addFriend(name);
                        return true;
                    }
                    return false;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    selectPlayer(this);
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    selectPlayer(this);
                    openPlayerMenu(name, role, mouseX, mouseY);
                    return true;
                }
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return addCandidate
                        ? Component.translatable("gui.avaritia.infinity_ring.add_candidate", name)
                        : Component.literal(name).append(" ").append(roleLabel(role));
            }
        }
    }
}
