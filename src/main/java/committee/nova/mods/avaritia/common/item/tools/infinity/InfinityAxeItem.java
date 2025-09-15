package committee.nova.mods.avaritia.common.item.tools.infinity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
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
        super(ModToolTiers.INFINITY, 100, -50f, (new Properties())
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant());

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
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            switchMode(pLevel, player, hand, "infinity_axe_range");
            return InteractionResultHolder.success(stack);
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
        if (entity instanceof ServerPlayer livingEntity && !livingEntity.level().isClientSide()) {
            if (livingEntity.isUsingItem() && livingEntity.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK)) {
                ItemStack shieldStack = livingEntity.getUseItem();
                ShieldItem shieldItem = (ShieldItem) shieldStack.getItem();

                livingEntity.stopUsingItem();

                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            livingEntity.getX(),
                            livingEntity.getY() + livingEntity.getBbHeight() / 2,
                            livingEntity.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                if (shieldStack.getDamageValue() >= shieldStack.getMaxDamage() - 1) {
                    livingEntity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                } else {
                    shieldStack.setDamageValue(shieldStack.getMaxDamage() - 1);
                }
                livingEntity.getCooldowns().addCooldown(shieldItem, 1200);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getTier().getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getTier().getSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

}
