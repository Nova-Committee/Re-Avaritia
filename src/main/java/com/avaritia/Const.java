package com.avaritia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.avaritia.common.ingredient.SimpleDatagenIngredient;
import com.avaritia.common.ingredient.StackIngredient;
import com.avaritia.init.registry.enums.Mods;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
public class Const {
    public static final String MOD_ID = "avaritia";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final GameProfile AVARITIA_FAKE_PLAYER = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
    public static final boolean curios = ModList.get().isLoaded("curios");


    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isLoad(String name) {
        return ModList.get().isLoaded(name);
    }

    public static Ingredient getDataIngredient(Mods modid, String name) {
        return new SimpleDatagenIngredient(modid, name).toVanilla();
    }

    public static Ingredient getStackIngredient(ItemStack stack) {
        return new StackIngredient(stack).toVanilla();
    }

    public static Item getItem(String modid, String name) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(modid, name)).orElseThrow();
    }

    public static <T> T checkExtraSlots(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
        if(curios) {
            AtomicReference<List<SlotResult>> s = new AtomicReference<>(new ArrayList<>());
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                s.set(curiosInventory.findCurios(is));
            });
            if(!s.get().isEmpty())return map.apply(s.get().get(0).stack());
        }
        return def;
    }
}
