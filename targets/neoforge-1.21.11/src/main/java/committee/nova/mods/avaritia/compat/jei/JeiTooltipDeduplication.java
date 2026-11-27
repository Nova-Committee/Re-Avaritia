package committee.nova.mods.avaritia.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.neoforged.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class JeiTooltipDeduplication {
    private static final String JADE_MOD_ID = "jade";
    private static final String IWAILA_CONFIG_CLASS = "snownee.jade.api.config.IWailaConfig";
    private static final String GENERAL_METHOD = "general";
    private static final String GET_METHOD = "get";
    private static final String SHOW_ITEM_MOD_NAME_TOOLTIP_METHOD = "showItemModNameTooltip";

    private JeiTooltipDeduplication() {
    }

    public static boolean shouldSuppressItemModNameTooltip(ITypedIngredient<?> typedIngredient) {
        if (typedIngredient == null || typedIngredient.getType() != VanillaTypes.ITEM_STACK) {
            return false;
        }
        return isJadeItemModNameTooltipEnabled();
    }

    private static boolean isJadeItemModNameTooltipEnabled() {
        if (!ModList.get().isLoaded(JADE_MOD_ID)) {
            return false;
        }

        try {
            Class<?> configClass = Class.forName(IWAILA_CONFIG_CLASS);
            Method getMethod = configClass.getMethod(GET_METHOD);
            Method generalMethod = configClass.getMethod(GENERAL_METHOD);

            Object config = getMethod.invoke(null);
            Object general = generalMethod.invoke(config);
            Method showTooltipMethod = general.getClass().getMethod(SHOW_ITEM_MOD_NAME_TOOLTIP_METHOD);

            // Jade 会在普通 ItemStack 提示中追加模组名；JEI 再追加一次时就会显示两遍。
            return Boolean.TRUE.equals(showTooltipMethod.invoke(general));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | LinkageError ignored) {
            return false;
        }
    }
}
