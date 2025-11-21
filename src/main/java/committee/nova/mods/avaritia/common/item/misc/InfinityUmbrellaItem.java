package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.transform.IToolTransform;
import committee.nova.mods.avaritia.common.entity.RainProEntity;
import committee.nova.mods.avaritia.common.entity.StormProEntity;
import committee.nova.mods.avaritia.common.entity.SunProEntity;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter, cu6
 * @CreateTime: 2025/08/23
 * @Description: Now, We Did it,Four Modes
 */
public class InfinityUmbrellaItem extends ResourceItem implements ISwitchable, IToolTransform {

    public static final List<String> MODES = Arrays.asList("infinity_umbrella_normal", "infinity_umbrella_sun", "infinity_umbrella_rain", "infinity_umbrella_storm");

    private static final int MODE_NORMAL = 0;
    private static final int MODE_SUN = 1;
    private static final int MODE_RAIN = 2;
    private static final int MODE_STORM = 3;

    private static final int MIN_DURATION = 10 * 60 * 20;
    private static final int MAX_DURATION = 20 * 60 * 20;

    public InfinityUmbrellaItem() {
        super(ModRarities.COSMIC.getValue(),false, new Properties().stacksTo(1));
    }

    private int getRandomDuration(Level level) {
        return MIN_DURATION + level.random.nextInt(MAX_DURATION - MIN_DURATION + 1);
    }

    private void onUse(Level level, Player player, ItemStack stack, @NotNull InteractionHand hand) {

        if (!level.isClientSide) {
            float pitch = player.getXRot();

            int currentMode = ISwitchable.getCurrentMode(stack, MODES);
            int duration = getRandomDuration(level);
            switch (currentMode) {
                case MODE_NORMAL:
                    //0
                    break;
                case MODE_SUN:
                    SunProEntity sunProEntity = ModEntities.SUN_PRO.get().create(level);
                    if (pitch <= -85.0F) {
                        if (level.getLevelData().isRaining() || !level.getLevelData().isThundering()) {
                            level.getLevelData().setRaining(false);
                        }
                    } else if (sunProEntity != null) {
                        sunProEntity.setOwner(player);
                        sunProEntity.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                        sunProEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                        level.addFreshEntity(sunProEntity);
                        level.playSound(player, player.getOnPos(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
                    }

                    break;
                case MODE_RAIN:
                    RainProEntity rainProEntity = ModEntities.RAIN_PRO.get().create(level);
                    if (pitch <= -85.0F && level instanceof ServerLevel server) {
                        level.getLevelData().setRaining(true);
                        server.setWeatherParameters(0, duration, true, false);
                    } else if (rainProEntity != null) {
                        rainProEntity.setOwner(player);
                        rainProEntity.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                        rainProEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                        level.addFreshEntity(rainProEntity);

                    }

                    break;
                case MODE_STORM:
                    StormProEntity stormProEntity = ModEntities.STORM_PRO.get().create(level);
                    if (pitch <= -85.0F && level instanceof ServerLevel server) {
                        level.getLevelData().setRaining(true);
                        server.setWeatherParameters(0, duration, true, true);

                    } else if (stormProEntity != null) {
                        stormProEntity.setOwner(player);
                        stormProEntity.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                        stormProEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                        level.addFreshEntity(stormProEntity);

                    }

                    break;
            }
            player.swing(hand, true);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            cycleMode(world, player, hand, MODES);
            return InteractionResultHolder.success(stack);
        }
        onUse(world, player, stack, hand);
        return super.use(world, player, hand);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide && pEntity instanceof Player player) {
            boolean isInEitherHand = pIsSelected || player.getOffhandItem() == pStack;
            var effect = new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0, false, false);
            if (isInEitherHand) {
                player.addEffect(effect);
            } else {
                player.removeEffect(effect.getEffect());
            }
        }
    }
}