package committee.nova.mods.avaritia.init.compact.rei.menu;

import committee.nova.mods.avaritia.init.compact.rei.display.ExtremeTableDisplay;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ExtremeTableMenuInfo(ExtremeTableDisplay display) implements SimpleGridMenuInfo {
    @Override
    public int getCraftingResultSlotIndex(AbstractContainerMenu menu) {
        return 0;
    }

    @Override
    public int getCraftingWidth(AbstractContainerMenu menu) {
        return 9;
    }

    @Override
    public int getCraftingHeight(AbstractContainerMenu menu) {
        return 9;
    }

    @Override
    public Display getDisplay() {
        return this.display ;
    }

}
