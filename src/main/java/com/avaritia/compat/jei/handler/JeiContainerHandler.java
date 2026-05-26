package com.avaritia.compat.jei.handler;

import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.client.screen.element.GuiElementAccess;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JeiContainerHandler implements IGuiContainerHandler<BaseContainerScreen<?>> {
    /** 指定需要 JEI 避让的区域。 */
    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(BaseContainerScreen<?> containerScreen) {
        List<Rect2i> areas = new ArrayList<>();
        for (Renderable renderable : containerScreen.renderables) {
            if (renderable instanceof GuiElementAccess access) {
                areas.add(access.getElementArea());
            }
        }
        return areas;
    }
}
