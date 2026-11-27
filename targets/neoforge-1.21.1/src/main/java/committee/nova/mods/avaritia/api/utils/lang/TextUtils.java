package committee.nova.mods.avaritia.api.utils.lang;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;

import static net.minecraft.ChatFormatting.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:33
 * Version: 1.0
 */
public class TextUtils {

    private static final ChatFormatting[] fabulousness = new ChatFormatting[]{RED, GOLD, YELLOW, GREEN, AQUA, BLUE, LIGHT_PURPLE};
    private static final ChatFormatting[] sanic = new ChatFormatting[]{BLUE, BLUE, BLUE, BLUE, WHITE, BLUE, WHITE, WHITE, BLUE, WHITE, WHITE, BLUE, RED, WHITE, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY};

    public static String makeFabulous(String input) {
        return ludicrousFormatting(input, fabulousness, 80.0, 1, 1);
    }

    public static String makeSANIC(String input) {
        return ludicrousFormatting(input, sanic, 50.0, 2, 1);
    }

    public static String ludicrousFormatting(String input, ChatFormatting[] colours, double delay, int step, int posstep) {
        StringBuilder sb = new StringBuilder(input.length() * 3);
        if (delay <= 0) {
            delay = 0.001;
        }

        int offset = (int) Math.floor(Util.getMillis() / delay) % colours.length;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            int col = ((i * posstep) + colours.length - offset) % colours.length;

            sb.append(colours[col].toString());
            sb.append(c);
        }

        return sb.toString();
    }


}
