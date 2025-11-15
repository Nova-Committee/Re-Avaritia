package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static committee.nova.mods.avaritia.init.registry.ModArmorMaterial.infinite_armor;

/**
 * InfinityHorseArmorItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 20:04
 */
public class NeutronHorseArmorItem extends AnimalArmorItem implements InitEnchantItem {
    private final InitEnchantment FROST_WALKER;
    private final InitEnchantment ALL_DAMAGE_PROTECTION;
    private final InitEnchantment FALL_PROTECTION;
    public NeutronHorseArmorItem() {
        super(infinite_armor, BodyType.EQUESTRIAN, false,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(ModRarities.RARE)
                        .fireResistant()
                        .setNoRepair());
        this.FROST_WALKER = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);
        this.ALL_DAMAGE_PROTECTION = new InitEnchantment(Enchantments.PROTECTION, 10);
        this.FALL_PROTECTION = new InitEnchantment(Enchantments.FEATHER_FALLING, 4);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.FROST_WALKER)) return 10;
        if (enchantment.is(Enchantments.PROTECTION)) return 10;
        return enchantment.is(Enchantments.FEATHER_FALLING) ? 4 : 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.FROST_WALKER.appendHoverText(context, tooltipComponents);
        this.ALL_DAMAGE_PROTECTION.appendHoverText(context, tooltipComponents);
        this.FALL_PROTECTION.appendHoverText(context, tooltipComponents);
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
        if (infinite_armor.value().layers().contains(layer)) return Res.NEUTRON_HORSE_ARMOR;
        else return super.getArmorTexture(stack, entity, slot, layer, innerModel);
    }
}
