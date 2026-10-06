package committee.nova.mods.avaritia.core.singularity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentParityP0ContractTest {
    private static final Path ANVIL = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/menu/ExtremeAnvilMenu.java");
    private static final Path CATALYST_RECIPE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/crafting/recipe/NoConsumeCatalystShapedRecipe.java");
    private static final Path LISTENER = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java");
    private static final Path RECIPE_MIXIN = Path.of(
            "src/main/java/committee/nova/mods/avaritia/mixin/RecipeManagerMixin.java");
    private static final Path RELOAD_HANDLER = Path.of(
            "src/main/java/committee/nova/mods/avaritia/init/handler/ResourceReloadHandler.java");
    private static final Path KUBE_PLUGIN = Path.of(
            "src/main/java/committee/nova/mods/avaritia/compat/kubejs/AvaritiaKubeJSPlugin.java");

    @Test
    void extremeAnvilUsesTargetAnvilContractWithoutOverridingFinalResultCalculation() throws IOException {
        String source = compact(Files.readString(ANVIL));

        assertAll(
                () -> assertTrue(source.contains("classExtremeAnvilMenuextendsAnvilMenu")),
                () -> assertFalse(source.contains("voidcreateResult(")),
                () -> assertTrue(source.contains("protectedvoidcreateResultInternal()")),
                () -> assertTrue(source.contains("super.createResultInternal();")),
                () -> assertTrue(source.contains("CommonHooks.fireAnvilCraftPre(")),
                () -> assertTrue(source.contains("CommonHooks.fireAnvilCraftPost(")),
                () -> assertTrue(source.contains("this.setCost(0);"))
        );
    }

    @Test
    void catalystAlwaysReturnsExactlyOneItem() throws IOException {
        String source = compact(Files.readString(CATALYST_RECIPE));

        assertTrue(source.contains("remaining.set(i,stack.copyWithCount(1));"));
        assertFalse(source.contains("remaining.set(i,stack.copy());"));
    }

    @Test
    void singularityPublicationRollsBackAndFinalizesRecipeDerivationAtomically() throws IOException {
        String listener = compact(Files.readString(LISTENER));
        String mixin = compact(Files.readString(RECIPE_MIXIN));
        String reloadHandler = compact(Files.readString(RELOAD_HANDLER));
        String kubePlugin = compact(Files.readString(KUBE_PLUGIN));

        assertAll(
                () -> assertTrue(listener.contains("EnumMap<ScriptSource,List<ScriptOperation>>scriptOperations")),
                () -> assertTrue(listener.contains("AtomicSnapshotTransaction<Snapshot>snapshotTransaction")),
                () -> assertTrue(listener.contains("this.snapshotTransaction.commit(candidate,()->")),
                () -> assertTrue(mixin.contains("finalizeScriptTransaction(()->NeoForge.EVENT_BUS.post(")),
                () -> assertTrue(mixin.contains("recipes.clear();recipes.addAll(originalRecipes);")),
                () -> assertTrue(reloadHandler.contains(
                        "event.addDependency(RELOAD_LISTENER_ID,VanillaServerListeners.RECIPES);")),
                () -> assertTrue(kubePlugin.contains(
                        "SingularityReloadListener.INSTANCE.beginScriptTransaction();"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
