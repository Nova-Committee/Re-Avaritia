package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.net.S2CTotemPacket;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.mixin.accessor.ServerPlayerGameModeAccessor;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class InfinityHandler {
    private static final Map<TemporaryFakeBlock, BlockState> TEMPORARY_FAKE_BLOCKS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        var item = event.getItemStack();
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var player = event.getEntity();
        var face = event.getFace();
        if (face == null || player.isCreative()) {
            return;
        }

        boolean hasBedrockMiningTool = hasBedrockMiningTool(item);
        if (isTemporaryFakeBlock(state)) {
            if (!hasBedrockMiningTool) {
                restoreTemporaryFakeBlock(level, pos, state);
                event.setCanceled(true);
                return;
            }
            if (!level.isClientSide() && !isTemporaryFakeBlockBeingMined((ServerLevel) level, pos)) {
                restoreTemporaryFakeBlock(level, pos, state);
                event.setCanceled(true);
            }
            return;
        }

        if (level.isClientSide() || item.isEmpty()) {
            return;
        }

        if (hasBedrockMiningTool) {
            if (state.is(Blocks.BEDROCK)) {
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, ModBlocks.fake_bedrock.get().defaultBlockState(), 2);
            } else if (state.is(Blocks.END_PORTAL_FRAME)) {
                BlockState fakeState = ModBlocks.fake_end_portal_frame.get().defaultBlockState();
                if (fakeState.hasProperty(BlockStateProperties.EYE) && state.hasProperty(BlockStateProperties.EYE)) {
                    fakeState = fakeState.setValue(BlockStateProperties.EYE, state.getValue(BlockStateProperties.EYE));
                }
                if (fakeState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    fakeState = fakeState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                }
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, fakeState, 2);
            } else if (state.is(Blocks.END_PORTAL)) {
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, ModBlocks.fake_end_portal.get().defaultBlockState(), 2);
            }
        }
    }

    @SubscribeEvent
    public static void restoreAbandonedTemporaryFakeBlocks(ServerTickEvent.Post event) {
        if (TEMPORARY_FAKE_BLOCKS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<TemporaryFakeBlock, BlockState>> iterator = TEMPORARY_FAKE_BLOCKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TemporaryFakeBlock, BlockState> entry = iterator.next();
            TemporaryFakeBlock temporaryBlock = entry.getKey();
            ServerLevel level = event.getServer().getLevel(temporaryBlock.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }

            BlockState currentState = level.getBlockState(temporaryBlock.pos());
            if (!isTemporaryFakeBlock(currentState)) {
                iterator.remove();
                continue;
            }

            if (!isTemporaryFakeBlockBeingMined(level, temporaryBlock.pos())) {
                level.setBlock(temporaryBlock.pos(), entry.getValue(), 2);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void restoreTemporaryFakeBlocksBeforeServerStop(ServerStoppingEvent event) {
        for (Map.Entry<TemporaryFakeBlock, BlockState> entry : TEMPORARY_FAKE_BLOCKS.entrySet()) {
            TemporaryFakeBlock temporaryBlock = entry.getKey();
            ServerLevel level = event.getServer().getLevel(temporaryBlock.dimension());
            if (level != null && isTemporaryFakeBlock(level.getBlockState(temporaryBlock.pos()))) {
                level.setBlock(temporaryBlock.pos(), entry.getValue(), 2);
            }
        }
        TEMPORARY_FAKE_BLOCKS.clear();
    }


    @SubscribeEvent
    public static void onPlayerMine(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;
        var level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!event.getPlayer().isCreative()) {
            if (state.is(ModBlocks.fake_bedrock.get())) {
                removeTemporaryFakeBlock(level, pos);
                Block.popResource(level, pos, Blocks.BEDROCK.asItem().getDefaultInstance());
            } else if (state.is(ModBlocks.fake_end_portal_frame.get())) {
                removeTemporaryFakeBlock(level, pos);
                ItemStack frameItem = Blocks.END_PORTAL_FRAME.asItem().getDefaultInstance();
                Block.popResource(level, pos, frameItem);
            } else if (state.is(ModBlocks.fake_end_portal.get())) {
                removeTemporaryFakeBlock(level, pos);
                Block.popResource(level, pos, Blocks.END_PORTAL.asItem().getDefaultInstance());
            } else if (state.is(Blocks.REINFORCED_DEEPSLATE)) {
                Block.popResource(level, pos, Blocks.REINFORCED_DEEPSLATE.asItem().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void digging(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            ItemStack held = event.getEntity().getMainHandItem();
            if (held.is(ModItems.infinity_pickaxe.get()) || held.is(ModItems.infinity_shovel.get())) {
                if (!event.getEntity().onGround()) {
                    event.setNewSpeed(event.getNewSpeed() * 5F);
                }
                if (!event.getEntity().isInWater()) {
                    event.setNewSpeed(event.getNewSpeed() * 5F);
                }
                if (ISwitchable.isMode(held, "infinity_pickaxe_hammer")
                        || ISwitchable.isMode(held, "infinity_shovel_destroyer")) {
                    event.setNewSpeed(event.getNewSpeed() * 0.5F);
                }
            }
        }
    }

    //鍚堝苟鐗╄川鍥?
    @SubscribeEvent
    public static void clusterCluster(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemEntity().getItem();
        if (ModConfig.isMergeMatterCluster.get() && event.getItemEntity().getItem().is(ModItems.matter_cluster.get())) {
            boolean mergedAny = false;

            for (ItemStack slot : player.getInventory().getNonEquipmentItems()) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.is(ModItems.matter_cluster.get())) {
                    mergedAny |= MatterClusterItem.mergeClusters(stack, slot);
                }
            }

            if (mergedAny) {
                player.level().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (player.level().getRandom().nextFloat() - player.level().getRandom().nextFloat()) * 1.4F + 2.0F);
            }
        }
    }


    @SubscribeEvent
    public static void expCancel(ItemExpireEvent event) {
        if (event.getEntity() instanceof ImmortalItemEntity itemEntity) {
            itemEntity.setUnlimitedLifetime();
        }
    }

    //鍙栨秷韬┛鏃犲敖濂楁椂鐨勪激瀹?
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ToolUtils.isInfinite(player)) {
                event.setCanceled(true);
                player.hurtTime = 0;
                player.deathTime = 0;
                player.setHealth(Math.max(20.0F, player.getMaxHealth()));
            } else {
                ItemStack totem = ToolUtils.getPlayerTotemItem(player);
                if (!totem.isEmpty()) {
                    PacketDistributor.sendToPlayer(player, new S2CTotemPacket(totem, player.getId()));

                    player.removeAllEffects();
                    if (totem.getDamageValue() % 10 == 0) { //姣忓綋涓?0鐨勫€嶆暟
                        player.setHealth(player.getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 800, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 800, 1));
                        ToolUtils.aoeAttack(player, 8, 1000.0f, false,false);//瑙﹀彂鏃犲敖鍥捐吘鍚庡闄勮繎閫犳垚浼ゅ
                        player.sendSystemMessage(Component.translatable("tooltip.avaritia.totem_break"), false);
                    } else {
                        player.setHealth(player.getMaxHealth());
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2600, 4));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 700, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1100, 0));
                    totem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    event.setCanceled(true);
                }
            }
        }
    }

    //鍙栨秷韬┛鏃犲敖濂楁椂鍙楀埌鐨勬墍鏈変激瀹?闄や簡鏃犲敖浼ゅ)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInfiniteHurt(LivingIncomingDamageEvent event) {
        DamageSource damageSource = event.getSource();
        if (event.getEntity() instanceof Player player) {
            if ((ToolUtils.isInfinite(player) || isUsingInfinityElytra(player)) && !damageSource.is(ModDamageTypes.INFINITY)) {
                event.setCanceled(true);
            }
        }
    }

    //鍙栨秷瀵规棤灏藉鐨勪激瀹?
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackedInfinite(AttackEntityEvent event) {
        if (event.getTarget() instanceof LivingEntity living && ToolUtils.isInfinite(living)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        DamageSource damageSource = event.getSource();
        if (event.getEntity() instanceof Player player) {
            if ((ToolUtils.isInfinite(player) || isUsingInfinityElytra(player)) && !damageSource.is(ModDamageTypes.INFINITY)) {
                event.setNewDamage(0.0F);
                player.hurtTime = 0;
                player.deathTime = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Post event) {
        DamageSource damageSource = event.getSource();
        if (event.getEntity() instanceof Player player) {
            if ((ToolUtils.isInfinite(player) || isUsingInfinityElytra(player)) && !damageSource.is(ModDamageTypes.INFINITY)) {
                player.hurtTime = 0;
                player.deathTime = 0;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInfinityElytraFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && isWearingInfinityElytra(player)) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            player.resetFallDistance();
            event.setCanceled(true);
        }
    }

    /*@SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InfinityElytraUtils.updateCuriosFallbackFallFlying(player);
            //InfinityElytraUtils.tickInfinityElytra(player);
        }
    }*/

    /*@SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        InfinityElytraUtils.clearCuriosFallbackFallFlying(event.getEntity());
    }*/

    private static boolean isUsingInfinityElytra(Player player) {
        return isWearingInfinityElytra(player) && (player.isFallFlying() || !player.onGround());
    }

    private static boolean isWearingInfinityElytra(Player player) {
        return InfinityElytraUtils.hasInfinityElytraEquipped(player);
    }

    private static boolean hasBedrockMiningTool(ItemStack item) {
        return item.is(ModItems.crystal_pickaxe.get()) || item.is(ModItems.infinity_pickaxe.get());
    }

    private static boolean isTemporaryFakeBlock(BlockState state) {
        return state.is(ModBlocks.fake_bedrock.get())
                || state.is(ModBlocks.fake_end_portal_frame.get())
                || state.is(ModBlocks.fake_end_portal.get());
    }

    private static void trackTemporaryFakeBlock(Level level, BlockPos pos, BlockState originalState) {
        if (!level.isClientSide()) {
            TEMPORARY_FAKE_BLOCKS.put(new TemporaryFakeBlock(level.dimension(), pos.immutable()), originalState);
        }
    }

    private static void removeTemporaryFakeBlock(ServerLevel level, BlockPos pos) {
        TEMPORARY_FAKE_BLOCKS.remove(new TemporaryFakeBlock(level.dimension(), pos.immutable()));
    }

    private static void restoreTemporaryFakeBlock(Level level, BlockPos pos, BlockState fakeState) {
        if (level.isClientSide()) {
            return;
        }
        TemporaryFakeBlock temporaryBlock = new TemporaryFakeBlock(level.dimension(), pos.immutable());
        BlockState originalState = TEMPORARY_FAKE_BLOCKS.remove(temporaryBlock);
        level.setBlock(pos, originalState != null ? originalState : getOriginalStateFromFakeState(fakeState), 2);
    }

    private static BlockState getOriginalStateFromFakeState(BlockState fakeState) {
        if (fakeState.is(ModBlocks.fake_end_portal_frame.get())) {
            BlockState originalState = Blocks.END_PORTAL_FRAME.defaultBlockState();
            if (originalState.hasProperty(BlockStateProperties.EYE) && fakeState.hasProperty(BlockStateProperties.EYE)) {
                originalState = originalState.setValue(BlockStateProperties.EYE, fakeState.getValue(BlockStateProperties.EYE));
            }
            if (originalState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    && fakeState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                originalState = originalState.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        fakeState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
            return originalState;
        }
        return fakeState.is(ModBlocks.fake_end_portal.get())
                ? Blocks.END_PORTAL.defaultBlockState()
                : Blocks.BEDROCK.defaultBlockState();
    }

    private static boolean isTemporaryFakeBlockBeingMined(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : level.players()) {
            ServerPlayerGameModeAccessor gameMode = (ServerPlayerGameModeAccessor) player.gameMode;
            if (gameMode.avaritia$isDestroyingBlock() && pos.equals(gameMode.avaritia$destroyPos())) {
                return true;
            }
            if (gameMode.avaritia$hasDelayedDestroy() && pos.equals(gameMode.avaritia$delayedDestroyPos())) {
                return true;
            }
        }
        return false;
    }

    private record TemporaryFakeBlock(ResourceKey<Level> dimension, BlockPos pos) {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() &&
                event.getEntity() instanceof AbstractSkeleton
                && event.getSource().getEntity() instanceof Player player
        ) {
            if (player.getMainHandItem().is(ModItems.blaze_sword.get()) || player.getOffhandItem().is(ModItems.blaze_sword.get())) {
                if (event.getDrops().isEmpty()) {
                    addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL));
                } else {
                    int skulls = 0;

                    for (var drop : event.getDrops()) {
                        ItemStack stack = drop.getItem();
                        if (stack.is(Items.WITHER_SKELETON_SKULL)) {
                            skulls++;
                        }
                    }

                    if (skulls == 0) {
                        addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL));
                    }
                }

            }
        }
    }


    private static void addDrop(LivingDropsEvent event, ItemStack drop) {
        ItemEntity entity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), drop);
        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }


    //endless鐗╁搧渚﹀惉
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.is(ModTags.IMMORTAL_ITEM) && !(itemEntity instanceof ImmortalItemEntity)) {
                Level level = event.getLevel();

                if (!level.isClientSide()) {

                    ImmortalItemEntity immortalEntity = ImmortalItemEntity.create(
                            ModEntityTypes.IMMORTAL.get(),
                            level,
                            itemEntity,
                            stack
                    );

                    if (immortalEntity != null) {

                        event.setCanceled(true);
                        itemEntity.discard();
                        level.addFreshEntity(immortalEntity);
                    }
                }
            }
        }
    }

}
