package committee.nova.mods.avaritia.api.common.tile;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import org.jetbrains.annotations.Nullable;

/**
 * INameableMenuProvider
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/23 上午1:43
 */
public interface INameableMenuProvider extends MenuProvider, Nameable {
    Component getDefaultName();

    /**
     * Gets the custom name for this tile entity
     * @return  Custom name
     */
    @Override
    @Nullable
    Component getCustomName();

    /**
     * Sets the name for this tile entity
     * @param name  New custom name
     */
    void setCustomName(Component name);

    @Override
    default Component getName() {
        Component customTitle = getCustomName();
        return customTitle != null ? customTitle : getDefaultName();
    }

    @Override
    default Component getDisplayName() {
        return getName();
    }
}
