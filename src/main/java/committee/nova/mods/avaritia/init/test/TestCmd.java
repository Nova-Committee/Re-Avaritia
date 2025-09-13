package committee.nova.mods.avaritia.init.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.mods.avaritia.util.KubeJsUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * @author: cnlimiter
 */
public class TestCmd {
    public static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

        //KubeJsUtils.exportShapeTableJSRecipe(3, 1, true, null);
        return Command.SINGLE_SUCCESS;
    }
}
