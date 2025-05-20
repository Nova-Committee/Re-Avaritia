package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
public class InfinityAxeItem extends AxeItem implements ISwitchable {

    public InfinityAxeItem() {
        super(ModToolTiers.INFINITY,
                new Properties()
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
        if (level instanceof ServerLevel serverLevel && isActive(stack, "infinity_axe_range") && canHarvest(pos, level) && miningEntity instanceof ServerPlayer player) {
            destroyTree(player, serverLevel, pos, stack);
        }
        return false;
    }
}
