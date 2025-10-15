package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;


public class CrystalBowItem extends BowItem {
    public CrystalBowItem(String name) {
        super(new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
    }
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {

            int chargeTime = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;

            chargeTime = EventHooks.onArrowLoose(pStack, pLevel, player, chargeTime, true);
            if (chargeTime < 0) {
                return;
            }

            float power = getPowerForTime(chargeTime);
            if (power >= 0.1) {
                int projectileCount = 1;

                if (!pLevel.isClientSide) {
                    for (int i = 0; i < projectileCount; i++) {
                        BladeSlashEntity bladeSlash = new BladeSlashEntity(pLevel, player);

                        float speed = BladeSlashEntity.defaultSpeed * (1.0F + power * 2.0F);

                        float inaccuracy = 0.0F;

                        float yawOffset = 0.0F;

                        bladeSlash.shootFromRotation(
                                player,
                                player.getXRot(),
                                player.getYRot() + yawOffset,
                                0.0F,
                                speed,
                                inaccuracy
                        );

                        float damageBoost = power * 5.0F;
                        bladeSlash.damage += damageBoost;

                        bladeSlash.duration += (int) (power * 20);

                        pLevel.addFreshEntity(bladeSlash);
                    }

                    pStack.hurtAndBreak(1, player, getSlotForHand(player.getUsedItemHand()));
                }

                pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                        1.0F, 0.8F + (power * 0.4F));


                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }


}
