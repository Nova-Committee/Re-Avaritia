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

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.entity.projectile.*;

import net.minecraft.world.entity.projectile.arrow.Arrow;

import net.minecraft.world.entity.projectile.arrow.SpectralArrow;

import net.minecraft.world.entity.projectile.arrow.ThrownTrident;

import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;

import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.*;

import net.minecraft.world.item.component.ChargedProjectiles;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;

import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


import java.util.function.Consumer;


public class InfinityCrossBowItem extends CrossbowItem implements InitEnchantItem, ISwitchable, IUndamageable, IBowTransform {

    private final InitEnchantment initEnchantment;

    public InfinityCrossBowItem() {
        super(ModItems.properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
        );
        this.initEnchantment = new InitEnchantment(Enchantments.INFINITY, 10);
    }



    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }



    @Override

    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            switchMode(level, player, hand, "infinity_crossbow_multi");
            return InteractionResult.SUCCESS;
        }

        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            performShooting(level, player, hand, stack, 1.0F, 1.0F);
            stack.remove(DataComponents.CHARGED_PROJECTILES);
            return InteractionResult.CONSUME;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }



    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        int useTicks = this.getUseDuration(stack, entity) - timeLeft;
        float charge = (float) useTicks / (float) CrossbowItem.getChargeDuration(stack, entity);
        if (charge < 1.0F || CrossbowItem.isCharged(stack)) {
            return false;
        }

        if (!level.isClientSide() && entity instanceof Player player) {
            loadInfinityProjectile(stack, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        return true;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return CrossbowItem.getChargeDuration(stack, entity) + 3;
    }

    private void loadInfinityProjectile(@NotNull ItemStack crossbow, Player player) {
        ItemStack ammo = findAmmo(player);
        ItemStack projectile = ammo.isEmpty() ? new ItemStack(Items.ARROW) : ammo.copyWithCount(1);
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(ItemStackTemplate.fromNonEmptyStack(projectile)));
    }

    private void performShooting(Level level, Player player, InteractionHand hand, ItemStack crossbow, float velocity, float inaccuracy) {
        if (level.isClientSide()) return;

        ItemStack stack = player.getItemInHand(hand);
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
            player.getCooldowns().addCooldown(stack, 200);
        }else {
            player.getCooldowns().addCooldown(stack, 20);
        }
    }



    //shoot method based on ammo type

    private void shootBasedOnAmmo(Level level, Player player, ItemStack ammo, float angle) {
        if (ammo.is(Items.ARROW)) {
            shootArrow(level, player, 3.0F, 1.0F, angle);
        } else if (ammo.is(Items.ENDER_PEARL)) {
            shootEnderPearl(level, player, angle, ammo);
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

            shootSnowball(level, player, angle, ammo);

        } else if (ammo.is(Items.EGG)) {

            shootEgg(level, player, angle, ammo);

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



    private void shootInfinityArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {

        HeavenArrowEntity arrow = new HeavenArrowEntity(level, player);

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);

        level.addFreshEntity(arrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);



    }



    private void shootArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {

        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);

        level.addFreshEntity(arrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    //閺堫偄濂栭悵宥囧綌

    private void shootEnderPearl(Level level, Player player, float angle, ItemStack ammo) {

        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player, ammo);

        pearl.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(pearl);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



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



    private void shootSpectralArrow(Level level, Player player, float velocity, float inaccuracy, float angle) {

        SpectralArrow arrow = new SpectralArrow(level, player, new ItemStack(Items.SPECTRAL_ARROW), null);

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);

        level.addFreshEntity(arrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    private void shootTippedArrow(Level level, Player player, ItemStack ammo, float velocity, float inaccuracy, float angle) {

        Arrow arrow = new Arrow(level, player, ammo, null);

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);

        level.addFreshEntity(arrow);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    //閻戠喕濮抽悘顐ゎ唲

    private void shootFireworkRocket(Level level, Player player, ItemStack fireworkItem, float velocity, float inaccuracy, float angle) {

        FireworkRocketEntity firework = new FireworkRocketEntity(

                level, fireworkItem, player,

                player.getX(), player.getEyeY(), player.getZ(),

                true

        );



        Vec3 lookVec = player.getLookAngle();

        // 鎼存梻鏁ょ憴鎺戝閸嬪繒些

        if (angle != 0) {

            lookVec = lookVec.yRot((float) Math.toRadians(angle));

        }

        Vec3 motion = lookVec.scale(velocity);



        if (inaccuracy > 0) {

            motion = motion.add(

                    level.getRandom().nextGaussian() * 0.0075F * inaccuracy,

                    level.getRandom().nextGaussian() * 0.0075F * inaccuracy,

                    level.getRandom().nextGaussian() * 0.0075F * inaccuracy

            );

        }



        firework.setDeltaMovement(motion);

        firework.setPos(player.getX(), player.getEyeY(), player.getZ());



        level.addFreshEntity(firework);



        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    private void shootTrident(Level level, Player player, ItemStack trident, float velocity, float inaccuracy, float angle) {

        ThrownTrident tridentEntity = new ThrownTrident(level, player, trident);

        tridentEntity.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, velocity, inaccuracy);

        level.addFreshEntity(tridentEntity);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    //闂嗩亞鎮?
    private void shootSnowball(Level level, Player player, float angle, ItemStack ammo) {

        Snowball snowball = new Snowball(level, player, ammo);

        snowball.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(snowball);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    //妤βゆ巢

    private void shootEgg(Level level, Player player, float angle, ItemStack ammo) {

        ThrownEgg egg = new ThrownEgg(level, player, ammo);

        egg.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(egg);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.EGG_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    //缂佸牊婀滈悵宥囧綌

    private void shootEndestPearl(Level level, Player player, float angle) {

        EndestPearlEntity pearl = new EndestPearlEntity(level, player.getX(), player.getEyeY(), player.getZ());

        pearl.setShooter(player);

        pearl.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(pearl);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }

    //妞嬪骸鑴?
    private void shootWindCharge(Level level, Player player, float angle) {

        WindCharge windCharge = new WindCharge(player, level, player.getX(), player.getEyeY(), player.getZ());

        windCharge.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 1.5F, 1.0F);

        level.addFreshEntity(windCharge);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),

                SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

    }



    @Override

    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantment) {

        return this.initEnchantment.getLevel(enchantment);

    }

}
