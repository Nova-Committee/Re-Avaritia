package committee.nova.mods.avaritia.common.item.misc;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 03:10
 * @Description: Infinity Bucket
 */
public class InfinityBucketItem extends ResourceItem {
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
            player.displayClientMessage(Component.translatable("gui.avaritia.infinity_bucket.over_capacity"), true);
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
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
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
        if (type.is(Tags.EntityTypes.BOSSES) || type.is(ModTags.CAPTURING_NOT_SUPPORTED)) {
            return false;
        }
        if (living.isPassenger() || living.isVehicle() || !living.getPassengers().isEmpty()) {
            return false;
        }
        return living instanceof Bucketable || type.is(ModTags.AQUATIC_CAPTURABLE);
    }

    public static boolean isEntityInWater(LivingEntity entity) {
        return entity.isInWaterOrBubble() || entity.level().getFluidState(entity.blockPosition()).is(FluidTags.WATER);
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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new InfinityBucketWrapper(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
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
            pTooltipComponents.add(component);
        }
        List<CreatureRecord> creatures = getCreatures(pStack);
        for (int i = 0; i < creatures.size(); i++) {
            CreatureRecord creature = creatures.get(i);
            MutableComponent component = Component.translatable("tooltip.avaritia.infinity_bucket.creature",
                    creature.displayName(), creature.typeId.toString());
            if (control.creatureSelected() && i == control.selectedIndex()) {
                component.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            pTooltipComponents.add(component);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pLevel.isClientSide && pEntity instanceof Player player && player.getInventory().getSelected() == pStack) {
            NumberFormat formater = DecimalFormat.getInstance();
            CreatureRecord creature = getSelectedCreature(pStack);
            if (creature != null) {
                player.displayClientMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message_creature", creature.displayName()), true);
                return;
            }
            FluidStack selected = getSelectedFluid(pStack);
            String displayName = selected.getDisplayName().getString();
            String amount = formater.format(selected.getAmount());
            player.displayClientMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message", displayName, amount), true);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide && pPlayer instanceof ServerPlayer serverPlayer) {
                int slot = pUsedHand == InteractionHand.MAIN_HAND ? serverPlayer.getInventory().selected : 40;
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, player) -> new InfinityBucketMenu(id, inventory, slot),
                        Component.translatable("container.avaritia.infinity_bucket")));
            }
            return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
        }
        if (pLevel.isClientSide) {
            return InteractionResultHolder.consume(itemStack);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        BlockPos hitPos = hitResult.getBlockPos();
        Direction side = hitResult.getDirection();
        if (!pLevel.mayInteract(pPlayer, hitPos) || !pPlayer.mayUseItemAt(hitPos, side, itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!getControl(itemStack).creatureSelected() && pLevel.getFluidState(hitPos).isSource()) {
            if (tryCollectFromWorld(pPlayer, pLevel, itemStack, hitPos, side)) {
                return InteractionResultHolder.success(itemStack);
            }
            return InteractionResultHolder.fail(itemStack);
        }
        if (tryOutputToWorld(pPlayer, pLevel, pUsedHand, itemStack, hitResult)) {
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level().isClientSide) {
            return canCaptureAt(player, stack, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return tryCapture(stack, player, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean tryCollectFromWorld(Player player, Level level, ItemStack stack, BlockPos hitPos, Direction side) {
        if (!level.getFluidState(hitPos).isSource()) {
            return false;
        }
        FluidActionResult pickUpResult = FluidUtil.tryPickUpFluid(stack, player, level, hitPos, side);
        if (pickUpResult.isSuccess()) {
            ItemStack result = pickUpResult.getResult();
            if (result != stack) {
                InfinityBucketContents.copyStoredState(result, stack);
            }
            return true;
        }
        return false;
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
            IFluidHandler tank = FluidUtil.getFluidHandler(level, hitPos, side).resolve().orElse(null);
            if (tank == null) {
                tank = FluidUtil.getFluidHandler(level, hitPos, null).resolve().orElse(null);
            }
            if (tank != null) {
                FluidStack selected = getSelectedFluid(stack);
                if (selected.isEmpty()) {
                    return false;
                }
                IFluidHandlerItem bucket = FluidUtil.getFluidHandler(stack).resolve().orElse(null);
                if (bucket == null) {
                    return false;
                }
                FluidStack moved = FluidUtil.tryFluidTransfer(tank, bucket, selected, true);
                return !moved.isEmpty();
            }
        }

        FluidStack selected = getSelectedFluid(stack);
        if (selected.isEmpty() || selected.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }
        FluidStack toPlace = selected.copy();
        toPlace.setAmount(FluidType.BUCKET_VOLUME);
        BlockPos placePos = canBlockContainFluid(level, hitPos, hitState, toPlace.getFluid()) ? hitPos : hitPos.relative(side);
        if (!level.mayInteract(player, placePos) || !player.mayUseItemAt(placePos, side, stack)) {
            return false;
        }
        return placeWorldFluid(player, level, hand, placePos, stack, toPlace);
    }

    private boolean canBlockContainFluid(Level level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(level, pos, state, fluid);
    }

    private boolean placeWorldFluid(Player player, Level level, InteractionHand hand, BlockPos pos, ItemStack container, FluidStack resource) {
        Fluid fluid = resource.getFluid();
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && !PlayerUtils.hasEditPermission(serverPlayer, pos)) {
            return false;
        }
        IFluidHandlerItem source = FluidUtil.getFluidHandler(container).resolve().orElse(null);
        if (source == null || source.drain(resource, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
            return false;
        }

        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand,
                new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false)));
        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestNonSolid = !destBlockState.isSolid();
        boolean isDestReplaceable = destBlockState.canBeReplaced(context);
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer
                && ((LiquidBlockContainer) destBlockState.getBlock()).canPlaceLiquid(level, pos, destBlockState, fluid);
        if (!level.isEmptyBlock(pos) && !isDestNonSolid && !isDestReplaceable && !canDestContainFluid) {
            return false;
        }

        BlockState placedState = fluid.getFluidType().getBlockForFluidState(level, pos, fluid.defaultFluidState());
        if (!canDestContainFluid && placedState.isAir()) {
            placedState = fluid.defaultFluidState().createLegacyBlock();
            if (placedState.isAir()) {
                return false;
            }
        }
        if (!placeProtectedFluid(player, level, pos, destBlockState, placedState, fluid, canDestContainFluid)) {
            return false;
        }
        FluidStack result = source.drain(resource, IFluidHandler.FluidAction.EXECUTE);
        if (result.isEmpty()) {
            return false;
        }
        SoundEvent soundevent = resource.getFluid().getFluidType().getSound(resource, SoundActions.BUCKET_EMPTY);
        if (soundevent != null) {
            level.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
        return true;
    }

    private boolean placeProtectedFluid(Player player, Level level, BlockPos pos, BlockState dest, BlockState placed,
                                        Fluid fluid, boolean canDestContainFluid) {
        BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
        boolean placedBlock;
        if (canDestContainFluid) {
            placedBlock = ((LiquidBlockContainer) dest.getBlock()).placeLiquid(level, pos, dest, fluid.defaultFluidState());
        } else {
            placedBlock = level.setBlock(pos, placed, Block.UPDATE_ALL_IMMEDIATE);
        }
        if (!placedBlock) {
            return false;
        }
        BlockEvent.EntityPlaceEvent evt = new BlockEvent.EntityPlaceEvent(before, dest, player);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled()) {
            level.restoringBlockSnapshots = true;
            before.restore(true, false);
            level.restoringBlockSnapshots = false;
            return false;
        }
        return true;
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
        Entity entity = mobBucket.getFishType().create(level);
        if (!(entity instanceof LivingEntity living) || !(entity instanceof Bucketable)) {
            if (entity != null) {
                entity.discard();
            }
            return false;
        }
        try {
            CompoundTag bucketData = source.getTag() == null ? new CompoundTag() : source.getTag().copy();
            EntityType.createDefaultStackConfig(level, source, player).accept(entity);
            applyMobBucketSpawnData(entity, bucketData, level, true);
            if (!isCapturable(living)) {
                return false;
            }
            CompoundTag tag = saveCreatureData(living);
            if (tag == null) {
                return false;
            }
            tag.put(BUCKETABLE_DATA_KEY, bucketData);
            ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
            if (typeId == null) {
                return false;
            }
            creatures.add(new CreatureRecord(typeId, tag));

            ItemStack proposed = stack.copy();
            if (!trySetCreatures(proposed, creatures)) {
                notifyOverCapacity(player);
                return false;
            }
            Fluid fluid = ((BucketItem) mobBucket).getFluid();
            FluidStack contained = fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, FluidType.BUCKET_VOLUME);
            if (!contained.isEmpty() && new InfinityBucketWrapper(proposed).fill(contained, IFluidHandler.FluidAction.EXECUTE) != contained.getAmount()) {
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
        Fluid fluid = ((BucketItem) mobBucket).getFluid();
        FluidStack contained = fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        if (!contained.isEmpty()) {
            InfinityBucketWrapper wrapper = new InfinityBucketWrapper(proposed);
            FluidStack drained = wrapper.drain(contained, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() != contained.getAmount() || !drained.isFluidEqual(contained)) {
                return ItemStack.EMPTY;
            }
            wrapper.drain(contained, IFluidHandler.FluidAction.EXECUTE);
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
        Entity entity = type == null ? null : type.create(level);
        try {
            if (!(entity instanceof LivingEntity) || !(entity instanceof Bucketable bucketable)) {
                return ItemStack.EMPTY;
            }
            CompoundTag tag = creature.entityData();
            if (tag.contains(BUCKETABLE_DATA_KEY, Tag.TAG_COMPOUND)) {
                bucketable.loadFromBucketTag(tag.getCompound(BUCKETABLE_DATA_KEY));
            }
            ItemStack filled = bucketable.getBucketItemStack();
            if (tag.contains(BUCKETABLE_DATA_KEY, Tag.TAG_COMPOUND)) {
                CompoundTag data = tag.getCompound(BUCKETABLE_DATA_KEY);
                if (!data.isEmpty()) {
                    CompoundTag filledTag = filled.getOrCreateTag();
                    for (String key : data.getAllKeys()) {
                        Tag value = data.get(key);
                        if (value != null) {
                            filledTag.put(key, value.copy());
                        }
                    }
                }
            } else {
                bucketable.saveToBucketTag(filled);
            }
            if (tag.contains("CustomName", Tag.TAG_STRING)) {
                try {
                    Component name = Component.Serializer.fromJson(tag.getString("CustomName"));
                    if (name != null) {
                        filled.setHoverName(name);
                    }
                } catch (RuntimeException ignored) {
                }
            }
            return filled;
        } finally {
            if (entity != null) {
                entity.discard();
            }
        }
    }

    @Nullable
    private static CompoundTag saveCreatureData(LivingEntity target) {
        CompoundTag tag = new CompoundTag();
        if (!target.save(tag)) {
            return null;
        }
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
        if (!canCaptureAt(player, stack, target) || !(player.level() instanceof ServerLevel)) {
            return false;
        }
        CompoundTag tag = saveCreatureData(target);
        if (tag == null) {
            return false;
        }
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (typeId == null) {
            return false;
        }
        if (target instanceof Bucketable bucketable) {
            ItemStack bucketTag = bucketable.getBucketItemStack();
            bucketable.saveToBucketTag(bucketTag);
            if (bucketTag.hasTag()) {
                tag.put(BUCKETABLE_DATA_KEY, bucketTag.getTag().copy());
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
            player.level().playSound(null, target.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        target.discard();
        return true;
    }

    /** 1.20 TropicalFish reads BucketVariantTag in finalizeSpawn(BUCKET), not loadFromBucketTag. */
    private static void applyMobBucketSpawnData(Entity entity, CompoundTag bucketData, ServerLevel level, boolean applyBucketSpawn) {
        boolean spawnFromBucket = applyBucketSpawn || bucketData.contains(TropicalFish.BUCKET_VARIANT_TAG, Tag.TAG_INT);
        if (spawnFromBucket && entity instanceof Mob mob) {
            ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.BUCKET, null, bucketData);
        }
        if (entity instanceof Bucketable bucketable) {
            bucketable.loadFromBucketTag(bucketData);
            bucketable.setFromBucket(true);
        }
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
        Optional<Entity> created = EntityType.create(tag, level);
        if (created.isEmpty()) {
            return false;
        }
        Entity entity = created.get();
        entity.setUUID(UUID.randomUUID());
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        entity.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        entity.setDeltaMovement(Vec3.ZERO);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (tag.contains(BUCKETABLE_DATA_KEY, Tag.TAG_COMPOUND)) {
            applyMobBucketSpawnData(entity, tag.getCompound(BUCKETABLE_DATA_KEY), level, false);
        }
        if (!level.noCollision(entity) || !level.addFreshEntity(entity)) {
            return false;
        }
        entity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
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
