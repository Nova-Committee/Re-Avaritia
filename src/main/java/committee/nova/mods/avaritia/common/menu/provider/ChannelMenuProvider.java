package committee.nova.mods.avaritia.common.menu.provider;

import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public record ChannelMenuProvider(TesseractTile tile) implements MenuProvider {
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.avaritia.tesseract");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TesseractMenu(id, inventory, tile);
    }
}
