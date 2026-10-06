package committee.nova.mods.avaritia.init.compat.emi.handler;

import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.client.screen.element.GuiElementAccess;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.function.Consumer;

public class EMIExclusionZones implements EmiExclusionArea<Screen> {

    @Override
    public void addExclusionArea(Screen screen, Consumer<Bounds> consumer) {
        if(screen instanceof BaseContainerScreen<?>) {
            // 仅为可渲染元素创造避让区域
            for(Renderable renderable: screen.renderables) {
                if(renderable instanceof GuiElementAccess access) {
                    Rect2i area = access.getElementArea();
                    consumer.accept(new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
                }
            }
        }
    }
}
