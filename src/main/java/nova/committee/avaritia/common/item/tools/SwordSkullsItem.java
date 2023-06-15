package nova.committee.avaritia.common.item.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.registry.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class SwordSkullsItem extends SwordItem {
    public SwordSkullsItem() {
        super(Tier.SKULL_SWORD, 0, -2.4f, (new Properties())
                .stacksTo(1)
                .fireResistant());

    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return Rarity.EPIC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + I18n.get("tooltip.skullfire_sword.desc")));
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
