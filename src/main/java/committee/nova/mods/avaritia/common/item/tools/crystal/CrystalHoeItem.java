package committee.nova.mods.avaritia.common.item.tools.crystal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.BonemealableBlock;
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
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        super.useOn(pContext);
        var pos = pContext.getClickedPos();
        var level = pContext.getLevel();
        var targetState = level.getBlockState(pos);
        var targetBlock = targetState.getBlock();
        if (level instanceof ServerLevel serverLevel && targetBlock instanceof BonemealableBlock growable
        ) {
            if (growable.isValidBonemealTarget(serverLevel, pos, targetState, false)) {// && ForgeHooks.onCropsGrowPre(serverLevel, pos, targetState, true)) {
                growable.performBonemeal(serverLevel, level.random, pos, targetState);
                serverLevel.levelEvent(2005, pos, 0);
//                ForgeHooks.onCropsGrowPost(serverLevel, pos, targetState);
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
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getTier().getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getTier().getSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }
}
