package committee.nova.mods.avaritia.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenSubArrowEntity;
import committee.nova.mods.avaritia.common.item.InfinityArmorItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.ItemCaptureHandler;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.math.RayTracer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:50
 * Version: 1.0
 */
public class ToolUtils {
    public static final Set<TagKey<Block>> materialsPick = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_PICKAXE,
            Tags.Blocks.STONE, Tags.Blocks.STORAGE_BLOCKS,
            Tags.Blocks.GLASS, Tags.Blocks.ORES,
            BlockTags.SCULK_REPLACEABLE_WORLD_GEN,
            Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE,
            Tags.Blocks.COBBLESTONE_DEEPSLATE

    );

    public static final Set<TagKey<Block>> materialsAxe = Sets.newHashSet(
            BlockTags.LOGS,
            BlockTags.FALL_DAMAGE_RESETTING,
            BlockTags.LEAVES
    );

    public static final Set<TagKey<Block>> materialsShovel = Sets.newHashSet(
            BlockTags.MINEABLE_WITH_SHOVEL
    );

    public static boolean canUseTool(BlockState state, Set<TagKey<Block>> keySets){
        return state.getTags().collect(Collectors.toSet()).retainAll(keySets);
    }

    /***
     * Equipment
     * ***/
    public static boolean isPlayerWearing(LivingEntity entity, EquipmentSlot slot, Predicate<Item> predicate) {
        ItemStack stack = entity.getItemBySlot(slot);
        return !stack.isEmpty() && predicate.test(stack.getItem());
    }

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


    /***
     * PickAxe
     * ***/
    public static void breakRangeBlocks(Player player, ItemStack stack, BlockPos pos, int range, Set<TagKey<Block>> keySets, boolean filterTrash) {
        BlockHitResult traceResult = RayTracer.retrace(player, range);
        var world = player.level();
        var state = world.getBlockState(pos);

        if (state.isAir()) {
            return;
        }

        var doY = traceResult.getDirection().getAxis() != Direction.Axis.Y;

        var minOffset = new BlockPos(-range, doY ? -1 : -range, -range);
        var maxOffset = new BlockPos(range, doY ? range * 2 - 2 : range, range);

        ToolUtils.breakBlocks(world, player, stack, pos, minOffset, maxOffset, keySets, filterTrash);
    }

    private static void breakBlocks(Level world, Player player, ItemStack stack, BlockPos origin, BlockPos min, BlockPos max, Set<TagKey<Block>> validMaterials, boolean filterTrash) {

        ItemCaptureHandler.enableItemCapture(true);//开启凋落物收集

        for (int lx = min.getX(); lx < max.getX(); lx++) {
            for (int ly = min.getY(); ly < max.getY(); ly++) {
                for (int lz = min.getZ(); lz < max.getZ(); lz++) {
                    BlockPos pos = origin.offset(lx, ly, lz);
                    removeBlockWithDrops(world, player, pos, stack, validMaterials);
                }
            }
        }

        ItemCaptureHandler.enableItemCapture(false);//关闭凋落物收集

        Set<ItemStack> drops = ItemCaptureHandler.getCapturedDrops();

        if (filterTrash) {//是否是黑名单
            ClustersUtils.removeTrash(drops);
        }

        ClustersUtils.spawnClusters(world, player, drops);

    }

    public static void removeBlockWithDrops(Level world, Player player, BlockPos pos, ItemStack stack, Set<TagKey<Block>> validMaterials) {
        if (!world.isLoaded(pos)) {
            return;
        }
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

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            if (!player.isCreative()) {//not creative
                BlockEntity tile = world.getBlockEntity(pos);
                block.playerWillDestroy(world, pos, state, player);
                if (block.onDestroyedByPlayer(state, world, pos, player, true, world.getFluidState(pos))) {
                    block.playerDestroy(world, player, pos, state, tile, stack);
                }
            } else {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

    }


    /***
     * Arrow
     * ***/
    private static final List<String> projectileAntiImmuneEntities = Lists.newArrayList("minecraft:enderman", "minecraft:wither", "minecraft:ender_dragon", "draconicevolution:guardian_wither");

    public static void arrowBarrage(Entity shooter, Level level, List<Entity> piercedAndKilledEntities, AbstractArrow.Pickup pickup, RandomSource randy, BlockPos pos) {
        for (int i = 0; i < 30; i++) {//30支箭
            double angle = randy.nextDouble() * 2 * Math.PI;
            double dist = randy.nextGaussian() * 0.5;

            double x = Math.sin(angle) * dist + pos.getX();
            double z = Math.cos(angle) * dist + pos.getZ();
            double y = pos.getY() + 25.0;//高度25

            double dangle = randy.nextDouble() * 2 * Math.PI;
            double ddist = randy.nextDouble() * 0.35;
            double dx = Math.sin(dangle) * ddist;
            double dz = Math.cos(dangle) * ddist;

            HeavenSubArrowEntity subArrow = HeavenSubArrowEntity.create(level, x, y, z);
            if (shooter != null) subArrow.setOwner(shooter);
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

    public static void infinityArrowDamage(@NotNull EntityHitResult result, Arrow arrow) {

        Entity entity = result.getEntity();
        float f = (float)arrow.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double)f * arrow.getBaseDamage(), 0.0D, 2.147483647E9D));
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
            i = (int)Math.min(j + (long)i, 2147483647L);
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
                arrow.level().broadcastEntityEvent(player, (byte)30);
                player.stopUsingItem();
            }
        }

        if (entity.hurt(damagesource, (float)i)) {
            if (entity instanceof LivingEntity livingentity) {
                if (!arrow.level().isClientSide && arrow.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (arrow.knockback > 0) {
                    Vec3 vector3d = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)arrow.knockback * 0.6D);
                    if (vector3d.lengthSqr() > 0.0D) {
                        livingentity.push(vector3d.x, 0.1D, vector3d.z);
                    }
                }

                if (!arrow.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity)owner, livingentity);
                }

                arrow.doPostHurtEffects(livingentity);
                if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer && !arrow.isSilent()) {
                    ((ServerPlayer)owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
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
                arrow.discard();
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
                arrow.discard();
            }
        }
    }

    /***
     * Sword
     * ***/
    public static void sweepAttack(Level level, LivingEntity livingEntity, LivingEntity victim) {
        if (livingEntity instanceof Player player){
            for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.getEntityReach()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                if (!player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand)livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.6F, Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float)Math.PI / 180F)));
                    victim.setHealth(0);
                    victim.die(player.damageSources().source(ModDamageTypes.INFINITY, player, victim));
                }
            }
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, livingEntity.getSoundSource(), 1.0F, 1.0F);
            double d0 = -Mth.sin(player.getYRot() * ((float)Math.PI / 180F));
            double d1 = Mth.cos(player.getYRot() * ((float)Math.PI / 180F));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        }
    }

    public static void aoeAttack(Player player, float range, float damage, boolean hurtAnimal, boolean lightOn) {
        if (player.level().isClientSide) return;
        AABB aabb = player.getBoundingBox().deflate(range);
        List<Entity> toAttack = player.level().getEntities(player, aabb);
        DamageSource src = player.damageSources().source(ModDamageTypes.INFINITY, player, player);
        toAttack.stream().filter(entity -> entity instanceof Mob).forEach(entity -> {
            if (entity instanceof Mob mob) {
                if (hurtAnimal && mob instanceof Animal animal) {
                    animal.hurt(src, damage);
                } else if (mob instanceof EnderDragon dragon) {
                    dragon.hurt(dragon.head, src, Float.POSITIVE_INFINITY);
                } else if (mob instanceof WitherBoss wither) {
                    wither.setInvulnerableTicks(0);
                    wither.hurt(src, damage);
                } else if (!(mob instanceof Animal)){
                    mob.hurt(src, damage);
                }
            }
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (hurtAnimal && lightOn && lightningbolt != null) {
                lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                lightningbolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                player.level().addFreshEntity(lightningbolt);
            }
        });
    }



    /***
     * Axe
     * ***/
    public static boolean canHarvest(BlockPos pos, Level world) {
        if (!isLog(world, pos)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.getProperties().stream().anyMatch(p -> p.equals(RotatedPillarBlock.AXIS))) {
            return state.getValue(RotatedPillarBlock.AXIS).equals(Direction.Axis.Y);
        }

        return true;
    }


    public static void destroyTree(Player player, Level world, BlockPos pos, ItemStack heldItem) {
        List<BlockPos> connectedLogs = getConnectedLogs(world, pos);

        for (BlockPos logPos : connectedLogs) {
            destroy(world, player, logPos, heldItem);
        }
    }

    private static void destroy(Level world, Player player, BlockPos pos, ItemStack heldItem) {
        if (heldItem != null) {
            heldItem.getItem().mineBlock(heldItem, world, world.getBlockState(pos), pos, player);
            world.destroyBlock(pos, true);
        }
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
                    if (isLog(world, p)) {
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

    private static boolean isLog(Level world, BlockPos pos) {
        BlockState b = world.getBlockState(pos);
        return b.is(BlockTags.LOGS);
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
}
