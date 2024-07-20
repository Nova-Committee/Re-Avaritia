package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModTiers;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
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
public class InfinityShovelItem extends ShovelItem {

    public InfinityShovelItem() {
        super(ModTiers.INFINITY_SHOVEL, -2, -2.8f, (new Properties())
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
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return ModItems.COSMIC_RARITY;
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
    public float getDestroySpeed(ItemStack stack, @NotNull BlockState state) {
        if (stack.getTag() != null && stack.getTag().getBoolean("destroyer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            CompoundTag tags = stack.getOrCreateTag();
            tags.putBoolean("destroyer", !tags.getBoolean("destroyer"));
            player.swing(hand);
            return InteractionResultHolder.success(stack);
        }
        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (stack.getOrCreateTag().getBoolean("destroyer")) {
            ToolUtils.breakRangeBlocks(player, stack, pos, ModConfig.shovelBreakRange.get(), ToolUtils.materialsShovel);
        }
        return false;
    }
}
