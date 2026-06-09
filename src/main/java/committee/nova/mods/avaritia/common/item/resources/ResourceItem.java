package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
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


    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

}
