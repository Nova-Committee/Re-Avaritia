package com.avaritia.init.handler;

import com.avaritia.Const;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.common.item.resources.MatterClusterItem;
import com.avaritia.common.net.S2CTotemPacket;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.*;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class InfinityHandler {
    @SubscribeEvent
    public static void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        var item = event.getItemStack();
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var player= event.getEntity();
        var face = event.getFace();
        if (face == null || level.isClientSide() || item.isEmpty() || player.isCreative()) {
            return;
        }

        if (item.is(ModItems.crystal_pickaxe.get()) || item.is(ModItems.infinity_pickaxe.get())){
            if (state.is(Blocks.BEDROCK)) {
                level.setBlock(pos, ModBlocks.fake_bedrock.get().defaultBlockState(), 2);
            } else if (state.is(Blocks.END_PORTAL_FRAME)) {

                BlockState fakeState = ModBlocks.fake_end_portal_frame.get().defaultBlockState();


                if (fakeState.hasProperty(BlockStateProperties.EYE) && state.hasProperty(BlockStateProperties.EYE)) {
                    fakeState = fakeState.setValue(BlockStateProperties.EYE, state.getValue(BlockStateProperties.EYE));
                }

                if (fakeState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    fakeState = fakeState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                }

                level.setBlock(pos, fakeState, 2);
            } else if (state.is(Blocks.END_PORTAL)) {
                level.setBlock(pos, ModBlocks.fake_end_portal.get().defaultBlockState(), 2);
            }
        }

        if (!(item.is(ModItems.crystal_pickaxe.get()) || item.is(ModItems.infinity_pickaxe.get()))){
            if (state.is(ModBlocks.fake_bedrock.get())) {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
            } else if (state.is(ModBlocks.fake_end_portal_frame.get())) {

                BlockState originalState = Blocks.END_PORTAL_FRAME.defaultBlockState();


                if (originalState.hasProperty(BlockStateProperties.EYE) && state.hasProperty(BlockStateProperties.EYE)) {
                    originalState = originalState.setValue(BlockStateProperties.EYE, state.getValue(BlockStateProperties.EYE));
                }

                if (originalState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    originalState = originalState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                }

                level.setBlock(pos, originalState, 2);
            } else if (state.is(ModBlocks.fake_end_portal.get())) {
                level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), 2);
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerMine(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;
        var level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!event.getPlayer().isCreative()) {
            if (state.is(ModBlocks.fake_bedrock.get())) {
                Block.popResource(level, pos, Blocks.BEDROCK.asItem().getDefaultInstance());
            } else if (state.is(ModBlocks.fake_end_portal_frame.get())) {

                ItemStack frameItem = Blocks.END_PORTAL_FRAME.asItem().getDefaultInstance();

                Block.popResource(level, pos, frameItem);
            } else if (state.is(ModBlocks.fake_end_portal.get())) {
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
            if (ToolUtils.isInfinite(player) && !damageSource.is(ModDamageTypes.INFINITY)) {
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
            if (ToolUtils.isInfinite(player) && !damageSource.is(ModDamageTypes.INFINITY)) {
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
            if (ToolUtils.isInfinite(player) && !damageSource.is(ModDamageTypes.INFINITY)) {
                player.hurtTime = 0;
                player.deathTime = 0;
            }
        }
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
                            itemEntity.getX(),
                            itemEntity.getY(),
                            itemEntity.getZ(),
                            stack
                    );

                    if (immortalEntity != null) {

                        immortalEntity.setDeltaMovement(itemEntity.getDeltaMovement());
                        immortalEntity.setPickUpDelay(0);

                        event.setCanceled(true);
                        itemEntity.discard();
                        level.addFreshEntity(immortalEntity);
                    }
                }
            }
        }
    }

}
