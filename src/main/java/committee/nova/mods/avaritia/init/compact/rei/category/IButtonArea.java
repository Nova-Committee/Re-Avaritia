package committee.nova.mods.avaritia.init.compact.rei.category;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;

public interface IButtonArea {
    static ButtonArea defaultArea() {
        return bounds -> new Rectangle(bounds.getMaxX() - 14, bounds.getMaxY() - 14, 10, 10);
    }

    /**
     * Declares the button bounds
     *
     * @param bounds the bounds of the recipe display
     * @return the bounds of the button
     */
    Rectangle get(Rectangle bounds);

    /**
     * Declares the button text
     *
     * @return the text of the button
     */
    default String getButtonText() {
        return "+";
    }
}
