package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.api.iface.IBowTransform;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import committee.nova.mods.avaritia.common.entity.arrow.NeutronArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CrystalBowItem extends BowItem implements ISwitchable, IBowTransform {

    public CrystalBowItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant());
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "blade_slash");
            return InteractionResult.SUCCESS;
        }
        return super.use(pLevel, player, hand);
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {
            InteractionHand hand = player.getUsedItemHand();
            boolean isBladeSlashActive = isActive(pStack, "blade_slash");
            Holder<Enchantment> infinity = pLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY);
            Holder<Enchantment> power = pLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER);
            Holder<Enchantment> flame = pLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FLAME);
            boolean hasInfinity = player.getAbilities().instabuild ||
                    EnchantmentHelper.getItemEnchantmentLevel(infinity, pStack) > 0;

            int chargeTime = this.getUseDuration(pStack, player) - pTimeLeft;
            if (chargeTime < 0) {
                return false;
            }

            float power2 = getPowerForTime(chargeTime);
            if (power2 >= 0.1) {
                if (!pLevel.isClientSide()) {
                    if (isBladeSlashActive) {

                        BladeSlashEntity bladeSlash = new BladeSlashEntity(pLevel, player);

                        float speed = BladeSlashEntity.defaultSpeed * (1.0F + power2 * 2.0F);
                        float inaccuracy = 0.5F - (power2 * 0.4F);
                        float yawOffset = 0.0F;

                        bladeSlash.shootFromRotation(
                                player,
                                player.getXRot(),
                                player.getYRot() + yawOffset,
                                0.0F,
                                speed,
                                inaccuracy
                        );

                        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(power, pStack);
                        float damageBoost = (float) powerLevel * 1.2F + (power2 * 5.0F);
                        bladeSlash.damage += damageBoost;

                        bladeSlash.duration += (int) (power2 * 20);

                        if (EnchantmentHelper.getItemEnchantmentLevel(flame, pStack) > 0) {
                            bladeSlash.igniteForSeconds(100);
                        }

                        pLevel.addFreshEntity(bladeSlash);

                        pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                                1.0F, 0.8F + (power2 * 0.4F));
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
                                1.0F, 0.8F + (power2 * 0.4F));

                        pLevel.addFreshEntity(neutronArrow);
                    }

                    pStack.hurtAndBreak(1, player, hand);
                }

                player.awardStat(Stats.ITEM_USED.get(this));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Player player) {
        Holder<Enchantment> infinity = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY);
        Holder<Enchantment> multishot = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT);
        stack.enchant(infinity, 1);
        stack.enchant(multishot, 1);
        super.onCraftedBy(stack, player);
    }
}
