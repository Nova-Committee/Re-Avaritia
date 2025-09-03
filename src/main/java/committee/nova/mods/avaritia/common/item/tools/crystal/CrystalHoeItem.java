package committee.nova.mods.avaritia.common.item.tools.crystal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalHoeItem extends HoeItem implements ITooltip {

    private final String name;

    public CrystalHoeItem(String name) {
        super(ModToolTiers.CRYSTAL, 0, -25F,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
        this.name = name;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        this.appendTooltip(pStack, pLevel, pTooltipComponents, pIsAdvanced, name);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        return super.use(world, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        var stack = context.getItemInHand();
        var world = context.getLevel();
        var blockpos = context.getClickedPos();
        var targetBlock = world.getBlockState(blockpos).getBlock();
        var player = context.getPlayer();
        var blockstate = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
        int range = 1; // 3x3 area
        var minPos = blockpos.offset(-range, 0, -range);
        var maxPos = blockpos.offset(range, 0, range);

        if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockpos.above()) &&
                (targetBlock instanceof GrassBlock || targetBlock.equals(Blocks.DIRT) || targetBlock.equals(Blocks.COARSE_DIRT))) {
            if (player != null && !world.isClientSide) {
                var boxMutable = BlockPos.betweenClosed(minPos, maxPos);
                for (BlockPos pos : boxMutable) {
                    var block = world.getBlockState(pos).getBlock();
                    if (world.isEmptyBlock(pos.above()) && (block instanceof GrassBlock || block.equals(Blocks.DIRT) || block.equals(
                            Blocks.COARSE_DIRT))) {
                        world.setBlock(pos, blockstate, 11);
                    }
                }
            }
            world.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        // Handle bonemeal functionality
        var targetState = world.getBlockState(blockpos);
        if (world instanceof ServerLevel serverLevel && targetBlock instanceof BonemealableBlock growable) {
            if (growable.isValidBonemealTarget(serverLevel, blockpos, targetState, false) && ForgeHooks.onCropsGrowPre(serverLevel, blockpos, targetState, true)) {
                growable.performBonemeal(serverLevel, world.random, blockpos, targetState);
                serverLevel.levelEvent(2005, blockpos, 0);
                ForgeHooks.onCropsGrowPost(serverLevel, blockpos, targetState);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 取消攻击冷却
            serverPlayer.resetAttackStrengthTicker();
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
