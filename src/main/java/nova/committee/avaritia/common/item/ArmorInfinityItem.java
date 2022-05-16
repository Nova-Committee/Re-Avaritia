package nova.committee.avaritia.common.item;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.ModArmorMaterial;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;
import nova.committee.avaritia.util.lang.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:12
 * Version: 1.0
 */
public class ArmorInfinityItem extends ArmorItem {
    public ArmorInfinityItem(EquipmentSlot pSlot) {
        super(
                ModArmorMaterial.infinite_armor,
                pSlot,
                new Properties()
                        .tab(ModTab.TAB)
                        .fireResistant()
                        .stacksTo(1)
        );
    }


    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return true;
    }


    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (slot == EquipmentSlot.HEAD) {
            player.setAirSupply(300);
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20f);
            MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
            if (nv == null) {
                nv = new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false);
                player.addEffect(nv);
            }
            nv.duration = 300;
        } else if (slot == EquipmentSlot.CHEST) {
            player.getAbilities().mayfly = true;
            List<MobEffectInstance> effects = Lists.newArrayList(player.getActiveEffects());
            for (MobEffectInstance potion : Collections2.filter(effects, potion -> !potion.getEffect().isBeneficial())) {
//                if (ModHelper.isHoldingCleaver(player) && potion.getPotion().equals(MobEffects.MINING_FATIGUE)) {
//                    continue;
//                }
                player.removeEffect(potion.getEffect());
            }
        } else if (slot == EquipmentSlot.LEGS) {
            if (player.fireImmune()) {
                player.clearFire();
            }
        }
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return ModItems.COSMIC_RARITY;
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        if (slot == EquipmentSlot.FEET) {
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtil.makeSANIC("SANIC") + ChatFormatting.RESET + "" + ChatFormatting.BLUE + "% Speed"));
        }
        super.appendHoverText(pStack, pLevel, tooltip, pIsAdvanced);
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }


}
