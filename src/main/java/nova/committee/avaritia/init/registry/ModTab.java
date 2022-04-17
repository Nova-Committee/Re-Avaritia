package nova.committee.avaritia.init.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModTab extends CreativeModeTab {
    public static CreativeModeTab TAB = new ModTab();

    public ModTab() {
        super("tab.Infinity");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModItems.pick_axe);
    }
}
