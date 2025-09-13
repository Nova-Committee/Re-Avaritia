package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: cnlimiter
 */
public class KubeJsUtils {
    public static String generateShapeTableJS(RecipeGeneratorMenu menu, int tier, boolean useNbt) {
        ItemStack output = menu.getSlotItem(81); // 输出槽位

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

        StringBuilder script = new StringBuilder();
        script.append("    avaritia.shaped_table(\n");
        script.append("        ").append(tier).append(",\n");

        ResourceLocation outputId = Const.getItemName(output.getItem());
        if (useNbt && output.hasTag()) {
            CompoundTag nbt = output.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                script.append("        Item.of('").append(outputId).append("', ");
                if (output.getCount() > 1) {
                    script.append(output.getCount()).append(", ");
                }
                script.append("'").append(nbt).append("'),\n");
            } else {
                script.append("        Item.of('").append(outputId).append("'");
                if (output.getCount() > 1) {
                    script.append(", ").append(output.getCount());
                }
                script.append("),\n");
            }
        } else {
            script.append("        Item.of('").append(outputId).append("'");
            if (output.getCount() > 1) {
                script.append(", ").append(output.getCount());
            }
            script.append("),\n");
        }

        script.append("        [\n");

        char currentKey = 'A';
        Map<String, Character> itemToKey = new HashMap<>();

        for (int row = 0; row < gridSize; row++) {
            script.append("            '");
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;
                ItemStack stack = index < inputs.size() ? inputs.get(index) : ItemStack.EMPTY;
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = Const.getItemName(stack.getItem());
                    if (itemId != null) {
                        String key;
                        key = itemId.toString();
                        if (useNbt && stack.hasTag()) {
                            CompoundTag nbt = stack.getTag();
                            if (nbt != null && !nbt.isEmpty()) {
                                key += "|NBT:" + nbt.toString();
                            }
                        }
                        if (!itemToKey.containsKey(key)) {
                            itemToKey.put(key, currentKey++);
                        }
                        script.append(itemToKey.get(key));
                    } else {
                        script.append(" ");
                    }
                } else {
                    script.append(" ");
                }
            }
            script.append(row < gridSize - 1 ? "',\n" : "'\n");
        }
        script.append("        ],\n");

        script.append("        {\n");
        boolean first = true;
        for (Map.Entry<String, Character> entry : itemToKey.entrySet()) {
            if (!first)
                script.append(",\n");
            String[] keyParts = entry.getKey().split("\\|");
            String itemKey = keyParts[0];
            script.append("            ").append(entry.getValue()).append(": ");

            if (keyParts.length > 1 && keyParts[1].startsWith("NBT:")) {
                script.append("Item.of('").append(itemKey).append("', '").append(keyParts[1].substring(4))
                        .append("').strongNBT()");
            } else if (useNbt) {
                script.append("Item.of('").append(itemKey).append("', '{}')");
            } else {
                script.append("'").append(itemKey).append("'");
            }
            first = false;
        }
        script.append("\n        }\n    )");


        //System.out.println(script);
        return script.toString();
    }

    public static String generateShapelessTableJS(RecipeGeneratorMenu menu, int tier, boolean useNbt) {
        ItemStack output = menu.getSlotItem(81); // 输出槽位

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
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slotIndex = (startRow + row) * 9 + (startCol + col);
                ItemStack stack = menu.getSlotItem(slotIndex);
                if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
                    inputs.add(stack);
                }
            }
        }

        StringBuilder script = new StringBuilder();
        script.append("    avaritia.shapeless_table(\n");
        script.append("        ").append(tier).append(",\n");

        ResourceLocation outputId = Const.getItemName(output.getItem());
        if (useNbt && output.hasTag()) {
            CompoundTag nbt = output.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                script.append("        Item.of('").append(outputId).append("', ");
                if (output.getCount() > 1) {
                    script.append(output.getCount()).append(", ");
                }
                script.append("'").append(nbt.toString()).append("'),\n");
            } else {
                script.append("        Item.of('").append(outputId).append("'");
                if (output.getCount() > 1) {
                    script.append(", ").append(output.getCount());
                }
                script.append("),\n");
            }
        } else {
            script.append("        Item.of('").append(outputId).append("'");
            if (output.getCount() > 1) {
                script.append(", ").append(output.getCount());
            }
            script.append("),\n");
        }

        script.append("        [\n");

        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            script.append("            ");

            ResourceLocation itemId = Const.getItemName(input.getItem());

            if (useNbt && input.hasTag()) {
                CompoundTag nbt = input.getTag();
                if (nbt != null && !nbt.isEmpty()) {
                    script.append("Item.of('").append(itemId).append("', '").append(nbt.toString())
                            .append("').strongNBT()");
                } else {
                    script.append("Item.of('").append(itemId).append("', '{}')");
                }
            } else if (useNbt) {
                script.append("Item.of('").append(itemId).append("', '{}')");
            } else {
                script.append("'").append(itemId).append("'");
            }

            if (i < inputs.size() - 1) {
                script.append(",");
            }
            script.append("\n");
        }

        script.append("        ]\n    )");

        return script.toString();
    }

    public static void exportTableJS(RecipeGeneratorMenu menu, boolean shaped, int tier, boolean useNbt, String name) {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("kubejs/server_scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve((name == null ? "avaritia_generated_recipe" :  name) + ".js");

            String newRecipe;
            if (shaped) {
                newRecipe = generateShapeTableJS(menu, tier, useNbt);
            } else {
                newRecipe = generateShapelessTableJS(menu, tier, useNbt);
            }
            String existingContent = "";

            if (Files.exists(recipePath)) {
                existingContent = Files.readString(recipePath);
            }

            if (!existingContent.contains("ServerEvents.recipes")) {
                existingContent = """
                        ServerEvents.recipes(event => {
                                const { avaritia } = event.recipes;
                        });
                        """;
            }

            if (!existingContent.contains(newRecipe.trim())) {
                String updatedContent = existingContent.replace("});", newRecipe + "\n});");
                Files.writeString(recipePath, updatedContent);
            }
        } catch (Exception ignored) {
        }
    }
}
