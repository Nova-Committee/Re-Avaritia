package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SChannelActionPack(int containerId, int actionId, String objectType, String objectId)
        implements CustomPacketPayload {
    public static final Type<C2SChannelActionPack> TYPE = new Type<>(Const.rl("c2s_tesseract_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChannelActionPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChannelActionPack::containerId,
            ByteBufCodecs.INT, C2SChannelActionPack::actionId,
            ChannelPayloadCodecs.KIND, C2SChannelActionPack::objectType,
            ChannelPayloadCodecs.IDENTIFIER, C2SChannelActionPack::objectId,
            C2SChannelActionPack::new);

    public C2SChannelActionPack(int containerId, int actionId, String[] object) {
        this(containerId, actionId, object != null && object.length > 0 ? object[0] : "",
                object != null && object.length > 1 ? object[1] : "");
    }
    public C2SChannelActionPack {
        objectType = ChannelPayloadCodecs.limit(objectType, 16);
        objectId = ChannelPayloadCodecs.limit(objectId, 256);
    }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    private boolean validObject() {
        ResourceLocation id = ResourceLocation.tryParse(objectId);
        return switch (objectType) {
            case "item" -> id != null && BuiltInRegistries.ITEM.containsKey(id);
            case "fluid" -> id != null && BuiltInRegistries.FLUID.containsKey(id);
            case "energy" -> objectId.equals("avaritia:forge_energy");
            default -> false;
        };
    }

    public static final class Handler implements IPayloadHandler<C2SChannelActionPack> {
        @Override public void handle(@NotNull C2SChannelActionPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractMenu menu = ChannelPayloadGuard.mainMenu(context, packet.containerId);
                if (menu != null && packet.validObject()
                        && packet.actionId >= StorageUtils.Action.LEFT_CLICK_DUMMY_SLOT
                        && packet.actionId <= StorageUtils.Action.DRAG_CLONE) {
                    menu.action(packet.actionId, packet.objectType, packet.objectId);
                    menu.broadcastChanges();
                }
            });
        }
    }
}
