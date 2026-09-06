package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import committee.nova.mods.avaritia.common.net.S2CInfinityRingOpenPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/** Owner / admin control panel with World and Permissions tabs. */
public final class InfinityRingControlScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HEADER_H = 20;
    private static final int TAB_H = 16;
    private static final int FOOTER_H = 22;
    private static final int ROW_H = 18;
    private static final int GAP = 3;

    private int time;
    private int weather;
    private int access;
    private final List<String> friends;
    private final UUID owner;
    private boolean canDelete;
    private int tab;
    private String nameInput = "";
    private String filter = "";
    @Nullable
    private String selectedName;
    private double listScroll;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int tabWorldX;
    private int tabAccessX;
    private int tabW;
    private int insetX;
    private int insetY;
    private int insetW;
    private int insetH;
    private int filterX;
    private int filterY;
    private int filterW;

    @Nullable
    private PlayerList playerList;
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
        panelW = Math.min(360, width - MARGIN * 2);
        panelH = Math.min(180, height - MARGIN * 2);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        int innerX = panelX + 8;
        int innerW = panelW - 16;
        int tabsY = panelY + HEADER_H + 2;
        tabW = Math.max(60, (innerW - GAP) / 2);
        tabWorldX = innerX;
        tabAccessX = innerX + tabW + GAP;
        insetX = panelX + 6;
        insetY = panelY + HEADER_H + TAB_H + 6;
        insetW = panelW - 12;
        insetH = Math.max(40, panelH - HEADER_H - TAB_H - FOOTER_H - 8);

        Button worldTab = addRenderableWidget(PortableUi.button(tabWorldX, tabsY, tabW, TAB_H,
                Component.translatable("gui.avaritia.infinity_ring.tab.world"), button -> switchTab(0)));
        Button accessTab = addRenderableWidget(PortableUi.button(tabAccessX, tabsY, tabW, TAB_H,
                Component.translatable("gui.avaritia.infinity_ring.tab.access"), button -> switchTab(1)));
        worldTab.active = tab != 0;
        accessTab.active = tab != 1;

        int contentY = tabsY + TAB_H + GAP;
        int footerY = panelY + panelH - FOOTER_H;
        int contentH = Math.max(48, footerY - contentY - GAP);
        if (tab == 0) {
            initWorldTab(innerX, contentY, innerW);
        } else {
            initAccessTab(innerX, contentY, innerW, contentH);
        }
        int doneW = 80;
        addRenderableWidget(PortableUi.button(panelX + panelW - 8 - doneW, footerY, doneW, 20,
                CommonComponents.GUI_DONE, button -> onClose()));
    }

    private void initWorldTab(int x, int y, int w) {
        addRenderableWidget(new OptionRow<>(x, y, w, ROW_H,
                "gui.avaritia.infinity_ring.option.time", "gui.avaritia.infinity_ring.time.",
                InfinityRingSettings.TimeMode.values(), InfinityRingSettings.TimeMode.byId(time), value -> {
                    time = value.ordinal();
                    sendModes();
                }));
        addRenderableWidget(new OptionRow<>(x, y + ROW_H + GAP, w, ROW_H,
                "gui.avaritia.infinity_ring.option.weather", "gui.avaritia.infinity_ring.weather.",
                InfinityRingSettings.WeatherMode.values(), InfinityRingSettings.WeatherMode.byId(weather), value -> {
                    weather = value.ordinal();
                    sendModes();
                }));
    }

    private void initAccessTab(int x, int y, int w, int h) {
        int cursor = y;
        addRenderableWidget(new OptionRow<>(x, cursor, w, ROW_H,
                "gui.avaritia.infinity_ring.option.access", "gui.avaritia.infinity_ring.access.",
                InfinityRingSettings.Access.values(), InfinityRingSettings.Access.byId(access), value -> {
                    access = value.ordinal();
                    sendModes();
                }));
        cursor += ROW_H + GAP;
        filterX = x;
        filterY = cursor;
        filterW = w;
        if (!filter.isBlank()) {
            cursor += font.lineHeight + GAP;
        }
        int listH = Math.max(32, h - (cursor - y));
        playerList = addRenderableWidget(new PlayerList(w, listH, cursor));
        playerList.setX(x);
        playerList.setScrollAmount(listScroll);
        if (selectedName != null) {
            for (PlayerList.PlayerEntry entry : playerList.children()) {
                if (entry.name.equals(selectedName)) {
                    playerList.setSelected(entry);
                    break;
                }
            }
        }
    }

    private void switchTab(int next) {
        if (tab == next) {
            return;
        }
        captureTransientState();
        menu.close();
        menuName = null;
        menuRole = null;
        tab = next;
        rebuildWidgets();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (tab == 1) {
            if (!filter.isBlank()) {
                PortableUi.text(graphics, font, Component.translatable("gui.avaritia.infinity_ring.filter", filter),
                        filterX, filterY, filterW, PortableUi.MUTED);
            }
            if (playerList != null && playerList.children().isEmpty()) {
                int textY = playerList.getY() + 6;
                int textW = Math.max(40, playerList.getWidth() - 8);
                PortableUi.text(graphics, font, Component.translatable(friends.isEmpty()
                                ? "gui.avaritia.infinity_ring.empty_players"
                                : "gui.avaritia.infinity_ring.no_matching_players"),
                        playerList.getX() + 4, textY, textW, PortableUi.MUTED);
                PortableUi.text(graphics, font, Component.translatable("gui.avaritia.infinity_ring.right_click_hint"),
                        playerList.getX() + 4, textY + font.lineHeight + 2, textW, PortableUi.MUTED);
            }
        }
        menu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        PortableUi.panel(graphics, panelX, panelY, panelW, panelH);
        PortableUi.header(graphics, font, title, panelX, panelY, panelW);
        PortableUi.inset(graphics, insetX, insetY, insetW, insetH);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && tab == 1 && inInset(mouseX, mouseY)) {
            openPanelMenu((int) mouseX, (int) mouseY);
            return true;
        }
        return false;
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
                openPlayerMenu(selected.name, selected.role, selected.menuX(), selected.menuY());
                return true;
            }
            if (tab == 1) {
                openPanelMenu(insetX + 8, insetY + 8);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean inInset(double mouseX, double mouseY) {
        return mouseX >= insetX && mouseX < insetX + insetW && mouseY >= insetY && mouseY < insetY + insetH;
    }

    private void sendModes() {
        PacketDistributor.sendToServer(pack(C2SInfinityRingPack.SET_MODES, access, ""));
    }

    private C2SInfinityRingPack pack(int action, int extra, String name) {
        return new C2SInfinityRingPack(action, 0, time, weather, extra, name, owner);
    }

    private void openPanelMenu(int mouseX, int mouseY) {
        menuName = null;
        menuRole = null;
        List<OperationMenu.Entry> entries = new ArrayList<>();
        entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.add_friend",
                () -> promptName(Component.translatable("gui.avaritia.infinity_ring.add_friend"), this::addFriend)));
        entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_ring.ban",
                () -> promptName(Component.translatable("gui.avaritia.infinity_ring.ban"), this::confirmBan)));
        entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.visit",
                () -> promptName(Component.translatable("gui.avaritia.infinity_ring.visit"), this::visitNamed)));
        entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.search_players", this::promptSearch));
        if (!filter.isBlank()) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.clear_filter", this::clearFilter));
        }
        if (canDelete) {
            entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_ring.delete", this::confirmDelete));
        }
        menu.open(mouseX, mouseY, width, height, font, entries);
    }

    private void promptName(Component title, Consumer<String> chosen) {
        PortableUi.prompt(this, title, nameInput, 32, false, playerNames(), value -> {
            nameInput = value;
            chosen.accept(value.trim());
        });
    }

    private void promptSearch() {
        PortableUi.prompt(this, Component.translatable("gui.avaritia.infinity_ring.search_players"),
                filter, 32, true, playerNames(), value -> {
                    filter = value.trim();
                    listScroll = 0;
                    rebuildWidgets();
                });
    }

    private void clearFilter() {
        filter = "";
        listScroll = 0;
        rebuildWidgets();
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

    private List<String> playerNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String raw : friends) {
            names.add(nameOf(raw));
        }
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().getOnlinePlayers()
                    .forEach(info -> names.add(info.getProfile().getName()));
        }
        return new ArrayList<>(names);
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

    private <T extends Enum<T>> void openOptionMenu(int mouseX, int mouseY, String valuePrefix,
                                                    T[] values, T current, Consumer<T> changed) {
        menuName = null;
        menuRole = null;
        List<OperationMenu.Entry> entries = new ArrayList<>();
        for (T value : values) {
            if (value == current) {
                continue;
            }
            T chosen = value;
            entries.add(new OperationMenu.Entry(
                    Component.translatable(valuePrefix + value.name().toLowerCase(Locale.ROOT)),
                    () -> changed.accept(chosen)));
        }
        menu.open(mouseX, mouseY, width, height, font, entries);
    }

    private final class OptionRow<T extends Enum<T>> extends AbstractWidget {
        private final String optionKey;
        private final String valuePrefix;
        private final T[] values;
        private final Consumer<T> changed;
        private T current;

        private OptionRow(int x, int y, int width, int height, String optionKey, String valuePrefix,
                          T[] values, T current, Consumer<T> changed) {
            super(x, y, width, height, Component.empty());
            this.optionKey = optionKey;
            this.valuePrefix = valuePrefix;
            this.values = values;
            this.current = current;
            this.changed = changed;
            refreshMessage();
        }

        private void refreshMessage() {
            setMessage(Component.translatable("options.generic_value",
                    Component.translatable(optionKey),
                    Component.translatable(valuePrefix + current.name().toLowerCase(Locale.ROOT))));
            setTooltip(Tooltip.create(Component.translatable(
                    valuePrefix + current.name().toLowerCase(Locale.ROOT) + ".info")));
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            PortableUi.row(graphics, getX(), getY(), getWidth(), getHeight(), isHovered(), isFocused());
            PortableUi.text(graphics, font, getMessage(), getX() + 4, getY() + (getHeight() - font.lineHeight) / 2,
                    getWidth() - 8, PortableUi.TEXT);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!active || !visible || !clicked(mouseX, mouseY)) {
                return false;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                setFocused(true);
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                openOptionMenu((int) mouseX, (int) mouseY, valuePrefix, values, current, value -> {
                    current = value;
                    refreshMessage();
                    changed.accept(value);
                });
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class PlayerList extends ObjectSelectionList<PlayerList.PlayerEntry> {
        PlayerList(int width, int height, int y) {
            super(InfinityRingControlScreen.this.minecraft, width, height, y, 16);
            this.centerListVertically = false;
            for (String raw : friends) {
                if (matchesFilter(nameOf(raw))) {
                    addEntry(new PlayerEntry(raw));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return Math.max(40, this.getWidth() - 8);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        protected boolean isValidMouseClick(int button) {
            return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        }

        @Override
        protected void renderListBackground(@NotNull GuiGraphics graphics) {
        }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics graphics) {
        }

        @Override
        protected void renderSelection(@NotNull GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
        }

        private final class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {
            private final String name;
            private final String role;
            private int lastLeft;
            private int lastTop;

            private PlayerEntry(String raw) {
                this.name = nameOf(raw);
                this.role = roleOf(raw);
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
                boolean selected = name.equals(selectedName);
                PortableUi.row(graphics, left, top, width, height, hovering, selected);
                int nameW = Math.max(20, width - 8 - font.width(roleLabel(role)) - 6);
                PortableUi.text(graphics, font, Component.literal(name), left + 4, top + 4, nameW, PortableUi.TEXT);
                PortableUi.text(graphics, font, roleLabel(role),
                        left + 4 + nameW + 2, top + 4, Math.max(16, width - nameW - 8),
                        "BANNED".equals(role) ? PortableUi.DANGER : PortableUi.MUTED);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
                return Component.literal(name).append(" ").append(roleLabel(role));
            }
        }
    }
}
