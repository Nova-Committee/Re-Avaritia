package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeAnvilBlock;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.net.S2CTotemPack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.mixins.ServerPlayerGameModeAccessor;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Native callbacks, called at the corresponding vanilla lifecycle boundaries. */
public final class InfinityHandler {
    private static final Map<TemporaryFakeBlock, BlockState> TEMPORARY_FAKE_BLOCKS = new HashMap<>();
    private InfinityHandler() {}

    public static boolean onPlayerMine(ServerPlayer player, BlockPos pos) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        if (player.isCreative()) return false;
        boolean tool = hasBedrockMiningTool(player.getMainHandItem());
        if (isTemporaryFakeBlock(state)) {
            if (!tool || !isTemporaryFakeBlockBeingMined(player.serverLevel(), pos)) {
                restoreTemporaryFakeBlock(level, pos, state);
                return true;
            }
            return false;
        }
        if (!tool) return false;
        BlockState fake = null;
        if (state.is(Blocks.BEDROCK)) fake = ModBlocks.fake_bedrock.get().defaultBlockState();
        else if (state.is(Blocks.END_PORTAL_FRAME)) fake = ModBlocks.fake_end_portal_frame.get().defaultBlockState()
                .setValue(EndPortalFrameBlock.FACING, state.getValue(EndPortalFrameBlock.FACING))
                .setValue(EndPortalFrameBlock.HAS_EYE, state.getValue(EndPortalFrameBlock.HAS_EYE));
        else if (state.is(Blocks.END_PORTAL)) fake = ModBlocks.fake_end_portal.get().defaultBlockState();
        if (fake != null) {
            TEMPORARY_FAKE_BLOCKS.put(new TemporaryFakeBlock(level.dimension(), pos.immutable()), state);
            level.setBlock(pos, fake, 2);
        }
        return false;
    }

    public static void restoreAbandonedTemporaryFakeBlocks(MinecraftServer server) {
        Iterator<Map.Entry<TemporaryFakeBlock, BlockState>> it = TEMPORARY_FAKE_BLOCKS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var key = entry.getKey();
            ServerLevel level = server.getLevel(key.dimension());
            if (level == null || !isTemporaryFakeBlock(level.getBlockState(key.pos()))) { it.remove(); continue; }
            if (!isTemporaryFakeBlockBeingMined(level, key.pos())) {
                level.setBlock(key.pos(), entry.getValue(), 2);
                it.remove();
            }
        }
    }

    public static void onBlockBroken(ServerPlayer player, BlockPos pos, BlockState state) {
        if (player.isCreative()) return;
        ServerLevel level = player.serverLevel();
        Block drop = state.is(ModBlocks.fake_bedrock.get()) ? Blocks.BEDROCK
                : state.is(ModBlocks.fake_end_portal_frame.get()) ? Blocks.END_PORTAL_FRAME
                : state.is(ModBlocks.fake_end_portal.get()) ? Blocks.END_PORTAL
                : state.is(Blocks.REINFORCED_DEEPSLATE) ? Blocks.REINFORCED_DEEPSLATE : null;
        if (drop != null) {
            TEMPORARY_FAKE_BLOCKS.remove(new TemporaryFakeBlock(level.dimension(), pos.immutable()));
            Block.popResource(level, pos, new ItemStack(drop));
        }
    }

    public static float digging(Player player, float speed) {
        ItemStack held = player.getMainHandItem();
        if (held.is(ModItems.infinity_pickaxe.get()) || held.is(ModItems.infinity_shovel.get())) {
            if (!player.onGround()) speed *= 5;
            if (!player.isInWater() && !EnchantmentHelper.hasAquaAffinity(player)) speed *= 5;
            if (ISwitchable.isMode(held, "infinity_pickaxe_hammer") || ISwitchable.isMode(held, "infinity_shovel_destroyer")) speed *= 0.5F;
        }
        return speed;
    }

    public static void clusterCluster(Player player, ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (!ModConfig.isMergeMatterCluster.get() || !stack.is(ModItems.matter_cluster.get())) return;
        boolean merged = false;
        for (ItemStack slot : player.getInventory().items) {
            if (stack.isEmpty()) break;
            if (slot.is(ModItems.matter_cluster.get())) merged |= MatterClusterItem.mergeClusters(stack, slot);
        }
        if (merged) player.level().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4F + 2F);
    }

    public static boolean preventDeath(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return false;
        if (ToolUtils.isInfinite(player)) { player.setHealth(player.getMaxHealth()); return true; }
        ItemStack totem = ToolUtils.getPlayerTotemItem(player);
        if (totem.isEmpty()) return false;
        NetworkHandler.CHANNEL.sendTo(player, new S2CTotemPack(totem, player.getId()));
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        if (totem.getDamageValue() == 1) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1));
            ToolUtils.aoeAttack(player, 8, 1000F, false, false);
            player.displayClientMessage(Component.translatable("tooltip.avaritia.totem_break"), false);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2600, 4));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 700, 2));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1100, 0));
        totem.hurtAndBreak(1, player, e -> e.swing(InteractionHand.MAIN_HAND));
        return true;
    }

    public static boolean preventDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!source.is(ModDamageTypes.INFINITY) && (ToolUtils.isInfinite(entity)
                || entity instanceof Player player && InfinityElytraUtils.hasInfinityElytraEquipped(player)
                && (player.isFallFlying() || !player.onGround()))) {
            entity.hurtTime = 0;
            entity.deathTime = 0;
            return true;
        }
        if (source.is(DamageTypes.FALLING_BLOCK) && source.getDirectEntity() instanceof FallingBlockEntity falling
                && falling.getBlockState().getBlock() instanceof ExtremeAnvilBlock) {
            entity.setHealth(Math.max(0F, entity.getHealth() - amount));
            if (entity.getHealth() <= 0F) entity.die(source);
            return true;
        }
        return false;
    }

    public static boolean toolEnchant(ServerPlayer player, BlockPos pos) {
        ItemStack tool = player.getMainHandItem();
        if ((tool.is(ModItems.blaze_pickaxe.get()) || tool.is(ModItems.blaze_shovel.get()))
                && tool.getItem() instanceof ISwitchable switchable && switchable.isActive(tool, "smelt")) {
            BlockState state = player.level().getBlockState(pos);
            return ToolUtils.melting(state.getBlock(), state, player.level(), pos, player, tool);
        }
        return false;
    }

    public static boolean isImmortal(ItemStack stack) {
        return stack.is(ModTags.IMMORTAL_ITEM) || stack.getItem() instanceof IUndamageable;
    }

    private static boolean hasBedrockMiningTool(ItemStack item) {
        return item.is(ModItems.crystal_pickaxe.get()) || item.is(ModItems.infinity_pickaxe.get());
    }
    private static boolean isTemporaryFakeBlock(BlockState state) {
        return state.is(ModBlocks.fake_bedrock.get()) || state.is(ModBlocks.fake_end_portal_frame.get()) || state.is(ModBlocks.fake_end_portal.get());
    }
    private static void restoreTemporaryFakeBlock(Level level, BlockPos pos, BlockState state) {
        BlockState original = TEMPORARY_FAKE_BLOCKS.remove(new TemporaryFakeBlock(level.dimension(), pos.immutable()));
        if (original == null) {
            original = state.is(ModBlocks.fake_end_portal_frame.get()) ? Blocks.END_PORTAL_FRAME.defaultBlockState()
                    .setValue(EndPortalFrameBlock.FACING, state.getValue(EndPortalFrameBlock.FACING))
                    .setValue(EndPortalFrameBlock.HAS_EYE, state.getValue(EndPortalFrameBlock.HAS_EYE))
                    : state.is(ModBlocks.fake_end_portal.get()) ? Blocks.END_PORTAL.defaultBlockState() : Blocks.BEDROCK.defaultBlockState();
        }
        level.setBlock(pos, original, 2);
    }
    private static boolean isTemporaryFakeBlockBeingMined(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : level.players()) {
            ServerPlayerGameModeAccessor mode = (ServerPlayerGameModeAccessor) player.gameMode;
            if (mode.avaritia$isDestroyingBlock() && pos.equals(mode.avaritia$destroyPos())
                    || mode.avaritia$hasDelayedDestroy() && pos.equals(mode.avaritia$delayedDestroyPos())) return true;
        }
        return false;
    }
    private record TemporaryFakeBlock(ResourceKey<Level> dimension, BlockPos pos) {}
}
