package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class InfinityThrownTrident extends AbstractArrow implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> CHANNELING = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOCKWAVE = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(InfinityThrownTrident.class, EntityDataSerializers.INT);
    private static final double RETURN_PICKUP_DISTANCE = 1.25D;
    private static final int RETURN_FORCE_PICKUP_TICKS = 30;
    private static final int NO_RETURN_SLOT = -1;

    private ItemStack tridentItem = new ItemStack(ModItems.infinity_trident.get());
    private boolean dealtDamage;
    private int loyaltyLevel = 3;
    private int returnSlot = NO_RETURN_SLOT;
    public int returningTicks;

    public InfinityThrownTrident(EntityType<? extends InfinityThrownTrident> type, Level worldIn) {
        super(type, worldIn);
    }

    public InfinityThrownTrident(Level world, LivingEntity thrower, ItemStack thrownStackIn, @Nullable ItemStack firedFromWeapon) {
        super(ModEntityTypes.INFINITY_THROWN_TRIDENT.get(), thrower, world, thrownStackIn, firedFromWeapon);
        setStackAndLoyalty(thrownStackIn.copy());
    }

    private void setStackAndLoyalty(@NotNull ItemStack stack) {
        if (!stack.isEmpty() && stack.is(ModItems.infinity_trident.get())) {
            tridentItem = stack;
        }
    }

    public void setLoyaltyLevel(int loyaltyLevel) {
        this.loyaltyLevel = loyaltyLevel;
    }

    public void setReturnSlot(int returnSlot) {
        this.returnSlot = returnSlot;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOCKWAVE, false);
        builder.define(CHANNELING, false);
        builder.define(RADIUS, 1);
    }


    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        if (this.loyaltyLevel > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptableReturnOwner()) {
                this.dropAndDiscard();
                return;
            }

            if (this.returnToOwner(owner)) {
                return;
            }
        }

        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity entity = getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        }
        return false;
    }

    private boolean returnToOwner(Entity owner) {
        if (owner instanceof Player player && this.tryCloseOwnerPickup(player)) {
            return true;
        }

        if (!(owner instanceof Player) && this.position().distanceTo(owner.getEyePosition()) < owner.getBbWidth() + 1.0D) {
            this.discard();
            return true;
        }

        this.setNoPhysics(true);
        Vec3 returnVector = owner.getEyePosition().subtract(this.position());
        if (returnVector.lengthSqr() < 1.0E-7D) {
            ++this.returningTicks;
            return owner instanceof Player player && this.tryOwnerPickupOrDrop(player);
        }

        this.setPosRaw(this.getX(), this.getY() + returnVector.y * 0.015D * this.loyaltyLevel, this.getZ());
        if (this.level().isClientSide()) {
            this.yOld = this.getY();
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.95D)
                .add(returnVector.normalize().scale(0.05D * this.loyaltyLevel)));
        if (this.returningTicks == 0) {
            this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
        }

        ++this.returningTicks;
        return false;
    }

    private boolean tryCloseOwnerPickup(Player player) {
        double pickupDistanceSqr = RETURN_PICKUP_DISTANCE * RETURN_PICKUP_DISTANCE;
        if (this.position().distanceToSqr(player.getEyePosition()) > pickupDistanceSqr) {
            return false;
        }

        return this.tryOwnerPickupOrDrop(player);
    }

    private boolean tryOwnerPickupOrDrop(Player player) {
        if (this.level().isClientSide()) {
            return false;
        }

        if (this.tryPickup(player)) {
            player.take(this, 1);
            this.discard();
            return true;
        }

        /* 忠诚返航已经贴到玩家身边仍无法入包时，落成物品实体，避免三叉戟持续卡在碰撞箱边缘。 */
        if (this.returningTicks >= RETURN_FORCE_PICKUP_TICKS) {
            this.dropAndDiscard();
            return true;
        }

        return false;
    }

    private void dropAndDiscard() {
        if (this.level() instanceof ServerLevel serverLevel && this.pickup == Pickup.ALLOWED) {
            this.spawnAtLocation(serverLevel, this.getPickupItem(), 0.1F);
        }

        this.discard();
    }

    @NotNull
    @Override
    public ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return ModItems.infinity_trident.toStack();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Entity thrower = getOwner();
        var blockPos = result.getBlockPos();
        trySummonLightning(level(), 2, blockPos,
                thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        float damage = Float.MAX_VALUE;
        Entity thrower = getOwner();
        DamageSource damagesource = damageSources().trident(this, thrower == null ? this : thrower);
        var blockPos = result.getEntity().blockPosition();
        trySummonLightning(level(), 2, blockPos,
                thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
        this.dealtDamage = true;
        if (damageEntity(hitEntity, damagesource, damage)) {
            if (this.level() instanceof ServerLevel serverlevel1) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, hitEntity, damagesource, this.getWeaponItem());
            }

            if (hitEntity instanceof LivingEntity livingentity) {
                this.doKnockback(livingentity, damagesource);
                this.doPostHurtEffects(livingentity);
            }
        }
        ProjectileDeflection.REVERSE.deflect(this, hitEntity, this.random);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02D, 0.2D, 0.02D));
        float volume = 1.0F;
        SoundEvent sound = SoundEvents.TRIDENT_HIT;
        AABB area = new AABB(hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), hitEntity.getX(), hitEntity.getY(), hitEntity.getZ()).inflate(this.entityData.get(RADIUS));
        List<Mob> mobs = this.entityData.get(SHOCKWAVE) ? this.level().getEntitiesOfClass(Mob.class, area) : Collections.emptyList();

        if (thrower instanceof Player && this.entityData.get(SHOCKWAVE)) {
            mobs.forEach(mobEntity -> {
                damageEntity(mobEntity, mobEntity.damageSources().playerAttack((Player) thrower), damage);
            });
            this.level().getEntitiesOfClass(ItemEntity.class, area.inflate(1)).forEach(itemEntity -> {
                itemEntity.setNoPickUpDelay();
                itemEntity.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY() + 1, thrower.blockPosition().getZ());
            });
            this.level().getEntitiesOfClass(ExperienceOrb.class, area.inflate(1))
                    .forEach(entityXPOrb ->
                            entityXPOrb.teleportTo(thrower.blockPosition().getX(), thrower.blockPosition().getY(), thrower.blockPosition().getZ()));
        }
        if (this.level() instanceof ServerLevel && this.entityData.get(CHANNELING)) {
            BlockPos blockpos = hitEntity.blockPosition();
            if (this.level().canSeeSky(blockpos)) {
                trySummonLightning(level(), 1, blockpos,
                        thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                sound = SoundEvents.TRIDENT_THUNDER.value();
                volume = 5.0F;
                mobs.forEach(mobEntity -> {
                    if (this.level().canSeeSky(mobEntity.blockPosition())) {
                        trySummonLightning(level(), 1, mobEntity.blockPosition(),
                                thrower instanceof ServerPlayer ? (ServerPlayer) thrower : null);
                    }
                });
            }
        }
        playSound(sound, volume, 1.0F);
    }


    @Override
    public void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        setStackAndLoyalty(input.read("Trident", ItemStack.CODEC).orElse(this.getDefaultPickupItem()));
        dealtDamage = input.getBooleanOr("DealtDamage", false);
        returnSlot = input.getIntOr("ReturnSlot", NO_RETURN_SLOT);
    }

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Trident", ItemStack.CODEC, tridentItem);
        output.putBoolean("DealtDamage", dealtDamage);
        output.putInt("ReturnSlot", returnSlot);
    }

    @Override
    protected void tickDespawn() {
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F + 0.5F;
    }

    public boolean isFoil() {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @NotNull
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (this.isNoPhysics() && this.ownedBy(player)) {
            if (this.pickup == Pickup.CREATIVE_ONLY) {
                return player.getAbilities().instabuild;
            }
            if (this.pickup == Pickup.ALLOWED) {
                return addToReturnSlot(player, this.getPickupItem());
            }
        }
        return super.tryPickup(player);
    }

    private boolean addToReturnSlot(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (returnSlot >= 0 && returnSlot < player.getInventory().getContainerSize()
                && player.getInventory().getItem(returnSlot).isEmpty()) {
            player.getInventory().setItem(returnSlot, stack);
            return true;
        }
        return player.getInventory().add(stack);
    }

    @Override
    public void playerTouch(Player entity) {
        if (this.ownedBy(entity) || this.getOwner() == null) {
            super.playerTouch(entity);
        }
    }

    @Override
    public void writeSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {
        ItemStack.STREAM_CODEC.encode(buffer, tridentItem);
    }

    @Override
    public void readSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {
        setStackAndLoyalty(ItemStack.STREAM_CODEC.decode(buffer));
    }

    @Override
    public ItemStack getPickResult() {
        return tridentItem.copy();
    }

    private boolean damageEntity(Entity entity, DamageSource source, float amount) {
        if (level() instanceof ServerLevel serverLevel) {
            return entity.hurtServer(serverLevel, source, amount);
        }
        return entity.hurtOrSimulate(source, amount);
    }

    private static boolean trySummonLightning(Level level, int bolts, BlockPos hitPos, @Nullable ServerPlayer thrower) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        boolean hasAction = false;
        for (int i = 0; i < bolts; i++) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.EVENT);
            if (lightning != null) {
                lightning.moveOrInterpolateTo(Vec3.atBottomCenterOf(hitPos));
                lightning.setCause(thrower);
                serverLevel.addFreshEntity(lightning);
                hasAction = true;
            }
        }
        return hasAction;
    }
}
