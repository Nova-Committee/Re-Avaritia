package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.PlayerUtils;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketContents.Control;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketContents.CreatureRecord;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.common.wrappers.InfinityBucketWrapper;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import committee.nova.mods.avaritia.core.io.FluidStack;
import committee.nova.mods.avaritia.core.io.FluidHandler;
import committee.nova.mods.avaritia.core.io.FluidItemHandler;
import committee.nova.mods.avaritia.core.io.FluidStorageProvider;
import committee.nova.mods.avaritia.core.io.FluidTransfer;
import committee.nova.mods.avaritia.core.io.StorageAccess;
import committee.nova.mods.avaritia.init.mixins.DimensionBucketItemAccessor;
import committee.nova.mods.avaritia.init.mixins.DimensionMobBucketItemAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 03:10
 * @Description: Infinity Bucket
 */
public class InfinityBucketItem extends ResourceItem implements FluidStorageProvider {
    public static final String FLUIDS_NBT = InfinityBucketContents.FLUIDS_NBT;
    public static final String FLUID_ID_KEY = InfinityBucketContents.LEGACY_FLUID_ID_KEY;
    public static final String FLUID_AMOUNT_KEY = InfinityBucketContents.LEGACY_FLUID_AMOUNT_KEY;
    public static final String BUCKETABLE_DATA_KEY = InfinityBucketContents.BUCKETABLE_DATA_KEY;

    public InfinityBucketItem() {
        super(ModRarities.LEGEND, true, new Properties().stacksTo(1));
    }

    public static Control getControl(ItemStack stack) {
        return InfinityBucketContents.getControl(stack);
    }

    public static void setControl(ItemStack stack, Control control) {
        InfinityBucketContents.setControl(stack, control);
    }

    public static List<FluidStack> getFluids(ItemStack stack) {
        return InfinityBucketContents.getFluids(stack);
    }

    public static boolean trySetFluids(ItemStack stack, List<FluidStack> fluids) {
        return InfinityBucketContents.setFluids(stack, fluids);
    }

    public static void setFluids(ItemStack stack, List<FluidStack> fluids) {
        trySetFluids(stack, fluids);
    }

    public static List<CreatureRecord> getCreatures(ItemStack stack) {
        return InfinityBucketContents.getCreatures(stack);
    }

    public static boolean trySetCreatures(ItemStack stack, List<CreatureRecord> creatures) {
        return InfinityBucketContents.setCreatures(stack, creatures);
    }

    public static void setCreatures(ItemStack stack, List<CreatureRecord> creatures) {
        trySetCreatures(stack, creatures);
    }

    public static void notifyOverCapacity(Player player) {
        if (player != null) {
            ItemUtils.sendActionbar(player, Component.translatable("gui.avaritia.infinity_bucket.over_capacity"));
        }
    }

    public static FluidStack getSelectedFluid(ItemStack stack) {
        return InfinityBucketContents.getSelectedFluid(stack);
    }

    @Nullable
    public static CreatureRecord getSelectedCreature(ItemStack stack) {
        return InfinityBucketContents.getSelectedCreature(stack);
    }

    @NotNull
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        return InfinityBucketContents.loadFluidStack(nbt);
    }

    @NotNull
    public static CompoundTag writeFluidStackToNBT(FluidStack fluidStack, CompoundTag nbt) {
        return fluidStack.writeToNBT(nbt);
    }

    @NotNull
    public static String getFluidName(FluidStack fluidStack) {
        Identifier key = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
        return key == null ? "minecraft:empty" : key.toString();
    }

    public static boolean isCapturable(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()) {
            return false;
        }
        if (living instanceof Player) {
            return false;
        }
        EntityType<?> type = living.getType();
        Holder<EntityType<?>> typeHolder = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type);
        if (typeHolder.is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", "bosses")))
                || type == EntityTypes.ENDER_DRAGON || type == EntityTypes.WITHER
                || typeHolder.is(ModTags.CAPTURING_NOT_SUPPORTED)) {
            return false;
        }
        if (living.isPassenger() || living.isVehicle() || !living.getPassengers().isEmpty()) {
            return false;
        }
        return living instanceof Bucketable || typeHolder.is(ModTags.AQUATIC_CAPTURABLE);
    }

    public static boolean isEntityInWater(LivingEntity entity) {
        return entity.isInWater() || entity.level().getFluidState(entity.blockPosition()).is(FluidTags.WATER);
    }

    public static boolean canCaptureAt(Player player, ItemStack stack, LivingEntity target) {
        if (!isCapturable(target) || !isEntityInWater(target)) {
            return false;
        }
        BlockPos pos = target.blockPosition();
        if (!player.level().mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return false;
        }
        return !(player instanceof ServerPlayer serverPlayer) || PlayerUtils.hasEditPermission(serverPlayer, pos);
    }

    @Override
    public FluidItemHandler getFluidHandler(ItemStack stack) {
        return new InfinityBucketWrapper(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, display, pTooltipComponents, pIsAdvanced);
        Control control = getControl(pStack);
        NumberFormat formater = DecimalFormat.getInstance();
        List<FluidStack> fluids = getFluids(pStack);
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack fluid = fluids.get(i);
            MutableComponent component = MutableComponent.create(fluid.getDisplayName().getContents());
            component.append(": " + formater.format(fluid.getAmount()) + " mB");
            if (!control.creatureSelected() && i == control.selectedIndex()) {
                component.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            pTooltipComponents.accept(component);
        }
        List<CreatureRecord> creatures = getCreatures(pStack);
        for (int i = 0; i < creatures.size(); i++) {
            CreatureRecord creature = creatures.get(i);
            MutableComponent component = Component.translatable("tooltip.avaritia.infinity_bucket.creature",
                    creature.displayName(), creature.typeId.toString());
            if (control.creatureSelected() && i == control.selectedIndex()) {
                component.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            pTooltipComponents.accept(component);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull ServerLevel pLevel, @NotNull Entity pEntity, @Nullable EquipmentSlot pSlot) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlot);
        if (pEntity instanceof Player player && player.getInventory().getSelectedItem() == pStack) {
            NumberFormat formater = DecimalFormat.getInstance();
            CreatureRecord creature = getSelectedCreature(pStack);
            if (creature != null) {
                ItemUtils.sendActionbar(player, Component.translatable("tooltip.avaritia.infinity_bucket.message_creature", creature.displayName()));
                return;
            }
            FluidStack selected = getSelectedFluid(pStack);
            String displayName = selected.getDisplayName().getString();
            String amount = formater.format(selected.getAmount());
            ItemUtils.sendActionbar(player, Component.translatable("tooltip.avaritia.infinity_bucket.message", displayName, amount));
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
                int slot = pUsedHand == InteractionHand.MAIN_HAND ? serverPlayer.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, player) -> new InfinityBucketMenu(id, inventory, slot),
                        Component.translatable("container.avaritia.infinity_bucket")));
            }
            return InteractionResult.SUCCESS;
        }
        if (pLevel.isClientSide()) {
            return InteractionResult.CONSUME;
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos hitPos = hitResult.getBlockPos();
        Direction side = hitResult.getDirection();
        if (!pLevel.mayInteract(pPlayer, hitPos) || !pPlayer.mayUseItemAt(hitPos, side, itemStack)) {
            return InteractionResult.FAIL;
        }
        if (!getControl(itemStack).creatureSelected() && pLevel.getFluidState(hitPos).isSource()) {
            if (tryCollectFromWorld(pPlayer, pLevel, itemStack, hitPos, side)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        if (tryOutputToWorld(pPlayer, pLevel, pUsedHand, itemStack, hitResult)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return canCaptureAt(player, stack, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return tryCapture(stack, player, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean tryCollectFromWorld(Player player, Level level, ItemStack stack, BlockPos hitPos, Direction side) {
        if (!level.getFluidState(hitPos).isSource()) {
            return false;
        }
        BlockState state = level.getBlockState(hitPos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) {
            return false;
        }
        FluidStack fluid = new FluidStack(state.getFluidState().getType(), InfinityBucketContents.BUCKET_VOLUME);
        InfinityBucketWrapper bucket = new InfinityBucketWrapper(stack);
        if (bucket.fill(fluid, FluidHandler.FluidAction.SIMULATE) != fluid.getAmount()) {
            notifyOverCapacity(player);
            return false;
        }
        ItemStack pickedUp = pickup.pickupBlock(player, level, hitPos, state);
        if (pickedUp.isEmpty()) {
            return false;
        }
        bucket.fill(fluid, FluidHandler.FluidAction.EXECUTE);
        pickup.getPickupSound().ifPresent(sound -> level.playSound(player, hitPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, hitPos);
        return true;
    }

    private boolean tryOutputToWorld(Player player, Level level, InteractionHand hand, ItemStack stack, BlockHitResult hitResult) {
        BlockPos hitPos = hitResult.getBlockPos();
        Direction side = hitResult.getDirection();
        BlockState hitState = level.getBlockState(hitPos);
        Block hitBlock = hitState.getBlock();
        boolean worldFluid = hitBlock instanceof LiquidBlock || hitBlock instanceof BucketPickup;

        CreatureRecord selectedCreature = getSelectedCreature(stack);
        if (selectedCreature != null) {
            BlockPos releasePos = hitState.getFluidState().is(FluidTags.WATER) ? hitPos : hitPos.relative(side);
            return tryRelease(stack, player, (ServerLevel) level, releasePos, selectedCreature);
        }

        if (!worldFluid) {
            FluidHandler tank = StorageAccess.fluids(level.getBlockEntity(hitPos), side).orElse(null);
            if (tank == null) {
                tank = StorageAccess.fluids(level.getBlockEntity(hitPos), null).orElse(null);
            }
            if (tank != null) {
                FluidStack selected = getSelectedFluid(stack);
                if (selected.isEmpty()) {
                    return false;
                }
                FluidItemHandler bucket = getFluidHandler(stack);
                if (bucket == null) {
                    return false;
                }
                FluidStack moved = FluidTransfer.tryFluidTransfer(tank, bucket, selected, true);
                return !moved.isEmpty();
            }
        }

        FluidStack selected = getSelectedFluid(stack);
        if (selected.isEmpty() || selected.getAmount() < InfinityBucketContents.BUCKET_VOLUME) {
            return false;
        }
        FluidStack toPlace = selected.copy();
        toPlace.setAmount(InfinityBucketContents.BUCKET_VOLUME);
        BlockPos placePos = canBlockContainFluid(player, level, hitPos, hitState, toPlace.getFluid()) ? hitPos : hitPos.relative(side);
        if (!level.mayInteract(player, placePos) || !player.mayUseItemAt(placePos, side, stack)) {
            return false;
        }
        return placeWorldFluid(player, level, hand, placePos, stack, toPlace);
    }

    private boolean canBlockContainFluid(Player player, Level level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(player, level, pos, state, fluid);
    }

    private boolean placeWorldFluid(Player player, Level level, InteractionHand hand, BlockPos pos, ItemStack container, FluidStack resource) {
        Fluid fluid = resource.getFluid();
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && !PlayerUtils.hasEditPermission(serverPlayer, pos)) {
            return false;
        }
        FluidItemHandler source = getFluidHandler(container);
        if (source.drain(resource, FluidHandler.FluidAction.SIMULATE).getAmount() < resource.getAmount()) {
            return false;
        }

        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand,
                new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false)));
        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestReplaceable = destBlockState.canBeReplaced(context);
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer
                && ((LiquidBlockContainer) destBlockState.getBlock()).canPlaceLiquid(player, level, pos, destBlockState, fluid);
        if (!destBlockState.isAir() && !isDestReplaceable && !canDestContainFluid) {
            return false;
        }

        BlockState placedState = fluid.defaultFluidState().createLegacyBlock();
        if (!canDestContainFluid && placedState.isAir()) {
            return false;
        }
        if (!placeFluid(level, pos, destBlockState, placedState, fluid, canDestContainFluid, isDestReplaceable)) {
            return false;
        }
        FluidStack result = source.drain(resource, FluidHandler.FluidAction.EXECUTE);
        if (result.isEmpty()) {
            return false;
        }
        SoundEvent soundevent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        return true;
    }

    private boolean placeFluid(Level level, BlockPos pos, BlockState dest, BlockState placed,
                               Fluid fluid, boolean canDestContainFluid, boolean isDestReplaceable) {
        boolean placedBlock;
        if (canDestContainFluid) {
            placedBlock = ((LiquidBlockContainer) dest.getBlock()).placeLiquid(level, pos, dest, fluid.defaultFluidState());
        } else {
            if (!level.isClientSide() && isDestReplaceable && !dest.liquid()) {
                level.destroyBlock(pos, true);
            }
            placedBlock = level.setBlock(pos, placed, Block.UPDATE_ALL_IMMEDIATE);
        }
        return placedBlock;
    }

    /** Stores a mob bucket's creature and fluid together, without spawning or consuming its source stack. */
    public static boolean tryStoreMobBucket(ItemStack stack, ItemStack source, Player player) {
        if (!(player.level() instanceof ServerLevel level) || !(stack.getItem() instanceof InfinityBucketItem)
                || stack.getCount() != 1 || source.getCount() != 1
                || !(source.getItem() instanceof MobBucketItem mobBucket)) {
            return false;
        }
        List<CreatureRecord> creatures = getCreatures(stack);
        if (creatures.size() >= InfinityBucketContents.MAX_CREATURE_ENTRIES) {
            notifyOverCapacity(player);
            return false;
        }
        Entity entity = ((DimensionMobBucketItemAccessor) mobBucket).avaritia$getType().create(level, EntitySpawnReason.BUCKET);
        if (!(entity instanceof LivingEntity living) || !(entity instanceof Bucketable bucketable)) {
            if (entity != null) {
                entity.discard();
            }
            return false;
        }
        try {
            CompoundTag bucketData = source.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
            EntityType.createDefaultStackConfig(level, source, player).apply(entity);
            bucketable.loadFromBucketTag(bucketData);
            bucketable.setFromBucket(true);
            if (!isCapturable(living)) {
                return false;
            }
            CompoundTag tag = saveCreatureData(living, level);
            if (tag == null) {
                return false;
            }
            tag.put(BUCKETABLE_DATA_KEY, bucketData);
            Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
            if (typeId == null) {
                return false;
            }
            creatures.add(new CreatureRecord(typeId, tag));

            ItemStack proposed = stack.copy();
            if (!trySetCreatures(proposed, creatures)) {
                notifyOverCapacity(player);
                return false;
            }
            Fluid fluid = ((DimensionBucketItemAccessor) mobBucket).avaritia$getContent();
            FluidStack contained = fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, InfinityBucketContents.BUCKET_VOLUME);
            if (!contained.isEmpty() && new InfinityBucketWrapper(proposed).fill(contained, FluidHandler.FluidAction.EXECUTE) != contained.getAmount()) {
                notifyOverCapacity(player);
                return false;
            }
            setControl(proposed, getControl(proposed).selectCreature(creatures.size() - 1));
            InfinityBucketContents.copyStoredState(proposed, stack);
            return true;
        } finally {
            entity.discard();
        }
    }

    /** Fills one empty bucket with a stored Bucketable creature and one bucket of its fluid. */
    public static ItemStack tryExtractMobBucket(ItemStack stack, ItemStack destination, Player player, int creatureIndex) {
        if (!(player.level() instanceof ServerLevel level) || !(stack.getItem() instanceof InfinityBucketItem)
                || stack.getCount() != 1 || destination.getCount() != 1 || !destination.is(Items.BUCKET)) {
            return ItemStack.EMPTY;
        }
        List<CreatureRecord> creatures = getCreatures(stack);
        if (creatureIndex < 0 || creatureIndex >= creatures.size()) {
            return ItemStack.EMPTY;
        }
        CreatureRecord creature = creatures.get(creatureIndex);
        ItemStack filled = toMobBucket(creature, level);
        if (filled.isEmpty() || !(filled.getItem() instanceof MobBucketItem mobBucket)) {
            return ItemStack.EMPTY;
        }
        ItemStack proposed = stack.copy();
        Fluid fluid = ((DimensionBucketItemAccessor) mobBucket).avaritia$getContent();
        FluidStack contained = fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, InfinityBucketContents.BUCKET_VOLUME);
        if (!contained.isEmpty()) {
            InfinityBucketWrapper wrapper = new InfinityBucketWrapper(proposed);
            FluidStack drained = wrapper.drain(contained, FluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() != contained.getAmount() || !drained.isFluidEqual(contained)) {
                return ItemStack.EMPTY;
            }
            wrapper.drain(contained, FluidHandler.FluidAction.EXECUTE);
        }
        List<CreatureRecord> next = getCreatures(proposed);
        if (creatureIndex >= next.size() || !next.get(creatureIndex).equals(creature)) {
            return ItemStack.EMPTY;
        }
        next.remove(creatureIndex);
        if (!trySetCreatures(proposed, next)) {
            return ItemStack.EMPTY;
        }
        InfinityBucketContents.copyStoredState(proposed, stack);
        return filled;
    }

    private static ItemStack toMobBucket(CreatureRecord creature, ServerLevel level) {
        EntityType<?> type = creature.entityType();
        if (type == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = creature.entityData().copy();
        tag.putString("id", creature.typeId.toString());
        Entity entity = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag),
                level, new EntitySpawnRequest(EntitySpawnReason.BUCKET, false))
                .orElseGet(() -> type.create(level, EntitySpawnReason.BUCKET));
        try {
            if (!(entity instanceof LivingEntity) || !(entity instanceof Bucketable bucketable)) {
                return ItemStack.EMPTY;
            }
            if (tag.contains(BUCKETABLE_DATA_KEY)) {
                bucketable.loadFromBucketTag(tag.getCompoundOrEmpty(BUCKETABLE_DATA_KEY));
            }
            bucketable.setFromBucket(true);
            ItemStack filled = bucketable.getBucketItemStack();
            bucketable.saveToBucketTag(filled);
            applyCustomName(tag, filled);
            return filled;
        } finally {
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static void applyCustomName(CompoundTag tag, ItemStack filled) {
        if (!tag.contains("CustomName")) {
            return;
        }
        try {
            Tag raw = tag.get("CustomName");
            if (raw != null) {
                ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, raw).result().ifPresent(name ->
                        filled.set(DataComponents.CUSTOM_NAME, name));
            }
        } catch (RuntimeException ignored) {
        }
    }

    @Nullable
    private static CompoundTag saveCreatureData(LivingEntity target, ServerLevel level) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        if (!target.save(output)) {
            return null;
        }
        CompoundTag tag = output.buildResult();
        tag.remove("Passengers");
        tag.remove("UUID");
        tag.remove("UUIDLeast");
        tag.remove("UUIDMost");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Dimension");
        tag.remove("PortalCooldown");
        return tag;
    }

    private boolean tryCapture(ItemStack stack, Player player, LivingEntity target) {
        if (!canCaptureAt(player, stack, target) || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        CompoundTag tag = saveCreatureData(target, level);
        if (tag == null) {
            return false;
        }
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (typeId == null) {
            return false;
        }
        if (target instanceof Bucketable bucketable) {
            ItemStack bucketTag = bucketable.getBucketItemStack();
            bucketable.saveToBucketTag(bucketTag);
            CustomData data = bucketTag.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            if (!data.isEmpty()) {
                tag.put(BUCKETABLE_DATA_KEY, data.copyTag());
            }
        }
        CreatureRecord creature = new CreatureRecord(typeId, tag);
        List<CreatureRecord> creatures = getCreatures(stack);
        creatures.add(creature);
        if (!trySetCreatures(stack, creatures)) {
            notifyOverCapacity(player);
            return false;
        }
        setControl(stack, getControl(stack).selectCreature(creatures.size() - 1));
        if (target instanceof Bucketable bucketable) {
            target.playSound(bucketable.getPickupSound(), 1.0F, 1.0F);
        } else {
            level.playSound(null, target.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        target.discard();
        return true;
    }

    private boolean tryRelease(ItemStack stack, Player player, ServerLevel level, BlockPos pos, CreatureRecord selected) {
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && !PlayerUtils.hasEditPermission(serverPlayer, pos)) {
            return false;
        }
        List<CreatureRecord> creatures = getCreatures(stack);
        int index = creatures.indexOf(selected);
        if (index < 0) {
            Control control = getControl(stack);
            if (!control.creatureSelected() || control.selectedIndex() >= creatures.size()) {
                return false;
            }
            index = control.selectedIndex();
            selected = creatures.get(index);
        }
        CompoundTag tag = selected.entityData().copy();
        tag.putString("id", selected.typeId.toString());
        tag.remove("Passengers");
        tag.remove("UUID");
        tag.remove("UUIDLeast");
        tag.remove("UUIDMost");
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Dimension");
        Optional<Entity> created = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag),
                level, new EntitySpawnRequest(EntitySpawnReason.BUCKET, false));
        if (created.isEmpty()) {
            return false;
        }
        Entity entity = created.get();
        entity.setUUID(UUID.randomUUID());
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (entity instanceof Bucketable bucketable && tag.contains(BUCKETABLE_DATA_KEY)) {
            bucketable.loadFromBucketTag(tag.getCompoundOrEmpty(BUCKETABLE_DATA_KEY));
            bucketable.setFromBucket(true);
        }
        if (!level.noCollision(entity) || !level.addFreshEntity(entity)) {
            return false;
        }
        entity.snapTo(x, y, z, entity.getYRot(), entity.getXRot());
        if (!entity.isAlive() || entity.isRemoved()) {
            entity.discard();
            return false;
        }
        creatures.remove(index);
        setCreatures(stack, creatures);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        return true;
    }
}
