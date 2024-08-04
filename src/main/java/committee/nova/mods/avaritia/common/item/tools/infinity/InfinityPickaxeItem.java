package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTiers;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class InfinityPickaxeItem extends PickaxeItem {

    public InfinityPickaxeItem() {
        super(ModTiers.INFINITY_PICKAXE, 1, -2.8F, (new Properties())
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
    public @NotNull Rarity getRarity(@NotNull ItemStack pStack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }


    @Override
    public float getDestroySpeed(ItemStack stack, @NotNull BlockState state) {
        if (stack.getOrCreateTag().getBoolean("hammer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0F);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            CompoundTag tags = stack.getOrCreateTag();
            tags.putBoolean("hammer", !tags.getBoolean("hammer"));
            player.swing(hand);
            if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(
                    Component.translatable(tags.getBoolean("hammer") ? "tooltip.infinity_pickaxe.type_2" : "tooltip.infinity_pickaxe.type_1"
                    ), true);
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, @NotNull LivingEntity victim, @NotNull LivingEntity player) {
        if (stack.getOrCreateTag().getBoolean("hammer")) {
            if (!(victim instanceof Player)) {
                int i = 10;
                victim.setDeltaMovement(-Mth.sin(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F, 2.0D, Mth.cos(player.yBodyRot * (float) Math.PI / 180.0F) * i * 0.5F);
            }
        }
        return true;
    }


    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (stack.getOrCreateTag().getBoolean("hammer")) {
            ToolUtils.breakRangeBlocks(player, stack, pos, ModConfig.pickAxeBreakRange.get(), ToolUtils.materialsPick, false);
        }
        return false;
    }

}
