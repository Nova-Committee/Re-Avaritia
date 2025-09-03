package committee.nova.mods.avaritia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.api.utils.data.RawValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
import java.util.stream.Stream;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:37
 * Version: 1.0
 */
public class Const {
    public static final String MOD_ID = "avaritia";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    public static final GameProfile AVARITIA_FAKE_PLAYER = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
    public static final boolean curios = FabricLoader.getInstance().isModLoaded("curios");


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

    public static <T> T checkExtraSlots(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
        if (curios) {
            AtomicReference<List<SlotResult>> s = new AtomicReference<>(new ArrayList<>());
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                s.set(curiosInventory.findCurios(is));
            });
            if (!s.get().isEmpty()) return map.apply(s.get().get(0).stack());
        }
        return def;
    }
}
