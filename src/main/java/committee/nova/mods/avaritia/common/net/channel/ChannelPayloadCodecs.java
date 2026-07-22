package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.core.channel.Channel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ChannelPayloadCodecs {
    static final int MAX_CHANNEL_LIST = 1_024;
    static final int MAX_SYNC_VARIANTS = 512;
    static final StreamCodec<RegistryFriendlyByteBuf, String> NAME = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(Channel.normalizeName(value), 64),
            buffer -> buffer.readUtf(64));
    static final StreamCodec<RegistryFriendlyByteBuf, String> FILTER = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(limit(value, 64), 64),
            buffer -> buffer.readUtf(64));
    static final StreamCodec<RegistryFriendlyByteBuf, Channel.Data> CHANNEL_DATA = StreamCodec.of(
            ChannelPayloadCodecs::encodeData, ChannelPayloadCodecs::decodeData);

    private ChannelPayloadCodecs() {
    }

    static String limit(String value, int maximum) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(maximum, safe.length()));
    }

    static void encodeMap(RegistryFriendlyByteBuf buffer, Map<Integer, String> values) {
        List<Map.Entry<Integer, String>> entries = values.entrySet().stream()
                .filter(entry -> entry.getKey() >= 0 && entry.getKey() <= 9_999)
                .limit(MAX_CHANNEL_LIST).toList();
        buffer.writeVarInt(entries.size());
        for (Map.Entry<Integer, String> entry : entries) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeUtf(Channel.normalizeName(entry.getValue()), 64);
        }
    }

    static Map<Integer, String> decodeMap(RegistryFriendlyByteBuf buffer) {
        int size = checkedSize(buffer.readVarInt(), MAX_CHANNEL_LIST, "channel list");
        Map<Integer, String> values = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int id = buffer.readVarInt();
            String name = buffer.readUtf(64);
            if (id >= 0 && id <= 9_999) values.put(id, name);
        }
        return values;
    }

    private static void encodeData(RegistryFriendlyByteBuf buffer, Channel.Data data) {
        if (data.items().size() + data.fluids().size() > MAX_SYNC_VARIANTS) {
            throw new EncoderException("Tesseract channel sync page exceeds " + MAX_SYNC_VARIANTS + " variants");
        }
        buffer.writeUtf(data.name(), 64);
        buffer.writeVarInt(data.items().size());
        for (Channel.ItemEntry entry : data.items()) {
            net.neoforged.neoforge.transfer.item.ItemResource.STREAM_CODEC.encode(buffer, entry.resource());
            buffer.writeVarLong(entry.amount());
        }
        buffer.writeVarInt(data.fluids().size());
        for (Channel.FluidEntry entry : data.fluids()) {
            net.neoforged.neoforge.transfer.fluid.FluidResource.STREAM_CODEC.encode(buffer, entry.resource());
            buffer.writeVarLong(entry.amount());
        }
        buffer.writeVarLong(data.energy());
    }

    private static Channel.Data decodeData(RegistryFriendlyByteBuf buffer) {
        String name = buffer.readUtf(64);
        int itemSize = checkedSize(buffer.readVarInt(), MAX_SYNC_VARIANTS, "item variants");
        List<Channel.ItemEntry> items = new ArrayList<>(itemSize);
        for (int i = 0; i < itemSize; i++) {
            var resource = net.neoforged.neoforge.transfer.item.ItemResource.STREAM_CODEC.decode(buffer);
            long amount = buffer.readVarLong();
            if (!resource.isEmpty() && amount >= 0) items.add(new Channel.ItemEntry(resource, amount));
        }
        int fluidSize = checkedSize(buffer.readVarInt(), MAX_SYNC_VARIANTS - itemSize, "fluid variants");
        List<Channel.FluidEntry> fluids = new ArrayList<>(fluidSize);
        for (int i = 0; i < fluidSize; i++) {
            var resource = net.neoforged.neoforge.transfer.fluid.FluidResource.STREAM_CODEC.decode(buffer);
            long amount = buffer.readVarLong();
            if (!resource.isEmpty() && amount >= 0) fluids.add(new Channel.FluidEntry(resource, amount));
        }
        return new Channel.Data(name, items, fluids, Math.max(0, buffer.readVarLong()));
    }

    private static int checkedSize(int declared, int maximum, String field) {
        if (declared < 0 || declared > maximum) {
            throw new DecoderException("Invalid Tesseract " + field + " size: " + declared);
        }
        return declared;
    }
}
