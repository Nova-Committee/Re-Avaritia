package committee.nova.mods.avaritia.common.item.tools.infinity;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.InfinityThrownTrident;
import committee.nova.mods.avaritia.common.entity.TNTProEntity;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ProjectileItemUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class InfinityTridentItem extends TridentItem implements IUndamageable, ISwitchable {
    public static final List<String> FUNC_MODES = Arrays.asList("infinity_trident_loyalty", "infinity_trident_riptide");
    public static final byte MODE_LOYALTY = 0;
    public static final byte MODE_RIPTIDE = 1;
    private static final float OFFHAND_PROJECTILE_VELOCITY = 2.5F;
    private static final float OFFHAND_PROJECTILE_INACCURACY = 1.0F;

    private static final String CHANNELING_NBT = "Channeling";
    private static final String SHOCKWAVE_NBT = "Shockwave";

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public InfinityTridentItem() {
        super((new Item.Properties())
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 100, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", 100, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public boolean getCurrentChanneling(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(CHANNELING_NBT);
    }

    public void setChanneling(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(CHANNELING_NBT, enabled);
    }

    public boolean getCurrentShockwave(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(SHOCKWAVE_NBT);
    }

    public void setShockwave(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(SHOCKWAVE_NBT, enabled);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 18000;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            int i = this.getUseDuration(itemStack) - timeLeft;
            int currentMode = ISwitchable.getCurrentMode(itemStack, FUNC_MODES);
            if (i >= 10) {
                switch (currentMode) {
                    case MODE_LOYALTY -> {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        if (!tryShootOffhandProjectile(level, player)) {
                            shootTrident(itemStack, level, player,  false);
                        }
                    }
                    case MODE_RIPTIDE -> {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        int riptideLevel = 5;
                        float toRadians = (float) Math.PI / 180F;
                        float yaw = player.getYRot() * toRadians;
                        float pitch = player.getXRot() * toRadians;
                        float xVelocity = -Mth.sin(yaw) * Mth.cos(pitch);
                        float yVelocity = -Mth.sin(pitch);
                        float zVelocity = Mth.cos(yaw) * Mth.cos(pitch);
                        float velocity = Mth.sqrt(xVelocity * xVelocity + yVelocity * yVelocity + zVelocity * zVelocity);
                        float velocityModifier = (0.75F + 0.75F * riptideLevel) / velocity;
                        player.push(xVelocity * velocityModifier, yVelocity * velocityModifier, zVelocity * velocityModifier);
                        player.startAutoSpinAttack(20);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
                        }
                        level.playSound(null, player, SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                }
            }
        }
    }

    private void shootTrident(@NotNull ItemStack itemStack, @NotNull Level level, Player player, boolean noReturn) {
        if (!level.isClientSide) {
            InfinityThrownTrident throwntrident = new InfinityThrownTrident(level, player, itemStack);
            throwntrident.setLoyaltyLevel(noReturn ? 0 : 2);
            throwntrident.setReturnSlot(findSourceSlot(player, itemStack));
            throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
                throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(throwntrident);
            level.playSound(null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(itemStack);
            }
        }
    }

    private int findSourceSlot(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == itemStack) {
                return slot;
            }
        }
        if (ItemStack.matches(inventory.getItem(inventory.selected), itemStack)) {
            return inventory.selected;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.matches(inventory.getItem(slot), itemStack)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean tryShootOffhandProjectile(Level level, Player player) {
        if (level.isClientSide) {
            return false;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (!ProjectileItemUtils.isLaunchableProjectileItem(level, offhandStack)) {
            return false;
        }

        ItemStack ammo = ProjectileItemUtils.copySingle(offhandStack);
        if (ammo.is(Items.ARROW)) {
            shootArrow(level, player, ammo, false);
        } else if (ammo.is(Items.SPECTRAL_ARROW)) {
            shootSpectralArrow(level, player);
        } else if (ammo.is(Items.TIPPED_ARROW)) {
            shootArrow(level, player, ammo, true);
        } else if (ammo.is(Items.FIREWORK_ROCKET)) {
            shootFireworkRocket(level, player, ammo);
        } else if (ammo.getItem() instanceof TridentItem) {
            shootOffhandTrident(level, player, ammo);
        } else if (ammo.is(Items.TNT)) {
            shootTNT(level, player);
        } else if (ammo.is(Items.FIRE_CHARGE)) {
            shootFireball(level, player);
        } else {
            return shootThrowableItemProjectile(level, player, ammo);
        }
        return true;
    }

    private void shootArrow(Level level, Player player, ItemStack ammo, boolean copyEffects) {
        Arrow arrow = new Arrow(level, player);
        if (copyEffects) {
            arrow.setEffectsFromItem(ammo);
        }
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, OFFHAND_PROJECTILE_VELOCITY, OFFHAND_PROJECTILE_INACCURACY);
        level.addFreshEntity(arrow);
        playOffhandSound(level, player, SoundEvents.TRIDENT_THROW);
    }

    private void shootSpectralArrow(Level level, Player player) {
        SpectralArrow arrow = new SpectralArrow(level, player);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, OFFHAND_PROJECTILE_VELOCITY, OFFHAND_PROJECTILE_INACCURACY);
        level.addFreshEntity(arrow);
        playOffhandSound(level, player, SoundEvents.TRIDENT_THROW);
    }

    private void shootFireball(Level level, Player player) {
        SmallFireball fireball = new SmallFireball(level, player,
                player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z);
        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
        fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, OFFHAND_PROJECTILE_INACCURACY);
        level.addFreshEntity(fireball);
        playOffhandSound(level, player, SoundEvents.BLAZE_SHOOT);
    }

    private void shootFireworkRocket(Level level, Player player, ItemStack fireworkItem) {
        FireworkRocketEntity firework = new FireworkRocketEntity(
                level, fireworkItem, player,
                player.getX(), player.getEyeY(), player.getZ(),
                true
        );
        firework.setDeltaMovement(player.getLookAngle().scale(OFFHAND_PROJECTILE_VELOCITY));
        firework.setPos(player.getX(), player.getEyeY(), player.getZ());
        level.addFreshEntity(firework);
        playOffhandSound(level, player, SoundEvents.FIREWORK_ROCKET_SHOOT);
    }

    private void shootOffhandTrident(Level level, Player player, ItemStack trident) {
        ThrownTrident tridentEntity = new ThrownTrident(level, player, trident);
        tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        tridentEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, OFFHAND_PROJECTILE_VELOCITY, OFFHAND_PROJECTILE_INACCURACY);
        level.addFreshEntity(tridentEntity);
        playOffhandSound(level, player, SoundEvents.TRIDENT_THROW);
    }

    private void shootTNT(Level level, Player player) {
        TNTProEntity tnt = new TNTProEntity(level, player.getX(), player.getEyeY(), player.getZ(), player);
        tnt.setDeltaMovement(player.getLookAngle().scale(1.5D));
        level.addFreshEntity(tnt);
        playOffhandSound(level, player, SoundEvents.TNT_PRIMED);
    }

    private boolean shootThrowableItemProjectile(Level level, Player player, ItemStack ammo) {
        var type = ProjectileItemUtils.findThrowableProjectileType(level, ammo);
        if (type == null) {
            return false;
        }

        Entity entity = ProjectileItemUtils.createEntitySafely(type, level);
        if (!(entity instanceof ThrowableItemProjectile projectile)) {
            if (entity != null) {
                entity.discard();
            }
            return false;
        }

        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        projectile.setItem(ammo);
        if (projectile instanceof EndestPearlEntity endestPearl) {
            endestPearl.setShooter(player);
        }
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                getThrowableProjectileXRotOffset(ammo), getThrowableProjectileVelocity(ammo), OFFHAND_PROJECTILE_INACCURACY);
        level.addFreshEntity(projectile);
        playOffhandSound(level, player, getThrowableProjectileSound(ammo));
        return true;
    }

    private float getThrowableProjectileXRotOffset(ItemStack ammo) {
        if (ammo.getItem() instanceof ExperienceBottleItem || ammo.getItem() instanceof ThrowablePotionItem) {
            return -20.0F;
        }
        return 0.0F;
    }

    private float getThrowableProjectileVelocity(ItemStack ammo) {
        if (ammo.getItem() instanceof ThrowablePotionItem) {
            return 0.5F;
        }
        if (ammo.getItem() instanceof ExperienceBottleItem) {
            return 0.7F;
        }
        return 1.5F;
    }

    private SoundEvent getThrowableProjectileSound(ItemStack ammo) {
        if (ammo.getItem() instanceof EnderpearlItem) {
            return SoundEvents.ENDER_PEARL_THROW;
        }
        if (ammo.getItem() instanceof SnowballItem) {
            return SoundEvents.SNOWBALL_THROW;
        }
        if (ammo.getItem() instanceof EggItem) {
            return SoundEvents.EGG_THROW;
        }
        if (ammo.getItem() instanceof ExperienceBottleItem) {
            return SoundEvents.EXPERIENCE_BOTTLE_THROW;
        }
        if (ammo.getItem() instanceof ThrowablePotionItem) {
            return ammo.is(Items.LINGERING_POTION) ? SoundEvents.LINGERING_POTION_THROW : SoundEvents.SPLASH_POTION_THROW;
        }
        return SoundEvents.TRIDENT_THROW;
    }

    private void playOffhandSound(Level level, Player player, SoundEvent sound) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(world, player, hand, FUNC_MODES);
            return InteractionResultHolder.success(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }
}
