package committee.nova.mods.avaritia.client.screen.side;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 方块配置界面
 * Description: 类似Mekanism的六面配置界面，支持多种方块类型
 * @author cnlimiter
 * Date: 2025/11/14
 * Version: 1.0
 */
public class SideConfigScreen extends Screen {

    private SideConfiguration sideConfig;
    private final Screen parentScreen;
    private final BlockPos blockPos;
    private final ITileIO tile;

    // 屏幕坐标字段
    // 屏幕坐标字段（由于Screen没有imageWidth等，我们需要自己管理）
    @Getter
    private int guiLeft;
    @Getter
    private int guiTop;
    private int imageWidth = 156;
    private int imageHeight = 117;
    private ScreenRectangle panel;

    // 六个面配置按钮
    private SideButton northButton;
    private SideButton southButton;
    private SideButton eastButton;
    private SideButton westButton;
    private SideButton upButton;
    private SideButton downButton;

    public SideConfigScreen(Screen parentScreen, SideConfiguration sideConfig, BlockPos blockPos, ITileIO tile) {
        super(Component.translatable("screen.avaritia.side_config.title"));
        this.parentScreen = parentScreen;
        this.sideConfig = new SideConfiguration(sideConfig);
        this.blockPos = blockPos;
        this.tile = tile;
    }

    @Override
    protected void init() {
        super.init();
        panel = PortableLayout.centered(width, height, imageWidth, imageHeight, 0);
        this.guiLeft = panel.left();
        this.guiTop = panel.top();
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 创建六个面的配置按钮（3D立体视图）
        createSideButtons(x, y);

        // 添加一键清除配置按钮
        this.addRenderableWidget(UiInspector.name(
                new ImageButton(x + 135, y + 93, 17, 18,
                        new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX),
                        (button) -> setAllSides(SideConfiguration.SideMode.OFF)) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int vOffset = 164, textureDifference = 18;
                        int i = vOffset;
                        if (!this.isActive()) {
                            i = vOffset + textureDifference * 2;
                        } else if (this.isHoveredOrFocused()) {
                            i = vOffset + textureDifference;
                        }

                        RenderSystem.enableDepthTest();
                        guiGraphics.blit(Res.SIDE_CONFIG_TEX, getX(), getY(), 17, i, getWidth(), getHeight(), 256, 256);
                    }
                }

        , "side.clear"));
        // 添加返回按钮
        this.addRenderableWidget(UiInspector.name(
                new ImageButton(x + 4, y + 4, 17, 18,
                        new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX),
                        (button) -> onClose()) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int vOffset = 164, textureDifference = 18;
                        int i = vOffset;
                        if (!this.isActive()) {
                            i = vOffset + textureDifference * 2;
                        } else if (this.isHoveredOrFocused()) {
                            i = vOffset + textureDifference;
                        }

                        RenderSystem.enableDepthTest();
                        guiGraphics.blit(Res.SIDE_CONFIG_TEX, getX(), getY(), 0, i, getWidth(), getHeight(), 256, 256);
                    }
                }
        , "side.back"));
    }

    /**
     * 创建六个面的配置按钮
     */
    private void createSideButtons(int x, int y) {
        int centerX = x + 67; // 中心点
        int centerY = y + 50;

        // 上（顶部视图）
        upButton = new SideButton(centerX, centerY - 24, Direction.UP, sideConfig.getSideMode(Direction.UP), this);
        this.addRenderableWidget(UiInspector.name(upButton, "side.up"));

        // 下（底部视图）
        downButton = new SideButton(centerX, centerY + 24, Direction.DOWN, sideConfig.getSideMode(Direction.DOWN), this);
        this.addRenderableWidget(UiInspector.name(downButton, "side.down"));

        // 北（前面）
        northButton = new SideButton(centerX, centerY, Direction.NORTH, sideConfig.getSideMode(Direction.NORTH), this);
        this.addRenderableWidget(UiInspector.name(northButton, "side.north"));

        // 南（后面）
        southButton = new SideButton(centerX - 23, centerY + 24, Direction.SOUTH, sideConfig.getSideMode(Direction.SOUTH), this);
        this.addRenderableWidget(UiInspector.name(southButton, "side.south"));

        // 西（左面）
        westButton = new SideButton(centerX - 23, centerY, Direction.WEST, sideConfig.getSideMode(Direction.WEST), this);
        this.addRenderableWidget(UiInspector.name(westButton, "side.west"));

        // 东（右面）
        eastButton = new SideButton(centerX + 23, centerY, Direction.EAST, sideConfig.getSideMode(Direction.EAST), this);
        this.addRenderableWidget(UiInspector.name(eastButton, "side.east"));
    }

    /**
     * 为指定方向切换模式
     */
    public void cycleModeForDirection(Direction direction) {
        tile.cycleSideModeForNeutronCollector(direction);
        // 更新本地配置显示
        sideConfig.setSideMode(direction, tile.getSideConfiguration().getSideMode(direction));
    }

    /**
     * 设置所有面为指定模式
     */
    private void setAllSides(SideConfiguration.SideMode mode) {
        for (Direction direction : Direction.values()) {
            sideConfig.setSideMode(direction, mode);
            tile.cycleSideModeForNeutronCollector(direction);
        }
        updateAllButtons();
        sendConfigUpdate();
    }

    /**
     * 更新所有按钮状态
     */
    public void updateAllButtons() {
        northButton.updateMode(sideConfig.getSideMode(Direction.NORTH));
        southButton.updateMode(sideConfig.getSideMode(Direction.SOUTH));
        eastButton.updateMode(sideConfig.getSideMode(Direction.EAST));
        westButton.updateMode(sideConfig.getSideMode(Direction.WEST));
        upButton.updateMode(sideConfig.getSideMode(Direction.UP));
        downButton.updateMode(sideConfig.getSideMode(Direction.DOWN));
    }

    /**
     * 发送配置更新到服务端
     */
    public void sendConfigUpdate() {
        NetworkHandler.sendSideConfigUpdate(blockPos, sideConfig);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderTransparentBackground(guiGraphics);
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        guiGraphics.blit(Res.SIDE_CONFIG_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        UiInspector.region("side.panel", panel, null, false);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        super.renderBlurredBackground(partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft.screen == this) {
            this.minecraft.popGuiLayer();
        }
    }

}
