package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingularityCraftingTest {
    private static final ResourceLocation INVALID_VALUE_ID = ResourceLocation.parse("test:ct_invalid_value");
    private static final ResourceLocation VALID_ID = ResourceLocation.parse("test:ct_valid");

    @Test
    void invalidIdsAndValuesOnlySkipTheirOwnDefinitions() {
        SingularityReloadListener listener = SingularityReloadListener.INSTANCE;
        listener.beginReload(Map.of());
        IIngredient ingredient = IIngredient.fromIngredient(Ingredient.EMPTY);

        assertDoesNotThrow(() -> SingularityCrafting.register("INVALID ID", "invalid", 0, 0,
                1, 1, ingredient, true, true));
        assertDoesNotThrow(() -> SingularityCrafting.remove("INVALID ID"));
        assertDoesNotThrow(() -> SingularityCrafting.removeRecipe("INVALID ID"));
        assertDoesNotThrow(() -> SingularityCrafting.register(INVALID_VALUE_ID.toString(), "invalid", 0, 0,
                0, 1, ingredient, true, true));
        assertDoesNotThrow(() -> SingularityCrafting.register(VALID_ID.toString(), "valid", 0, 0,
                1, 1, ingredient, true, true));

        assertFalse(listener.getRunSingularities().containsKey(INVALID_VALUE_ID));
        assertTrue(listener.getRunSingularities().containsKey(VALID_ID));
        listener.beginReload(Map.of());
    }

    @Test
    void unrelatedIngredientFailuresContinueToPropagate() {
        assertThrows(NullPointerException.class, () -> SingularityCrafting.register(
                VALID_ID.toString(), "valid", 0, 0, 1, 1, null, true, true));
    }
}
