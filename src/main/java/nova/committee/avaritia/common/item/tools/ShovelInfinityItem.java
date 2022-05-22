package nova.committee.avaritia.common.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;
import nova.committee.avaritia.util.ToolHelper;
import nova.committee.avaritia.util.math.RayTracer;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 16:33
 * Version: 1.0
 */
public class ShovelInfinityItem extends ShovelItem {

    public ShovelInfinityItem() {
        super(Tier.INFINITY_SHOVEL, -2, -2.8f, (new Properties())
                .tab(ModTab.TAB)
                .stacksTo(1)
                .fireResistant());

        setRegistryName("infinity_shovel");
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.getTag() != null && stack.getTag().getBoolean("destroyer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            CompoundTag tags = stack.getOrCreateTag();
            tags.putBoolean("destroyer", !tags.getBoolean("destroyer"));
            player.setMainArm(HumanoidArm.RIGHT);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (stack.getOrCreateTag().getBoolean("destroyer")) {
            var world = player.level;
            if (!world.isClientSide) {
                BlockHitResult traceResult = RayTracer.retrace(player, 10);
                breakOtherBlock(player, stack, pos, traceResult.getDirection());
            }
        }
        return false;
    }

    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, Direction sideHit) {

        var world = player.level;
        var state = world.getBlockState(pos);
        var mat = state.getMaterial();
        if (!PickaxeInfinityItem.MATERIALS.contains(mat)) {
            return;
        }

        if (state.isAir()) {
            return;
        }

        var doY = sideHit.getAxis() != Direction.Axis.Y;

        var range = 8;
        var min = new BlockPos(-range, doY ? -1 : -range, -range);
        var max = new BlockPos(range, doY ? range * 2 - 2 : range, range);

        ToolHelper.aoeBlocks(player, stack, world, pos, min, max, null, PickaxeInfinityItem.MATERIALS, false);

    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }


}
