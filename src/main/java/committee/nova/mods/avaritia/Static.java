package committee.nova.mods.avaritia;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:37
 * Version: 1.0
 */
public class Static {
    public static final String MOD_ID = "avaritia";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static final GameProfile avaritiaFakePlayer = new GameProfile(UUID.fromString("32283731-bbef-487c-bb69-c7e32f84ed27"), "[Avaritia]");
    public static final ResourceLocation ICON_ATLAS_INFO = rl("cosmic_icons");
    public static boolean isLoad(String name) {
        return ModList.get().isLoaded(name);
    }

    public static Item getItem(String modid, String name) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name));
    }

}
