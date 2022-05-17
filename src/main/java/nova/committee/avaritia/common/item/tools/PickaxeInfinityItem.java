package nova.committee.avaritia.common.item.tools;

import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;
import nova.committee.avaritia.init.registry.ModTab;
import nova.committee.avaritia.util.ToolHelper;
import nova.committee.avaritia.util.math.RayTracer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class PickaxeInfinityItem extends PickaxeItem {
    public static final Set<Material> MATERIALS = Sets.newHashSet(Material.STONE, Material.HEAVY_METAL, Material.METAL, Material.ICE, Material.GLASS, Material.PISTON, Material.GRASS, Material.DIRT, Material.SAND, Material.SNOW, Material.CLAY, Material.ICE, Material.ICE_SOLID);


    public PickaxeInfinityItem() {
        super(Tier.INFINITY_PICKAXE, 1, -2.8F, (new Properties())
                .tab(ModTab.TAB)
                .stacksTo(1)
                .fireResistant());

        setRegistryName("infinity_pickaxe");
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
    public Rarity getRarity(ItemStack p_77613_1_) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 0;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TextComponent(ChatFormatting.GRAY + "由" + ChatFormatting.BLUE + "演变" + "-" + ChatFormatting.DARK_PURPLE + "cnlimiter" + ChatFormatting.YELLOW + "倾情制作~"));
        }
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.getOrCreateTag().getBoolean("hammer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            CompoundTag tags = stack.getOrCreateTag();
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) < 10) {
                stack.enchant(Enchantments.BLOCK_FORTUNE, 10);
            }
            tags.putBoolean("hammer", !tags.getBoolean("hammer"));
            player.setMainArm(HumanoidArm.RIGHT);
            return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity player) {
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

        var world = player.level;
        var state = world.getBlockState(pos);
        var mat = state.getMaterial();
        if (!MATERIALS.contains(mat)) {
            return;
        }

        if (state.isAir()) {
            return;
        }

        var doY = sideHit.getAxis() != Direction.Axis.Y;

        int range = 8;
        var minOffset = new BlockPos(-range, doY ? -1 : -range, -range);
        var maxOffset = new BlockPos(range, doY ? range * 2 - 2 : range, range);
        ToolHelper.aoeBlocks(player, stack, world, pos, minOffset, maxOffset, null, MATERIALS);

    }


}
