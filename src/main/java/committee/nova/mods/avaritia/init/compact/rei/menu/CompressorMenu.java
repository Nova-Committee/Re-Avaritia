package committee.nova.mods.avaritia.init.compact.rei.menu;

import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.init.compact.rei.display.CompressorDisplay;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;

import java.util.List;

public record CompressorMenu(
        CompressorDisplay display) implements SimplePlayerInventoryMenuInfo<NeutronCollectorMenu, CompressorDisplay> {
    @Override
    public Iterable<SlotAccessor> getInputSlots(MenuInfoContext<NeutronCollectorMenu, ?, CompressorDisplay> context) {
        return List.of

       (SlotAccessor.fromSlot(context.getMenu().getSlot(0)),
        SlotAccessor.fromSlot(context.getMenu().getSlot(1)));


    }

    @Override
    public CompressorDisplay getDisplay() {
       return this.display;
    }
}
