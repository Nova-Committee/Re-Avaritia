package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.item.misc.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSpaces;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

/** GUI mutations for the Neutron Ring library. */
public class C2SNeutronRingPack {
    public static final int SELECT = 0;
    public static final int RENAME = 1;
    public static final int DELETE = 2;
    public static final int DESELECT = 3;
    public static final int SET_SIZE = 4;
    public static final int PREVIEW = 5;

    private final int action;
    private final String id;
    private final String name;
    private final int hand;
    private final UUID storageId;
    private final NeutronRingContents.Size size;

    public C2SNeutronRingPack(int action, String id, String name, int hand, UUID storageId,
                              NeutronRingContents.Size size) {
        this.action = action;
        this.id = id;
        this.name = name;
        this.hand = hand;
        this.storageId = storageId;
        this.size = size;
    }

    public C2SNeutronRingPack(FriendlyByteBuf buf) {
        this.action = buf.readInt();
        this.id = buf.readUtf();
        this.name = buf.readUtf();
        this.hand = buf.readVarInt();
        this.storageId = buf.readUUID();
        this.size = NeutronRingContents.Size.read(buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(action);
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeVarInt(hand);
        buf.writeUUID(storageId);
        size.write(buf);
    }

    public int action() {
        return action;
    }

    public String id() {
        return id;
    }

    public UUID storageId() {
        return storageId;
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                apply(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public void apply(ServerPlayer player) {
        ItemStack stack = NeutronRingItem.resolve(player, hand);
        if (stack.isEmpty()) {
            return;
        }
        NeutronRingContents data = NeutronRingItem.bind(player, stack);
        if (!data.storageId().equals(storageId)) {
            return;
        }
        NeutronRingSavedData store = NeutronRingSavedData.get(player.server);
        UUID library = data.storageId();
        boolean refreshLibrary = true;
        switch (action) {
            case SELECT -> store.get(library, id).ifPresent(space ->
                    data.select(id, NeutronRingSpaces.sizeOf(space.template())).save(stack));
            case RENAME -> {
                if (!name.isBlank()) {
                    store.rename(library, id, name.trim());
                }
            }
            case DELETE -> {
                store.remove(library, id);
                if (data.selectedId().filter(id::equals).isPresent()) {
                    data.deselect().save(stack);
                }
            }
            case DESELECT -> data.deselect().save(stack);
            case SET_SIZE -> {
            }
            case PREVIEW -> {
                refreshLibrary = false;
                store.get(library, id).ifPresentOrElse(
                        space -> S2CNeutronRingPreviewPack.send(player, id, space.template()),
                        () -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                S2CNeutronRingPreviewPack.missing(id)));
            }
            default -> {
            }
        }
        if (refreshLibrary) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    S2CNeutronRingOpenPack.from(player.server, NeutronRingItem.bind(player, stack), handOf(hand)));
        }
    }

    private static InteractionHand handOf(int hand) {
        return hand == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
}
