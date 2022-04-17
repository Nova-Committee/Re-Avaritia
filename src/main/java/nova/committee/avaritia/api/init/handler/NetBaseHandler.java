package nova.committee.avaritia.api.init.handler;

import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import nova.committee.avaritia.api.common.net.IPacket;
import nova.committee.avaritia.api.common.net.LoginPacket;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:01
 * Version: 1.0
 */
public class NetBaseHandler {
    private final SimpleChannel channel;
    private int id = 0;

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
        messageBuilder.consumer(message::run).add();
    }

    public <T extends LoginPacket<T>> void register(Class<T> clazz, LoginPacket<T> message) {
        SimpleChannel.MessageBuilder<T> messageBuilder = this.channel.messageBuilder(clazz, this.id++);
        Objects.requireNonNull(message);
        messageBuilder = messageBuilder.encoder(message::write);
        Objects.requireNonNull(message);
        messageBuilder.decoder(message::read).consumer((loginMessage, context) -> {
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
            if (context.get().getDirection().getReceptionSide().isServer()) {
                handler = HandshakeHandler.indexFirst((handshake, msg, ctx) -> {
                    message.run(msg, ctx);
                });
            } else {
                Objects.requireNonNull(message);
                handler = message::run;
            }

            handler.accept(loginMessage, context);
        }).loginIndex(LoginPacket::getLoginIndex, LoginPacket::setLoginIndex).markAsLoginPacket().add();
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
