package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:07
 * Version: 1.0
 * from <a href="https://github.com/yuoft/Endless/blob/master/src/main/java/com/yuo/endless/Items/Tool/InfinityCrossBow.java">...</a>
 */
public class InfinityCrossBowItem extends CrossbowItem implements ITooltip {
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;

    public InfinityCrossBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
        );
    }

    /**
     * 获取弹药速度
     *
     * @return 速度
     */
    private static float getShootingPower() {
        return 5F;//增加默认速度
    }

    public static void performShooting(@NotNull Level worldIn, LivingEntity shooter, @NotNull InteractionHand pUsedHand, ItemStack stack, int counts, float velocityIn, float inaccuracyIn) {
        if (shooter instanceof Player player && EventHooks.onArrowLoose(stack, shooter.level(), player, 1, true) < 0) return;
        float[] afloat = getShotPitches(shooter.getRandom()); //声音大小
        boolean flag = shooter instanceof Player player && player.getAbilities().instabuild;
        for (int i = 0; i < counts; ++i) {
            shootProjectile(worldIn, shooter, pUsedHand, stack, afloat[i < 10 ? 1 : 2], flag, velocityIn, inaccuracyIn, getArrowAngle(i, i < 10));
        }
        onCrossbowShot(worldIn, shooter, stack);
    }

    /**
     * 获取箭矢散射角度
     *
     * @param i    箭矢序数
     * @param flag 是否偏向左边
     * @return 角度
     */
    private static float getArrowAngle(int i, boolean flag) {
        return flag ? -(i * 12f - 24) : (i - 10) * 4.5f;
    }

    private static float[] getShotPitches(RandomSource rand) {
        boolean flag = rand.nextBoolean();
        return new float[]{1.0F, getRandomSoundPitch(flag, rand), getRandomSoundPitch(!flag, rand)};
    }

    private static float getRandomSoundPitch(boolean flagIn, RandomSource rand) {
        float f = flagIn ? 0.63F : 0.43F;
        return 1.0F / (rand.nextFloat() * 0.5F + 1.8F) + f;
    }


    //修改玩家状态
    private static void onCrossbowShot(Level worldIn, LivingEntity shooter, ItemStack stack) {
        if (shooter instanceof ServerPlayer serverPlayer) {
            if (!worldIn.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, stack);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
    }

    //射出一发
    private static void shootProjectile(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pCrossbowStack, float pSoundPitch, boolean pIsCreativeMode, float pVelocity, float pInaccuracy, float pProjectileAngle) {
        if (!pLevel.isClientSide) {
            AbstractArrow projectile = getArrow(pShooter);
            if (pIsCreativeMode || pProjectileAngle != 0.0F) {
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
//            if (pShooter instanceof CrossbowAttackMob crossbowattackmob) {
//                crossbowattackmob.shootCrossbowProjectile(Objects.requireNonNull(crossbowattackmob.getTarget()), pCrossbowStack, projectile, pProjectileAngle);
//            } else {
                Vec3 vec31 = pShooter.getUpVector(1.0F);
                Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(pProjectileAngle * ((float) Math.PI / 180F), vec31.x, vec31.y, vec31.z);
                Vec3 vec3 = pShooter.getViewVector(1.0F);
                Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
                projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), pVelocity, pInaccuracy);
            //}
            pLevel.addFreshEntity(projectile);
            pLevel.playSound(null, pShooter.getX(), pShooter.getY(), pShooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pSoundPitch);
        }
    }

    //创建投掷物实体
    private static AbstractArrow getArrow(LivingEntity shooter) {
        AbstractArrow arrow = new HeavenSubArrowEntity(shooter);
        if (shooter instanceof Player) {
            arrow.setCritArrow(true);
        } //暴击粒子
        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        //arrow.shotFromCrossbow(true);
        //arrow.setPierceLevel((byte) 5);
        return arrow;
    }

    public static int getChargeDuration() {
        return 25 - 5 * 3;//快速装填3
    }

    //使用程度
    private static float getPowerForTime(int useTime) {
        float f = (float) useTime / (float) getChargeDuration();
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    public static boolean isCharged(ItemStack pCrossbowStack) {
        ChargedProjectiles chargedprojectiles = pCrossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        return !chargedprojectiles.isEmpty();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 99;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.CROSSBOW;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return getChargeDuration() + 3; //使用时间
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (isCharged(itemstack)) { //弹药已装填 发射5发
            performShooting(level, player, hand, itemstack, 5, getShootingPower(), 1.0F);
            return InteractionResultHolder.consume(itemstack);
        }
        else if (!isCharged(itemstack)) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void onUseTick(Level pLevel, @NotNull LivingEntity pLivingEntity, @NotNull ItemStack pStack, int pCount) {
        if (!pLevel.isClientSide) {
            SoundEvent soundevent = SoundEvents.CROSSBOW_LOADING_START.value();
            SoundEvent soundevent1 = SoundEvents.CROSSBOW_LOADING_MIDDLE.value();
            float f = (float)(pStack.getUseDuration(pLivingEntity) - pCount) / getChargeDuration();
            if (f < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                pLevel.playSound(null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent1, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level pLevel, @NotNull LivingEntity entity, int pTimeLeft) {
        int i = this.getUseDuration(stack, entity) - pTimeLeft;
        float f = getPowerForTime(i);
        if (f >= 1.0F && !isCharged(stack)) {
            //setCharged(stack, true);
            SoundSource soundcategory = entity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundcategory, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }
}
