package committee.nova.mods.avaritia.common.menu.provider;

import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public record ChannelSelectMenuProvider(TesseractTile tile) implements MenuProvider {
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.avaritia.tesseract.channel_select");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TesseractChannelMenu(id, inventory, tile, tile.getBlockPos());
    }
}
