package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTiers;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
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
public class InfinityAxeItem extends AxeItem {

    public InfinityAxeItem() {
        super(ModTiers.INFINITY_PICKAXE, 10, -3.0f, (new Properties())
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            CompoundTag tags = stack.getOrCreateTag();
            tags.putBoolean("range", !tags.getBoolean("range"));
            player.swing(hand);
            if(!pLevel.isClientSide && player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(
                    Component.translatable(tags.getBoolean("range") ? "tooltip.infinity_axe.type_2" : "tooltip.infinity_axe.type_1"
                    ), true);
            return InteractionResultHolder.success(stack);
        }
        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level world = player.level();
        if (!world.isClientSide && stack.getOrCreateTag().getBoolean("range") && canHarvest(pos, world)) {
            destroyTree(player, (ServerLevel) world, pos, stack);
        }
        return false;
    }

}
