package committee.nova.mods.avaritia.api.init.registry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Vanilla menu type with the extra opening payload sent by NetworkHandler. */
public final class DataMenuType<T extends AbstractContainerMenu> extends MenuType<T> {
    @FunctionalInterface
    public interface Factory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, FriendlyByteBuf data);
    }

    private record OpeningData(int id, FriendlyByteBuf data) {}
    private static final ThreadLocal<OpeningData> OPENING_DATA = new ThreadLocal<>();
    private final Factory<T> factory;

    private DataMenuType(Factory<T> factory) {
        super((id, inventory) -> { throw new IllegalStateException("Missing menu payload"); }, FeatureFlags.VANILLA_SET);
        this.factory = factory;
    }

    public static <T extends AbstractContainerMenu> DataMenuType<T> create(Factory<T> factory) {
        return new DataMenuType<>(factory);
    }

    public static void setOpeningData(int id, FriendlyByteBuf data) {
        OpeningData previous = OPENING_DATA.get();
        if (previous != null) previous.data().release();
        OPENING_DATA.set(new OpeningData(id, data));
    }

    @Override
    public T create(int id, Inventory inventory) {
        OpeningData opening = OPENING_DATA.get();
        OPENING_DATA.remove();
        if (opening == null) throw new IllegalStateException("Missing extra menu data for " + id);
        try {
            if (opening.id() != id) throw new IllegalStateException("Mismatched menu data for " + id);
            return factory.create(id, inventory, opening.data());
        } finally {
            opening.data().release();
        }
    }

    public T create(int id, Inventory inventory, FriendlyByteBuf data) {
        return factory.create(id, inventory, data);
    }
}
