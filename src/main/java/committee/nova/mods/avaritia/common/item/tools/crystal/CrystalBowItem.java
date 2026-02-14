package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.IBowTransform;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import committee.nova.mods.avaritia.common.entity.arrow.NeutronArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

public class CrystalBowItem extends BowItem implements ISwitchable , IBowTransform {

    public CrystalBowItem() {
        super(
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant());
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "blade_slash");
            return InteractionResultHolder.success(stack);
        }
        return super.use(pLevel, player, hand);
    }
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {
            boolean isBladeSlashActive = isActive(pStack, "blade_slash");
            boolean hasInfinity = player.getAbilities().instabuild ||
                    EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;
            ItemStack ammo = player.getProjectile(pStack);

            int chargeTime = this.getUseDuration(pStack) - pTimeLeft;
            chargeTime = ForgeEventFactory.onArrowLoose(pStack, pLevel, player, chargeTime, !ammo.isEmpty() || hasInfinity);
            if (chargeTime < 0) {
                return;
            }

            float power = getPowerForTime(chargeTime);
            if (power >= 0.1) {
                if (!pLevel.isClientSide) {
                    if (isBladeSlashActive) {

                        BladeSlashEntity bladeSlash = new BladeSlashEntity(pLevel, player);

                        float speed = BladeSlashEntity.defaultSpeed * (1.0F + power * 2.0F);
                        float inaccuracy = 0.5F - (power * 0.4F);
                        float yawOffset = 0.0F;

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

                        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                                1.0F, 0.8F + (power * 0.4F));
                    } else {

                        NeutronArrowEntity neutronArrow = new NeutronArrowEntity(ModEntities.NEUTRON_ARROW.get(), pLevel);
                        neutronArrow.setOwner(player);
                        neutronArrow.setPos(player.getX(), player.getEyeY() - 0.1F, player.getZ());

                        float speed = 3.0F;
                        float inaccuracy = 0.0F;
                        float yawOffset = 0.0F;

                        neutronArrow.shootFromRotation(
                                player,
                                player.getXRot(),
                                player.getYRot() + yawOffset,
                                0.0F,
                                speed,
                                inaccuracy
                        );

                        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                                1.0F, 0.8F + (power * 0.4F));

                        pLevel.addFreshEntity(neutronArrow);
                    }

                    pStack.hurtAndBreak(1, player, (user) -> user.broadcastBreakEvent(player.getUsedItemHand()));
                }

                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }


    @Override
    public void onCraftedBy(ItemStack stack, @NotNull Level level, @NotNull Player player) {
        stack.enchant(Enchantments.INFINITY_ARROWS,1);
        stack.enchant(Enchantments.MULTISHOT,1);
        super.onCraftedBy(stack, level, player);
    }
}
