package committee.nova.mods.avaritia.client.screen.side;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.common.menu.BaseTileMenu;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.client.screen.element.GuiElementAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cnlimiter
 */
public class SideConfigButton extends ImageButton implements GuiElementAccess {
    private final BaseContainerScreen<?> parentScreen;
    private final List<FormattedCharSequence> tips = new ArrayList<>();

    public SideConfigButton(BaseContainerScreen<?> parentScreen, int pX, int pY) {
        super(pX, pY, 20, 24, new WidgetSprites(Res.SIDE_CONFIG_TEX, Res.SIDE_CONFIG_TEX), pButton -> {
            if (parentScreen.getMinecraft().screen == parentScreen && parentScreen.getMinecraft().player != null) {
                var level = parentScreen.getMinecraft().level;
                if (level != null && parentScreen.getMenu() instanceof BaseTileMenu<?> menu) {
                    var tile = level.getBlockEntity(menu.getBlockPos());
                    if (tile instanceof ITileIO tileIO) {
                        var sideConfig = tileIO.getSideConfiguration();
                        var blockPos = menu.getBlockPos();
                        var configScreen = new SideConfigScreen(parentScreen, sideConfig, blockPos, tileIO);
                        parentScreen.getMinecraft().pushGuiLayer(configScreen);
                    }
                }
            }
        });
        this.parentScreen = parentScreen;
        tips.add(Component.translatable("button.avaritia.side_config_button_1").withStyle(ChatFormatting.LIGHT_PURPLE).getVisualOrderText());
        tips.add(Component.translatable("button.avaritia.side_config_button_2").withStyle(ChatFormatting.GRAY).getVisualOrderText());
        parentScreen.setTooltipForNextRenderPass(tips);
    }

    @Override
    public Rect2i getElementArea() {
        return new Rect2i(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var xTexStart = 156;
        var yTexStart = 0;
        if (this.isHovered) {
            parentScreen.setTooltipForNextRenderPass(tips);
            pPoseStack.blit(Res.SIDE_CONFIG_TEX, getX(), getY(), xTexStart, yTexStart + 24, getWidth(), getHeight(), 256, 256);
        } else {
            pPoseStack.blit(Res.SIDE_CONFIG_TEX, getX(), getY(), xTexStart, yTexStart, getWidth(), getHeight(), 256, 256);
        }
    }
}
