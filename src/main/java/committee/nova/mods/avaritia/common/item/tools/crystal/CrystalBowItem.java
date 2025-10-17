package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrystalBowItem extends BowItem {

    public CrystalBowItem() {
        super(
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant());
    }
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {

            boolean hasInfinity = player.getAbilities().instabuild ||
                    EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;
            ItemStack ammo = player.getProjectile(pStack);


            int chargeTime = this.getUseDuration(pStack) - pTimeLeft;
            chargeTime = ForgeEventFactory.onArrowLoose(pStack, pLevel, player, chargeTime, !ammo.isEmpty() || hasInfinity);
            if (chargeTime < 0) {
                return;
            }

            if (!ammo.isEmpty() || hasInfinity) {

                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }


                float power = getPowerForTime(chargeTime);
                if (power >= 0.1) {

                    boolean isInfinite = player.getAbilities().instabuild || hasInfinity;

                    int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, pStack);

                    int projectileCount = multishotLevel > 0 ? 3 : 1;

                    if (!pLevel.isClientSide) {

                        for (int i = 0; i < projectileCount; i++) {
                            BladeSlashEntity bladeSlash = new BladeSlashEntity(pLevel, player);

                            float speed = BladeSlashEntity.defaultSpeed * (1.0F + power * 2.0F);
                            float inaccuracy = 0.5F - (power * 0.4F);


                            float yawOffset = 0.0F;
                            if (multishotLevel > 0) {

                                yawOffset = (i == 0) ? -10.0F : (i == 1) ? 10.0F : 0.0F;
                            }


                            bladeSlash.shootFromRotation(
                                    player,
                                    player.getXRot(),
                                    player.getYRot() + yawOffset,
                                    0.0F,
                                    speed,
                                    inaccuracy
                            );

                            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, pStack);
                            float damageBoost = (float) powerLevel * 1.2F + (power * 5.0F);
                            bladeSlash.damage += damageBoost;

                            bladeSlash.duration += (int) (power * 20);

                            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, pStack) > 0) {
                                bladeSlash.setSecondsOnFire(100);
                            }

                            pLevel.addFreshEntity(bladeSlash);
                        }


                        pStack.hurtAndBreak(1, player, (user) -> user.broadcastBreakEvent(player.getUsedItemHand()));
                    }

                    pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                            1.0F, 0.8F + (power * 0.4F));

                    if (!isInfinite && !player.getAbilities().instabuild) {
                        ammo.shrink(1);
                        if (ammo.isEmpty()) {
                            player.getInventory().removeItem(ammo);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        stack.enchant(Enchantments.INFINITY_ARROWS,1);
        stack.enchant(Enchantments.MULTISHOT,1);
        super.onCraftedBy(stack, level, player);
    }
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.crystal_bow"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
