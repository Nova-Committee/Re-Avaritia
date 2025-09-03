package committee.nova.mods.avaritia.api.init.handler;

import committee.nova.mods.avaritia.api.common.net.IPacket;
import io.github.fabricators_of_create.porting_lib.util.NetworkDirection;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:01
 * Version: 1.0
 */
public class NetBaseHandler {
    private final SimpleChannel channel;
    public int id = 0;

    public NetBaseHandler(ResourceLocation id) {
        this.channel = NetworkRegistry.newSimpleChannel(id, () -> {
            return "1.0";
        }, (s) -> {
            return true;
        }, (s) -> {
            return true;
        });
    }

    public SimpleChannel getChannel() {
        return this.channel;
    }

    public <T extends IPacket<T>> void register(Class<T> clazz, IPacket<T> message) {
        SimpleChannel.MessageBuilder<T> messageBuilder = this.channel.messageBuilder(clazz, this.id++);
        Objects.requireNonNull(message);
        messageBuilder = messageBuilder.encoder(message::write);
        Objects.requireNonNull(message);
        messageBuilder = messageBuilder.decoder(message::read);
        Objects.requireNonNull(message);
        messageBuilder.consumerNetworkThread(message::run).add();
    }

    public <M> void sendToServer(M message) {
        this.channel.sendToServer(message);
    }

    public <M> void sendTo(M message, Connection manager, NetworkDirection direction) {
        this.channel.sendTo(message, manager, direction);
    }

    public <M> void send(PacketDistributor.PacketTarget target, M message) {
        this.channel.send(target, message);
    }

    public <M> void reply(M message, NetworkEvent.Context context) {
        this.channel.reply(message, context);
    }
}
