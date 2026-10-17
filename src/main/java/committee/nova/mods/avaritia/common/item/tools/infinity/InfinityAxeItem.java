package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import static committee.nova.mods.avaritia.util.ToolUtils.canHarvest;
import static committee.nova.mods.avaritia.util.ToolUtils.destroyTree;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/5/15 17:11
 * Version: 1.0
 */
public class InfinityAxeItem extends Item implements ISwitchable, IUndamageable {
    private static final ToolMaterial INFINITY_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 9999, 100.0F, 100.0F, 9999,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("avaritia", "infinity_tool_materials")));

    public InfinityAxeItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant()
                .axe(INFINITY_MATERIAL, 0, INFINITY_MATERIAL.speed()));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "infinity_axe_range");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (level instanceof ServerLevel serverLevel && isActive(stack, "infinity_axe_range") && canHarvest(pos, serverLevel) && miningEntity instanceof ServerPlayer player) {
            destroyTree(player, serverLevel, pos, state);
        }
        return false;
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, @NotNull Entity entity) {
        if (entity instanceof ServerPlayer livingEntity) {
            Level level = livingEntity.level();

            if (!level.isClientSide()) {
                if (livingEntity.isUsingItem() && livingEntity.getUseItem().getItem() instanceof ShieldItem) {
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
                    livingEntity.getCooldowns().addCooldown(shieldStack, 1200);
                }
            }
        }
        return false;
    }
}
