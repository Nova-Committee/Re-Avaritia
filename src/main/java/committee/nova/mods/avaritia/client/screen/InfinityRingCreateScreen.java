package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/** First-use Infinity Ring creation with on-panel CycleButtons and Create/Cancel. */
public final class InfinityRingCreateScreen extends Screen {
    private static final int MARGIN = 8;
    private static final int HEADER_H = 20;
    private static final int FOOTER_H = 24;
    private static final int BTN_H = 20;
    private static final int GAP = 3;

    private int terrain;
    private int time;
    private int weather;
    private int access;

    private ScreenRectangle panel;
    private ScreenRectangle help;

    public InfinityRingCreateScreen(int terrain, int time, int weather, int access) {
        super(Component.translatable("gui.avaritia.infinity_ring.create"));
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
    }

    @Override
    protected void init() {
        panel = PortableLayout.centered(width, height, 300, 200, MARGIN);
        int panelX = panel.left();
        int panelY = panel.top();
        int panelW = panel.width();
        int panelH = panel.height();
        int innerX = panelX + 8;
        int innerW = panelW - 16;
        GridLayout form = new GridLayout(innerX, panelY + HEADER_H + 4).rowSpacing(GAP);
        int footerY = panelY + panelH - FOOTER_H;

        addCycle(form, 0, innerW,
                "gui.avaritia.infinity_ring.option.terrain", "gui.avaritia.infinity_ring.terrain.",
                InfinityRingSettings.Terrain.values(), InfinityRingSettings.Terrain.byId(terrain),
                value -> terrain = value.ordinal());
        addCycle(form, 1, innerW,
                "gui.avaritia.infinity_ring.option.time", "gui.avaritia.infinity_ring.time.",
                InfinityRingSettings.TimeMode.values(), InfinityRingSettings.TimeMode.byId(time),
                value -> time = value.ordinal());
        addCycle(form, 2, innerW,
                "gui.avaritia.infinity_ring.option.weather", "gui.avaritia.infinity_ring.weather.",
                InfinityRingSettings.WeatherMode.values(), InfinityRingSettings.WeatherMode.byId(weather),
                value -> weather = value.ordinal());
        addCycle(form, 3, innerW,
                "gui.avaritia.infinity_ring.option.access", "gui.avaritia.infinity_ring.access.",
                InfinityRingSettings.Access.values(), InfinityRingSettings.Access.byId(access),
                value -> access = value.ordinal());
        form.arrangeElements();
        int cursor = form.getY() + form.getHeight() + GAP;

        help = new ScreenRectangle(innerX, cursor, Math.max(0, innerW), Math.max(0, footerY - GAP - cursor));

        int btnW = Math.min(100, (innerW - GAP) / 2);
        addRenderableWidget(UiInspector.name(PortableUi.button(innerX, footerY, btnW, BTN_H,
                CommonComponents.GUI_CANCEL, button -> onClose()), "ring.create.cancel"));
        addRenderableWidget(UiInspector.name(PortableUi.button(innerX + innerW - btnW, footerY, btnW, BTN_H,
                Component.translatable("gui.avaritia.infinity_ring.confirm"), button -> create()), "ring.create.confirm"));
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (help.width() > 0 && help.height() > 0) {
            graphics.enableScissor(help.left(), help.top(), help.right(), help.bottom());
            graphics.textWithWordWrap(font, Component.translatable("gui.avaritia.infinity_ring.access.help"),
                    help.left(), help.top(), help.width(), PortableUi.MUTED);
            UiInspector.region("ring.create.help", help, help, false);
            graphics.disableScissor();
        }
    }

    @Override
    protected void extractMenuBackground(@NotNull GuiGraphicsExtractor graphics) {
        PortableUi.panel(graphics, panel);
        UiInspector.region("ring.create.panel", panel, null, false);
        PortableUi.header(graphics, font, title, panel.left(), panel.top(), panel.width());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void create() {
        NetworkHandler.sendToServer(new C2SInfinityRingPack(
                C2SInfinityRingPack.CREATE, terrain, time, weather, access, "", new UUID(0L, 0L)));
        onClose();
    }

    private <T extends Enum<T>> void addCycle(GridLayout form, int row, int w, String optionKey, String valuePrefix,
                                              T[] values, T current, Consumer<T> changed) {
        CycleButton<T> cycle = addRenderableWidget(CycleButton.builder((T value) -> Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT)), current)
                .withValues(values)
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT) + ".info")))
                .create(0, 0, w, BTN_H, Component.translatable(optionKey),
                        (ignored, value) -> changed.accept(value)));
        form.addChild(cycle, row, 0);
        if (UiInspector.enabled()) {
            UiInspector.name(cycle, "ring.create." + optionKey.substring(optionKey.lastIndexOf('.') + 1));
        }
    }
}
