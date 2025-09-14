package committee.nova.mods.avaritia.util;

import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToTextComponentVisitor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author: cnlimiter
 */
public class CrtUtils {
    private static final String NEW_LINE = System.lineSeparator() + "\t";
    private static final char[] KEYS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-_*/".toCharArray();
    private static String writeTag(CompoundTag tag) {
        return (new MapData(tag).accept(new DataToTextComponentVisitor("", 0)).getString());
    }

    public static String generateShapeTableZS(RecipeGeneratorMenu menu, int tier, boolean useNbt, boolean useTag) {
        ItemStack output = menu.getSlotItem(81); // 输出槽位
        ResourceLocation outputId = Const.getItemName(output.getItem());
        String outputIdString = outputId == null ? "item:minecraft:air" : "item:" + outputId;

        var string = new StringBuilder();
        var uuid = UUID.randomUUID();

        // 根据等级确定网格大小
        int gridSize = switch (tier) {
            case 1 -> 3;   // 3x3
            case 2 -> 5;   // 5x5
            case 3 -> 7;   // 7x7
            case 4 -> 9;   // 9x9
            default -> 9;
        };

        // 计算起始位置（从中心向外扩散）
        int startRow = (9 - gridSize) / 2;
        int startCol = (9 - gridSize) / 2;

        // 收集输入槽位的物品
        List<ItemStack> inputs = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = (startRow + row) * 9 + (startCol + col);
                inputs.add(menu.getSlotItem(slotIndex));
            }
        }

        string.append("mods.avaritia.").append("CraftingTable").append(".addShaped(\"").append(uuid).append("\", ");
        string.append(tier).append(", ");
        string.append("<").append(outputIdString).append(">");
        if (useNbt && !output.isEmpty() && output.hasTag() && ModList.get().isLoaded("crafttweaker")) {
            var nbt = output.getTag();
            var tag = writeTag(nbt);

            string.append(".withTag(").append(tag).append("),");
        }
        string.append(" [").append(NEW_LINE);

        for (int row = 0; row < gridSize; row++) {
            string.append("    [");
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;
                ItemStack stack = index < inputs.size() ? inputs.get(index) : ItemStack.EMPTY;

                var item = "";

                if (!stack.isEmpty() && useTag) {
                    var tagId = stack.getTags().findFirst().orElse(null);

                    if (tagId != null) {
                        item = "tag:items:" + tagId.location();
                    }
                }

                if (item.isEmpty()) {
                    var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    item = id == null ? "item:minecraft:air" : "item:" + id;
                }

                string.append("<").append(item).append(">");

                if (useNbt && !stack.isEmpty() && stack.hasTag() && !item.startsWith("tag") && ModList.get().isLoaded("crafttweaker")) {
                    var nbt = stack.getTag();
                    var tag = writeTag(nbt);

                    string.append(".withTag(").append(tag).append(")");
                }
                if (col < gridSize - 1) string.append(", ");
            }
            if (row <= gridSize - 1) string.append("]");
            if (row < gridSize - 1) string.append(",").append(System.lineSeparator());
        }
        string.append(System.lineSeparator()).append("]);");
        return string.toString();
    }

    public static String generateShapelessTableZS(RecipeGeneratorMenu menu, int tier, boolean useNbt, boolean useTag) {
        ItemStack output = menu.getSlotItem(81); // 输出槽位
        ResourceLocation outputId = Const.getItemName(output.getItem());
        String outputIdString = outputId == null ? "item:minecraft:air" : "item:" + outputId;

        var string = new StringBuilder();
        var uuid = UUID.randomUUID();

        // 根据等级确定网格大小
        int gridSize = switch (tier) {
            case 1 -> 3;   // 3x3
            case 2 -> 5;   // 5x5
            case 3 -> 7;   // 7x7
            case 4 -> 9;   // 9x9
            default -> 9;
        };

        // 计算起始位置（从中心向外扩散）
        int startRow = (9 - gridSize) / 2;
        int startCol = (9 - gridSize) / 2;

        // 收集输入槽位的物品 (只收集非空物品)
        List<ItemStack> inputs = new ArrayList<>();
        int lastSlot = 0;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = (startRow + row) * 9 + (startCol + col);
                ItemStack stack = menu.getSlotItem(slotIndex);
                if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
                    inputs.add(stack);
                    lastSlot = slotIndex;
                }
            }
        }


        string.append("mods.avaritia.").append("CraftingTable").append(".addShapeless(\"").append(uuid).append("\", ");
        string.append(tier).append(", ");
        string.append("<").append(outputIdString).append(">");
        if (useNbt && !output.isEmpty() && output.hasTag() && ModList.get().isLoaded("crafttweaker")) {
            var nbt = output.getTag();
            var tag = writeTag(nbt);

            string.append(".withTag(").append(tag).append("),");
        }
        string.append(" [").append(NEW_LINE);

        for (int i = 0; i < inputs.size(); i++) {
            var stack = menu.getSlotItem(i);
            var tagId = stack.getTags().findFirst().orElse(null);

            String item;
            if (tagId != null && useTag) {
                item = "tag:items:" + tagId;
            } else {
                var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                item = id == null ? "item:minecraft:air" : "item:" + id;
            }

            string.append("<").append(item).append(">");

            if (useNbt && !stack.isEmpty() && stack.hasTag() && !item.startsWith("tag") && ModList.get().isLoaded("crafttweaker")) {
                var nbt = stack.getTag();
                var tag = writeTag(nbt);

                string.append(".withTag(").append(tag).append(")");
            }

            if (i != lastSlot) {
                string.append(", ");
            }
        }
        string.append(System.lineSeparator()).append("]);");
        return string.toString();
    }

    public static void exportTableZS(RecipeGeneratorMenu menu, boolean shaped, int tier, boolean useNbt, String name) {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve((name == null ? "avaritia_generated_recipe" :  name) + ".zs");

            String newRecipe;
            if (shaped) {
                newRecipe = generateShapeTableZS(menu, tier, useNbt, false);
            } else {
                newRecipe = generateShapelessTableZS(menu, tier, useNbt, false);
            }
            String existingContent = "";

            if (Files.exists(recipePath)) {
                existingContent = Files.readString(recipePath);
            }

            String updatedContent = existingContent + (newRecipe + "\n");
            Files.writeString(recipePath, updatedContent);

        } catch (Exception ignored) {
        }
    }


//    private static String makeCraftTweakerCombinationRecipe(CraftingCoreTileEntity tile) {
//        var string = new StringBuilder();
//        var uuid = UUID.randomUUID();
//
//        string.append("mods.extendedcrafting.CombinationCrafting.addRecipe(\"").append(uuid).append("\", <>, 100000, [").append(NEW_LINE);
//
//        var inputId = ForgeRegistries.ITEMS.getKey(tile.getInventory().getStackInSlot(0).getItem());
//        var input = "item:minecraft:air";
//
//        if (inputId != null)
//            input = "item:" + inputId;
//
//        string.append("<").append(input).append(">, ");
//
//        var stacks = tile.getPedestalsWithItems().values().stream().filter(s -> !s.isEmpty()).toArray(ItemStack[]::new);
//
//        for (int i = 0; i < stacks.length; i++) {
//            var stack = stacks[i];
//            var tagId = stack.getTags().findFirst().orElse(null);
//
//            String item;
//            if (ModConfigs.RECIPE_MAKER_USE_TAGS.get() && tagId != null) {
//                item = "tag:items:" + tagId;
//            } else {
//                var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
//                item = id == null ? "item:minecraft:air" : "item:" + id;
//            }
//
//            if (ModConfigs.RECIPE_MAKER_USE_NBT.get() && !stack.isEmpty() && stack.hasTag() && !item.startsWith("tag") && ModList.get().isLoaded("crafttweaker")) {
//                var nbt = stack.getTag();
//                var tag = CraftTweakerUtils.writeTag(nbt);
//
//                string.append(".withTag(").append(tag).append(")");
//            }
//
//            string.append("<").append(item).append(">");
//
//            if (i != stacks.length - 1) {
//                string.append(", ");
//            }
//        }
//
//        string.append(System.lineSeparator()).append("]);");
//
//        return string.toString();
//    }

    // Create a shaped Datapack recipe for a Table, Flux Crafter or Ender Crafter
    private static String makeShapedDatapackTableRecipe(RecipeGeneratorMenu menu, int tier) {
        var object = new JsonObject();

        object.addProperty("tier", tier);

        Map<Ingredient, Character> keysMap = new LinkedHashMap<>();
        int slots = menu.getAvailableSlotsForTier(tier);

        for (int i = 0; i < slots; i++) {
            var stack = menu.getSlotItem(i);

            if (stack.isEmpty() || keysMap.keySet().stream().anyMatch(ing -> ing.test(stack)))
                continue;

            var tag = stack.getTags().findFirst().orElse(null);
            char key = KEYS[keysMap.size()];
            if (tag != null) {
                keysMap.put(Ingredient.of(tag), key);
            } else {
                if (stack.hasTag()) {
                    keysMap.put(StrictNBTIngredient.of(stack), key);
                } else {
                    keysMap.put(Ingredient.of(stack), key);
                }
            }

            if (keysMap.size() >= KEYS.length)
                return "TOO MANY ITEMS";
        }

        var pattern = new JsonArray();
        int size = (int) Math.sqrt(slots);
        var keys = keysMap.entrySet();

        for (int i = 0; i < size; i++) {
            var line = new StringBuilder();

            for (int j = 0; j < size; j++) {
                var stack = menu.getSlotItem(i * size + j);
                var entry = keys.stream()
                        .filter(e -> e.getKey().test(stack)).findFirst().orElse(null);

                if (entry == null) {
                    line.append(" ");
                } else {
                    line.append(entry.getValue());
                }
            }

            pattern.add(line.toString());
        }

        object.add("pattern", pattern);

        var key = new JsonObject();

        for (var entry : keys) {
            key.add(entry.getValue().toString(), entry.getKey().toJson());
        }

        object.add("key", key);

        var result = new JsonObject();

        result.addProperty("item", "");
        object.add("result", result);

        return Const.GSON.toJson(object);
    }

    // Create a shapeless Datapack recipe for a Table Flux Crafter or Ender Crafter
    private static String makeShapelessDatapackTableRecipe(RecipeGeneratorMenu menu, int tier) {
        var object = new JsonObject();

        object.addProperty("tier", tier);

        var ingredients = new JsonArray();
        int slots = menu.getAvailableSlotsForTier(tier);

        for (int i = 0; i < slots; i++) {
            var stack = menu.getSlotItem(i);

            if (!stack.isEmpty()) {
                var tagId = stack.getTags().findFirst().orElse(null);

                if (tagId != null) {
                    var tag = new JsonObject();

                    tag.addProperty("tag", tagId.toString());
                    ingredients.add(tag);
                } else {
                    if (stack.hasTag()) {
                        ingredients.add(StrictNBTIngredient.of(stack).toJson());
                    } else {
                        ingredients.add(Ingredient.of(stack).toJson());
                    }
                }
            }
        }

        object.add("ingredients", ingredients);

        var result = new JsonObject();

        result.addProperty("item", "");
        object.add("result", result);

        return Const.GSON.toJson(object);
    }

//    private static String makeDatapackCombinationRecipe(CraftingCoreTileEntity core) {
//        var object = new JsonObject();
//
//        object.addProperty("type", "extendedcrafting:combination");
//        object.addProperty("powerCost", 100000);
//
//        var input = core.getInventory().getStackInSlot(0);
//
//        object.add("input", Ingredient.of(input).toJson());
//
//        var ingredients = new JsonArray();
//        var stacks = core.getPedestalsWithItems().values().stream().filter(s -> !s.isEmpty()).toArray(ItemStack[]::new);
//
//        for (var stack : stacks) {
//            var tagId = stack.getTags().findFirst().orElse(null);
//
//            if (ModConfigs.RECIPE_MAKER_USE_TAGS.get() && tagId != null) {
//                var tag = new JsonObject();
//
//                tag.addProperty("tag", tagId.toString());
//                ingredients.add(tag);
//            } else {
//                if (ModConfigs.RECIPE_MAKER_USE_NBT.get() && stack.hasTag()) {
//                    ingredients.add(StrictNBTIngredient.of(stack).toJson());
//                } else {
//                    ingredients.add(Ingredient.of(stack).toJson());
//                }
//            }
//        }
//
//        object.add("ingredients", ingredients);
//
//        var result = new JsonObject();
//
//        result.addProperty("item", "");
//        object.add("result", result);
//
//        return GSON.toJson(object);
//    }
}
