package committee.nova.mods.avaritia.init.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.ModApi;
import committee.nova.mods.avaritia.api.init.data.ResourceConditions;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Commits script operations after every resource reload listener has completed.
 *
 * <p>26.3 起 {@link RecipeManager} 的配方映射由冻结后的动态注册表
 * （{@code Registries.RECIPE}）一次性构建，vanilla 已删除 {@code replaceRecipes} 且
 * {@link Recipe} 不再自带 id；因此这里按奇点重放生成的压缩配方，构建
 * {@link RecipeHolder}（{@code ResourceKey}）后整体重建内部 {@link RecipeMap}。
 * 本方法在 {@code loadResources} 完成后、{@code RecipeManager#finalizeRecipeLoading}
 * 之前于主线程执行，写回结果会参与后续的展示信息构建。</p>
 */
public final class SingularityScriptTransactionFinalizer {
    private static Map<Identifier, Recipe<?>> generatedRecipes = Map.of();

    private SingularityScriptTransactionFinalizer() {}

    public static void finalizeReload(RecipeManager recipeManager) {
        if (!SingularityReloadListener.INSTANCE.finalizeScriptTransaction(() -> {
            // 对外保留事件协议：第三方监听器（脚本整合等）仍可在事务回调内追加配方。
            RegisterRecipesEvent event = new RegisterRecipesEvent(recipeManager, new CopyOnWriteArrayList<>());
            InternalRecipeHandler.onRegisterRecipes(event);
            RegisterRecipesEvent.post(event);
        })) {
            return;
        }
        Map<Identifier, Recipe<?>> replacements = collectGeneratedRecipes();
        replaceGeneratedRecipes(recipeManager, replacements);
        Const.LOGGER.info("Singularity: Finalized script transaction and regenerated {} internal recipes",
                replacements.size());
    }

    private static Map<Identifier, Recipe<?>> collectGeneratedRecipes() {
        Map<Identifier, Recipe<?>> generated = new LinkedHashMap<>();
        for (Singularity singularity : SingularityReloadListener.INSTANCE.getAllSingularities().values()) {
            if (!singularity.isEnabled() || !singularity.isRecipeEnabled()
                    || !singularity.getConditions().stream().allMatch(ResourceConditions::test)) {
                continue;
            }
            CompressorRecipe recipe = ModApi.addSingularityRecipe(singularity);
            if (recipe == null) continue;
            Identifier id = singularity.getRegistryName();
            generated.put(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_singularity"), recipe);
        }
        return generated;
    }

    private static void replaceGeneratedRecipes(RecipeManager recipeManager, Map<Identifier, Recipe<?>> replacements) {
        // Nothing to remove from a previous reload and nothing to add: keep the vanilla RecipeMap
        // untouched. It is built through RecipeMap.create(...), which lets Fabric's recipe-sync
        // mixin attach its synced-serializer index; rebuilding it via the private constructor
        // would leave that index null and disconnect players with "invalid player data".
        if (replacements.isEmpty() && generatedRecipes.isEmpty()) {
            return;
        }
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> merged = new LinkedHashMap<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            merged.put(holder.id(), holder);
        }
        for (Identifier previous : generatedRecipes.keySet()) {
            merged.remove(ResourceKey.create(Registries.RECIPE, previous));
        }
        Map<Identifier, Recipe<?>> nextGenerated = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Recipe<?>> entry : replacements.entrySet()) {
            RecipeHolder<?> holder = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, entry.getKey()), entry.getValue());
            merged.put(holder.id(), holder);
            nextGenerated.put(entry.getKey(), entry.getValue());
        }
        if (merged.isEmpty()) return;
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();
        for (RecipeHolder<?> holder : merged.values()) {
            byType.put(holder.value().getType(), holder);
            byKey.put(holder.id(), holder);
        }
        writeRecipeMap(recipeManager, byType.build(), byKey.build());
        generatedRecipes = nextGenerated;
    }

    private static void writeRecipeMap(RecipeManager recipeManager,
                                       Multimap<RecipeType<?>, RecipeHolder<?>> byType,
                                       Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
        try {
            Constructor<RecipeMap> constructor = RecipeMap.class.getDeclaredConstructor(Multimap.class, Map.class);
            constructor.setAccessible(true);
            RecipeMap recipeMap = constructor.newInstance(byType, byKey);
            attachFabricSyncedSerializerIndex(recipeMap, byKey.values());
            Field recipes = RecipeManager.class.getDeclaredField("recipes");
            recipes.setAccessible(true);
            recipes.set(recipeManager, recipeMap);
            Field learnableRecipes = RecipeManager.class.getDeclaredField("learnableRecipes");
            learnableRecipes.setAccessible(true);
            learnableRecipes.set(recipeManager, learnable(byKey.values()));
            rebuildFabricSynchronizedRecipes(recipeManager, recipeMap);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Singularity: Cannot rebuild recipe map", exception);
        }
    }

    /**
     * fabric-recipe-api's {@code RecipeManagerMixin} snapshots {@code synchronizedRecipes} once
     * in the {@code RecipeManager} constructor tail; replacing {@code recipes} afterwards leaves
     * {@code getSynchronizedRecipes()} (used by the neutron compressor via
     * {@code recipeAccess().getSynchronizedRecipes()}) pointing at the pre-transaction map.
     * Rebuild the snapshot exactly like the mixin does: {@code new SynchronizedRecipesImpl(map)}.
     */
    private static void rebuildFabricSynchronizedRecipes(RecipeManager recipeManager, RecipeMap recipeMap) {
        try {
            Class<?> impl = Class.forName("net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl");
            Object snapshot = impl.getConstructor(RecipeMap.class).newInstance(recipeMap);
            Field synchronizedRecipes = RecipeManager.class.getDeclaredField("synchronizedRecipes");
            synchronizedRecipes.setAccessible(true);
            synchronizedRecipes.set(recipeManager, snapshot);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            // Fabric API absent or internals changed: synchronized queries stay on the old snapshot.
            Const.LOGGER.warn("Singularity: Could not rebuild Fabric synchronizedRecipes snapshot", exception);
        }
    }

    /**
     * Replicates fabric-recipe-api's {@code RecipeMapMixin} attachment: the mixin only fills
     * its {@code bySyncedSerializer} index on {@link RecipeMap#create} return values, so a map
     * built through the private constructor must receive an equivalent index or Fabric's
     * configuration-phase recipe sync NPEs ("this.bySyncedSerializer is null").
     */
    private static void attachFabricSyncedSerializerIndex(RecipeMap recipeMap, Collection<RecipeHolder<?>> holders) {
        try {
            Class<?> syncClass = Class.forName("net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl");
            Method getSynced = syncClass.getMethod("getSyncedSerializers");
            @SuppressWarnings("unchecked")
            Set<Object> syncedSerializers = (Set<Object>) getSynced.invoke(null);
            Map<Object, List<RecipeHolder<?>>> index = new IdentityHashMap<>();
            for (Object serializer : syncedSerializers) {
                index.put(serializer, new ArrayList<>());
            }
            for (RecipeHolder<?> holder : holders) {
                List<RecipeHolder<?>> list = index.get(holder.value().getSerializer());
                if (list != null) {
                    list.add(holder);
                }
            }
            Field uniqueField = findFabricUniqueField();
            if (uniqueField != null) {
                uniqueField.setAccessible(true);
                uniqueField.set(recipeMap, index);
            } else {
                Const.LOGGER.warn("Singularity: Fabric RecipeMap synced-serializer field not found; "
                        + "custom recipe sync may be unavailable");
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            // Fabric API absent or internals changed: vanilla recipe handling still works.
            Const.LOGGER.warn("Singularity: Could not attach Fabric synced-serializer index", exception);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Field findFabricUniqueField() {
        for (String candidate : new String[]{"bySyncedSerializer", "fabric$bySyncedSerializer"}) {
            try {
                return RecipeMap.class.getDeclaredField(candidate);
            } catch (NoSuchFieldException ignored) {
                // fall through
            }
        }
        // Fallback for refactored @Unique names: the only non-final Map field on RecipeMap
        // added by the mixin (vanilla fields are byType/byKey, both private final).
        for (Field field : RecipeMap.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(field.getType()) && !Modifier.isFinal(field.getModifiers())) {
                return field;
            }
        }
        return null;
    }

    private static Collection<RecipeHolder<?>> learnable(Collection<RecipeHolder<?>> recipes) {
        return recipes.stream().filter(holder -> !holder.value().isSpecial()).collect(Collectors.toUnmodifiableList());
    }
}
