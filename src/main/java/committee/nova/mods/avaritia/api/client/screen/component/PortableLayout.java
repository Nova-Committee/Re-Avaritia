package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.gui.navigation.ScreenRectangle;

/** GUI-space geometry; mouse coordinates deliberately retain their fractional part. */
public final class PortableLayout {
    private PortableLayout() {
    }

    public static ScreenRectangle centered(int screenWidth, int screenHeight, int preferredWidth, int preferredHeight, int margin) {
        int width = Math.min(preferredWidth, Math.max(0, screenWidth - 2 * margin));
        int height = Math.min(preferredHeight, Math.max(0, screenHeight - 2 * margin));
        return new ScreenRectangle((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }

    public static ScreenRectangle inset(ScreenRectangle bounds, int left, int top, int right, int bottom) {
        return new ScreenRectangle(bounds.left() + left, bounds.top() + top,
                Math.max(0, bounds.width() - left - right), Math.max(0, bounds.height() - top - bottom));
    }

    public static ScreenRectangle translate(ScreenRectangle bounds, int dx, int dy) {
        return new ScreenRectangle(bounds.left() + dx, bounds.top() + dy, bounds.width(), bounds.height());
    }

    public static boolean contains(ScreenRectangle bounds, double mouseX, double mouseY) {
        return bounds.width() > 0 && bounds.height() > 0
                && mouseX >= bounds.left() && mouseX < bounds.right()
                && mouseY >= bounds.top() && mouseY < bounds.bottom();
    }
}
