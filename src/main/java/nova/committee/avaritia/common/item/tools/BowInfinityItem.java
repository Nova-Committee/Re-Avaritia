package nova.committee.avaritia.common.item.tools;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import nova.committee.avaritia.common.entity.HeavenArrowEntity;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 */
public class BowInfinityItem extends BowItem {
    public BowInfinityItem() {
        super(new Properties()
                .tab(ModTab.TAB)
                .stacksTo(1)
                .fireResistant()
        );
        setRegistryName("infinity_bow");
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 0;
    }


    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 1200;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, level, player, hand, true);
        if (ret != null) return ret;

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity player, int count) {
        var max = getUseDuration(stack);
        var velocity = BowItem.getPowerForTime(max - count);

        ItemStack itemstack = player.getProjectile(stack);

        velocity = velocity < 1.0D ? 1.0f : velocity;
        HeavenArrowEntity arrow = HeavenArrowEntity.create(level, player);
        arrow.setPos(player.getX() - 0.2, player.getEyeY() + 0.1, player.getZ());
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity * 4.0F, 1.0F);
        arrow.setCritArrow(true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        if (!level.isClientSide) {
            level.addFreshEntity(arrow);
            if (player instanceof Player player1) player1.awardStat(Stats.ITEM_USED.get(this));
        }
    }



}
