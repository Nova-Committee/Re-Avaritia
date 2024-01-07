package committee.nova.mods.avaritia.common.item.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/8 0:41
 * @Description:
 */

public class TotemInfinityItem extends Item {

    public TotemInfinityItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }


}
