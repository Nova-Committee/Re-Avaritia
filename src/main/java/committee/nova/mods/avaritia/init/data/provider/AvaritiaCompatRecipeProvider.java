package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 生成原项目中针对其它模组 creative 物品的条件配方。
 * <p>
 * 这些配方的结果物品不属于 Avaritia，datagen 阶段不能通过 ItemLike 构造，
 * 因此直接写出带 mod_loaded 条件的 JSON，保持原项目配方语义不变。
 */
public class AvaritiaCompatRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public AvaritiaCompatRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        return CompletableFuture.allOf(
                save(output, "ae2_creative_energy_cell", recipe("ae2:creative_energy_cell", mods("ae2"),
                        pattern("YYYYXYYYY", "YCACXCACY", "YACBXBCAY", "YCBBXBBCY", "XXXXDXXXX", "YCBBXBBCY", "YACBXBCAY", "YCACXCACY", "YYYYXYYYY"),
                        keys(
                                key('A', "ae2:vibration_chamber"),
                                key('B', "ae2:calculation_processor"),
                                key('C', "avaritia:infinity_ingot"),
                                key('D', "ae2:cell_component_256k"),
                                key('Y', "ae2:dense_energy_cell"),
                                key('X', "ae2:singularity")))),
                save(output, "botania_mana_tablet", recipe("botania:mana_tablet", botaniaCreativeManaTabletComponents(), mods("botania"),
                        pattern("BAAACAAAD", "ATTJKLTTA", "ATUUMUUTA", "ANUOPOUQA", "EUUPRPUUF", "ASUOPOUSA", "ATUUUUUTA", "ATTSISTTA", "GAAAHAAAA"),
                        keys(
                                key('A', "avaritia:infinity_ingot"),
                                key('B', "botania:rune_envy"),
                                key('C', "botania:rune_gluttony"),
                                key('D', "botania:rune_winter"),
                                key('E', "botania:rune_lust"),
                                key('F', "botania:rune_pride"),
                                key('G', "botania:rune_wrath"),
                                key('H', "botania:rune_greed"),
                                key('I', "botania:rune_sloth"),
                                key('J', "botania:infinite_fruit"),
                                key('K', "botania:flight_tiara"),
                                key('L', "botania:king_key"),
                                key('M', "botania:flugel_eye"),
                                key('N', "botania:odin_ring"),
                                key('O', "botania:spawner_mover"),
                                key('P', "botania:mana_mirror"),
                                key('Q', "botania:thor_ring"),
                                key('R', "botania:mana_tablet"),
                                key('S', "botania:dice"),
                                key('T', "botania:fabulous_pool"),
                                key('U', "botania:terrasteel_block")))),
                save(output, "botania_creative_pool", recipe("botania:creative_pool", mods("botania"),
                        pattern("NNNNNNNNN", "NXCXYXCXN", "NCXEYEXCN", "NXEEYEEXN", "YYYYFYYYY", "NXEEYEEXN", "NCXEYEXCN", "NXCXYXCXN", "NNNNNNNNN"),
                        keys(
                                key('X', "avaritia:infinity_catalyst"),
                                key('N', "avaritia:neutron_ingot"),
                                key('C', "botania:mana_pool"),
                                key('Y', "botania:fabulous_pool"),
                                key('E', "botania:dragonstone_block"),
                                key('F', "botania:mana_tablet")))),
                save(output, "de_creative_capacitor", recipe("draconicevolution:creative_capacitor", mods("draconicevolution"),
                        pattern("AAAACAAAA", "AEEBCBEEA", "AEBFCFBEA", "ABFFCFFBA", "CCCCDCCCC", "ABFFCFFBA", "AEBFCFBEA", "AEEBCBEEA", "AAAACAAAA"),
                        keys(
                                key('A', "avaritia:infinity_ingot"),
                                key('B', "draconicevolution:chaotic_crafting_injector"),
                                key('C', "draconicevolution:reactor_stabilizer"),
                                key('D', "draconicevolution:reactor_core"),
                                key('E', "draconicevolution:chaotic_core"),
                                key('F', "draconicevolution:chaotic_capacitor")))),
                save(output, "de_creative_op_capacitor", recipe("draconicevolution:creative_op_capacitor", mods("draconicevolution"),
                        pattern("BBCCCCCBB", "BBBBBBBBB", "CBAAAAABC", "CBACECABC", "CBAEDEABC", "CBACECABC", "CBAAAAABC", "BBBBBBBBB", "BBCCCCCBB"),
                        keys(
                                key('A', "avaritia:infinity_ingot"),
                                key('B', "avaritia:infinity"),
                                key('C', "draconicevolution:reactor_stabilizer"),
                                key('D', "draconicevolution:reactor_core"),
                                key('E', "draconicevolution:creative_capacitor")))),
                save(output, "rs_creative_controller", recipe("refinedstorage:creative_controller", mods("refinedstorage"),
                        pattern("ABBBCBBBA", "BDDDCDDDB", "BDDCCCDDB", "BDCCFCCDB", "CCCFAFCCC", "BECCFCCEB", "BEECCCEEB", "BEEECEEEB", "ABBBCBBBA"),
                        keys(
                                key('A', "avaritia:infinity_catalyst"),
                                key('B', "avaritia:neutron_ingot"),
                                key('C', "refinedstorage:advanced_processor"),
                                key('D', "refinedstorage:4096b_fluid_storage_part"),
                                key('E', "refinedstorage:64k_storage_part"),
                                key('F', "#refinedstorage:controller")))),
                save(output, "rs_creative_fluid_storage_disk", recipe("refinedstorage:creative_fluid_storage_disk", mods("refinedstorage"),
                        pattern("CAAABAAAC", "AAAABAAAA", "AAAABAAAA", "AAADCDAAA", "BBBCDCBBB", "AAADCDAAA", "AAAABAAAA", "AAAABAAAA", "CAAABAAAC"),
                        keys(
                                key('A', "avaritia:infinity"),
                                key('B', "avaritia:neutron_compressor"),
                                key('C', "refinedstorage:creative_controller"),
                                key('D', "refinedstorage:4096b_fluid_storage_part")))),
                save(output, "rs_creative_storage_disk", recipe("refinedstorage:creative_storage_disk", mods("refinedstorage"),
                        pattern("CAAABAAAC", "AAAABAAAA", "AAAABAAAA", "AAADCDAAA", "BBBCDCBBB", "AAADCDAAA", "AAAABAAAA", "AAAABAAAA", "CAAABAAAC"),
                        keys(
                                key('A', "avaritia:infinity"),
                                key('B', "avaritia:neutron_compressor"),
                                key('C', "refinedstorage:creative_controller"),
                                key('D', "refinedstorage:64k_storage_part")))),
                save(output, "rs_creative_wireless_grid", recipe("refinedstorage:creative_wireless_grid", mods("refinedstorage"),
                        pattern("HH     HH", "H       H", " BCCCCCB ", " CDDFEEC ", " CGFAFGC ", " CDDFEEC ", " BCCCCCB ", "H       H", "HH     HH"),
                        keys(
                                key('A', "avaritia:endest_pearl"),
                                key('B', "refinedstorage:range_upgrade"),
                                key('C', "refinedstorage:wireless_transmitter"),
                                key('D', "refinedstorage:destruction_core"),
                                key('E', "refinedstorage:construction_core"),
                                key('F', "refinedstorage:wireless_grid"),
                                key('G', "refinedstorage:network_receiver"),
                                key('H', "refinedstorage:storage_housing")))),
                save(output, "creative_storage_upgrade", recipe("storagedrawers:creative_storage_upgrade", mods("storagedrawers"),
                        pattern("    S    ", " NDDDDDN ", " DNDDDND ", " DDNDNDD ", "SDDDEDDDS", " DDNDNDD ", " DNDDDND ", " NDDDDDN ", "    S    "),
                        keys(
                                key('N', "avaritia:neutron_nugget"),
                                key('S', "minecraft:nether_star"),
                                key('D', "#storagedrawers:drawers"),
                                key('E', "storagedrawers:emerald_storage_upgrade")))),
                save(output, "tc3_creative_slot_ability", tconstructCreativeSlotRecipe("abilities", "tconstruct:ichor_slime_crystal")),
                save(output, "tc3_creative_slot_defense", tconstructCreativeSlotRecipe("defense", "tconstruct:earth_slime_crystal")),
                save(output, "tc3_creative_slot_souls", tconstructCreativeSlotRecipe("souls", "tconstruct:sky_slime_crystal")),
                save(output, "tc3_creative_slot_upgrades", tconstructCreativeSlotRecipe("upgrades", "tconstruct:ender_slime_crystal")),
                save(output, "mek_creative_energy_cube", recipe("mekanism:creative_energy_cube", mekanismCreativeEnergyCubeComponents(), mods("mekanism"),
                        pattern("BBBCECBBB", "BDDDADDDB", "BDSDADSDB", "CDDDIDDDC", "EAAISIAAE", "CDDDIDDDC", "BDSDADSDB", "BDDDADDDB", "BBBCECBBB"),
                        keys(
                                key('I', "avaritia:infinity_ingot"),
                                key('S', "avaritia:infinity_catalyst"),
                                key('A', "mekanism:ultimate_energy_cube"),
                                key('B', "mekanism:induction_casing"),
                                key('C', "mekanism:induction_port"),
                                key('D', "mekanism:ultimate_induction_cell"),
                                key('E', "mekanism:ultimate_induction_provider")))),
                save(output, "mek_creative_fluid_tank", recipe("mekanism:creative_fluid_tank", mods("mekanism"),
                        pattern("         ", " SAADAAS ", " ABBCBBA ", " ABBCBBA ", " ACCICCA ", " ABBCBBA ", " ABBCBBA ", " SAADAAS ", "         "),
                        keys(
                                key('I', "avaritia:infinity_ingot"),
                                key('S', "avaritia:infinity_catalyst"),
                                key('A', "mekanism:ultimate_fluid_tank"),
                                key('B', "mekanism:dynamic_tank"),
                                key('C', "mekanism:structural_glass"),
                                key('D', "mekanism:dynamic_valve")))),
                save(output, "mek_creative_chemical_tank", recipe("mekanism:creative_chemical_tank", mods("mekanism"),
                        pattern("   B B   ", " SAADAAS ", " ABBBBBA ", " ABBCBBA ", " ABCICBA ", " ABBCBBA ", " ABBBBBA ", " SAADAAS ", "         "),
                        keys(
                                key('I', "avaritia:infinity_ingot"),
                                key('S', "avaritia:infinity_catalyst"),
                                key('A', "mekanism:ultimate_chemical_tank"),
                                key('B', "mekanism:dynamic_tank"),
                                key('C', "mekanism:structural_glass"),
                                key('D', "mekanism:dynamic_valve")))),
                save(output, "mek_creative_bin", recipe("mekanism:creative_bin", mods("mekanism", "mekanismgenerators"),
                        pattern("AAAAAAAAA", "AEIIIIIEA", "AEIIIIIEA", "AEIIIIIEA", "AEEESEEEA", "AEEEEEEEA", "AEBCDCBEA", "AEBCDCBEA", "AAAAAAAAA"),
                        keys(
                                key('I', "avaritia:infinity_ingot"),
                                key('S', "avaritia:infinity_catalyst"),
                                key('A', "mekanismgenerators:fusion_reactor_frame"),
                                key('B', "mekanism:ultimate_energy_cube"),
                                key('C', "mekanism:ultimate_fluid_tank"),
                                key('D', "mekanism:ultimate_chemical_tank"),
                                key('E', "mekanism:ultimate_bin")))),
                save(output, "eio_creative_power", recipe("enderio:creative_power", mods("enderio"),
                        pattern("INIIIIINI", "NZEEEEEZN", "IECWWWCEI", "IEWZIZWEI", "IEWIVIWEI", "IEWZIZWEI", "IECWWWCEI", "NZEEEEEZN", "INIIIIINI"),
                        keys(
                                key('I', "avaritia:infinity_ingot"),
                                key('N', "avaritia:neutron_ingot"),
                                key('W', "enderio:weather_crystal"),
                                key('V', "enderio:vibrant_capacitor_bank"),
                                key('Z', "enderio:frank_n_zombie"),
                                key('E', "enderio:sentient_ender"),
                                key('C', "enderio:ender_crystal"))))
        );
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Compat Recipes";
    }

    private CompletableFuture<?> save(CachedOutput output, String name, JsonObject recipe) {
        return DataProvider.saveStable(output, recipe, this.pathProvider.json(Identifier.fromNamespaceAndPath(Const.MOD_ID, name)));
    }

    private static JsonObject recipe(String result, String[] modIds, String[] pattern, Map<Character, String> keys) {
        return recipe(result, new JsonObject(), modIds, pattern, keys);
    }

    private static JsonObject recipe(String result, JsonObject components, String[] modIds, String[] pattern, Map<Character, String> keys) {
        JsonObject json = new JsonObject();
        JsonArray conditions = new JsonArray();
        for (String modId : modIds) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "neoforge:mod_loaded");
            condition.addProperty("modid", modId);
            conditions.add(condition);
        }
        json.add("neoforge:conditions", conditions);
        json.addProperty("type", "avaritia:shaped_table");

        JsonObject key = new JsonObject();
        keys.forEach((symbol, ingredient) -> key.add(String.valueOf(symbol), ingredient(ingredient)));
        json.add("key", key);

        JsonArray patternJson = new JsonArray();
        for (String line : pattern) {
            patternJson.add(line);
        }
        json.add("pattern", patternJson);

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("count", 1);
        resultJson.addProperty("id", result);
        if (components.size() > 0) {
            resultJson.add("components", components);
        }
        json.add("result", resultJson);
        json.addProperty("tier", 4);
        return json;
    }

    private static JsonObject tconstructCreativeSlotRecipe(String slot, String slimeCrystal) {
        return recipe("tconstruct:creative_slot", tconstructCreativeSlotComponents(slot), mods("tconstruct"),
                pattern("GGBHHHBGG", "GCCCCCCCG", "BCBFFFBCB", "HCFFEFFCH", "HCFEAEFCH", "HCFFEFFCH", "BCBFFFBCB", "GCCCCCCCG", "GGBHHHBGG"),
                keys(
                        key('A', "avaritia:infinity_catalyst"),
                        key('B', "tconstruct:iron_reinforcement"),
                        key('C', "tconstruct:knightslime_ingot"),
                        key('E', "tconstruct:manyullyn_block"),
                        key('F', "tconstruct:jeweled_apple"),
                        key('G', "tconstruct:iron_reinforcement"),
                        key('H', slimeCrystal)));
    }

    private static JsonObject botaniaCreativeManaTabletComponents() {
        JsonObject customData = new JsonObject();
        customData.addProperty("mana", 500000);
        customData.addProperty("creative", true);
        return customDataComponents(customData);
    }

    private static JsonObject tconstructCreativeSlotComponents(String slot) {
        JsonObject customData = new JsonObject();
        customData.addProperty("slot", slot);
        return customDataComponents(customData);
    }

    private static JsonObject mekanismCreativeEnergyCubeComponents() {
        JsonArray energyContainers = new JsonArray();
        energyContainers.add(Long.MAX_VALUE);

        JsonObject energy = new JsonObject();
        energy.add("energy_containers", energyContainers);

        JsonObject components = new JsonObject();
        components.add("mekanism:energy", energy);
        return components;
    }

    private static JsonObject customDataComponents(JsonObject customData) {
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        return components;
    }

    private static JsonElement ingredient(String ingredient) {
        JsonObject json = new JsonObject();
        if (ingredient.startsWith("#")) {
            json.addProperty("tag", ingredient.substring(1));
            return json;
        }
        if (ingredient.startsWith(Const.MOD_ID + ":") || ingredient.startsWith("minecraft:")) {
            json.addProperty("item", ingredient);
            return json;
        }

        // 外部模组物品在 datagen 阶段可能不存在，沿用原项目的 compound 写法避免解析缺席注册项。
        JsonArray ingredients = new JsonArray();
        JsonObject item = new JsonObject();
        item.addProperty("item", ingredient);
        ingredients.add(item);
        json.addProperty("type", "neoforge:compound");
        json.add("ingredients", ingredients);
        return json;
    }

    private static String[] mods(String... ids) {
        return ids;
    }

    private static String[] pattern(String... rows) {
        return rows;
    }

    private static Map.Entry<Character, String> key(char symbol, String ingredient) {
        return Map.entry(symbol, ingredient);
    }

    @SafeVarargs
    private static Map<Character, String> keys(Map.Entry<Character, String>... entries) {
        Map<Character, String> keys = new LinkedHashMap<>();
        for (Map.Entry<Character, String> entry : entries) {
            keys.put(entry.getKey(), entry.getValue());
        }
        return keys;
    }
}
