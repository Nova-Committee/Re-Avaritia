package committee.nova.mods.avaritia.client.screen.config;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.client.screen.NeutronCompressorScreen;
import committee.nova.mods.avaritia.common.tile.config.SideConfiguration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
 * 方块配置界面
 * Description: 类似Mekanism的六面配置界面
 * Author: 幽浮喵
 * Date: 2025/11/01
 * Version: 1.0
 */
public class SideConfigScreen extends Screen {

    private SideConfiguration sideConfig;
    private final NeutronCompressorScreen parentScreen;
    private final BlockPos blockPos;

    // 屏幕坐标字段（由于Screen没有imageWidth等，我们需要自己管理）
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

    // 快捷配置按钮
    private Button allOffButton;
    private Button allInputButton;
    private Button allOutputButton;
    private Button passiveModeButton;
    private Button activeModeButton;

    public SideConfigScreen(NeutronCompressorScreen parentScreen, SideConfiguration sideConfig, BlockPos blockPos) {
        super(Component.translatable("screen.avaritia.side_config.title"));
        this.parentScreen = parentScreen;
        this.sideConfig = new SideConfiguration(sideConfig); // 创建副本
        this.blockPos = blockPos;
        // Screen类没有imageWidth和imageHeight，我们使用固定的尺寸
    }

    @Override
    protected void init() {
        super.init();
        // 计算居中位置（Screen没有getGuiLeft()方法，我们需要自己计算）
        this.guiLeft = (this.width - this.imageWidth) / 2;
        this.guiTop = (this.height - this.imageHeight) / 2;
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 创建六个面的配置按钮（3D立体视图）
        createSideButtons(x, y);

        // 创建快捷配置按钮
        //createQuickConfigButtons(x, y);

        // 添加一键清除配置按钮
        this.addRenderableWidget(
                new ImageButton(x + 135, y + 93, 17, 18,
                        34, 164, 18, Res.SIDE_CONFIG_TEX,
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
     * 创建快捷配置按钮
     */
    private void createQuickConfigButtons(int x, int y) {
        // 第一行快捷按钮
        this.addRenderableWidget(Button.builder(
                Component.literal("×").withStyle(style -> style.withColor(0xFF5555)),
                button -> setAllSides(SideConfiguration.SideMode.OFF)
        ).bounds(x + 5, y + 5, 20, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("←").withStyle(style -> style.withColor(0x55FF55)),
                button -> setAllSides(SideConfiguration.SideMode.PASSIVE_INPUT)
        ).bounds(x + 30, y + 5, 20, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("→").withStyle(style -> style.withColor(0x5555FF)),
                button -> setAllSides(SideConfiguration.SideMode.PASSIVE_OUTPUT)
        ).bounds(x + 55, y + 5, 20, 20).build());

        // 第二行快捷按钮
        this.addRenderableWidget(Button.builder(
                Component.literal("⇐").withStyle(style -> style.withColor(0xFFFF55)),
                button -> setAllSides(SideConfiguration.SideMode.ACTIVE_INPUT)
        ).bounds(x + 125, y + 5, 20, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("⇒").withStyle(style -> style.withColor(0xFF55FF)),
                button -> setAllSides(SideConfiguration.SideMode.ACTIVE_OUTPUT)
        ).bounds(x + 150, y + 5, 20, 20).build());
    }

    /**
     * 为指定方向切换模式
     */
    private void cycleModeForDirection(Direction direction) {
        sideConfig.cycleSideMode(direction);
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

        // 渲染标题
        guiGraphics.drawString(font, Component.translatable("screen.avaritia.side_config.title").getString(),
                this.getGuiLeft() + 20, this.getGuiTop() + 4, 4210752, false);

        // 渲染子组件（按钮等）
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

    /**
     * 面配置按钮类
     */
    private class SideButton extends ImageButton {
        private final Direction direction;
        private SideConfiguration.SideMode mode;
        private final List<FormattedCharSequence> tooltip;

        public SideButton(int x, int y, Direction direction, SideConfiguration.SideMode mode) {
            super(x, y, 22, 23, 0, 118, 23, Res.SIDE_CONFIG_TEX, button -> {
                SideConfigScreen.this.cycleModeForDirection(direction);
                updateAllButtons();
                sendConfigUpdate();
            });
            this.direction = direction;
            this.mode = mode;
            this.tooltip = new ArrayList<>();
            SideConfigScreen.this.updateButtonTooltip(this);
        }

        private void cycleMode() {
            sideConfig.cycleSideMode(direction);
            this.mode = sideConfig.getSideMode(direction);
        }

        private void updateTooltip() {
            SideConfigScreen.this.updateButtonTooltip(this);
        }

        public void updateMode(SideConfiguration.SideMode newMode) {
            this.mode = newMode;
            updateTooltip();
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            // 根据模式选择不同的纹理区域
            int texX = mode.ordinal() * 22;
            int texY = 118; // 按钮纹理在图集中的Y位置

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