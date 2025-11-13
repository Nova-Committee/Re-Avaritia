package committee.nova.mods.avaritia.client.screen.side;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * NeutronCollector专用面配置界面
 * Description: 只允许PASSIVE_OUTPUT和ACTIVE_OUTPUT两种模式
 * @author cnlimiter
 * Date: 2025/11/14
 * Version: 1.0
 */
public class NCollectorSideConfigScreen extends Screen {

    private SideConfiguration sideConfig;
    private final Screen parentScreen;
    private final BlockPos blockPos;
    private final NeutronCollectorTile tile;

    // 屏幕坐标字段
    private int guiLeft;
    private int guiTop;
    private int imageWidth = 156;
    private int imageHeight = 117;

    // 六个面配置按钮
    private SideButton northButton;
    private SideButton southButton;
    private SideButton eastButton;
    private SideButton westButton;
    private SideButton upButton;
    private SideButton downButton;

    public NCollectorSideConfigScreen(Screen parentScreen, SideConfiguration sideConfig, BlockPos blockPos, NeutronCollectorTile tile) {
        super(Component.translatable("screen.avaritia.side_config.title"));
        this.parentScreen = parentScreen;
        this.sideConfig = new SideConfiguration(sideConfig);
        this.blockPos = blockPos;
        this.tile = tile;
    }

    @Override
    protected void init() {
        super.init();
        // 计算居中位置
        this.guiLeft = (this.width - this.imageWidth) / 2;
        this.guiTop = (this.height - this.imageHeight) / 2;
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 创建六个面的配置按钮（3D立体视图）
        createSideButtons(x, y);

        // 添加一键清除配置按钮
        this.addRenderableWidget(
                new ImageButton(x + 135, y + 93, 17, 18,
                        17, 164, 18, Res.SIDE_CONFIG_TEX,
                        (button) -> setAllSides(SideConfiguration.SideMode.OFF)));
        // 添加返回按钮
        this.addRenderableWidget(
                new ImageButton(x + 4, y + 4, 17, 18,
                        0, 164, 18, Res.SIDE_CONFIG_TEX,
                        (button) -> onClose()));
    }

    /**
     * 创建六个面的配置按钮
     */
    private void createSideButtons(int x, int y) {
        int centerX = x + 67; // 中心点
        int centerY = y + 50;

        // 上（顶部视图）
        upButton = new SideButton(centerX, centerY - 24, Direction.UP, sideConfig.getSideMode(Direction.UP));
        this.addRenderableWidget(upButton);

        // 下（底部视图）
        downButton = new SideButton(centerX, centerY + 24, Direction.DOWN, sideConfig.getSideMode(Direction.DOWN));
        this.addRenderableWidget(downButton);

        // 北（前面）
        northButton = new SideButton(centerX, centerY, Direction.NORTH, sideConfig.getSideMode(Direction.NORTH));
        this.addRenderableWidget(northButton);

        // 南（后面）
        southButton = new SideButton(centerX - 23, centerY + 24, Direction.SOUTH, sideConfig.getSideMode(Direction.SOUTH));
        this.addRenderableWidget(southButton);

        // 西（左面）
        westButton = new SideButton(centerX - 23, centerY, Direction.WEST, sideConfig.getSideMode(Direction.WEST));
        this.addRenderableWidget(westButton);

        // 东（右面）
        eastButton = new SideButton(centerX + 23, centerY, Direction.EAST, sideConfig.getSideMode(Direction.EAST));
        this.addRenderableWidget(eastButton);
    }

    /**
     * 为指定方向切换模式（仅在PASSIVE_OUTPUT和ACTIVE_OUTPUT之间切换）
     */
    private void cycleModeForDirection(Direction direction) {
        tile.cycleSideModeForNeutronCollector(direction);
        // 更新本地配置显示
        sideConfig.setSideMode(direction, tile.getSideConfiguration().getSideMode(direction));
    }

    /**
     * 更新按钮的工具提示
     */
    private void updateButtonTooltip(SideButton button) {
        button.tooltip.clear();
        String sideName = Component.translatable("direction.avaritia." + button.direction.getName()).getString();
        String modeName = button.mode.getDisplayName().getString();

        button.tooltip.add(Component.literal(sideName + ": " + modeName).getVisualOrderText());
        button.tooltip.add(Component.translatable("tooltip.avaritia.side.click_to_cycle").getVisualOrderText());
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
    private void updateAllButtons() {
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
    private void sendConfigUpdate() {
        NetworkHandler.sendSideConfigUpdate(blockPos, sideConfig);
    }

    /**
     * 获取GUI左边界坐标
     */
    public int getGuiLeft() {
        return guiLeft;
    }

    /**
     * 获取GUI上边界坐标
     */
    public int getGuiTop() {
        return guiTop;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染背景
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        guiGraphics.blit(Res.SIDE_CONFIG_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        // 渲染子组件（按钮等）
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

    /**
     * 面配置按钮类（为NeutronCollector定制）
     */
    private class SideButton extends ImageButton {
        private final Direction direction;
        private SideConfiguration.SideMode mode;
        private final List<FormattedCharSequence> tooltip;

        public SideButton(int x, int y, Direction direction, SideConfiguration.SideMode mode) {
            super(x, y, 22, 23, 0, 118, 23, Res.SIDE_CONFIG_TEX, button -> {
                NCollectorSideConfigScreen.this.cycleModeForDirection(direction);
                // 更新按钮显示
                updateAllButtons();
                sendConfigUpdate();
            });
            this.direction = direction;
            this.mode = mode;
            this.tooltip = new ArrayList<>();
            updateTooltip();
        }

        private void updateTooltip() {
            NCollectorSideConfigScreen.this.updateButtonTooltip(this);
        }


        public void updateMode(SideConfiguration.SideMode newMode) {
            this.mode = newMode;
            updateTooltip();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            // 根据模式选择不同的纹理区域
            // OFF = 0, PASSIVE_INPUT = 1, PASSIVE_OUTPUT = 2, PASSIVE_MIXIN = 3,
            // ACTIVE_INPUT = 4, ACTIVE_OUTPUT = 5, ACTIVE_MIXIN = 6
            int texX;
            int texY = 118; // 按钮纹理在图集中的Y位置

            if (this.mode == SideConfiguration.SideMode.OFF) {
                texX = 0;
            } else if (this.mode == SideConfiguration.SideMode.PASSIVE_OUTPUT) {
                texX = 2 * 22; // PASSIVE_OUTPUT在位置2
            } else if (this.mode == SideConfiguration.SideMode.ACTIVE_OUTPUT) {
                texX = 5 * 22; // ACTIVE_OUTPUT在位置5
            } else {
                // 非法模式，显示为OFF
                texX = 0;
            }

            if (this.isHovered) {
                texY += 23; // 悬停状态
            }

            pGuiGraphics.blit(Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), texX, texY, this.width, this.height, 256, 256);

            if (this.isHovered) {
                setTooltipForNextRenderPass(tooltip);
            }
        }
    }
}
