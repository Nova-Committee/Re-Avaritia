package nova.committee.avaritia.common.item.tools;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.init.registry.ModTab;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class BowInfinityItem extends BowItem {
    public BowInfinityItem() {
        super(new Properties()
                .tab(ModTab.TAB)
                .durability(9999)
        );
        setRegistryName("infinity_bow");
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }


}
