package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.util.lang.TextUtils;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeAnvilBlock;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.resources.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.*;
import committee.nova.mods.avaritia.common.net.S2CTotemPack;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.mixins.ServerPlayerGameModeAccessor;
import committee.nova.mods.avaritia.init.registry.*;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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

        var hasBedrockMiningTool = hasBedrockMiningTool(item);
        if (isTemporaryFakeBlock(state)) {
            if (!hasBedrockMiningTool) {
                restoreTemporaryFakeBlock(level, pos, state);
                event.setCanceled(true);
                return;
            }
            if (!level.isClientSide && !isTemporaryFakeBlockBeingMined((ServerLevel) level, pos)) {
                restoreTemporaryFakeBlock(level, pos, state);
                event.setCanceled(true);
                return;
            }
            return;
        }

        if (level.isClientSide || item.isEmpty()) {
            return;
        }

        if (hasBedrockMiningTool) {
            if (state.is(Blocks.BEDROCK)) {
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, ModBlocks.fake_bedrock.get().defaultBlockState(), 2);
            } else if (state.is(Blocks.END_PORTAL_FRAME)) {
                // 保留原方块状态（包括是否有末影之眼）
                BlockState fakeEndPortalFrameState = ModBlocks.fake_end_portal_frame.get().defaultBlockState()
                        .setValue(EndPortalFrameBlock.FACING, state.getValue(EndPortalFrameBlock.FACING))
                        .setValue(EndPortalFrameBlock.HAS_EYE, state.getValue(EndPortalFrameBlock.HAS_EYE));
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, fakeEndPortalFrameState, 2);
            } else if (state.is(Blocks.END_PORTAL)) {
                // 保留末地传送门状态
                trackTemporaryFakeBlock(level, pos, state);
                level.setBlock(pos, ModBlocks.fake_end_portal.get().defaultBlockState(), 2);
            }
        }
    }

    @SubscribeEvent
    public static void restoreAbandonedTemporaryFakeBlocks(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TEMPORARY_FAKE_BLOCKS.isEmpty()) {
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
    public static void onPlayerMine(BlockEvent.BreakEvent event) {
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
                Block.popResource(level, pos, Blocks.END_PORTAL_FRAME.asItem().getDefaultInstance());
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
                    event.setNewSpeed(event.getNewSpeed() * 5);
                }
                if (!event.getEntity().isInWater() && !EnchantmentHelper.hasAquaAffinity(event.getEntity())) {
                    event.setNewSpeed(event.getNewSpeed() * 5);
                }
                if (ISwitchable.isMode(held, "infinity_pickaxe_hammer")
                        || ISwitchable.isMode(held, "infinity_shovel_destroyer")) {
                    event.setNewSpeed(event.getNewSpeed() * 0.5F);
                }
            }
        }
    }

    //合并物质团
    @SubscribeEvent
    public static void clusterCluster(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItem().getItem();
        if (player != null && ModConfig.isMergeMatterCluster.get() && event.getItem().getItem().is(ModItems.matter_cluster.get())) {
            boolean mergedAny = false;

            for (ItemStack slot : player.getInventory().items) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.is(ModItems.matter_cluster.get())) {
                    mergedAny |= MatterClusterItem.mergeClusters(stack, slot);
                }
            }

            if (mergedAny) {
                player.level().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (player.level().random.nextFloat() - player.level().random.nextFloat()) * 1.4F + 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void expCancel(ItemExpireEvent event) {
        if (event.getEntity() instanceof ImmortalItemEntity) {
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (ModConfig.isSwordAttackEndless.get() && event.getItemStack().getItem() instanceof InfinitySwordItem swordItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.attack_damage"))) {
                    var endlessDamage = ModConfig.isSwordAttackEndless.get();
                    event.getToolTip().set(x, Component.literal(endlessDamage ? TextUtils.makeFabulous(I18n.get("tooltip.infinity")) : String.valueOf(swordItem.getTier().getAttackDamageBonus())).append(" ").append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.DARK_GREEN)));
                    return;
                }
            }
        } else if (event.getItemStack().getItem() instanceof InfinityArmorItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.armor"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                } else if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.armor_toughness"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }

            }
        }
    }

    //取消身穿无尽套时的伤害
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ToolUtils.isInfinite(player)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            } else {
                ItemStack totem = ToolUtils.getPlayerTotemItem(player);
                if (!totem.isEmpty()) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CTotemPack(totem, player.getId()));

                    player.removeAllEffects();
                    if (totem.getDamageValue() == 1) { //最后一次
                        player.setHealth(player.getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1));
                        ToolUtils.aoeAttack(player, 8, 1000.0f, false, false);//触发无尽图腾后对附近造成伤害
                        player.displayClientMessage(Component.translatable("tooltip.avaritia.totem_break"), false);
                    } else {
                        player.setHealth(player.getMaxHealth());
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2600, 4));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 700, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1100, 0));
                    totem.hurtAndBreak(1, player, e -> e.swing(InteractionHand.MAIN_HAND));
                    event.setCanceled(true);
                }
            }
        }
    }

    //取消身穿无尽套时受到的所有伤害
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGetHurt(LivingHurtEvent event) {
        DamageSource damageSource = event.getSource();
        if (event.getEntity() instanceof Player player) {
            if (isInfiniteOrUsingInfinityElytra(player) && !damageSource.is(ModDamageTypes.INFINITY)) {
                event.setCanceled(true);
            }
        }
    }

    //取消对无尽套的伤害
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttacked(LivingAttackEvent event) {
        if (event.getSource().is(ModDamageTypes.INFINITY)) return;
        if (ToolUtils.isInfinite(event.getEntity())
                || event.getEntity() instanceof Player player && isUsingInfinityElytra(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource damageSource = event.getSource();
        if (event.getEntity() instanceof Player player) {
            if (isInfiniteOrUsingInfinityElytra(player) && !damageSource.is(ModDamageTypes.INFINITY)) {
                event.setAmount(0.0F);
                player.hurtTime = 0;
                player.deathTime = 0;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInfinityElytraFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player
                && InfinityElytraUtils.hasInfinityElytraEquipped(player)) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
            player.resetFallDistance();
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            // cnlimiter：仅维护无 Caelus 时 Curios 背饰槽无尽鞘翅的服务端滑翔状态。
            InfinityElytraUtils.updateCuriosFallbackFallFlying(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        InfinityElytraUtils.clearCuriosFallbackFallFlying(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() &&
                event.getEntity() instanceof AbstractSkeleton
                && event.getSource().getEntity() instanceof Player player
        ) {
            if (player.getMainHandItem().is(ModItems.blaze_sword.get()) || player.getOffhandItem().is(ModItems.blaze_sword.get())) {
                if (event.getDrops().isEmpty()) {
                    addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                } else {
                    int skulls = 0;

                    for (var drop : event.getDrops()) {
                        ItemStack stack = drop.getItem();
                        if (stack.is(Items.WITHER_SKELETON_SKULL)) {
                            skulls++;
                        }
                    }

                    if (skulls == 0) {
                        addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                    }
                }

            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void toolEnchant(BlockEvent.BreakEvent event) {//炽热
        var player = event.getPlayer();
        if (player == null) return;
        var tool = player.getMainHandItem();
        if (tool.isEmpty()) return;
        var world = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        Block block = event.getState().getBlock();
        BlockState state = event.getState();
        if (
                (tool.is(ModItems.blaze_pickaxe.get()) || tool.is(ModItems.blaze_shovel.get()))
                        && tool.getItem() instanceof ISwitchable switchable
        ) {
            if (switchable.isActive(tool, "smelt"))
                ToolUtils.melting(block, state, world, pos, player, tool, event);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void entityItemUnDeath(ItemEvent event) {//取消无尽物品受到的伤害
        ItemEntity entityItem = event.getEntity();
        Item item = entityItem.getItem().getItem();
        if (item instanceof InfinityArmorItem || item instanceof InfinityAxeItem || item instanceof InfinityBowItem ||
                item instanceof InfinityHoeItem || item instanceof InfinityShovelItem || item instanceof InfinityPickaxeItem ||
                item instanceof InfinitySwordItem || item instanceof InfinityCrossBowItem) {
            entityItem.setInvulnerable(true);
        }
    }

    private static boolean isInfiniteOrUsingInfinityElytra(Player player) {
        return ToolUtils.isInfinite(player) || isUsingInfinityElytra(player);
    }

    private static boolean isUsingInfinityElytra(Player player) {
        return InfinityElytraUtils.hasInfinityElytraEquipped(player)
                && (player.isFallFlying() || !player.onGround());
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
        if (!level.isClientSide) {
            TEMPORARY_FAKE_BLOCKS.put(new TemporaryFakeBlock(level.dimension(), pos.immutable()), originalState);
        }
    }

    private static void removeTemporaryFakeBlock(ServerLevel level, BlockPos pos) {
        TEMPORARY_FAKE_BLOCKS.remove(new TemporaryFakeBlock(level.dimension(), pos.immutable()));
    }

    private static void restoreTemporaryFakeBlock(Level level, BlockPos pos, BlockState fakeState) {
        if (level.isClientSide) {
            return;
        }

        TemporaryFakeBlock temporaryBlock = new TemporaryFakeBlock(level.dimension(), pos.immutable());
        BlockState originalState = TEMPORARY_FAKE_BLOCKS.remove(temporaryBlock);
        if (originalState == null) {
            originalState = getOriginalStateFromFakeState(fakeState);
        }
        level.setBlock(pos, originalState, 2);
    }

    private static BlockState getOriginalStateFromFakeState(BlockState fakeState) {
        if (fakeState.is(ModBlocks.fake_end_portal_frame.get())) {
            return Blocks.END_PORTAL_FRAME.defaultBlockState()
                    .setValue(EndPortalFrameBlock.FACING, fakeState.getValue(EndPortalFrameBlock.FACING))
                    .setValue(EndPortalFrameBlock.HAS_EYE, fakeState.getValue(EndPortalFrameBlock.HAS_EYE));
        }
        if (fakeState.is(ModBlocks.fake_end_portal.get())) {
            return Blocks.END_PORTAL.defaultBlockState();
        }
        return Blocks.BEDROCK.defaultBlockState();
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

    private static void addDrop(LivingDropsEvent event, ItemStack drop) {
        ItemEntity entity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), drop);
        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }

    //endless物品侦听
    @SubscribeEvent
    public static void onItemSpawn(ItemEvent event) {
        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();

        if (stack.is(ModTags.IMMORTAL_ITEM) && !(entity instanceof ImmortalItemEntity)) {
            Level level = entity.level();

            ImmortalItemEntity immortalEntity = ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, entity, stack);

            if (immortalEntity != null) {
                event.setCanceled(true);
            }
            if (!level.isClientSide && immortalEntity != null) {
                entity.discard();
                level.addFreshEntity(immortalEntity);
            }
        }

    }


    @SubscribeEvent
    public static void onEntityHurting(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.FALLING_BLOCK)) {
            Entity sourceEntity = event.getSource().getDirectEntity();
            if (sourceEntity instanceof FallingBlockEntity fallingBlock) {
                Block block = fallingBlock.getBlockState().getBlock();

                if (block instanceof ExtremeAnvilBlock) {
                    LivingEntity entity = event.getEntity();
                    float damage = event.getAmount();
                    float newHealth = Math.max(0.0F, entity.getHealth() - damage);
                    entity.setHealth(newHealth);
                    event.setCanceled(true);
                }
            }
        }
    }

}
