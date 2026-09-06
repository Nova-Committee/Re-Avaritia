package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.item.IItemCapability;
import committee.nova.mods.avaritia.api.utils.PlayerUtils;
import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.common.wrappers.InfinityBucketWrapper;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.wrappers.BlockWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午10:36
 * @Description:
 */
public class InfinityBucketItem extends ResourceItem implements IItemCapability {
    public static final String FLUIDS_NBT = "Fluids";
    public static final String FLUID_ID_KEY = "Id";
    public static final String FLUID_AMOUNT_KEY = "Amount";
    public static final String BUCKETABLE_DATA_KEY = "BucketableData";

    public InfinityBucketItem() {
        super(ModRarities.LEGEND.getValue(), true, new Properties().stacksTo(1));
    }

    public static InfinityBucketControl getControl(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_CONTROL.get(), InfinityBucketControl.DEFAULT);
    }

    public static void setControl(ItemStack stack, InfinityBucketControl control) {
        stack.set(ModDataComponents.INFINITY_BUCKET_CONTROL.get(), control.clamp(getFluids(stack).size(), getCreatures(stack).size()));
    }

    public static List<FluidStack> getFluids(ItemStack stack) {
        return getFluids(stack, null);
    }

    public static List<FluidStack> getFluids(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        migrateLegacyFluids(stack, registries);
        return stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), InfinityBucketFluids.EMPTY).copyFluids();
    }

    public static boolean trySetFluids(ItemStack stack, List<FluidStack> fluids) {
        if (!InfinityBucketBudget.canStore(fluids, getCreatures(stack))) {
            return false;
        }
        InfinityBucketControl control = getControl(stack);
        InfinityBucketFluids previous = stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), InfinityBucketFluids.EMPTY);
        InfinityBucketFluids stored = new InfinityBucketFluids(fluids);
        if (stored.isEmpty()) {
            stack.remove(ModDataComponents.INFINITY_BUCKET_FLUIDS.get());
        } else {
            stack.set(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), stored);
        }
        int selectedIndex = control.selectedIndex();
        if (!control.creatureSelected() && selectedIndex >= 0 && selectedIndex < previous.size()) {
            FluidStack selected = previous.fluids().get(selectedIndex);
            // Removing another row must not change which fluid is used outside the GUI.
            if (selectedIndex >= stored.size()
                    || !FluidStack.isSameFluidSameComponents(selected, stored.fluids().get(selectedIndex))) {
                for (int i = 0; i < stored.size(); i++) {
                    if (FluidStack.isSameFluidSameComponents(selected, stored.fluids().get(i))) {
                        control = control.selectFluid(i);
                        break;
                    }
                }
            }
        }
        setControl(stack, control);
        return true;
    }

    public static void setFluids(ItemStack stack, List<FluidStack> fluids) {
        trySetFluids(stack, fluids);
    }

    public static List<InfinityBucketCreature> getCreatures(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), InfinityBucketCreatures.EMPTY).copyCreatures();
    }

    public static boolean trySetCreatures(ItemStack stack, List<InfinityBucketCreature> creatures) {
        if (!InfinityBucketBudget.canStore(getFluids(stack), creatures)) {
            return false;
        }
        InfinityBucketControl control = getControl(stack);
        InfinityBucketCreatures previous = stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), InfinityBucketCreatures.EMPTY);
        InfinityBucketCreatures stored = new InfinityBucketCreatures(creatures);
        if (stored.isEmpty()) {
            stack.remove(ModDataComponents.INFINITY_BUCKET_CREATURES.get());
        } else {
            stack.set(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), stored);
        }
        int selectedIndex = control.selectedIndex();
        if (control.creatureSelected() && selectedIndex >= 0 && selectedIndex < previous.size()) {
            InfinityBucketCreature selected = previous.creatures().get(selectedIndex);
            if (selectedIndex >= stored.size() || !selected.equals(stored.creatures().get(selectedIndex))) {
                int newIndex = stored.creatures().indexOf(selected);
                if (newIndex >= 0) {
                    control = control.selectCreature(newIndex);
                }
            }
        }
        setControl(stack, control);
        return true;
    }

    public static void setCreatures(ItemStack stack, List<InfinityBucketCreature> creatures) {
        trySetCreatures(stack, creatures);
    }

    public static void notifyOverCapacity(Player player) {
        if (player != null) {
            player.displayClientMessage(Component.translatable("gui.avaritia.infinity_bucket.over_capacity"), true);
        }
    }

    public static FluidStack getSelectedFluid(ItemStack stack) {
        InfinityBucketControl control = getControl(stack);
        if (control.creatureSelected()) {
            return FluidStack.EMPTY;
        }
        List<FluidStack> fluids = getFluids(stack);
        if (fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int index = Math.min(control.selectedIndex(), fluids.size() - 1);
        return fluids.get(Math.max(0, index));
    }

    @Nullable
    public static InfinityBucketCreature getSelectedCreature(ItemStack stack) {
        InfinityBucketControl control = getControl(stack);
        if (!control.creatureSelected()) {
            return null;
        }
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        if (creatures.isEmpty()) {
            return null;
        }
        int index = Math.min(control.selectedIndex(), creatures.size() - 1);
        return creatures.get(Math.max(0, index));
    }

    public static void migrateLegacyFluids(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return;
        }
        CompoundTag nbt = customData.copyTag();
        if (!nbt.contains(FLUIDS_NBT, Tag.TAG_LIST)) {
            return;
        }
        if (stack.has(ModDataComponents.INFINITY_BUCKET_FLUIDS.get())) {
            return;
        }
        ListTag listTag = nbt.getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        List<FluidStack> migrated = new ArrayList<>();
        for (Tag tag : listTag) {
            if (tag.getId() != Tag.TAG_COMPOUND) {
                continue;
            }
            FluidStack fluid = loadFluidStackFromNBT((CompoundTag) tag, registries);
            if (!fluid.isEmpty()) {
                migrated.add(fluid);
            }
        }
        if (migrated.isEmpty()) {
            return;
        }
        if (!InfinityBucketBudget.canStore(migrated, getCreatures(stack))) {
            return;
        }
        stack.set(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), new InfinityBucketFluids(migrated));
        nbt.remove(FLUIDS_NBT);
        if (nbt.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
    }

    @NotNull
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        return loadFluidStackFromNBT(nbt, null);
    }

    @NotNull
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt, @Nullable HolderLookup.Provider registries) {
        if (nbt == null || nbt.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (nbt.contains(FLUID_ID_KEY, Tag.TAG_STRING)) {
            Optional<Fluid> fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(nbt.getString(FLUID_ID_KEY)));
            if (fluid.isEmpty() || fluid.get() == Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }
            int amount = nbt.getInt(FLUID_AMOUNT_KEY);
            return amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid.get(), amount);
        }
        if (registries != null) {
            return FluidStack.parseOptional(registries, nbt);
        }
        return FluidStack.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElse(FluidStack.EMPTY);
    }

    @NotNull
    public static CompoundTag writeFluidStackToNBT(FluidStack fluidStack, CompoundTag nbt) {
        Tag saved = fluidStack.saveOptional(net.minecraft.core.RegistryAccess.EMPTY);
        if (saved instanceof CompoundTag compoundTag) {
            nbt.merge(compoundTag);
        }
        return nbt;
    }

    @NotNull
    public static String getFluidName(FluidStack fluidStack) {
        return BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString();
    }

    public static boolean isCapturable(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()) {
            return false;
        }
        if (living instanceof Player) {
            return false;
        }
        EntityType<?> type = living.getType();
        if (type.is(Tags.EntityTypes.BOSSES) || type.is(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED)) {
            return false;
        }
        if (living.isPassenger() || living.isVehicle() || !living.getPassengers().isEmpty()) {
            return false;
        }
        return living instanceof Bucketable
                || type.is(EntityTypeTags.AQUATIC)
                || type.is(ModTags.INFINITY_BUCKET_AQUATIC);
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
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new InfinityBucketWrapper(stack), this);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, TooltipContext context, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, pTooltipComponents, pIsAdvanced);
        HolderLookup.Provider registries = context != null ? context.registries() : null;
        migrateLegacyFluids(pStack, registries);
        InfinityBucketControl control = getControl(pStack);
        NumberFormat formater = DecimalFormat.getInstance();
        List<FluidStack> fluids = getFluids(pStack, registries);
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack fluid = fluids.get(i);
            MutableComponent component = MutableComponent.create(fluid.getHoverName().getContents());
            component.append(": " + formater.format(fluid.getAmount()) + " mB");
            if (!control.creatureSelected() && i == control.selectedIndex()) {
                component.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            pTooltipComponents.add(component);
        }
        List<InfinityBucketCreature> creatures = getCreatures(pStack);
        for (int i = 0; i < creatures.size(); i++) {
            InfinityBucketCreature creature = creatures.get(i);
            MutableComponent component = Component.translatable("tooltip.avaritia.infinity_bucket.creature", creature.displayName(), creature.typeId().toString());
            if (control.creatureSelected() && i == control.selectedIndex()) {
                component.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            pTooltipComponents.add(component);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide) {
            migrateLegacyFluids(pStack, pLevel.registryAccess());
        }
        if (pLevel.isClientSide && pEntity instanceof Player player && player.getInventory().getSelected() == pStack) {
            NumberFormat formater = DecimalFormat.getInstance();
            InfinityBucketCreature creature = getSelectedCreature(pStack);
            if (creature != null) {
                player.displayClientMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message_creature", creature.displayName()), true);
                return;
            }
            FluidStack selected = getSelectedFluid(pStack);
            String displayName = selected.getHoverName().getString();
            String amount = formater.format(selected.getAmount());
            player.displayClientMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message", displayName, amount), true);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide) {
                int slot = pUsedHand == InteractionHand.MAIN_HAND ? pPlayer.getInventory().selected : 40;
                pPlayer.openMenu(
                        new SimpleMenuProvider((id, inventory, player) -> new InfinityBucketMenu(id, inventory, slot),
                                Component.translatable("container.avaritia.infinity_bucket")),
                        buf -> buf.writeInt(slot));
            }
            return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide());
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
        if (pLevel.getFluidState(hitPos).isSource()) {
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
                copyStoredState(result, stack);
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

        InfinityBucketCreature selectedCreature = getSelectedCreature(stack);
        if (selectedCreature != null) {
            BlockPos releasePos = hitState.getFluidState().is(FluidTags.WATER) ? hitPos : hitPos.relative(side);
            return tryRelease(stack, player, (ServerLevel) level, releasePos, selectedCreature);
        }

        if (!worldFluid) {
            IFluidHandler tank = FluidUtil.getFluidHandler(level, hitPos, side).orElse(null);
            if (tank == null) {
                tank = FluidUtil.getFluidHandler(level, hitPos, null).orElse(null);
            }
            if (tank != null) {
                FluidStack selected = getSelectedFluid(stack);
                if (selected.isEmpty()) {
                    return false;
                }
                IFluidHandlerItem bucket = FluidUtil.getFluidHandler(stack).orElse(null);
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
        FluidStack toPlace = selected.copyWithAmount(FluidType.BUCKET_VOLUME);
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
        IFluidHandlerItem source = FluidUtil.getFluidHandler(container).orElse(null);
        if (source == null || source.drain(resource, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
            return false;
        }

        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, container, new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false));
        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestNonSolid = !destBlockState.isSolid();
        boolean isDestReplaceable = destBlockState.canBeReplaced(context);
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer
                && ((LiquidBlockContainer) destBlockState.getBlock()).canPlaceLiquid(player, level, pos, destBlockState, fluid);
        if (!level.isEmptyBlock(pos) && !isDestNonSolid && !isDestReplaceable && !canDestContainFluid) {
            return false;
        }

        BlockState placedState = fluid.getFluidType().getBlockForFluidState(level, pos, fluid.defaultFluidState());
        if (!canDestContainFluid && placedState.isAir()) {
            // Ignore positional vetoes, but still require a real fluid block.
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
        SoundEvent soundevent = resource.getFluidType().getSound(resource, SoundActions.BUCKET_EMPTY);
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
        if (EventHooks.onBlockPlace(player, before, Direction.UP)) {
            level.restoringBlockSnapshots = true;
            before.restore();
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
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        if (creatures.size() >= InfinityBucketBudget.MAX_CREATURE_ENTRIES) {
            notifyOverCapacity(player);
            return false;
        }
        Entity entity = mobBucket.type.create(level);
        if (!(entity instanceof LivingEntity living) || !(entity instanceof Bucketable bucketable)) {
            return false;
        }
        EntityType.createDefaultStackConfig(level, source, player).accept(entity);
        CustomData bucketData = source.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
        bucketable.loadFromBucketTag(bucketData.copyTag());
        bucketable.setFromBucket(true);
        if (!isCapturable(living)) {
            return false;
        }
        CompoundTag tag = saveCreatureData(living);
        if (tag == null) {
            return false;
        }
        tag.put(BUCKETABLE_DATA_KEY, bucketData.copyTag());
        creatures.add(new InfinityBucketCreature(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()), tag));

        ItemStack proposed = stack.copy();
        if (!trySetCreatures(proposed, creatures)) {
            notifyOverCapacity(player);
            return false;
        }
        FluidStack fluid = new FluidStack(mobBucket.content, FluidType.BUCKET_VOLUME);
        if (!fluid.isEmpty() && new InfinityBucketWrapper(proposed).fill(fluid, IFluidHandler.FluidAction.EXECUTE) != fluid.getAmount()) {
            notifyOverCapacity(player);
            return false;
        }
        setControl(proposed, getControl(proposed).selectCreature(creatures.size() - 1));
        copyStoredState(proposed, stack);
        return true;
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
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (target instanceof Bucketable bucketable) {
            ItemStack bucketTag = bucketable.getBucketItemStack();
            bucketable.saveToBucketTag(bucketTag);
            CustomData data = bucketTag.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            if (!data.isEmpty()) {
                tag.put(BUCKETABLE_DATA_KEY, data.copyTag());
            }
        }
        InfinityBucketCreature creature = new InfinityBucketCreature(typeId, tag);
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        creatures.add(creature);
        if (!trySetCreatures(stack, creatures)) {
            notifyOverCapacity(player);
            return false;
        }
        InfinityBucketControl control = getControl(stack);
        setControl(stack, control.selectCreature(creatures.size() - 1));
        if (target instanceof Bucketable bucketable) {
            target.playSound(bucketable.getPickupSound(), 1.0F, 1.0F);
        } else {
            player.level().playSound(null, target.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        target.discard();
        return true;
    }

    private boolean tryRelease(ItemStack stack, Player player, ServerLevel level, BlockPos pos, InfinityBucketCreature selected) {
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && !PlayerUtils.hasEditPermission(serverPlayer, pos)) {
            return false;
        }
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        int index = creatures.indexOf(selected);
        if (index < 0) {
            InfinityBucketControl control = getControl(stack);
            if (!control.creatureSelected() || control.selectedIndex() >= creatures.size()) {
                return false;
            }
            index = control.selectedIndex();
            selected = creatures.get(index);
        }
        CompoundTag tag = selected.entityData().copy();
        tag.putString("id", selected.typeId().toString());
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
        if (entity instanceof Bucketable bucketable && tag.contains(BUCKETABLE_DATA_KEY, Tag.TAG_COMPOUND)) {
            bucketable.loadFromBucketTag(tag.getCompound(BUCKETABLE_DATA_KEY));
            bucketable.setFromBucket(true);
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

    private static void copyStoredState(ItemStack from, ItemStack to) {
        var fluids = from.get(ModDataComponents.INFINITY_BUCKET_FLUIDS.get());
        if (fluids != null) {
            to.set(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), fluids);
        } else {
            to.remove(ModDataComponents.INFINITY_BUCKET_FLUIDS.get());
        }
        var creatures = from.get(ModDataComponents.INFINITY_BUCKET_CREATURES.get());
        if (creatures != null) {
            to.set(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), creatures);
        } else {
            to.remove(ModDataComponents.INFINITY_BUCKET_CREATURES.get());
        }
        var control = from.get(ModDataComponents.INFINITY_BUCKET_CONTROL.get());
        if (control != null) {
            to.set(ModDataComponents.INFINITY_BUCKET_CONTROL.get(), control);
        } else {
            to.remove(ModDataComponents.INFINITY_BUCKET_CONTROL.get());
        }
    }
}
