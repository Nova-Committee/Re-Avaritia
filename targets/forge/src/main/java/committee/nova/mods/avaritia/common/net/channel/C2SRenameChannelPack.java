package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/3/2 01:07
 * @Description:
 */
public class C2SRenameChannelPack {
    private final int containerId;
    private final String name;

    public C2SRenameChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.name = buf.readUtf();
    }

    public C2SRenameChannelPack(int containerId, String name) {
        this.containerId = containerId;
        this.name = name;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(name, 64);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (name.isEmpty()) return;
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((TesseractChannelMenu) player.containerMenu).renameChannel(name);
            }
        });
        context.get().setPacketHandled(true);
    }
}
