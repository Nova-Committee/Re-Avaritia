package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModCreativeModeTabs;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolHelper;
import committee.nova.mods.avaritia.util.math.RayTracer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class PickaxeInfinityItem extends PickaxeItem {

    public PickaxeInfinityItem() {
        super(Tier.INFINITY_PICKAXE, 1, -2.8F, (new Properties())
                .stacksTo(1)
                .tab(ModCreativeModeTabs.TAB)
                .fireResistant());

    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack p_77613_1_) {
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
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) < 10) {
                stack.enchant(Enchantments.BLOCK_FORTUNE, 10);//FORTUNE X enchantment
            }
            tags.putBoolean("hammer", !tags.getBoolean("hammer"));
            player.setMainArm(HumanoidArm.RIGHT);
            return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
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
            BlockHitResult traceResult = RayTracer.retrace(player, 10);
            breakOtherBlock(player, stack, pos, traceResult.getDirection());
        }
        return false;
    }

    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, Direction sideHit) {

        var world = player.getCommandSenderWorld();
        var state = world.getBlockState(pos);
        var mat = state.getMaterial();
        if (!ToolHelper.materialsPick.contains(mat)) {
            return;
        }

        if (state.isAir()) {
            return;
        }

        var doY = sideHit.getAxis() != Direction.Axis.Y;

        int range = ModConfig.pickAxeBreakRange.get();
        var minOffset = new BlockPos(-range, doY ? -1 : -range, -range);
        var maxOffset = new BlockPos(range, doY ? range * 2 - 2 : range, range);

        ToolHelper.aoeBlocks(player, stack, world, pos, minOffset, maxOffset, null, ToolHelper.materialsPick, false);

    }


}
