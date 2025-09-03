package committee.nova.mods.avaritia.common.menu.provider;

import committee.nova.mods.avaritia.common.menu.ChannelMenu;
import committee.nova.mods.avaritia.common.tile.BlackHoleChestTile;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 20:10
 * @Description:
 */
public class ChannelMenuProvider implements ExtendedMenuProvider {

    private final BlackHoleChestTile blockEntity;
    private Consumer<FriendlyByteBuf> writer;
    private final int slotIndex;

    public ChannelMenuProvider(BlackHoleChestTile blockEntity, Consumer<FriendlyByteBuf> writer) {
        this.blockEntity = blockEntity;
        this.writer = writer;
        this.slotIndex = -2;
    }

    public ChannelMenuProvider(int slotIndex) {
        this.blockEntity = null;
        this.slotIndex = slotIndex;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ChannelMenu(pContainerId, pPlayer, blockEntity, slotIndex);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf friendlyByteBuf) {
        this.writer.accept(friendlyByteBuf);
    }
}