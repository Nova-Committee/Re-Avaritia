package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.IPacket;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 14:34
 * Version: 1.0
 */
public class SyncMachinePacket extends IPacket<SyncMachinePacket> {
    private BlockPos pos;
    private boolean isFull;

    public SyncMachinePacket(BlockPos pos, boolean full) {
        this.pos = pos;
        this.isFull = full;
    }

    @Override
    public SyncMachinePacket read(FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var full = buf.readBoolean();
        return new SyncMachinePacket(pos, full);
    }

    @Override
    public void write(SyncMachinePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isFull);
    }

    @Override
    public void run(SyncMachinePacket msg, Supplier<NetworkEvent.Context> ctx) {
    	if(ctx.get().getDirection().getReceptionSide().isServer())
    		return;
    	
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NeutronCollectorTile tile = (NeutronCollectorTile) ctx.get().getSender().getCommandSenderWorld().getBlockEntity(pos);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
