package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/** First-use Infinity Ring creation with World and Permissions tabs. */
public final class InfinityRingCreateScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HEADER_H = 20;
    private static final int TAB_H = 16;
    private static final int FOOTER_H = 22;
    private static final int ROW_H = 18;
    private static final int GAP = 3;

    private int terrain;
    private int time;
    private int weather;
    private int access;
    private int tab;

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
    private int helpX;
    private int helpY;
    private int helpW;

    private final OperationMenu menu = new OperationMenu();

    public InfinityRingCreateScreen(int terrain, int time, int weather, int access) {
        super(Component.translatable("gui.avaritia.infinity_ring.create"));
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
    }

    @Override
    protected void init() {
        menu.close();
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
        if (tab == 0) {
            addRenderableWidget(new OptionRow<>(innerX, contentY, innerW, ROW_H,
                    "gui.avaritia.infinity_ring.option.terrain", "gui.avaritia.infinity_ring.terrain.",
                    InfinityRingSettings.Terrain.values(), InfinityRingSettings.Terrain.byId(terrain),
                    value -> terrain = value.ordinal()));
            addRenderableWidget(new OptionRow<>(innerX, contentY + ROW_H + GAP, innerW, ROW_H,
                    "gui.avaritia.infinity_ring.option.time", "gui.avaritia.infinity_ring.time.",
                    InfinityRingSettings.TimeMode.values(), InfinityRingSettings.TimeMode.byId(time),
                    value -> time = value.ordinal()));
            addRenderableWidget(new OptionRow<>(innerX, contentY + (ROW_H + GAP) * 2, innerW, ROW_H,
                    "gui.avaritia.infinity_ring.option.weather", "gui.avaritia.infinity_ring.weather.",
                    InfinityRingSettings.WeatherMode.values(), InfinityRingSettings.WeatherMode.byId(weather),
                    value -> weather = value.ordinal()));
            helpX = innerX;
            helpY = contentY + (ROW_H + GAP) * 3;
            helpW = innerW;
        } else {
            addRenderableWidget(new OptionRow<>(innerX, contentY, innerW, ROW_H,
                    "gui.avaritia.infinity_ring.option.access", "gui.avaritia.infinity_ring.access.",
                    InfinityRingSettings.Access.values(), InfinityRingSettings.Access.byId(access),
                    value -> access = value.ordinal()));
            helpX = innerX;
            helpY = contentY + ROW_H + GAP;
            helpW = innerW;
        }

        int btnW = 90;
        addRenderableWidget(PortableUi.button(panelX + panelW - 8 - btnW, footerY, btnW, 20,
                CommonComponents.GUI_CANCEL, button -> onClose()));
    }

    private void switchTab(int next) {
        if (tab == next) {
            return;
        }
        menu.close();
        tab = next;
        rebuildWidgets();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (helpW > 0) {
            int helpBottom = panelY + panelH - FOOTER_H - GAP;
            graphics.enableScissor(helpX, helpY, helpX + helpW, Math.max(helpY + 8, helpBottom));
            int textY = helpY;
            if (tab == 1) {
                graphics.drawWordWrap(font, Component.translatable("gui.avaritia.infinity_ring.access.help"),
                        helpX, textY, helpW, PortableUi.MUTED);
                textY += font.split(Component.translatable("gui.avaritia.infinity_ring.access.help"), helpW).size()
                        * font.lineHeight + GAP;
            }
            graphics.drawWordWrap(font, Component.translatable("gui.avaritia.infinity_ring.create_hint"),
                    helpX, textY, helpW, PortableUi.MUTED);
            graphics.disableScissor();
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && inInset(mouseX, mouseY)) {
            openCreateMenu((int) mouseX, (int) mouseY);
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
            openCreateMenu(insetX + 8, insetY + 8);
            return true;
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

    private void openCreateMenu(int mouseX, int mouseY) {
        menu.open(mouseX, mouseY, width, height, font, List.of(
                OperationMenu.Entry.of("gui.avaritia.infinity_ring.confirm", this::create)));
    }

    private void create() {
        PacketDistributor.sendToServer(new C2SInfinityRingPack(
                C2SInfinityRingPack.CREATE, terrain, time, weather, access, "", new UUID(0L, 0L)));
        onClose();
    }

    private <T extends Enum<T>> void openOptionMenu(int mouseX, int mouseY, String valuePrefix,
                                                    T[] values, T current, Consumer<T> changed) {
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
}
