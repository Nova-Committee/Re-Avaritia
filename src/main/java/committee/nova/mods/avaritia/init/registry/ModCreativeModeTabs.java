package committee.nova.mods.avaritia.init.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModCreativeModeTabs extends CreativeModeTab{
    public static CreativeModeTab TAB = new ModCreativeModeTabs();

    public ModCreativeModeTabs() {
        super("tab.Infinity");
    }

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(ModItems.infinity_pickaxe.get());
    }


}
