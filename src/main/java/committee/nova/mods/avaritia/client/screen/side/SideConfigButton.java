package committee.nova.mods.avaritia.client.screen.side;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class SideConfigButton extends ImageButton {
    private final AbstractContainerScreen<?> parentScreen;
    private final List<FormattedCharSequence> tips = new ArrayList<>();

    public SideConfigButton(AbstractContainerScreen<?> parentScreen, int pX, int pY) {
        super(pX, pY, 20, 24, new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX), pButton -> {
            if (parentScreen.getMinecraft().player != null) {
                var level = parentScreen.getMinecraft().level;
                if (level != null && parentScreen.getMenu() instanceof BaseTileMenu<?> menu) {
                    var tile = level.getBlockEntity(menu.getBlockPos());
                    if (tile instanceof ITileIO tileIO) {
                        var sideConfig = tileIO.getSideConfiguration();
                        var blockPos = menu.getBlockPos();
                        var configScreen = new SideConfigScreen(parentScreen, sideConfig, blockPos, tileIO);
                        parentScreen.getMinecraft().setScreen(configScreen);
                    }
                }
            }
        });
        this.parentScreen = parentScreen;
        tips.add(Component.translatable("button.avaritia.side_config_button_1").withStyle(ChatFormatting.LIGHT_PURPLE).getVisualOrderText());
        tips.add(Component.translatable("button.avaritia.side_config_button_2").withStyle(ChatFormatting.GRAY).getVisualOrderText());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void extractContents(GuiGraphicsExtractor pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var xTexStart = 156;
        var yTexStart = 0;
        if (this.isHovered) {
            pPoseStack.setTooltipForNextFrame(parentScreen.getMinecraft().font, tips, pMouseX, pMouseY);
            pPoseStack.blit(RenderPipelines.GUI_TEXTURED, Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), xTexStart, yTexStart + 24, this.width, this.height, 256, 256);
        } else {
            pPoseStack.blit(RenderPipelines.GUI_TEXTURED, Res.SIDE_CONFIG_TEX, this.getX(), this.getY(), xTexStart, yTexStart, this.width, this.height, 256, 256);
        }
    }
}
