package committee.nova.mods.avaritia.client.screen.side;

import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SideConfigScreen extends Screen {

    private SideConfiguration sideConfig;
    private final Screen parentScreen;
    private final BlockPos blockPos;
    private final ITileIO tile;

    private int guiLeft;
    private int guiTop;
    private int imageWidth = 156;
    private int imageHeight = 117;

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

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.imageWidth) / 2;
        this.guiTop = (this.height - this.imageHeight) / 2;
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        createSideButtons(x, y);

        this.addRenderableWidget(
                new ImageButton(x + 135, y + 93, 17, 18,
                        new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX),
                        (button) -> setAllSides(SideConfiguration.SideMode.OFF)) {
                    @Override
                    public void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int vOffset = 164, textureDifference = 18;
                        int i = vOffset;
                        if (!this.isActive()) {
                            i = vOffset + textureDifference * 2;
                        } else if (this.isHoveredOrFocused()) {
                            i = vOffset + textureDifference;
                        }

                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), 17, i, width, height, 256, 256);
                    }
                }

        );
        this.addRenderableWidget(
                new ImageButton(x + 4, y + 4, 17, 18,
                        new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX),
                        (button) -> onClose()) {
                    @Override
                    public void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int vOffset = 164, textureDifference = 18;
                        int i = vOffset;
                        if (!this.isActive()) {
                            i = vOffset + textureDifference * 2;
                        } else if (this.isHoveredOrFocused()) {
                            i = vOffset + textureDifference;
                        }

                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), 0, i, width, height, 256, 256);
                    }
                }
        );
    }

    private void createSideButtons(int x, int y) {
        int centerX = x + 67;
        int centerY = y + 50;

        upButton = new SideButton(centerX, centerY - 24, Direction.UP, sideConfig.getSideMode(Direction.UP), this);
        this.addRenderableWidget(upButton);

        downButton = new SideButton(centerX, centerY + 24, Direction.DOWN, sideConfig.getSideMode(Direction.DOWN), this);
        this.addRenderableWidget(downButton);

        northButton = new SideButton(centerX, centerY, Direction.NORTH, sideConfig.getSideMode(Direction.NORTH), this);
        this.addRenderableWidget(northButton);

        southButton = new SideButton(centerX - 23, centerY + 24, Direction.SOUTH, sideConfig.getSideMode(Direction.SOUTH), this);
        this.addRenderableWidget(southButton);

        westButton = new SideButton(centerX - 23, centerY, Direction.WEST, sideConfig.getSideMode(Direction.WEST), this);
        this.addRenderableWidget(westButton);

        eastButton = new SideButton(centerX + 23, centerY, Direction.EAST, sideConfig.getSideMode(Direction.EAST), this);
        this.addRenderableWidget(eastButton);
    }

    public void cycleModeForDirection(Direction direction) {
        tile.cycleSideModeForNeutronCollector(direction);
        sideConfig.setSideMode(direction, tile.getSideConfiguration().getSideMode(direction));
    }

    private void setAllSides(SideConfiguration.SideMode mode) {
        for (Direction direction : Direction.values()) {
            sideConfig.setSideMode(direction, mode);
            tile.cycleSideModeForNeutronCollector(direction);
        }
        updateAllButtons();
        sendConfigUpdate();
    }

    public void updateAllButtons() {
        northButton.updateMode(sideConfig.getSideMode(Direction.NORTH));
        southButton.updateMode(sideConfig.getSideMode(Direction.SOUTH));
        eastButton.updateMode(sideConfig.getSideMode(Direction.EAST));
        westButton.updateMode(sideConfig.getSideMode(Direction.WEST));
        upButton.updateMode(sideConfig.getSideMode(Direction.UP));
        downButton.updateMode(sideConfig.getSideMode(Direction.DOWN));
    }

    public void sendConfigUpdate() {
        NetworkHandler.sendSideConfigUpdate(blockPos, sideConfig);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractTransparentBackground(guiGraphics);
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Res.SIDE_CONFIG_TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, this.guiTop + 7, 0xFF404040);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

}
