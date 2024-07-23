package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/1 21:25
 * Version: 1.0
 */
public class ResourceItem extends Item {
    private final Rarity rarity;
    private final String name;
    private final boolean needsTooltip;

    public ResourceItem(Rarity rarity, String registryName, boolean needsTooltip){
        this(rarity, registryName, needsTooltip, new Properties());
    }

    public ResourceItem(Rarity rarity, String registryName, boolean needsTooltip, Properties properties) {
        super(properties);
        this.rarity = rarity;
        this.name = registryName;
        this.needsTooltip = needsTooltip;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return rarity;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> components, @NotNull TooltipFlag p_41424_) {
        if (needsTooltip)
            components.add(Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + I18n.get("tooltip." + name + ".desc")));
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
