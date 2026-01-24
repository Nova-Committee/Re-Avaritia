package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.api.iface.transform.IBowTransform;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class InfinityCrossBowItem extends CrossbowItem implements InitEnchantItem, ISwitchable, IUndamageable, IBowTransform {
    private final InitEnchantment initEnchantment;

    public InfinityCrossBowItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
        );
        this.initEnchantment = new InitEnchantment(Enchantments.INFINITY, 10);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            switchMode(level, player, hand, "infinity_crossbow_multi");
            return InteractionResultHolder.success(stack);
        }

        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            performShooting(level, player, hand, stack, 1.0F, 1.0F);
            stack.remove(DataComponents.CHARGED_PROJECTILES);
            return InteractionResultHolder.consume(stack);
        } else {
            if (!level.isClientSide) {
                ItemStack ammo = findAmmo(player);

                if (!ammo.isEmpty()) {
                    stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(ammo));
                } else {
                    stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
                }
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 10;
    }


    private void performShooting(Level level, Player player, InteractionHand hand, ItemStack crossbow, float velocity, float inaccuracy) {
        if (level.isClientSide) return;

        ItemStack ammo = findAmmo(player);
        boolean isMulti = isActive(crossbow, "infinity_crossbow_multi");
        int projectileCount = isMulti ? 5 : 1;
        float[] angles = isMulti ? new float[]{-20.0F, -10.0F, 0.0F, 10.0F, 20.0F} : new float[]{0.0F};

        for (int i = 0; i < projectileCount; i++) {
            float angle = angles[Math.min(i, angles.length - 1)];
            if (!ammo.isEmpty()) {
                shootBasedOnAmmo(level, player, ammo, angle);
            } else {
                shootInfinityArrow(level, player, 3.0F, 1.0F, angle);
            }
        }
        if (isMulti) {
            player.getCooldowns().addCooldown(stack.getItem(), 200);
        }else {
            player.getCooldowns().addCooldown(stack.getItem(), 20);
        }
    }

    //在这里添加方法到发射,默认发射天堂箭
    private void shootBasedOnAmmo(Level level, Player player, ItemStack ammo, float angle) {
        if (ammo.is(Items.ARROW)) {
            shootArrow(level, player, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.ENDER_PEARL)) {
            shootEnderPearl(level, player, angle);
        } else if (ammo.is(Items.FIRE_CHARGE)) {
            shootFireball(level, player, angle);
        } else if (ammo.is(Items.SPECTRAL_ARROW)) {
            shootSpectralArrow(level, player, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.TIPPED_ARROW)) {
            shootTippedArrow(level, player, ammo, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.FIREWORK_ROCKET)) {
            shootFireworkRocket(level, player, ammo, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.TRIDENT)) {
            shootTrident(level, player, ammo, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.SNOWBALL)) {
            shootSnowball(level, player, angle);
        } else if (ammo.is(Items.EGG)) {
            shootEgg(level, player, angle);
        } else if (ammo.is(ModItems.endest_pearl.get())) {
            shootEndestPearl(level, player, angle);
        } else if (ammo.is(Items.WIND_CHARGE)) {
            shootWindCharge(level, player, angle);
        } else {
            shootInfinityArrow(level, player, 3.0F, 1.0F, angle);
        }
    }

    private ItemStack findAmmo(Player player) {
        ItemStack offhandItem = player.getOffhandItem();
        if (isAmmo(offhandItem)) {
            return offhandItem;
        }
        return ItemStack.EMPTY;
    }

    private boolean isAmmo(ItemStack stack) {
        return stack.is(Items.ARROW) ||
                stack.is(Items.ENDER_PEARL) ||
                stack.is(Items.FIRE_CHARGE) ||
                stack.is(Items.SPECTRAL_ARROW) ||
                stack.is(Items.TIPPED_ARROW) ||
                stack.is(Items.FIREWORK_ROCKET) ||
                stack.is(Items.TRIDENT) ||
                stack.is(Items.SNOWBALL) ||
                stack.is(Items.EGG) ||
                stack.is(ModItems.endest_pearl.get()) ||
                stack.is(Items.TNT) ||
                stack.is(Items.WIND_CHARGE);
    }

    //天堂箭
    private void shootInfinityArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {
        HeavenArrowEntity arrow = new HeavenArrowEntity(level, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

    }

    //箭
    private void shootArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {
        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //末影珍珠
    private void shootEnderPearl(Level level, Player player, float angle) {
        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);
        pearl.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(pearl);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //烈焰弹
    private void shootFireball(Level level, Player player, float angle) {
        Vec3 lookAngle = player.getLookAngle();
        if (angle != 0) {
            lookAngle = lookAngle.yRot((float) Math.toRadians(angle));
        }
        SmallFireball fireball = new SmallFireball(level, player, lookAngle);
        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
        fireball.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.0F, 1.0F);
        level.addFreshEntity(fireball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //光灵箭
    private void shootSpectralArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {
        SpectralArrow arrow = new SpectralArrow(level, player, new ItemStack(Items.SPECTRAL_ARROW), null);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //药水箭
    private void shootTippedArrow(Level level, Player player, ItemStack ammo, float velocity, float inaccuracy, float angle) {
        Arrow arrow = new Arrow(level, player, ammo, null);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //烟花火箭
    private void shootFireworkRocket(Level level, Player player, ItemStack fireworkItem, float velocity, float inaccuracy, float angle) {
        FireworkRocketEntity firework = new FireworkRocketEntity(
                level, fireworkItem, player,
                player.getX(), player.getEyeY(), player.getZ(),
                true
        );

        Vec3 lookVec = player.getLookAngle();
        // 应用角度偏移
        if (angle != 0) {
            lookVec = lookVec.yRot((float) Math.toRadians(angle));
        }
        Vec3 motion = lookVec.scale(velocity);

        if (inaccuracy > 0) {
            motion = motion.add(
                    level.random.nextGaussian() * 0.0075F * inaccuracy,
                    level.random.nextGaussian() * 0.0075F * inaccuracy,
                    level.random.nextGaussian() * 0.0075F * inaccuracy
            );
        }

        firework.setDeltaMovement(motion);
        firework.setPos(player.getX(), player.getEyeY(), player.getZ());

        level.addFreshEntity(firework);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //三叉戟
    private void shootTrident(Level level, Player player, ItemStack trident, float velocity, float inaccuracy, float angle) {
        ThrownTrident tridentEntity = new ThrownTrident(level, player, trident);
        tridentEntity.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(tridentEntity);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //雪球
    private void shootSnowball(Level level, Player player, float angle) {
        Snowball snowball = new Snowball(level, player);
        snowball.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(snowball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //鸡蛋
    private void shootEgg(Level level, Player player, float angle) {
        ThrownEgg egg = new ThrownEgg(level, player);
        egg.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(egg);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    //终望珍珠
    private void shootEndestPearl(Level level, Player player, float angle) {
        EndestPearlEntity pearl = new EndestPearlEntity(level, player.getX(), player.getEyeY(), player.getZ());
        pearl.setShooter(player);
        pearl.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(pearl);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    //风弹
    private void shootWindCharge(Level level, Player player, float angle) {
        WindCharge windCharge = new WindCharge(player, level, player.getX(), player.getEyeY(), player.getZ());
        windCharge.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(windCharge);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.initEnchantment.getLevel(enchantment);
    }
}
