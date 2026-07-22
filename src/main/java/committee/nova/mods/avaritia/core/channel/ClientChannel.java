package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client-side immutable mirror of the channel currently shown by a menu. */
public final class ClientChannel {
    private Channel.Data data = new Channel.Data(Channel.DEFAULT_NAME, List.of(), List.of(), 0);
    private final Map<ItemResource, Long> incomingItems = new LinkedHashMap<>();
    private final Map<FluidResource, Long> incomingFluids = new LinkedHashMap<>();
    private String incomingName = Channel.DEFAULT_NAME;
    private long incomingEnergy;
    private Runnable listener = () -> { };

    public Channel.Data data() {
        return data;
    }

    public List<Channel.ItemEntry> items() {
        return data.items();
    }

    public List<Channel.FluidEntry> fluids() {
        return data.fluids();
    }

    public void replace(Channel.Data next) {
        data = next == null ? new Channel.Data(Channel.DEFAULT_NAME, List.of(), List.of(), 0) : next;
        listener.run();
    }

    public void apply(ChannelState state, Channel.Data update) {
        if (state == ChannelState.FULL_START) {
            incomingItems.clear();
            incomingFluids.clear();
            incomingName = update.name();
            incomingEnergy = update.energy();
            mergePage(update, incomingItems, incomingFluids);
            return;
        }
        if (state == ChannelState.FULL_PAGE || state == ChannelState.FULL_END) {
            incomingName = update.name();
            incomingEnergy = update.energy();
            mergePage(update, incomingItems, incomingFluids);
            if (state == ChannelState.FULL_END) {
                replace(toData(incomingName, incomingItems, incomingFluids, incomingEnergy));
            }
            return;
        }

        Map<ItemResource, Long> items = itemMap(data);
        Map<FluidResource, Long> fluids = fluidMap(data);
        mergePage(update, items, fluids);
        replace(toData(update.name(), items, fluids, update.energy()));
    }

    public void listen(Runnable callback) {
        listener = callback == null ? () -> { } : callback;
    }

    public void clearListener() {
        listener = () -> { };
    }

    private static void mergePage(Channel.Data page, Map<ItemResource, Long> items,
                                  Map<FluidResource, Long> fluids) {
        page.items().forEach(entry -> put(items, entry.resource(), entry.amount()));
        page.fluids().forEach(entry -> put(fluids, entry.resource(), entry.amount()));
    }

    private static <T> void put(Map<T, Long> values, T resource, long amount) {
        if (amount <= 0) values.remove(resource); else values.put(resource, amount);
    }

    private static Map<ItemResource, Long> itemMap(Channel.Data source) {
        Map<ItemResource, Long> result = new LinkedHashMap<>();
        source.items().forEach(entry -> result.put(entry.resource(), entry.amount()));
        return result;
    }

    private static Map<FluidResource, Long> fluidMap(Channel.Data source) {
        Map<FluidResource, Long> result = new LinkedHashMap<>();
        source.fluids().forEach(entry -> result.put(entry.resource(), entry.amount()));
        return result;
    }

    private static Channel.Data toData(String name, Map<ItemResource, Long> items,
                                       Map<FluidResource, Long> fluids, long energy) {
        return new Channel.Data(name,
                items.entrySet().stream().map(entry -> new Channel.ItemEntry(entry.getKey(), entry.getValue())).toList(),
                fluids.entrySet().stream().map(entry -> new Channel.FluidEntry(entry.getKey(), entry.getValue())).toList(),
                energy);
    }
}
