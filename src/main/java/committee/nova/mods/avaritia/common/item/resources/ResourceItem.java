package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/1 21:25
 * Version: 1.0
 */
public class ResourceItem extends Item implements ITooltip {
    private final boolean needsTooltip;

    public ResourceItem(Rarity rarity, boolean needsTooltip) {
        this(rarity, needsTooltip, ModItems.properties().rarity(rarity));
    }

    public ResourceItem(Rarity rarity, boolean needsTooltip, Properties properties) {
        super(ModItems.applyId(properties).rarity(rarity));
        this.needsTooltip = needsTooltip;
    }

    @Override
    public boolean hasDescTooltip() {
        return needsTooltip;
    }
}
