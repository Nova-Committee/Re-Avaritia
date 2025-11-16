package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.item.mode.IItemMode;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.init.registry.modes.InfinityMode;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 16:33
 * Version: 1.0
 */
public class InfinityShovelItem extends ShovelItem implements IItemMode<InfinityMode> {

    public InfinityShovelItem() {
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
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        if (getMode(stack).equals(InfinityMode.RANGE)) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        var itemstack = player.getItemInHand(hand);
        if (player.isCrouching() && !pLevel.isClientSide) {
            changeMode(player, itemstack, hand);
            return InteractionResultHolder.success(itemstack);
        }

        //右键发射发射终望珍珠,冷却20s
        if (getMode(itemstack).equals(InfinityMode.RANGE)) {
            ToolUtils.pearlAttack(player, ModItems.endest_pearl.get().getDefaultInstance(), pLevel);//
            player.getCooldowns().addCooldown(itemstack.getItem(), 200);
        }

        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (getMode(stack).equals(InfinityMode.RANGE) && miningEntity instanceof Player player) {
            ToolUtils.destroyMaterialBlocks((ServerPlayer) player, pos, ModConfig.pickAxeBreakRange.get(), ToolUtils.materialsShovel);
        }
        return false;
    }

    @Override
    public DataComponentType<InfinityMode> getDataComponentType() {
        return ModDataComponents.INFINITY_MODE.get();
    }

    @Override
    public InfinityMode getDefaultMode() {
        return InfinityMode.DEFAULT;
    }
}
