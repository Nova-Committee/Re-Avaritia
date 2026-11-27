package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.menu.ExtremeAnvilMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class C2SRenamePack {
    private final String name;

    public C2SRenamePack(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public C2SRenamePack(String name) {
        this.name = name;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public void run(Supplier<PacketContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.containerMenu.stillValid(player) || name.length() > 50) return;
            AbstractContainerMenu abstractcontainermenu = player.containerMenu;
            if (abstractcontainermenu instanceof ExtremeAnvilMenu anvilmenu) {
                anvilmenu.setItemName(name);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
