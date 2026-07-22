package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates recipes whose result items belong to optional mods.
 *
 * <p>Using a dedicated JSON provider keeps those result IDs intact when the
 * optional mod is absent from the data-generator runtime. Constructing an
 * {@code ItemStack} through the vanilla registry would otherwise resolve the
 * unknown result to air before the mod-loaded condition can be serialized.</p>
 */
public final class ModCompatRecipes implements DataProvider {
    private static final String[] CREATIVE_SLOT_PATTERN = {
            "GGBHHHBGG",
            "GCCCCCCCG",
            "BCBFFFBCB",
            "HCFFEFFCH",
            "HCFEAEFCH",
            "HCFFEFFCH",
            "BCBFFFBCB",
            "GCCCCCCCG",
            "GGBHHHBGG"
    };

    private final PackOutput.PathProvider recipePath;
    private final PackOutput.PathProvider advancementPath;

    public ModCompatRecipes(PackOutput output) {
        this.recipePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.advancementPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>(14);

        save(saves, output, "botania_mana_tablet", "botania", "botania:mana_tablet",
                "botania:terrasteel_block",
                new String[]{
                        "BAAACAAAD", "ATTJKLTTA", "ATUUMUUTA", "ANUOPOUQA", "EUUPRPUUF",
                        "ASUOPOUSA", "ATUUUUUTA", "ATTSISTTA", "GAAAHAAAA"
                }, customData("mana", 500000, "creative", true),
                "A", "avaritia:infinity_ingot",
                "B", "botania:rune_envy",
                "C", "botania:rune_gluttony",
                "D", "botania:rune_winter",
                "E", "botania:rune_lust",
                "F", "botania:rune_pride",
                "G", "botania:rune_wrath",
                "H", "botania:rune_greed",
                "I", "botania:rune_sloth",
                "J", "botania:infinite_fruit",
                "K", "botania:flight_tiara",
                "L", "botania:king_key",
                "M", "botania:flugel_eye",
                "N", "botania:odin_ring",
                "O", "botania:spawner_mover",
                "P", "botania:mana_mirror",
                "Q", "botania:thor_ring",
                "R", "botania:mana_tablet",
                "S", "botania:dice",
                "T", "botania:fabulous_pool",
                "U", "botania:terrasteel_block");

        save(saves, output, "botania_creative_pool", "botania", "botania:creative_pool",
                "botania:mana_tablet",
                new String[]{
                        "NNNNNNNNN", "NXCXYXCXN", "NCXEYEXCN", "NXEEYEEXN", "YYYYFYYYY",
                        "NXEEYEEXN", "NCXEYEXCN", "NXCXYXCXN", "NNNNNNNNN"
                }, null,
                "X", "avaritia:infinity_catalyst",
                "N", "avaritia:neutron_ingot",
                "C", "botania:mana_pool",
                "Y", "botania:fabulous_pool",
                "E", "botania:dragonstone_block",
                "F", "botania:mana_tablet");

        saveCreativeSlot(saves, output, "tc3_creative_slot_ability", "abilities", "ichor_slime_crystal");
        saveCreativeSlot(saves, output, "tc3_creative_slot_defense", "defense", "earth_slime_crystal");
        saveCreativeSlot(saves, output, "tc3_creative_slot_souls", "souls", "sky_slime_crystal");
        saveCreativeSlot(saves, output, "tc3_creative_slot_upgrades", "upgrades", "ender_slime_crystal");

        JsonObject mekanismComponents = new JsonObject();
        JsonObject energy = new JsonObject();
        JsonArray energyContainers = new JsonArray();
        energyContainers.add(Long.MAX_VALUE);
        energy.add("energy_containers", energyContainers);
        mekanismComponents.add("mekanism:energy", energy);
        save(saves, output, "mek_creative_energy_cube", "mekanism", "mekanism:creative_energy_cube",
                "mekanism:ultimate_energy_cube",
                new String[]{
                        "BBBCECBBB", "BDDDADDDB", "BDSDADSDB", "CDDDIDDDC", "EAAISIAAE",
                        "CDDDIDDDC", "BDSDADSDB", "BDDDADDDB", "BBBCECBBB"
                }, mekanismComponents,
                "I", "avaritia:infinity_ingot",
                "S", "avaritia:infinity_catalyst",
                "A", "mekanism:ultimate_energy_cube",
                "B", "mekanism:induction_casing",
                "C", "mekanism:induction_port",
                "D", "mekanism:ultimate_induction_cell",
                "E", "mekanism:ultimate_induction_provider");

        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private void saveCreativeSlot(List<CompletableFuture<?>> saves, CachedOutput output,
                                  String recipeName, String slot, String slimeCrystal) {
        save(saves, output, recipeName, "tconstruct", "tconstruct:creative_slot",
                "tconstruct:" + slimeCrystal, CREATIVE_SLOT_PATTERN, customData("slot", slot),
                "A", "avaritia:infinity_catalyst",
                "B", "tconstruct:iron_reinforcement",
                "C", "tconstruct:queens_slime_ingot",
                "E", "tconstruct:manyullyn_block",
                "F", "tconstruct:jeweled_apple",
                "G", "tconstruct:iron_reinforcement",
                "H", "tconstruct:" + slimeCrystal);
    }

    private void save(List<CompletableFuture<?>> saves, CachedOutput output,
                      String recipeName, String modId, String resultId, String unlockItem,
                      String[] pattern, JsonObject components, String... keyEntries) {
        if ((keyEntries.length & 1) != 0) {
            throw new IllegalArgumentException("Recipe key entries must be symbol/item pairs");
        }

        ResourceLocation id = Const.rl(recipeName);
        JsonObject recipe = conditionedRoot(modId);
        recipe.addProperty("type", "avaritia:shaped_table");

        JsonObject keys = new JsonObject();
        for (int i = 0; i < keyEntries.length; i += 2) {
            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("item", keyEntries[i + 1]);
            keys.add(keyEntries[i], ingredient);
        }
        recipe.add("key", keys);

        JsonArray rows = new JsonArray();
        for (String row : pattern) {
            rows.add(row);
        }
        recipe.add("pattern", rows);

        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", resultId);
        if (components != null && !components.isEmpty()) {
            result.add("components", components);
        }
        recipe.add("result", result);
        recipe.addProperty("tier", 4);

        saves.add(DataProvider.saveStable(output, recipe, recipePath.json(id)));
        saves.add(DataProvider.saveStable(output, advancement(id, modId, unlockItem),
                advancementPath.json(id.withPrefix("recipes/tools/"))));
    }

    private static JsonObject advancement(ResourceLocation recipeId, String modId, String unlockItem) {
        JsonObject advancement = conditionedRoot(modId);
        advancement.addProperty("parent", "minecraft:recipes/root");

        JsonObject criteria = new JsonObject();
        JsonObject hasItem = new JsonObject();
        hasItem.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject hasItemConditions = new JsonObject();
        JsonArray itemPredicates = new JsonArray();
        JsonObject itemPredicate = new JsonObject();
        itemPredicate.addProperty("items", unlockItem);
        itemPredicates.add(itemPredicate);
        hasItemConditions.add("items", itemPredicates);
        hasItem.add("conditions", hasItemConditions);
        criteria.add("has_item", hasItem);

        JsonObject hasRecipe = new JsonObject();
        hasRecipe.addProperty("trigger", "minecraft:recipe_unlocked");
        JsonObject hasRecipeConditions = new JsonObject();
        hasRecipeConditions.addProperty("recipe", recipeId.toString());
        hasRecipe.add("conditions", hasRecipeConditions);
        criteria.add("has_the_recipe", hasRecipe);
        advancement.add("criteria", criteria);

        JsonArray requirementGroup = new JsonArray();
        requirementGroup.add("has_the_recipe");
        requirementGroup.add("has_item");
        JsonArray requirements = new JsonArray();
        requirements.add(requirementGroup);
        advancement.add("requirements", requirements);

        JsonObject rewards = new JsonObject();
        JsonArray recipes = new JsonArray();
        recipes.add(recipeId.toString());
        rewards.add("recipes", recipes);
        advancement.add("rewards", rewards);
        return advancement;
    }

    private static JsonObject conditionedRoot(String modId) {
        JsonObject root = new JsonObject();
        JsonArray conditions = new JsonArray();
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modId);
        conditions.add(condition);
        root.add("neoforge:conditions", conditions);
        return root;
    }

    private static JsonObject customData(Object... entries) {
        JsonObject data = new JsonObject();
        for (int i = 0; i < entries.length; i += 2) {
            Object value = entries[i + 1];
            if (value instanceof Number number) {
                data.addProperty(entries[i].toString(), number);
            } else if (value instanceof Boolean bool) {
                data.addProperty(entries[i].toString(), bool);
            } else {
                data.addProperty(entries[i].toString(), value.toString());
            }
        }
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", data);
        return components;
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Optional Compatibility Recipes";
    }
}
