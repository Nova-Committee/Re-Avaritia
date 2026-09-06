package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
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

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int helpX;
    private int helpY;
    private int helpW;
    private int helpH;

    public InfinityRingCreateScreen(int terrain, int time, int weather, int access) {
        super(Component.translatable("gui.avaritia.infinity_ring.create"));
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
    }

    @Override
    protected void init() {
        panelW = Math.min(300, width - MARGIN * 2);
        panelH = Math.min(200, height - MARGIN * 2);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        int innerX = panelX + 8;
        int innerW = panelW - 16;
        int cursor = panelY + HEADER_H + 4;
        int footerY = panelY + panelH - FOOTER_H;

        addCycle(innerX, cursor, innerW,
                "gui.avaritia.infinity_ring.option.terrain", "gui.avaritia.infinity_ring.terrain.",
                InfinityRingSettings.Terrain.values(), InfinityRingSettings.Terrain.byId(terrain),
                value -> terrain = value.ordinal());
        cursor += BTN_H + GAP;
        addCycle(innerX, cursor, innerW,
                "gui.avaritia.infinity_ring.option.time", "gui.avaritia.infinity_ring.time.",
                InfinityRingSettings.TimeMode.values(), InfinityRingSettings.TimeMode.byId(time),
                value -> time = value.ordinal());
        cursor += BTN_H + GAP;
        addCycle(innerX, cursor, innerW,
                "gui.avaritia.infinity_ring.option.weather", "gui.avaritia.infinity_ring.weather.",
                InfinityRingSettings.WeatherMode.values(), InfinityRingSettings.WeatherMode.byId(weather),
                value -> weather = value.ordinal());
        cursor += BTN_H + GAP;
        addCycle(innerX, cursor, innerW,
                "gui.avaritia.infinity_ring.option.access", "gui.avaritia.infinity_ring.access.",
                InfinityRingSettings.Access.values(), InfinityRingSettings.Access.byId(access),
                value -> access = value.ordinal());
        cursor += BTN_H + GAP;

        helpX = innerX;
        helpY = cursor;
        helpW = innerW;
        helpH = Math.max(8, footerY - GAP - helpY);

        int btnW = Math.min(100, (innerW - GAP) / 2);
        addRenderableWidget(PortableUi.button(innerX, footerY, btnW, BTN_H,
                CommonComponents.GUI_CANCEL, button -> onClose()));
        addRenderableWidget(PortableUi.button(innerX + innerW - btnW, footerY, btnW, BTN_H,
                Component.translatable("gui.avaritia.infinity_ring.confirm"), button -> create()));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (helpW > 0 && helpH > 0) {
            graphics.enableScissor(helpX, helpY, helpX + helpW, helpY + helpH);
            graphics.drawWordWrap(font, Component.translatable("gui.avaritia.infinity_ring.access.help"),
                    helpX, helpY, helpW, PortableUi.MUTED);
            graphics.disableScissor();
        }
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        PortableUi.panel(graphics, panelX, panelY, panelW, panelH);
        PortableUi.header(graphics, font, title, panelX, panelY, panelW);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void create() {
        PacketDistributor.sendToServer(new C2SInfinityRingPack(
                C2SInfinityRingPack.CREATE, terrain, time, weather, access, "", new UUID(0L, 0L)));
        onClose();
    }

    private <T extends Enum<T>> void addCycle(int x, int y, int w, String optionKey, String valuePrefix,
                                              T[] values, T current, Consumer<T> changed) {
        addRenderableWidget(CycleButton.builder((T value) -> Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT)))
                .withValues(values)
                .withInitialValue(current)
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        valuePrefix + value.name().toLowerCase(Locale.ROOT) + ".info")))
                .create(x, y, w, BTN_H, Component.translatable(optionKey),
                        (ignored, value) -> changed.accept(value)));
    }
}
