package com.avaritia.util;

import com.avaritia.init.registry.ModEntityTypes;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.avaritia.api.utils.InventoryUtils;
import com.avaritia.common.entity.BladeSlashEntity;
import com.avaritia.common.entity.EndestPearlEntity;
import com.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import com.avaritia.common.entity.arrow.TraceArrowEntity;
import com.avaritia.common.item.tools.InfinityArmorItem;
import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModDamageTypes;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 10:50
 * @Description:
 */
public class ToolUtils {
    public static final Set<TagKey<Block>> materialsPick = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_PICKAXE,
            Tags.Blocks.STONES, Tags.Blocks.STORAGE_BLOCKS,
            Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.ORES,
            BlockTags.SCULK_REPLACEABLE_WORLD_GEN,
            Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE,
            Tags.Blocks.COBBLESTONES_DEEPSLATE,
            BlockTags.FEATURES_CANNOT_REPLACE
    );

    public static final Set<TagKey<Block>> materialsAxe = Sets.newHashSet(
            BlockTags.LOGS,
            BlockTags.FALL_DAMAGE_RESETTING,
            BlockTags.LEAVES
    );

    public static final Set<TagKey<Block>> materialsShovel = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_SHOVEL
    );

    private static final List<String> projectileAntiImmuneEntities = Lists.newArrayList("minecraft:enderman", "minecraft:wither", "minecraft:ender_dragon", "draconicevolution:guardian_wither");

    /**
     * 列表中生物被弓箭攻击使用无尽伤害
     */
    /***
     * Common
     ***/
    public static boolean canUseTool(BlockState state, Set<TagKey<Block>> keySets) {
        return state.tags().collect(Collectors.toSet()).retainAll(keySets);
    }

    /**
     * 破坏方块
     *
     * @param world    世界
     * @param player   玩家
     * @param pos      点击坐标
     */
    public static void destroy(ServerLevel world, Player player, BlockPos pos) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, world.getBlockState(pos)));
    }

    /**
     * 是否穿着
     *
     * @param entity    生物
     * @param slot      装备槽
     * @param predicate 过滤
     * @return 是否穿着
     */
    public static boolean isPlayerWearing(LivingEntity entity, EquipmentSlot slot, Predicate<Item> predicate) {
        ItemStack stack = entity.getItemBySlot(slot);
        return !stack.isEmpty() && predicate.test(stack.getItem());
    }

    /**
     * 身穿全套无尽装备
     *
     * @param player 玩家
     * @return 是否身穿全套无尽装备
     */
    public static boolean isInfinite(LivingEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof InfinityArmorItem)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 身穿无尽头盔
     *
     * @param player 玩家
     * @return 是否身穿无尽头盔
     */
    public static boolean isWearingInfinityHelmet(LivingEntity player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return !helmet.isEmpty() && helmet.getItem() instanceof InfinityArmorItem;
    }

    /**
     * 身穿无尽胸甲
     *
     * @param player 玩家
     * @return 是否身穿无尽胸甲
     */
    public static boolean isWearingInfinityChestplate(LivingEntity player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        return !chestplate.isEmpty() && chestplate.getItem() instanceof InfinityArmorItem;
    }

    /**
     * 身穿无尽护腿
     *
     * @param player 玩家
     * @return 是否身穿无尽护腿
     */
    public static boolean isWearingInfinityPants(LivingEntity player) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        return !leggings.isEmpty() && leggings.getItem() instanceof InfinityArmorItem;
    }

    /**
     * 身穿无尽靴子
     *
     * @param player 玩家
     * @return 是否身穿无尽靴子
     */
    public static boolean isWearingInfinityBoots(LivingEntity player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        return !boots.isEmpty() && boots.getItem() instanceof InfinityArmorItem;
    }

    /**
     * 无尽镐范围挖掘
     *
     * @param player   玩家
     * @param startPos 起始坐标
     * @param range    挖掘范围
     */
    public static void destroyMaterialBlocks(ServerPlayer player, BlockPos startPos, int range) {
        ServerLevel world = player.level();

        int halfRange = range / 2;
        BlockPos minPos = startPos.offset(-halfRange, -halfRange, -halfRange);
        BlockPos maxPos = startPos.offset(halfRange, halfRange, halfRange);

        Set<ItemStack> drops = Sets.newHashSet();

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockPos currentPos = pos.immutable();
            BlockState state = world.getBlockState(currentPos);

            if (state.getBlock().canHarvestBlock(state, world, currentPos, player)) {

                List<ItemStack> blockDrops = Block.getDrops(state, world, currentPos, null);
                if (!blockDrops.isEmpty()) {
                    drops.addAll(blockDrops);
                } else {
                    var blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    Item blockItem = BuiltInRegistries.ITEM.getValue(blockKey);
                    if (blockItem != Items.AIR && blockItem != null) drops.add(new ItemStack(blockItem));
                }

                world.destroyBlock(currentPos, false, player);
                world.playSound(null, currentPos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }

        // 将所有掉落物合并为物质团
        ClustersUtils.spawnClusters(world, player, drops);
    }

    /**
     * 无尽铲范围挖掘
     *
     * @param player   玩家
     * @param startPos 起始坐标
     * @param range    挖掘范围
     */
    public static void destroyShovelBlocks(ServerPlayer player, BlockPos startPos, int range) {
        ServerLevel world = player.level();

        int halfRange = range / 2;
        BlockPos minPos = startPos.offset(-halfRange, -halfRange, -halfRange);
        BlockPos maxPos = startPos.offset(halfRange, halfRange, halfRange);

        Set<ItemStack> drops = Sets.newHashSet();

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockPos currentPos = pos.immutable();
            BlockState state = world.getBlockState(currentPos);

            if (state.is(BlockTags.MINEABLE_WITH_SHOVEL) && state.getBlock().canHarvestBlock(state, world, currentPos, player)) {

                List<ItemStack> blockDrops = Block.getDrops(state, world, currentPos, null);
                if (!blockDrops.isEmpty()) {
                    drops.addAll(blockDrops);
                } else {
                    var blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    Item blockItem = BuiltInRegistries.ITEM.getValue(blockKey);
                    if (blockItem != Items.AIR && blockItem != null) drops.add(new ItemStack(blockItem));
                }

                world.destroyBlock(currentPos, false, player);
                world.playSound(null, currentPos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }

        ClustersUtils.spawnClusters(world, player, drops);
    }

    public static void removeBlockWithDrops(ServerLevel world, Player player,
                                            BlockPos pos, ItemStack stack,
                                            Set<ItemStack> drops,
                                            Set<TagKey<Block>> validMaterials
    ) {
        if (!world.isLoaded(pos)) return;
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.is(Blocks.GRASS_BLOCK) && stack.is(ModItems.infinity_pickaxe.get())) {
            world.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }

        //if material contains
        if (!block.canHarvestBlock(state, world, pos, player) || !ToolUtils.canUseTool(state, validMaterials)) {
            return;
        }

        List<ItemStack> blockDrops = Block.getDrops(state, world, pos,
                null);

        if (!blockDrops.isEmpty()) {
            drops.addAll(blockDrops);
        } else {
            var blockKey = BuiltInRegistries.BLOCK.getKey(block);
            Item blockItem = BuiltInRegistries.ITEM.getValue(blockKey);
            drops.add(new ItemStack(blockItem));
        }

        if (!(block instanceof BaseFireBlock)) {
            world.levelEvent(2001, pos, Block.getId(state));
        }
        destroy(world, player, pos);
    }

    /**
     * 召唤箭
     *
     * @param shooter                  攻击者
     * @param level                    世界
     * @param piercedAndKilledEntities 无视护甲的实体
     * @param pickup                   拾起箭
     * @param randy                    随机
     * @param pos                      击中坐标
     */
    public static void arrowBarrage(Entity shooter, Level level, List<Entity> piercedAndKilledEntities, AbstractArrow.Pickup pickup, RandomSource randy, BlockPos pos) {
        for (int i = 0; i < 50; i++) {//50支箭
            double angle = randy.nextDouble() * 9 * Math.PI;
            double dist = randy.nextGaussian() * 0.8;

            double x = Math.sin(angle) * dist + pos.getX();
            double z = Math.cos(angle) * dist + pos.getZ();
            double y = pos.getY() + 25.0;//高度25

            double dangle = randy.nextDouble() * 9 * Math.PI;
            double dDist = randy.nextDouble() * 0.35;
            double dx = Math.sin(dangle) * dDist;
            double dz = Math.cos(dangle) * dDist;

            HeavenSubArrowEntity subArrow = new HeavenSubArrowEntity(level, shooter, x, y, z);
            subArrow.push(dx, -(randy.nextDouble() * 1.85 + 0.15), dz);
            subArrow.setCritArrow(true);//子箭必定暴击
            subArrow.setBaseDamage(ModConfig.subArrowDamage.get());
            subArrow.pickup = pickup;

            level.addFreshEntity(subArrow);
        }
    }


    public static DamageSource getArrowDamageSource(AbstractArrow arrow, Entity owner, Entity target) {
        DamageSource damagesource;
        if (owner == null) {
            damagesource = target.damageSources().arrow(arrow, arrow);
        } else {
            damagesource = target.damageSources().arrow(arrow, owner);
            if (owner instanceof LivingEntity livingEntity) {
                livingEntity.setLastHurtMob(target);
            }
        }

        if (owner != null && projectileAntiImmuneEntities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString())) {
            damagesource = ModDamageTypes.causeRandomDamage(owner);
        }
        return damagesource;
    }

    /**
     * 追踪箭
     *
     * @param result 命中结果
     * @param arrow  弓箭
     */
    public static void infinityTraceArrowDamage(@NotNull EntityHitResult result, TraceArrowEntity arrow) {
        arrow.infinityTraceArrowDamage(result);
    }

    public static void sweepAttack(Level level, LivingEntity livingEntity, Entity victim) {
        if (livingEntity instanceof Player player) {
            for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.entityInteractionRange()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                if (!player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand) livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.6F, Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
            double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
            double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        }
    }

    /**
     * 终望珍珠攻击
     *
     * @param player 玩家
     * @param stack  Pearl
     * @param world  世界
     */
    public static void pearlAttack(Player player, ItemStack stack, Level world) {
        if (!world.isClientSide()) {
            EndestPearlEntity pearl = ModEntityTypes.ENDER_PEARL.get().create(player.level(), EntitySpawnReason.EVENT);
            if (pearl != null) {
                pearl.setItem(stack);
                pearl.setShooter(player);
                pearl.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                world.addFreshEntity(pearl);
                player.getCooldowns().addCooldown(stack, 30);
            }
        }
        world.playSound(player, player.getOnPos(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
    }


    /**
     * 范围攻击
     *
     * @param player     玩家
     * @param range      范围
     * @param damage     伤害
     * @param hurtAnimal 是否攻击动物
     * @param lightOn    使用闪电
     */
    public static void aoeAttack(Player player, float range, float damage, boolean hurtAnimal, boolean lightOn) {
        if (player.level().isClientSide()) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.level().getEntities(player, aabb);
        DamageSource src = ModDamageTypes.causeRandomDamage(player);
        toAttack.stream()
                .filter(entity -> {
                    boolean attack = ModConfig.isSwordAttackItemEntity.get();
                    if (attack == false) {
                        return !(entity instanceof ItemEntity);
                    } else return true;
                })
                .filter(entity -> {
                    boolean attack = ModConfig.isSwordAttackProjectile.get();
                    if (attack == false) {
                        return !(entity instanceof Projectile);
                    } else return true;
                })
                .filter(entity -> !(entity.getClass().getSimpleName().equals("ImmortalItemEntity")))
                .filter(entity -> {
                    if (hurtAnimal) {
                        return true;
                    } else {
                        return entity instanceof Enemy && !entity.is(ModTags.NEUTRAL_CREATURES);
                    }
                })

                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity instanceof EnderDragon dragon) {
                            dragon.setHealth(0);
                        } else if (livingEntity instanceof WitherBoss wither) {
                            wither.setInvulnerableTicks(0);
                            wither.hurt(src, damage);
                        } else {
                            livingEntity.hurt(src, damage);
                        }
                    } else if (entity instanceof ExperienceOrb || entity instanceof AbstractArrow) {
                        entity.discard();
                    }else if(entity instanceof Projectile){
                        entity.discard();
                    }else if (entity instanceof Entity) {
                        entity.hurt(src, damage);
                    }
                    if (lightOn) trySummonLightning(player.level(), 1, entity.blockPosition(),
                            player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                });
    }

    /**
     * 尝试在指定位置召唤闪电
     *
     * @param level 世界对象，用于创建和添加实体
     * @param bolts 生成的闪电数量
     * @param hitPos 闪电生成的位置
     * @param thrower 可为空的服务器玩家对象，作为闪电的施放者
     * @return 如果成功生成至少一个闪电则返回true，否则返回false
     */
    public static boolean trySummonLightning(Level level, int bolts, BlockPos hitPos, @Nullable ServerPlayer thrower) {
        if (level instanceof ServerLevel serverLevel){
            boolean hasAction = false;
            for (int i = 0; i < bolts; i++) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.EVENT);
                if (lightning != null) {
                    lightning.moveOrInterpolateTo(Vec3.atBottomCenterOf(hitPos));
                    lightning.setCause(thrower);
                    serverLevel.addFreshEntity(lightning);
                }
                hasAction = true;
            }
            return hasAction;
        }
        return false;
    }

    /**
     * 范围收获
     *
     * @param serverLevel 世界
     * @param player      玩家
     * @param stack       使用工具
     * @param blockPos    点击位置
     * @param rang        范围
     * @param height      高度
     */
    public static void rangeHarvest(ServerLevel serverLevel, Player player, ItemStack stack, BlockPos blockPos, int rang, int height) {
        BlockPos minPos = blockPos.offset(-rang, -height, -rang);
        BlockPos maxPos = blockPos.offset(rang, height, rang);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = serverLevel.getBlockState(pos);
            Block block = state.getBlock();
            Map<ItemStack, Integer> map = new HashMap<>();
            //harvest
            if (block instanceof CropBlock cropBlock) { //common
                if (cropBlock instanceof BeetrootBlock ? state.getValue(BeetrootBlock.AGE) >= 3 : state.getValue(CropBlock.AGE) >= 7) {
                    ClustersUtils.putMapDrops(serverLevel, pos, player, stack, map);
                    serverLevel.setBlock(pos, state.setValue(block instanceof BeetrootBlock ? BeetrootBlock.AGE : CropBlock.AGE, 0), 11);
                }
            }
            if (block instanceof CocoaBlock) { //coca
                if (state.getValue(CocoaBlock.AGE) >= 2) {
                    ClustersUtils.putMapDrops(serverLevel, pos, player, stack, map);
                    serverLevel.setBlock(pos, state.setValue(CocoaBlock.AGE, 0), 11);
                }
            }
            if (block instanceof StemBlock) { //pumpkin
                ClustersUtils.putMapDrops(serverLevel, pos, player, stack, map);
                serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            }
            if (block instanceof SweetBerryBushBlock) { //SweetBerry
                if (state.getValue(SweetBerryBushBlock.AGE) >= 3) {
                    ClustersUtils.putMapDrops(serverLevel, pos, player, stack, map);
                    serverLevel.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 0), 11);
                }
            }
            if (block instanceof NetherWartBlock) { //NetherWart
                if (state.getValue(NetherWartBlock.AGE) >= 3) {
                    ClustersUtils.putMapDrops(serverLevel, pos, player, stack, map);
                    serverLevel.setBlock(pos, state.setValue(NetherWartBlock.AGE, 0), 11);
                }
            }
            ClustersUtils.spawnClusters(serverLevel, player, map);
        }
    }

    /**
     * 范围催熟
     *
     * @param serverLevel 世界
     * @param blockPos    点击位置
     * @param rang        范围
     * @param height      高度
     * @param cost        次数
     */
    public static void rangeBonemealable(ServerLevel serverLevel, BlockPos blockPos, int rang, int height, int cost) {
        BlockPos minPos = blockPos.offset(-rang, -height, -rang);
        BlockPos maxPos = blockPos.offset(rang, height, rang);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = serverLevel.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BonemealableBlock bonemealableBlock && !(block instanceof GrassBlock)
                    && bonemealableBlock.isValidBonemealTarget(serverLevel, pos, state)

            ) {
                for (int i = 0; i < cost; i++) {
                    bonemealableBlock.performBonemeal(serverLevel, serverLevel.getRandom(), pos, state);
                    serverLevel.levelEvent(2005, pos, 0);
                }
            }
        }
    }

    /***
     * Axe
     ***/
    public static boolean canHarvest(BlockPos pos, Level world) {
        if (!isLogOrLeaves(world, pos)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.getProperties().stream().anyMatch(p -> p.equals(RotatedPillarBlock.AXIS))) {
            return state.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.Y);
        }

        return true;
    }


    /**
     * 连锁砍树
     *
     * @param player   玩家
     * @param world    世界
     * @param pos      点击坐标
     */
    public static void destroyTree(Player player, ServerLevel world, BlockPos pos, BlockState state) {

        int maxBlocks = ModConfig.axeChainCount.get();

        List<BlockPos> connectedLogs = getConnectedLogs(world, pos);
        Set<ItemStack> drops = Sets.newHashSet();
        int blockCount = 0; // 记录已砍伐的方块数量

        for (BlockPos logPos : connectedLogs) {
            // 如果已达到最大砍伐数量，则停止砍伐
            if (blockCount >= maxBlocks) {
                break;
            }

            BlockState logState = world.getBlockState(logPos);
            List<ItemStack> blockDrops = Block.getDrops(logState, world, logPos, null);

            if (!blockDrops.isEmpty()) {
                drops.addAll(blockDrops);
            } else {
                var blockKey = BuiltInRegistries.BLOCK.getKey(logState.getBlock());
                Item blockItem = BuiltInRegistries.ITEM.getValue(blockKey);
                if (blockItem != Items.AIR && blockItem != null) {
                    drops.add(new ItemStack(blockItem));
                }
            }

            // 破坏方块并播放音效
            world.levelEvent(2001, logPos, Block.getId(logState));
            destroy(world, player, logPos);

            blockCount++; // 增加已砍伐数量
        }

        // 将所有掉落物合并为物质团
        ClustersUtils.spawnClusters(world, player, drops);
    }

    private static List<BlockPos> getConnectedLogs(Level world, BlockPos pos) {
        BlockPosList positions = new BlockPosList();
        collectLogs(world, pos, positions);
        return positions;
    }

    private static void collectLogs(Level world, BlockPos pos, BlockPosList positions) {
        List<BlockPos> posList = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    if (isLogOrLeaves(world, p)) {
                        if (positions.add(p)) {
                            posList.add(p);
                        }
                    }
                }
            }
        }

        for (BlockPos p : posList) {
            collectLogs(world, p, positions);
        }
    }

    private static boolean isLogOrLeaves(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return b.is(BlockTags.LOGS) || b.is(BlockTags.LEAVES);
    }

    /**
     * 获取玩家背包中的图腾
     *
     * @param player 玩家
     * @return 图腾
     */
    public static ItemStack getPlayerTotemItem(Player player) {
        return InventoryUtils.findItemInInv(player, stack -> stack.is(ModItems.infinity_totem.get()), stack -> stack);
    }

    /**
     * 炽热 自动识别可进行的熔炉配方进行处理（如：原矿-矿物锭）
     *
     * @param state  原矿状态
     * @param world  世界
     * @param pos    点击坐标
     * @param player 玩家
     * @param tool   使用的工具
     */
    public static void melting(BlockState state, ServerLevel world, BlockPos pos, Player player, ItemStack tool) {
        if (!state.getBlock().canHarvestBlock(state, world, pos, player) || state.getBlock() instanceof CropBlock) return;
        List<ItemStack> drops = Block.getDrops(state, world, pos, null);
        Holder<Enchantment> fortune =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FORTUNE);
        int unLuck = EnchantmentHelper.getTagEnchantmentLevel(fortune, tool);
        //霉运影响
        boolean flag = unLuck > 0 && world.getRandom().nextDouble() < unLuck * 0.2; //霉运判断结果 true触发
        if (drops.isEmpty() || flag) return;
        drops.forEach(itemStack -> {
            ItemStack dropStack = getMeltingItem(player, world, itemStack, tool);
            if (!dropStack.equals(itemStack)) {
                ToolUtils.meltingAchieve(world, player, pos);
                world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack));
            }
        });
    }

    /**
     * 获取物品烧炼后产物
     *
     * @param world     world
     * @param itemStack 烧炼前物品
     * @param tool      使用工具
     * @return 烧炼产物
     */
    public static ItemStack getMeltingItem(Player player, ServerLevel world, ItemStack itemStack, ItemStack tool) {
        ItemStack dropStack = world.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), world)
                .map(smeltingRecipe -> smeltingRecipe.value().getResultItem(world.registryAccess())).filter(e -> !e.isEmpty())
                .map(e -> e.copyWithCount(tool.getCount() * e.getCount()))
                .orElse(itemStack);
        Holder<Enchantment> fortuneEnchant =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.FORTUNE);
        int fortune = EnchantmentHelper.getTagEnchantmentLevel(fortuneEnchant, tool);
        if (fortune > 0) { //时运影响产物数量
            RandomSource random = RandomSource.create();
            int count = 1;
            if (random.nextDouble() < 0.3 + fortune * 0.1)
                count += Mth.nextInt(random, 0, fortune + 1);
            if (random.nextDouble() < 0.1 + fortune * 0.05) { //触发暴击
                count *= Mth.nextInt(random, 1, fortune);
            }
            dropStack.setCount(count);
        }
        return dropStack;
    }


    /**
     * 熔炼附魔的伪实现 通过取消方块破坏事件，同时生成掉落物
     *
     * @param world  世界
     * @param player 玩家
     * @param pos    坐标
     */
    public static void meltingAchieve(ServerLevel world, Player player, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.FLAME, pos.getX() + world.getRandom().nextDouble(), pos.getY() + 1d,
                    pos.getZ() + world.getRandom().nextDouble(), 1, 0, 0);
        }
        world.playSound(player, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); //设置此坐标为空气
    }

    /**
     * 发射剑气
     *
     * @param stack  工具
     * @param player 玩家
     */
    public static void shootBladeSlash(ItemStack stack, Player player) {
        Level world = player.level();
        Holder<Enchantment> sweeping_edge =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SWEEPING_EDGE);
        BladeSlashEntity projectile = new BladeSlashEntity(world, player, EnchantmentHelper.getTagEnchantmentLevel(sweeping_edge, stack));
        world.addFreshEntity(projectile);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.swing(player.getUsedItemHand());
    }

    private static class BlockPosList extends ArrayList<BlockPos> {
        @Override
        public boolean add(BlockPos pos) {
            if (!contains(pos)) {
                return super.add(pos);
            }
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return stream().anyMatch(pos1 -> pos1.equals(o));
        }
    }


    /**
     * 加速方块实体和更新
     * @param pos 被加速方块位置
     * @param level 世界
     * @param speed 速度
     * @param randomTicks 随机刻
     * from Torcherino
     */
    @Deprecated
    public static void speedBlockTick(BlockPos pos, ServerLevel level, int speed, int randomTicks) {
        int random_tick_rate = 4;
        var targetState = level.getBlockState(pos);
        var targetBlock = targetState.getBlock();
        if (!(targetBlock instanceof EntityBlock entityBlock)) {
            return;
        }
        if (level instanceof ServerLevel
                && targetBlock.isRandomlyTicking
                && level.getRandom().nextInt(Mth.clamp(4096 / (speed * random_tick_rate), 1, 4096)) < randomTicks) {
            targetState.randomTick(level, pos, level.getRandom());
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            //noinspection unchecked
            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) entityBlock.getTicker(level, targetState, blockEntity.getType());
            if (blockEntity.isRemoved() || ticker == null) {
                return;
            }
            for (int i = 0; i < speed; i++) {
                if (blockEntity.isRemoved()) {
                    break;
                }
                ticker.tick(level, pos, targetState, blockEntity);
            }
        }
    }

    /**
     * 加速方块实体和更新
     *
     * @param level 世界
     * @param pos   被加速方块位置
     * @param be    被加速的实体
     * @param times 随机刻
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void accelerateBlockEntity(ServerLevel level, BlockPos pos, BlockEntity be, int times) {
        if (be.isRemoved()) return;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof EntityBlock entityBlock) {
            BlockEntityType type = be.getType();
            BlockEntityTicker ticker = entityBlock.getTicker(level, state, type);

            if (ticker != null) {
                for (int i = 0; i < times; i++) {
                    ticker.tick(level, pos, state, be);
                    if (be.isRemoved()) break;
                }
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }
}
