package committee.nova.mods.avaritia.util;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.ChatFormatting;

import java.util.Locale;

import static io.github.fabricators_of_create.porting_lib.asm.ASMUtils.mapC;

/**
 * ASMUtil
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午1:55
 */
public class ASMUtil implements Runnable {

    @Override
    public void run() {
        extendEnums();
    }

    private static void extendEnums() {
       ClassTinkerers.enumBuilder(mapC("class_1814"), "L" + mapC("class_124") + ";")  // Rarity // ChatFormatting
                .addEnum("COSMIC", () -> new Object[] { ChatFormatting.RED })
                .build();
    }

    public static String prefix(String name) {
        return "avaritia:" + name.toLowerCase(Locale.ROOT);
    }
}
