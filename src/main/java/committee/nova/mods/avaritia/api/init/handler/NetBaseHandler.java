package committee.nova.mods.avaritia.api.init.handler;

import committee.nova.mods.avaritia.api.common.net.IPacket;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

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
