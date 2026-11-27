package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.common.entity.InfinityThrownTrident;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ProjectileItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class InfinityTridentItem extends TridentItem implements IUndamageable, ISwitchable {
    public static final List<String> FUNC_MODES = Arrays.asList("infinity_trident_loyalty", "infinity_trident_riptide");

    public static final byte MODE_LOYALTY = 0;
    public static final byte MODE_RIPTIDE = 1;

    private static final String CHANNELING_NBT = "Channeling";
    private static final String SHOCKWAVE_NBT = "Shockwave";

    public InfinityTridentItem() {
        super((ModItems.properties())
                .rarity(ModRarities.COSMIC.getValue())
                .stacksTo(1)
                .fireResistant()
                .attributes(createAttributes()));
    }

    public static @NotNull ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 100.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, 100.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    public boolean getCurrentChanneling(ItemStack stack) {
        return ItemUtils.getOrCreateTag(stack).getBoolean(CHANNELING_NBT).orElseThrow();
    }

    public boolean getCurrentShockwave(ItemStack stack) {
        return ItemUtils.getOrCreateTag(stack).getBoolean(SHOCKWAVE_NBT).orElseThrow();
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 18000;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            int i = this.getUseDuration(itemStack, player) - timeLeft;
            int currentMode = ISwitchable.getCurrentMode(itemStack, FUNC_MODES);
            if (i >= 10) {
                switch (currentMode) {
                    case MODE_LOYALTY -> {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        if (!tryShootOffhandProjectile(level, player)) {
                            shootTrident(itemStack, level, player, false);
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
                        player.startAutoSpinAttack(20, 100.0F, itemStack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
                        }
                        Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                                .orElse(SoundEvents.TRIDENT_THROW);
                        level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void shootTrident(@NotNull ItemStack itemStack, @NotNull Level level, Player player, boolean noReturn) {
        if (!level.isClientSide()) {
            InfinityThrownTrident throwntrident = new InfinityThrownTrident(level, player, itemStack, null);
            throwntrident.setLoyaltyLevel(noReturn ? 0 : 2);
            throwntrident.setReturnSlot(findSourceSlot(player, itemStack));
            throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
                throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(throwntrident);
            Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                    .orElse(SoundEvents.TRIDENT_THROW);
            level.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.matches(inventory.getItem(slot), itemStack)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean tryShootOffhandProjectile(Level level, Player player) {
        if (level.isClientSide()) {
            return false;
        }

        ProjectileItemUtils.LaunchProjectile launch =
                ProjectileItemUtils.createLaunchProjectile(level, player, player.getOffhandItem());
        if (launch == null) {
            return false;
        }
        launch.shootFromRotation(player, 0.0F);
        level.addFreshEntity(launch.entity());
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                launch.sound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleMode(world, player, hand, FUNC_MODES);
            return InteractionResult.SUCCESS;
        } else {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }
}
