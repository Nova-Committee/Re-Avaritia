package committee.nova.mods.avaritia.common.container;

import committee.nova.mods.avaritia.core.channel.Channel;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Read-only 11x9 projection of item, fluid and energy channel resources. */
public final class DummyChannelContainer implements Container {
    public static final int SIZE = 99;

    private final List<Entry> visible = new ArrayList<>();

    public void refresh(Channel.Data data, String filter, byte sortType, byte viewType) {
        String query = filter == null ? "" : filter.toLowerCase(Locale.ROOT);
        List<Entry> entries = new ArrayList<>();
        if (viewType == 0 || viewType == 1) {
            data.items().stream().map(Entry::item).filter(entry -> matches(entry, query)).forEach(entries::add);
        }
        if (viewType == 0 || viewType == 2) {
            data.fluids().stream().map(Entry::fluid).filter(entry -> matches(entry, query)).forEach(entries::add);
            if (data.energy() > 0) {
                Entry energy = Entry.energy(data.energy());
                if (matches(energy, query)) entries.add(energy);
            }
        }
        entries.sort(comparator(sortType));
        visible.clear();
        entries.stream().limit(SIZE).forEach(visible::add);
    }

    public Entry entry(int slot) {
        return slot >= 0 && slot < visible.size() ? visible.get(slot) : Entry.empty();
    }

    public long amount(int slot) {
        return entry(slot).amount();
    }

    private static boolean matches(Entry entry, String query) {
        return query.isEmpty() || entry.identifier().toLowerCase(Locale.ROOT).contains(query)
                || entry.displayName().toLowerCase(Locale.ROOT).contains(query);
    }

    private static Comparator<Entry> comparator(byte sortType) {
        Comparator<Entry> identifier = Comparator.comparing(Entry::identifier);
        Comparator<Entry> path = Comparator.comparing(Entry::path).thenComparing(identifier);
        Comparator<Entry> namespace = Comparator.comparing(Entry::namespace).thenComparing(identifier);
        Comparator<Entry> count = Comparator.comparingLong(Entry::amount).thenComparing(identifier);
        return switch (sortType) {
            case 1 -> path.reversed();
            case 2 -> identifier;
            case 3 -> identifier.reversed();
            case 4 -> namespace;
            case 5 -> namespace.reversed();
            case 6 -> count;
            case 7 -> count.reversed();
            default -> path;
        };
    }

    @Override public int getContainerSize() { return SIZE; }
    @Override public boolean isEmpty() { return visible.isEmpty(); }

    @Override
    public ItemStack getItem(int slot) {
        Entry entry = entry(slot);
        return switch (entry.kind()) {
            case ITEM -> entry.item().toStack(1);
            case FLUID -> new ItemStack(entry.fluid().getFluid().getBucket());
            case ENERGY -> new ItemStack(ModItems.forge_energy.get());
            case EMPTY -> ItemStack.EMPTY;
        };
    }

    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) { }
    @Override public void setChanged() { }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { visible.clear(); }

    public enum Kind { EMPTY, ITEM, FLUID, ENERGY }

    public record Entry(Kind kind, ItemResource item, FluidResource fluid, long amount) {
        private static Entry item(Channel.ItemEntry entry) {
            return new Entry(Kind.ITEM, entry.resource(), FluidResource.EMPTY, entry.amount());
        }

        private static Entry fluid(Channel.FluidEntry entry) {
            return new Entry(Kind.FLUID, ItemResource.EMPTY, entry.resource(), entry.amount());
        }

        private static Entry energy(long amount) {
            return new Entry(Kind.ENERGY, ItemResource.EMPTY, FluidResource.EMPTY, amount);
        }

        private static Entry empty() {
            return new Entry(Kind.EMPTY, ItemResource.EMPTY, FluidResource.EMPTY, 0);
        }

        public String identifier() {
            return switch (kind) {
                case ITEM -> item.typeHolder().getRegisteredName();
                case FLUID -> fluid.getFluid().builtInRegistryHolder().getRegisteredName();
                case ENERGY -> "avaritia:forge_energy";
                case EMPTY -> "";
            };
        }

        public String displayName() {
            return switch (kind) {
                case ITEM -> item.toStack(1).getHoverName().getString();
                case FLUID -> fluid.getHoverName().getString();
                case ENERGY -> new ItemStack(ModItems.forge_energy.get()).getHoverName().getString();
                case EMPTY -> "";
            };
        }

        public String namespace() {
            int separator = identifier().indexOf(':');
            return separator < 0 ? "minecraft" : identifier().substring(0, separator);
        }

        public String path() {
            int separator = identifier().indexOf(':');
            return separator < 0 ? identifier() : identifier().substring(separator + 1);
        }
    }
}
