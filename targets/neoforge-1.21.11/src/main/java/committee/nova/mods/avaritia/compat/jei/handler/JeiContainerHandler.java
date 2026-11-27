package committee.nova.mods.avaritia.compat.jei.handler;

import committee.nova.mods.avaritia.client.screen.element.GuiElementAccess;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JeiContainerHandler<T extends AbstractContainerScreen<?>> implements IGuiContainerHandler<T> {
    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(T containerScreen) {
        List<Rect2i> areas = new ArrayList<>();
        for (Renderable renderable : containerScreen.renderables) {
            if (renderable instanceof GuiElementAccess access) {
                areas.add(access.getElementArea());
            }
        }
        return areas;
    }
}
