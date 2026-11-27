package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.Minecraft;

/** Temporarily lowers the vanilla GUI scale to fit a fixed-size container screen. */
public final class ScreenGuiScale {
    private int originalScale = -1;

    public boolean resizeToFit(Minecraft minecraft, int width, int height, int panelWidth, int panelHeight, int margin) {
        if (width >= panelWidth + margin * 2 && height >= panelHeight + margin * 2) {
            return false;
        }
        int currentScale = minecraft.getWindow().getGuiScale();
        int targetScale = currentScale;
        int framebufferWidth = minecraft.getWindow().getWidth();
        int framebufferHeight = minecraft.getWindow().getHeight();
        while (targetScale > 1
                && (framebufferWidth / targetScale < panelWidth + margin * 2
                || framebufferHeight / targetScale < panelHeight + margin * 2)) {
            targetScale--;
        }
        if (targetScale >= currentScale || minecraft.options.guiScale().get() == targetScale) {
            return false;
        }
        if (originalScale < 0) {
            originalScale = minecraft.options.guiScale().get();
        }
        minecraft.options.guiScale().set(targetScale);
        minecraft.resizeGui();
        return true;
    }

    public void restore(Minecraft minecraft) {
        if (originalScale < 0) {
            return;
        }
        int scale = originalScale;
        originalScale = -1;
        // Wait until screen replacement, otherwise resizing reopens the scale adjustment.
        minecraft.schedule(() -> {
            if (minecraft.options.guiScale().get() != scale) {
                minecraft.options.guiScale().set(scale);
                minecraft.resizeGui();
            }
        });
    }
}
