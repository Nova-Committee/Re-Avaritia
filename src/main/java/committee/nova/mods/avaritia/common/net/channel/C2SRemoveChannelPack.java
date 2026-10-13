package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/3/2 01:07
 * @Description:
 */
public class C2SRemoveChannelPack {
    private final int containerId;
    private final byte type;
    private final int id;

    public C2SRemoveChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.type = buf.readByte();
        this.id = buf.readInt();
    }

    public C2SRemoveChannelPack(int containerId, byte type, int id) {
        this.containerId = containerId;
        this.type = type;
        this.id = id;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeByte(type);
        buf.writeInt(id);
    }

    public void run(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof TesseractChannelMenu) || player.containerMenu.containerId != containerId || type < 0 || type > 2 || id < 0) return;
            if (!player.containerMenu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((TesseractChannelMenu) player.containerMenu).removeChannel();
            }
        });
        context.get().setPacketHandled(true);
    }
}
