package committee.nova.mods.avaritia.init.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Common code never links the optional Trinkets API when the mod is absent. */
public final class TrinketsIntegration {
    private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("trinkets");

    private TrinketsIntegration() {}

    public static boolean available() {
        return AVAILABLE;
    }

    public static void initialize() {
        if (AVAILABLE) TrinketsCompat.initialize();
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        return AVAILABLE ? TrinketsCompat.find(entity, predicate) : ItemStack.EMPTY;
    }

    public static ItemStack findBack(LivingEntity entity, Predicate<ItemStack> predicate) {
        return AVAILABLE ? TrinketsCompat.findBack(entity, predicate) : ItemStack.EMPTY;
    }
}
