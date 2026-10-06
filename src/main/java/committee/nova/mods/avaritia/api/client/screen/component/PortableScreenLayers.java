package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Modal screens retain their parent instances and menus; vanilla routes input, ticks and pause to the top screen. */
public final class PortableScreenLayers {
    private static final List<Screen> parents = new ArrayList<>();

    private PortableScreenLayers() {
    }

    public static void open(Screen parent, Screen child) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != parent) {
            return;
        }
        parents.add(parent);
        minecraft.screen = child;
        child.added();
        child.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        minecraft.getNarrator().sayNow(child.getNarrationMessage());
    }

    public static boolean close(Screen child, Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != child || parents.isEmpty() || parents.get(parents.size() - 1) != parent) {
            return false;
        }
        removeTop(minecraft);
        minecraft.getNarrator().sayNow(parent.getNarrationMessage());
        return true;
    }

    private static void removeTop(Minecraft minecraft) {
        Screen child = minecraft.screen;
        minecraft.screen = parents.remove(parents.size() - 1);
        UiInspector.onClosing(child);
        child.removed();
    }

    /** Called before vanilla replaces a screen: dispose children first, then let vanilla remove the root. */
    public static void beforeScreenChange(Minecraft minecraft) {
        while (!parents.isEmpty()) {
            removeTop(minecraft);
        }
    }

    public static void resizeParents(Minecraft minecraft) {
        for (Screen parent : parents) {
            parent.resize(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        }
    }

    @Nullable
    public static Screen root(@Nullable Screen screen) {
        return screen == Minecraft.getInstance().screen && !parents.isEmpty() ? parents.get(0) : screen;
    }

    public static float extraDepth() {
        return parents.size() * 2000.0F;
    }

    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();
        try {
            for (Screen parent : parents) {
                parent.renderWithTooltip(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
                graphics.pose().translate(0, 0, 2000);
            }
            screen.renderWithTooltip(graphics, mouseX, mouseY, partialTick);
        } finally {
            graphics.pose().popPose();
        }
    }
}
