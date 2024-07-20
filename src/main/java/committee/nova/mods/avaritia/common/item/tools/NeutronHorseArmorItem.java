package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * InfinityHorseArmorItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 20:04
 */
public class NeutronHorseArmorItem extends HorseArmorItem {
    private static final String TEX_PATH = "textures/item/armor/horse/neutron_horse_armor.png";

    public NeutronHorseArmorItem(Properties pProperties) {
        super(Integer.MAX_VALUE, new ResourceLocation(Static.MOD_ID, TEX_PATH), pProperties);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> components, @NotNull TooltipFlag p_41424_) {
        components.add(Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + I18n.get("tooltip.neutron_horse_armor.desc")));
    }

}
