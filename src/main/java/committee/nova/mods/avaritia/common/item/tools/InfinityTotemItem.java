package committee.nova.mods.avaritia.common.item.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/8 0:41
 * @Description:
 */

public class InfinityTotemItem extends Item {

    public InfinityTotemItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }
    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

}
