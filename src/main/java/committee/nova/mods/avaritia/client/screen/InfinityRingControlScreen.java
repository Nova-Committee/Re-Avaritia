package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;

/** Owner control panel with vanilla-style World / Permissions tabs. */
public final class InfinityRingControlScreen extends Screen {
    private static final int TAB_COLUMN_WIDTH = 260;
    private static final ResourceLocation TAB_HEADER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private int time;
    private int weather;
    private int access;
    private final List<String> friends;
    private final UUID owner;
    private final boolean canDelete;
    private boolean confirmDelete;
    @Nullable
    private TabNavigationBar tabNavigationBar;
    @Nullable
    private EditBox nameBox;
    @Nullable
    private StringWidget friendsWidget;
    private final OperationMenu menu = new OperationMenu();
    private final OperationMenu suggestions = new OperationMenu();

    public InfinityRingControlScreen(int terrain, int time, int weather, int access, List<String> friends,
                                     UUID owner, boolean canDelete) {
        super(Component.translatable("gui.avaritia.infinity_ring.control"));
        this.time = time;
        this.weather = weather;
        this.access = access;
        this.friends = new ArrayList<>(friends);
        this.owner = owner;
        this.canDelete = canDelete;
    }

    @Override
    protected void init() {
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                .addTabs(new WorldTab(), new AccessTab())
                .build();
        this.addRenderableWidget(this.tabNavigationBar);
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());
        this.layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    protected void setInitialFocus() {
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int headerBottom = this.tabNavigationBar.getRectangle().bottom();
        this.tabManager.setTabArea(new ScreenRectangle(0, headerBottom, this.width,
                this.height - this.layout.getFooterHeight() - headerBottom));
        this.layout.setHeaderHeight(headerBottom);
        this.layout.arrangeElements();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        graphics.blit(Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
        suggestions.render(graphics, font, mouseX, mouseY);
        menu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        graphics.blit(TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.mouseClicked(mouseX, mouseY, button) || suggestions.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendModes() {
        PacketDistributor.sendToServer(pack(C2SInfinityRingPack.SET_MODES, access, ""));
    }

    private int columnWidth() {
        return Math.min(260, Math.max(160, width - 48));
    }

    private C2SInfinityRingPack pack(int action, int extra, String name) {
        return new C2SInfinityRingPack(action, 0, time, weather, extra, name, owner);
    }

    private Component friendsLabel() {
        return Component.translatable("gui.avaritia.infinity_ring.friends",
                friends.isEmpty() ? "-" : String.join(", ", friends));
    }

    private void refreshFriends() {
        if (friendsWidget != null) {
            friendsWidget.setMessage(friendsLabel());
        }
    }

    private void updateNameSuggestions(String value) {
        if (nameBox == null || value.isBlank()) {
            suggestions.close();
            return;
        }
        String prefix = value.toLowerCase();
        List<OperationMenu.Entry> entries = new ArrayList<>();
        for (String name : playerNames()) {
            if (name.toLowerCase().startsWith(prefix) && !name.equalsIgnoreCase(value)) {
                String chosen = name;
                entries.add(new OperationMenu.Entry(Component.literal(chosen), () -> nameBox.setValue(chosen)));
            }
            if (entries.size() >= 8) {
                break;
            }
        }
        if (entries.isEmpty()) {
            suggestions.close();
            return;
        }
        suggestions.open(nameBox.getX(), nameBox.getY() + nameBox.getHeight(), width, height, font, entries);
    }

    private List<String> playerNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String raw : friends) {
            int split = raw.lastIndexOf('|');
            names.add(split < 0 ? raw : raw.substring(0, split));
        }
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().getOnlinePlayers()
                    .forEach(info -> names.add(info.getProfile().getName()));
        }
        return new ArrayList<>(names);
    }

    private void openPlayerMenu(String name, String role, double mouseX, double mouseY) {
        List<OperationMenu.Entry> entries = new ArrayList<>();
        if ("BANNED".equals(role)) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.unban",
                    () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.UNBAN, access, name))));
        } else {
            for (InfinityRingSettings.Role next : InfinityRingSettings.Role.values()) {
                entries.add(new OperationMenu.Entry(
                        Component.translatable("gui.avaritia.infinity_ring.role." + next.name().toLowerCase()),
                        () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.SET_ROLE, next.ordinal(), name))));
            }
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.remove_friend",
                    () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.REMOVE_FRIEND, access, name))));
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.ban",
                    () -> PacketDistributor.sendToServer(pack(C2SInfinityRingPack.BAN, access, name))));
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_ring.visit", () -> {
                PacketDistributor.sendToServer(pack(C2SInfinityRingPack.VISIT, access, name));
                onClose();
            }));
        }
        menu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private final class WorldTab extends GridLayoutTab {
        WorldTab() {
            super(Component.translatable("gui.avaritia.infinity_ring.tab.world"));
            var rows = this.layout.rowSpacing(8).createRowHelper(1);
            rows.addChild(cycle(
                    InfinityRingSettings.TimeMode.values(),
                    InfinityRingSettings.TimeMode.byId(time),
                    "gui.avaritia.infinity_ring.option.time",
                    "gui.avaritia.infinity_ring.time.",
                    value -> {
                        time = value.ordinal();
                        sendModes();
                    }));
            rows.addChild(cycle(
                    InfinityRingSettings.WeatherMode.values(),
                    InfinityRingSettings.WeatherMode.byId(weather),
                    "gui.avaritia.infinity_ring.option.weather",
                    "gui.avaritia.infinity_ring.weather.",
                    value -> {
                        weather = value.ordinal();
                        sendModes();
                    }));
        }
    }

    private final class AccessTab extends GridLayoutTab {
        AccessTab() {
            super(Component.translatable("gui.avaritia.infinity_ring.tab.access"));
            int col = columnWidth();
            var rows = this.layout.rowSpacing(6).createRowHelper(1);
            rows.addChild(cycle(
                    InfinityRingSettings.Access.values(),
                    InfinityRingSettings.Access.byId(access),
                    "gui.avaritia.infinity_ring.option.access",
                    "gui.avaritia.infinity_ring.access.",
                    value -> {
                        access = value.ordinal();
                        sendModes();
                    }));
            nameBox = new EditBox(font, 0, 0, col, 20,
                    Component.translatable("gui.avaritia.infinity_ring.player"));
            nameBox.setMaxLength(32);
            nameBox.setResponder(InfinityRingControlScreen.this::updateNameSuggestions);
            rows.addChild(CommonLayouts.labeledElement(font, nameBox,
                    Component.translatable("gui.avaritia.infinity_ring.player")));
            int actionW = Math.max(50, (col - 12) / 3);
            LinearLayout actions = LinearLayout.horizontal().spacing(6);
            actions.addChild(Button.builder(Component.translatable("gui.avaritia.infinity_ring.add_friend"), button -> {
                String name = nameBox.getValue().trim();
                if (name.isEmpty()) {
                    return;
                }
                PacketDistributor.sendToServer(pack(C2SInfinityRingPack.ADD_FRIEND, access, name));
            }).width(actionW).build());
            actions.addChild(Button.builder(Component.translatable("gui.avaritia.infinity_ring.ban"), button -> {
                String name = nameBox.getValue().trim();
                if (!name.isEmpty()) {
                    PacketDistributor.sendToServer(pack(C2SInfinityRingPack.BAN, access, name));
                }
            }).width(actionW).build());
            actions.addChild(Button.builder(Component.translatable("gui.avaritia.infinity_ring.visit"), button -> {
                String name = nameBox.getValue().trim();
                if (!name.isEmpty()) {
                    PacketDistributor.sendToServer(pack(C2SInfinityRingPack.VISIT, access, name));
                    onClose();
                }
            }).width(actionW).build());
            rows.addChild(actions);
            int listH = Math.min(110, Math.max(48, InfinityRingControlScreen.this.height / 4));
            rows.addChild(new PlayerList(col, listH));
            if (canDelete) {
                rows.addChild(Button.builder(Component.translatable("gui.avaritia.infinity_ring.delete"), button -> {
                    if (!confirmDelete) {
                        confirmDelete = true;
                        button.setMessage(Component.translatable("gui.avaritia.infinity_ring.delete_confirm"));
                        return;
                    }
                    PacketDistributor.sendToServer(pack(C2SInfinityRingPack.DELETE, access, ""));
                    onClose();
                }).width(col).build());
            }
        }
    }

    private final class PlayerList extends ObjectSelectionList<PlayerList.PlayerEntry> {
        PlayerList(int width, int height) {
            super(InfinityRingControlScreen.this.minecraft, width, height, 0, 18);
            for (String raw : friends) {
                addEntry(new PlayerEntry(raw));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.max(80, this.getWidth() - 12);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.getWidth() - 6;
        }

        private final class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {
            private final String raw;
            private final String name;
            private final String role;

            private PlayerEntry(String raw) {
                this.raw = raw;
                int split = raw.lastIndexOf('|');
                this.name = split < 0 ? raw : raw.substring(0, split);
                this.role = split < 0 ? "MEMBER" : raw.substring(split + 1);
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                graphics.drawString(font, name + "  [" + role + "]", left + 2, top + 4, 0xFFFFFF, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                nameBox.setValue(name);
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    openPlayerMenu(name, role, mouseX, mouseY);
                }
                return true;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(raw);
            }
        }
    }

    private <T extends Enum<T>> CycleButton<T> cycle(T[] values, T current, String optionKey, String valuePrefix,
                                                     Consumer<T> setter) {
        CycleButton<T> button = CycleButton.builder((T value) -> Component.translatable(valuePrefix + value.name().toLowerCase()))
                .withValues(values)
                .withTooltip(value -> Tooltip.create(Component.translatable(valuePrefix + value.name().toLowerCase() + ".info")))
                .create(0, 0, columnWidth(), 20, Component.translatable(optionKey),
                        (ignored, value) -> setter.accept(value));
        button.setValue(current);
        return button;
    }
}
