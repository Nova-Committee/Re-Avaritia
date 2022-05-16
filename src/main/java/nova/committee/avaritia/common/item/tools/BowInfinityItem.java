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
                .tab(ModTab.TAB).stacksTo(1)
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
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        var world = player.level;
        var max = getUseDuration(stack);
        var velocity = BowItem.getPowerForTime(max - count);

        if (!world.isClientSide) {
            velocity = velocity < 1.0D ? 1.0f : velocity;
            HeavenArrowEntity arrow = HeavenArrowEntity.create(world, player);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity * 3.0F, 1.0F);
            arrow.setCritArrow(true);

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

            world.addFreshEntity(arrow);
            if (player instanceof Player player1) player1.awardStat(Stats.ITEM_USED.get(this));
        }
    }
}
