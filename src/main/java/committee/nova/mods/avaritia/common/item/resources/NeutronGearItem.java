package committee.nova.mods.avaritia.common.item.resources;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * NeutronGearItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/14 下午12:15
 */
public class NeutronGearItem extends ResourceItem{
    public NeutronGearItem() {
        super(Rarity.RARE, "neutron_gear", true);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        return super.useOn(pContext);
    }
}
