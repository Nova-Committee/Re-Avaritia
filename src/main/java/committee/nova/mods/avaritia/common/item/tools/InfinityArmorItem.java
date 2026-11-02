package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.util.lang.TextUtils;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/21 15:12
 * Version: 1.0
 */
public class InfinityArmorItem extends Item implements IUndamageable {
    private final ArmorType armorType;

    public InfinityArmorItem(ArmorType pSlot, Item.Properties properties) {
        super(ModItems.applyId(properties)
                .humanoidArmor(ModArmorMaterial.infinity_armor, pSlot)
                .rarity(ModRarities.COSMIC)
                .fireResistant()
                .stacksTo(1)
        );
        this.armorType = pSlot;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        if (armorType.getSlot() == EquipmentSlot.HEAD) {
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("effect.minecraft.night_vision")));
        }
        if (armorType.getSlot() == EquipmentSlot.CHEST) {
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.flying_speed")));
        }
        if (armorType.getSlot() == EquipmentSlot.LEGS) {
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.walking_speed")));
        }
        if (armorType.getSlot() == EquipmentSlot.FEET) {
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.movement_speed")));
        }
        super.appendHoverText(stack, context, display, tooltip, isAdvanced);
    }
}
