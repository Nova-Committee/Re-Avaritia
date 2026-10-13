package committee.nova.mods.avaritia.init.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Common code never links the optional Trinkets API when the mod is absent.
 *  TrinketsCompat is excluded from compilation (optional integration); all access goes through reflection. */
public final class TrinketsIntegration {
    private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("trinkets");
    private static final String TRINKETS_COMPAT = "committee.nova.mods.avaritia.init.compat.trinkets.TrinketsCompat";

    private TrinketsIntegration() {}

    public static boolean available() {
        return AVAILABLE;
    }

    private static Object invokeCompat(String method, Class<?>[] types, Object... args) {
        if (!AVAILABLE) return null;
        try {
            Class<?> clazz = Class.forName(TRINKETS_COMPAT);
            return clazz.getMethod(method, types).invoke(null, args);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void initialize() {
        invokeCompat("initialize", new Class<?>[0]);
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        Object result = invokeCompat("find", new Class<?>[]{LivingEntity.class, Predicate.class}, entity, predicate);
        return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    public static ItemStack findBack(LivingEntity entity, Predicate<ItemStack> predicate) {
        Object result = invokeCompat("findBack", new Class<?>[]{LivingEntity.class, Predicate.class}, entity, predicate);
        return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }
}
