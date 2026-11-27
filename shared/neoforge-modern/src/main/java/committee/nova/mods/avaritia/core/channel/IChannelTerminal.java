package committee.nova.mods.avaritia.core.channel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Server-authoritative endpoint that owns a selected channel reference. */
public interface IChannelTerminal {
    UUID getTerminalOwner();

    @Nullable
    ChannelInfo getChannelInfo();

    void setChannel(@Nullable ChannelInfo channel);

    boolean canPlayerModify(Player player);

    boolean stillValid(Player player);

    void openMainMenu(ServerPlayer player);
}
