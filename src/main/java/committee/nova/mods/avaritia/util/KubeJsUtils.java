package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
    public static int getSize(int tier) {
        int size = 0;
        if (tier == 1) { size = 9;}
        else if (tier == 2) { size = 25;}
        else if (tier == 3) { size = 49;}
        else if (tier == 5) { size = 81;}
        return size;
    }
    public static String generateShapeTableJS(RecipeGeneratorMenu menu, int tier, boolean useNbt) {
        int size = getSize(tier);
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = menu.getSlotItem(size); // 输出槽位

        // 收集输入槽位的物品 (81个输入槽)
        for (int i = 0; i < size; i++) {
            inputs.add(menu.getSlotItem(i));
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

        for (int row = 0; row < size; row++) {
            script.append("            '");
            for (int col = 0; col < size; col++) {
                ItemStack stack = inputs.get(row * size + col);
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
            script.append(row < size - 1 ? "',\n" : "'\n");
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


        System.out.println(script);
        return script.toString();
    }

    public static String generateShapelessTableJS(RecipeGeneratorMenu menu, int tier, boolean useNbt) {
        int size = getSize(tier);
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = menu.getSlotItem(size); // 输出槽位

        // 收集输入槽位的物品 (只收集非空的物品)
        for (int i = 0; i < size; i++) {
            ItemStack stack = menu.getSlotItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
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

    public static void exportJSRecipe(RecipeGeneratorMenu menu,  boolean shaped, int tier, boolean useNbt, String name) {
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
