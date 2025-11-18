package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.init.registry.modes.InfinityMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static committee.nova.mods.avaritia.util.ToolUtils.canHarvest;
import static committee.nova.mods.avaritia.util.ToolUtils.destroyTree;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 17:11
 * Version: 1.0
 */
public class InfinityAxeItem extends AxeItem implements ISwitchable, IUndamageable {

    public InfinityAxeItem() {
        super(ModToolTiers.INFINITY,
                new Properties()
                        .component(ModDataComponents.INFINITY_MODE, InfinityMode.DEFAULT)
                        .rarity(ModRarities.COSMIC.getValue())
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.INFINITY, 0, ModToolTiers.INFINITY.getSpeed()))
        );

    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "infinity_axe_range");
            return InteractionResultHolder.success(itemstack);
        }
        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (level instanceof ServerLevel serverLevel && isActive(stack, "infinity_axe_range") && canHarvest(pos, serverLevel) && miningEntity instanceof ServerPlayer player) {
            destroyTree(player, serverLevel, pos, state);
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof ServerPlayer livingEntity) {
            Level level = livingEntity.level();

            if (!level.isClientSide()) {
                if (livingEntity.isUsingItem() && livingEntity.getUseItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                    ItemStack shieldStack = livingEntity.getUseItem();
                    ShieldItem shieldItem = (ShieldItem) shieldStack.getItem();
                    boolean isInfinityShield = shieldStack.is(ModItems.infinity_shield.get());

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                livingEntity.getX(),
                                livingEntity.getY() + livingEntity.getBbHeight() / 2,
                                livingEntity.getZ(),
                                1, 0.0D, 0.0D, 0.0D, 0.0D);

                        serverLevel.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                                SoundEvents.GENERIC_EXPLODE,
                                SoundSource.BLOCKS,
                                1.0F, 1.0F);
                    }

                    // 如果是无尽盾，只产生粒子效果
                    if (isInfinityShield) {
                        return true;
                    }

                    livingEntity.stopUsingItem();

                    if (shieldStack.getDamageValue() >= shieldStack.getMaxDamage() - 1) {
                        livingEntity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    } else {
                        shieldStack.setDamageValue(shieldStack.getMaxDamage() - 1);
                    }
                    livingEntity.getCooldowns().addCooldown(shieldItem, 1200);
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
