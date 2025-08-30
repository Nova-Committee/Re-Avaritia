package committee.nova.mods.avaritia.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.api.utils.InventoryUtils;
import committee.nova.mods.avaritia.api.utils.math.RayTracer;
import committee.nova.mods.avaritia.common.entity.BladeSlashEntity;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.entity.arrow.TraceArrowEntity;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.ItemCaptureHandler;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTags;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static committee.nova.mods.avaritia.util.ClustersUtils.defaultTrashOres;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 10:50
 * @Description:
 */
public class ToolUtils {
    public static final Set<TagKey<Block>> materialsPick = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_PICKAXE,
            Tags.Blocks.STONE, Tags.Blocks.STORAGE_BLOCKS,
            Tags.Blocks.GLASS, Tags.Blocks.ORES,
            BlockTags.SCULK_REPLACEABLE_WORLD_GEN,
            Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE,
            Tags.Blocks.COBBLESTONE_DEEPSLATE,
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
    /**
     * 列表中生物被弓箭攻击使用无尽伤害
     */
    private static final List<String> projectileAntiImmuneEntities = Lists.newArrayList("minecraft:enderman", "minecraft:wither", "minecraft:ender_dragon", "draconicevolution:guardian_wither");

    /***
     * Common
     * ***/
    public static boolean canUseTool(BlockState state, Set<TagKey<Block>> keySets) {
        return state.getTags().collect(Collectors.toSet()).retainAll(keySets);
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
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
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
     * 无尽镐And无尽铲破坏
     * @param player 玩家
     * @param world 世界
     * @param startPos 起始坐标
     * @param range 挖掘范围
     */
    public static void destroyOres(ServerPlayer player, ServerLevel world, BlockPos startPos, int range) {
        Set<BlockPos> processedPos = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        processedPos.add(startPos);

        // 收集所有掉落物
        Set<ItemStack> drops = Sets.newHashSet();

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);

            // 仅处理可被镐挖掘的方块（不排除任何符合条件的方块）
            if (ToolUtils.canUseTool(state, materialsPick) && state.getBlock().canHarvestBlock(state, world, pos, player)) {
                // 收集掉落物
                List<ItemStack> blockDrops = Block.getDrops(state, world, pos, null);
                if (!blockDrops.isEmpty()) {
                    drops.addAll(blockDrops);
                } else {
                    ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    Item blockItem = BuiltInRegistries.ITEM.get(blockKey);
                    if (blockItem != Items.AIR) {
                        drops.add(new ItemStack(blockItem));
                    }
                }

                // 破坏方块
                world.levelEvent(2001, pos, Block.getId(state));
                destroy(world, player, pos);

                // 递归处理周围方块（范围限制内）
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    if (!processedPos.contains(neighborPos) &&
                            neighborPos.distManhattan(startPos) <= range &&
                            world.isLoaded(neighborPos)) {
                        processedPos.add(neighborPos);
                        queue.add(neighborPos);
                    }
                }
            }
        }

        // 将所有掉落物合并为物质团
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
        if (world.isClientSide) return;

        if (state.is(Blocks.GRASS) && stack.is(ModItems.infinity_pickaxe.get())) {
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
            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);

            Item blockItem = BuiltInRegistries.ITEM.get(blockKey);
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
            subArrow.piercedAndKilledEntities = piercedAndKilledEntities;
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

        if (owner != null && projectileAntiImmuneEntities.contains(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(target.getType())).toString())) {
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

        Entity entity = result.getEntity();
        float f = (float) arrow.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double) f * arrow.getBaseDamage(), 0.0D, 2.147483647E9D));
        Entity owner = arrow.getOwner() == null ? arrow : arrow.getOwner();
        if (arrow.getPierceLevel() > 0) {
            if (arrow.piercingIgnoreEntityIds == null) {
                arrow.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (arrow.piercedAndKilledEntities == null) {
                arrow.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (arrow.piercingIgnoreEntityIds.size() >= arrow.getPierceLevel() + 1) {
                arrow.discard();
                return;
            }

            arrow.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (arrow.isCritArrow()) {
            long j = arrow.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        DamageSource damagesource = ToolUtils.getArrowDamageSource(arrow, owner, entity);
        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (arrow.isOnFire() && !isEnderman) {
            entity.setSecondsOnFire(5);
        }

        if (entity instanceof Player player) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
                player.getCooldowns().addCooldown(player.getUseItem().getItem(), 100);
                arrow.level().broadcastEntityEvent(player, (byte) 30);
                player.stopUsingItem();
            }
        }

        if (entity.hurt(damagesource, (float) i)) {
            if (entity instanceof LivingEntity livingentity) {
                if (!arrow.level().isClientSide && arrow.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (arrow.knockback > 0) {
                    Vec3 vector3d = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) arrow.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        livingentity.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!arrow.level().isClientSide && owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingentity);
                }

                arrow.doPostHurtEffects(livingentity);
                if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer serverPlayer && !arrow.isSilent()) {
                    serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && arrow.piercedAndKilledEntities != null) {
                    arrow.piercedAndKilledEntities.add(livingentity);
                }

                if (!arrow.level().isClientSide && owner instanceof ServerPlayer serverPlayer) {
                    if (arrow.piercedAndKilledEntities != null && arrow.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, arrow.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && arrow.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, List.of(entity));
                    }
                }
            }

            arrow.playSound(arrow.getHitGroundSoundEvent(), 1.0F, 1.2F / (arrow.random.nextFloat() * 0.2F + 0.9F));
            if (arrow.getPierceLevel() <= 0) {
                arrow.setDeltaMovement(entity.getDeltaMovement().scale(0.0D));
                arrow.setPos(entity.position());
                arrow.seekNextTarget();
                arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        } else {
            entity.setRemainingFireTicks(k);
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(0.0D));
            arrow.setYRot(arrow.getYRot() + 180.0F);
            arrow.setPos(entity.position());
            arrow.yRotO += 180.0F;
            if (!arrow.level().isClientSide && arrow.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (arrow.pickup == AbstractArrow.Pickup.ALLOWED) {
                    arrow.spawnAtLocation(arrow.getPickupItem(), 0.1F);
                }
                arrow.seekNextTarget();
                arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        }
    }

    /**
     * 横扫攻击
     *
     * @param level        世界
     * @param livingEntity 玩家
     * @param victim       被攻击者
     */
    public static void sweepAttack(Level level, LivingEntity livingEntity, Entity victim) {
        if (livingEntity instanceof Player player) {
            for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.getEntityReach()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
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
        if (!world.isClientSide) {
            EndestPearlEntity pearl = ModEntities.ENDER_PEARL.get().create(player.level());
            if (pearl != null) {
                pearl.setItem(stack);
                pearl.setShooter(player);
                pearl.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                world.addFreshEntity(pearl);
                player.getCooldowns().addCooldown(stack.getItem(), 30);
            }
        }
        world.playSound(player, player.getOnPos(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
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
        if (player.level().isClientSide) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.level().getEntities(player, aabb);
        DamageSource src = player.damageSources().source(ModDamageTypes.INFINITY, player, player);
        toAttack.stream()
                .filter(entity -> entity instanceof Mob)
                .filter(entity -> !entity.getType().is(ModTags.NEUTRAL_CREATURES))
                .filter(entity -> !(entity instanceof Villager))
                .forEach(entity -> {
                    if (entity instanceof Mob mob) {
                        if (mob instanceof Animal animal && hurtAnimal) {
                            animal.hurt(src, damage);
                        } else if (mob instanceof EnderDragon dragon) {
                            dragon.hurt(dragon.head, src, Float.POSITIVE_INFINITY);
                        } else if (mob instanceof WitherBoss wither) {
                            wither.setInvulnerableTicks(0);
                            wither.hurt(src, damage);
                        } else if (!(mob instanceof Animal)) {
                            mob.hurt(src, damage);
                        }
                    }
                    LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(player.level());
                    if (lightOn && lightningbolt != null) {
                        if (!(entity instanceof Animal && hurtAnimal)) {
                            lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                            lightningbolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                            player.level().addFreshEntity(lightningbolt);
                        }
                    }
        });
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
            if (block instanceof StemGrownBlock) { //pumpkin
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
                    && bonemealableBlock.isValidBonemealTarget(serverLevel, pos, state, false)
                    && ForgeHooks.onCropsGrowPre(serverLevel, pos, state, true)
            ) {
                for (int i = 0; i < cost; i++) {
                    bonemealableBlock.performBonemeal(serverLevel, serverLevel.random, pos, state);
                    serverLevel.levelEvent(2005, pos, 0);
                    ForgeHooks.onCropsGrowPost(serverLevel, pos, state);
                }
            }
        }
    }

    /***
     * Axe
     * ***/
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
     * @param state    方块状态
     */
    public static void destroyTree(Player player, ServerLevel world, BlockPos pos, BlockState state) {
        List<BlockPos> connectedLogs = getConnectedLogs(world, pos);
        Set<ItemStack> drops = Sets.newHashSet();
        for (BlockPos logPos : connectedLogs) {
            List<ItemStack> blockDrops = Block.getDrops(world.getBlockState(logPos), world, logPos,
                    null);
            if (!blockDrops.isEmpty()) {
                drops.addAll(blockDrops);
            } else {
                ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(world.getBlockState(logPos).getBlock());

                Item blockItem = BuiltInRegistries.ITEM.get(blockKey);
                drops.add(new ItemStack(blockItem));
            }
            world.levelEvent(2001, pos, Block.getId(state));
            destroy(world, player, logPos);
        }
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
     * @param block  原矿方块
     * @param state  原矿状态
     * @param world  世界
     * @param pos    点击坐标
     * @param player 玩家
     * @param tool   使用的工具
     * @param event  破坏事件
     */
    public static void melting(Block block, BlockState state, Level world, BlockPos pos, Player player, ItemStack tool, BlockEvent.BreakEvent event) {
        if (!block.canHarvestBlock(state, world, pos, player) || block instanceof CropBlock) return;
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, pos, null);
        int unLuck = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        //霉运影响
        boolean flag = unLuck > 0 && world.random.nextDouble() < unLuck * 0.2; //霉运判断结果 true触发
        if (drops.isEmpty() || flag) return;
        drops.forEach(itemStack -> {
            ItemStack dropStack = getMeltingItem(world, itemStack, tool);
            if (!dropStack.equals(itemStack)) {
                ToolUtils.meltingAchieve(world, player, pos, event);
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
    public static ItemStack getMeltingItem(Level world, ItemStack itemStack, ItemStack tool) {
        ItemStack dropStack = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), world)
                .map(smeltingRecipe -> smeltingRecipe.getResultItem(world.registryAccess())).filter(e -> !e.isEmpty())
                .map(e -> ItemHandlerHelper.copyStackWithSize(e, tool.getCount() * e.getCount()))
                .orElse(itemStack);
        int fortune = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
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
     * @param event  事件
     *               from <a href="https://github.com/yuoft/MoreEnchants/blob/master/src/main/java/com/yuo/enchants/Event/EventHelper.java">...</a>
     */
    public static void meltingAchieve(Level world, Player player, BlockPos pos, BlockEvent.BreakEvent event) {
        if (!world.isClientSide) {
            ServerLevel serverWorld = (ServerLevel) world;
            for (int i = 0; i < 10; i++) {
                serverWorld.addParticle(ParticleTypes.FLAME, pos.getX() + world.random.nextDouble(), pos.getY() + 1d,
                        pos.getZ() + world.random.nextDouble(), 1, 0, 0);
            }
        }
        world.playSound(player, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        world.setBlockAndUpdate(event.getPos(), Blocks.AIR.defaultBlockState()); //设置此坐标为空气
    }

    /**
     * 发射剑气
     *
     * @param stack  工具
     * @param player 玩家
     */
    public static void shootBladeSlash(ItemStack stack, Player player) {
        Level world = player.level();
        BladeSlashEntity projectile = new BladeSlashEntity(world, player, EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SWEEPING_EDGE, stack));
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
     *已弃用
     */
    public static void speedBlockTick(BlockPos pos, ServerLevel level, int speed, int randomTicks) {
        int random_tick_rate = 4;
        var targetState = level.getBlockState(pos);
        var targetBlock = targetState.getBlock();
        if (!(targetBlock instanceof EntityBlock entityBlock)) {
            return;
        }
        if (level instanceof ServerLevel && targetBlock.isRandomlyTicking(targetState) &&
                level.getRandom().nextInt(Mth.clamp(4096 / (speed * random_tick_rate), 1, 4096)) < randomTicks) {
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
}
