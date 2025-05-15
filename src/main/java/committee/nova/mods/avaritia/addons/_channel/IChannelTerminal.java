package committee.nova.mods.avaritia.addons._channel;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 19:52
 * @Description:
 */
public interface IChannelTerminal {
    UUID getTerminalOwner();
    @Nullable
    ChannelInfo getChannelInfo();
    void setChannel(UUID channelOwner, int channelID);
    void removeChannel(ServerPlayer actor);
    void renameChannel(ServerPlayer actor, String name);
    void addChannelSelector(ServerPlayer player);
    void removeChannelSelector(ServerPlayer player);
    boolean stillValid();
    void tryReOpenMenu(ServerPlayer player);
}
