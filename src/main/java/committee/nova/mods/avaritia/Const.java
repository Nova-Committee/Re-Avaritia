package committee.nova.mods.avaritia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.api.util.data.RawValue;
import committee.nova.mods.avaritia.init.compat.trinkets.TrinketsIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 11:37
 * Version: 1.0
 */
public class Const {
    public static final String MOD_ID = "avaritia";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    public static final GameProfile AVARITIA_FAKE_PLAYER = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");


    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static boolean isLoad(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    public static Ingredient getIngredient(String modid, String name) {
        return Ingredient.fromValues(Stream.of(new RawValue(new ResourceLocation(modid, name))));
    }

    public static Item getItem(String modid, String name) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(modid, name));
    }

    public static ResourceLocation getItemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    /** Equipped optional accessories retain precedence over the native inventory. */
    public static <T> T findInventoryItem(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
        ItemStack equipped = TrinketsIntegration.find(player, is);
        if (!equipped.isEmpty()) return map.apply(equipped);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && is.test(stack)) return map.apply(stack);
        }
        return def;
    }
}
