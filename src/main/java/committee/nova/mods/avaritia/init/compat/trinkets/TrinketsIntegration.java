package committee.nova.mods.avaritia.init.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

/** Common code must not link the optional Trinkets API when the mod is absent.
 *  TrinketsCompat is compiled but only touched through reflection so the mod loads fine without Trinkets. */
public final class TrinketsIntegration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("trinkets");
    private static final String TRINKETS_COMPAT = "committee.nova.mods.avaritia.init.compat.trinkets.TrinketsCompat";
    // find() runs per tick, so report a reflection failure only once instead of spamming the log.
    private static boolean failureLogged;

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
            if (!failureLogged) {
                failureLogged = true;
                LOGGER.warn("Trinkets compat bridge failed on {}#{}, trinket integration disabled", TRINKETS_COMPAT, method, t);
            }
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
