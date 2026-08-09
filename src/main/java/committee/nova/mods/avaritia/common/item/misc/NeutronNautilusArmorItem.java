package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEnchants;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static committee.nova.mods.avaritia.init.registry.ModArmorMaterial.infinity_horse_armor;

public class NeutronNautilusArmorItem extends Item implements InitEnchantItem {
    public NeutronNautilusArmorItem() {
        super(ModItems.properties()
                .nautilusArmor(infinity_horse_armor)
                .stacksTo(1)
                .rarity(ModRarities.RARE)
                .fireResistant()
                .setNoCombineRepair());
    }
    private final InitEnchantment ALL_DAMAGE_PROTECTION = new InitEnchantment(Enchantments.PROTECTION, 10);
    private final InitEnchantment FALL_PROTECTION = new InitEnchantment(Enchantments.FEATHER_FALLING, 10);
    @Override
    public boolean supportsEnchantment(@NonNull ItemStack stack, @NonNull Holder<Enchantment> enchantment) {
        return true;
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
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
    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location, stack);
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
         if (enchantmentHolder.is(Enchantments.PROTECTION)) {
            return 10;
        }else if (enchantmentHolder.is(Enchantments.FEATHER_FALLING)) {
            return 10;
        }
        return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NonNull Consumer<Component> builder,
                                @NotNull TooltipFlag isAdvanced) {
        this.ALL_DAMAGE_PROTECTION.appendHoverText(context, builder);
        this.FALL_PROTECTION.appendHoverText(context, builder);
    }
}

