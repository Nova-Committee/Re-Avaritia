package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.common.net.C2SInfinityRingPack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** First-use Infinity Ring creation, laid out like vanilla Create World. */
public final class InfinityRingCreateScreen extends Screen {
    private static final int TAB_COLUMN_WIDTH = 210;
    private static final ResourceLocation TAB_HEADER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/tab_header_background.png");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private int terrain;
    private int time;
    private int weather;
    private int access;
    @Nullable
    private TabNavigationBar tabNavigationBar;

    public InfinityRingCreateScreen(int terrain, int time, int weather, int access) {
        super(Component.translatable("gui.avaritia.infinity_ring.create"));
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
    }

    @Override
    protected void init() {
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                .addTabs(new WorldTab(), new AccessTab())
                .build();
        this.addRenderableWidget(this.tabNavigationBar);
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("gui.avaritia.infinity_ring.confirm"), button -> create())
                .build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).build());
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
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            create();
            return true;
        }
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        graphics.blit(Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
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

    private void create() {
        PacketDistributor.sendToServer(new C2SInfinityRingPack(
                C2SInfinityRingPack.CREATE, terrain, time, weather, access, "", new UUID(0L, 0L)));
        onClose();
    }

    private final class WorldTab extends GridLayoutTab {
        WorldTab() {
            super(Component.translatable("gui.avaritia.infinity_ring.tab.world"));
            var rows = this.layout.rowSpacing(8).createRowHelper(1);
            rows.addChild(cycle(
                    InfinityRingSettings.Terrain.values(),
                    InfinityRingSettings.Terrain.byId(terrain),
                    "gui.avaritia.infinity_ring.option.terrain",
                    "gui.avaritia.infinity_ring.terrain.",
                    value -> terrain = value.ordinal()));
            rows.addChild(cycle(
                    InfinityRingSettings.TimeMode.values(),
                    InfinityRingSettings.TimeMode.byId(time),
                    "gui.avaritia.infinity_ring.option.time",
                    "gui.avaritia.infinity_ring.time.",
                    value -> time = value.ordinal()));
            rows.addChild(cycle(
                    InfinityRingSettings.WeatherMode.values(),
                    InfinityRingSettings.WeatherMode.byId(weather),
                    "gui.avaritia.infinity_ring.option.weather",
                    "gui.avaritia.infinity_ring.weather.",
                    value -> weather = value.ordinal()));
        }
    }

    private final class AccessTab extends GridLayoutTab {
        AccessTab() {
            super(Component.translatable("gui.avaritia.infinity_ring.tab.access"));
            var rows = this.layout.rowSpacing(8).createRowHelper(1);
            rows.addChild(cycle(
                    InfinityRingSettings.Access.values(),
                    InfinityRingSettings.Access.byId(access),
                    "gui.avaritia.infinity_ring.option.access",
                    "gui.avaritia.infinity_ring.access.",
                    value -> access = value.ordinal()));
            rows.addChild(new MultiLineTextWidget(
                    Component.translatable("gui.avaritia.infinity_ring.access.help"), font)
                    .setMaxWidth(TAB_COLUMN_WIDTH));
        }
    }

    private static <T extends Enum<T>> CycleButton<T> cycle(T[] values, T current, String optionKey, String valuePrefix,
                                                            java.util.function.Consumer<T> setter) {
        CycleButton<T> button = CycleButton.builder((T value) -> Component.translatable(valuePrefix + value.name().toLowerCase()))
                .withValues(values)
                .withTooltip(value -> Tooltip.create(Component.translatable(valuePrefix + value.name().toLowerCase() + ".info")))
                .create(0, 0, TAB_COLUMN_WIDTH, 20, Component.translatable(optionKey),
                        (ignored, value) -> setter.accept(value));
        button.setValue(current);
        return button;
    }
}
