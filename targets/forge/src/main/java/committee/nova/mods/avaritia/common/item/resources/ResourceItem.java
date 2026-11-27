package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/1 21:25
 * Version: 1.0
 */
public class ResourceItem extends Item implements ITooltip {
    private final Rarity rarity;
    private final boolean needsTooltip;

    public ResourceItem(Rarity rarity, boolean needsTooltip) {
        this(rarity, needsTooltip, new Properties());
    }

    public ResourceItem(Rarity rarity, boolean needsTooltip, Properties properties) {
        super(properties);
        this.rarity = rarity;
        this.needsTooltip = needsTooltip;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return rarity;
    }

    @Override
    public boolean hasDescTooltip() {
        return needsTooltip;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location, stack);
    }

}
