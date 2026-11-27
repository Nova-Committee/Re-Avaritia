package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.Minecraft;

/** Temporarily lowers the vanilla GUI scale to fit a fixed-size container screen. */
public final class ScreenGuiScale {
    private int originalScale = -1;

    public boolean resizeToFit(Minecraft minecraft, int width, int height, int panelWidth, int panelHeight, int margin) {
        if (minecraft == null || (width >= panelWidth + margin * 2 && height >= panelHeight + margin * 2)) {
            return false;
        }
        double currentScale = minecraft.getWindow().getGuiScale();
        int targetScale = targetScale(width, height, currentScale, panelWidth, panelHeight, margin);
        if (targetScale >= currentScale || minecraft.options.guiScale().get() == targetScale) {
            return false;
        }
        if (originalScale < 0) {
            originalScale = minecraft.options.guiScale().get();
        }
        minecraft.options.guiScale().set(targetScale);
        minecraft.resizeDisplay();
        return true;
    }

    static int targetScale(int width, int height, double currentScale, int panelWidth, int panelHeight, int margin) {
        int targetScale = Math.max(1, (int) Math.floor(currentScale));
        double framebufferWidth = width * currentScale;
        double framebufferHeight = height * currentScale;
        while (targetScale > 1
                && (framebufferWidth / targetScale < panelWidth + margin * 2
                || framebufferHeight / targetScale < panelHeight + margin * 2)) {
            targetScale--;
        }
        return targetScale;
    }

    public void restore(Minecraft minecraft) {
        if (originalScale < 0 || minecraft == null) {
            return;
        }
        int scale = originalScale;
        originalScale = -1;
        // Restore after screen replacement, otherwise its resize callback immediately scales it down again.
        minecraft.tell(() -> {
            if (minecraft.options.guiScale().get() != scale) {
                minecraft.options.guiScale().set(scale);
                minecraft.resizeDisplay();
            }
        });
    }
}
