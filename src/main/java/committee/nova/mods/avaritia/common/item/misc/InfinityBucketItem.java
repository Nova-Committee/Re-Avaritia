package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.item.IItemCapability;
import committee.nova.mods.avaritia.api.utils.PlayerUtils;
import committee.nova.mods.avaritia.common.component.InfinityBucketBudget;
import committee.nova.mods.avaritia.common.component.InfinityBucketContents;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.common.wrappers.InfinityBucketWrapper;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Infinity Bucket: unbounded fluid storage, aquatic capture, and a bound management menu.
 */
public class InfinityBucketItem extends ResourceItem implements IItemCapability {
    public static final String FLUIDS_NBT = "Fluids";
    public static final String FLUID_ID_KEY = "Id";
    public static final String FLUID_AMOUNT_KEY = "Amount";
    public static final String BUCKETABLE_DATA_KEY = "BucketableData";

    public InfinityBucketItem() {
        super(ModRarities.LEGEND.getValue(), true, ModItems.properties().stacksTo(1));
    }

    public static InfinityBucketControl getControl(ItemStack stack) {
        migrateLegacy(stack);
        return stack.getOrDefault(ModDataComponents.INFINITY_BUCKET_CONTROL.get(), InfinityBucketControl.DEFAULT);
    }

    public static void setControl(ItemStack stack, InfinityBucketControl control) {
        InfinityBucketControl clamped = control.clamp(getFluids(stack).size(), getCreatures(stack).size());
        if (clamped.equals(InfinityBucketControl.DEFAULT)) {
            stack.remove(ModDataComponents.INFINITY_BUCKET_CONTROL.get());
        } else {
            stack.set(ModDataComponents.INFINITY_BUCKET_CONTROL.get(), clamped);
        }
    }

    public static List<FluidStack> getFluids(ItemStack stack) {
        migrateLegacy(stack);
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
        migrateLegacy(stack);
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
            player.sendOverlayMessage(Component.translatable("gui.avaritia.infinity_bucket.over_capacity"));
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

    public static void migrateLegacy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof InfinityBucketItem)) {
            return;
        }
        boolean hasSplit = stack.has(ModDataComponents.INFINITY_BUCKET_FLUIDS.get())
                || stack.has(ModDataComponents.INFINITY_BUCKET_CREATURES.get())
                || stack.has(ModDataComponents.INFINITY_BUCKET_CONTROL.get());
        InfinityBucketContents combined = stack.get(ModDataComponents.INFINITY_BUCKET.get());
        if (combined != null && !hasSplit) {
            combined.writeSplitComponents(stack);
            stack.remove(ModDataComponents.INFINITY_BUCKET.get());
            hasSplit = true;
        } else if (combined != null && hasSplit) {
            stack.remove(ModDataComponents.INFINITY_BUCKET.get());
        }
        CustomData custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (custom.isEmpty()) {
            return;
        }
        CompoundTag nbt = custom.copyTag();
        if (!nbt.contains(FLUIDS_NBT) || stack.has(ModDataComponents.INFINITY_BUCKET_FLUIDS.get())) {
            return;
        }
        List<FluidStack> migrated = new ArrayList<>();
        for (Tag tag : nbt.getListOrEmpty(FLUIDS_NBT)) {
            if (!(tag instanceof CompoundTag compound)) {
                continue;
            }
            FluidStack fluid = loadFluidStackFromNBT(compound);
            if (!fluid.isEmpty()) {
                migrated.add(fluid);
            }
        }
        if (migrated.isEmpty() || !InfinityBucketBudget.canStore(migrated, getCreatures(stack))) {
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
        if (nbt == null) {
            return FluidStack.EMPTY;
        }
        String rawId = nbt.contains(FLUID_ID_KEY) ? nbt.getStringOr(FLUID_ID_KEY, "") : nbt.getStringOr("id", "");
        Identifier fluidName = Identifier.tryParse(rawId);
        if (fluidName == null) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidName);
        int amount = nbt.contains(FLUID_AMOUNT_KEY) ? nbt.getIntOr(FLUID_AMOUNT_KEY, 0) : nbt.getIntOr("amount", 0);
        if (amount <= 0 || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = new FluidStack(fluid, amount);
        if (nbt.contains("components")) {
            net.minecraft.core.component.DataComponentPatch.CODEC
                    .parse(NbtOps.INSTANCE, nbt.get("components"))
                    .result()
                    .ifPresent(stack::applyComponents);
        }
        return stack.isEmpty() ? FluidStack.EMPTY : stack;
    }

    @NotNull
    public static CompoundTag writeFluidStackToNBT(FluidStack fluidStack, CompoundTag nbt) {
        nbt.putString(FLUID_ID_KEY, getFluidName(fluidStack));
        nbt.putInt(FLUID_AMOUNT_KEY, fluidStack.getAmount());
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
        if (living.typeHolder().is(ModTags.INFINITY_BUCKET_BOSSES)
                || living.typeHolder().is(ModTags.INFINITY_BUCKET_CAPTURING_NOT_SUPPORTED)) {
            return false;
        }
        if (living.isPassenger() || living.isVehicle() || !living.getPassengers().isEmpty()) {
            return false;
        }
        return living instanceof Bucketable
                || living.typeHolder().is(EntityTypeTags.AQUATIC)
                || living.typeHolder().is(ModTags.INFINITY_BUCKET_AQUATIC);
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
        event.registerItem(Capabilities.Fluid.ITEM, (stack, context) -> {
            ItemAccess access = context != null ? context : ItemAccess.forStack(stack);
            return new InfinityBucketWrapper(access);
        }, this);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        migrateLegacy(stack);
        InfinityBucketControl control = getControl(stack);
        NumberFormat formatter = DecimalFormat.getInstance();
        List<FluidStack> fluids = getFluids(stack);
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack fluid = fluids.get(i);
            MutableComponent line = MutableComponent.create(fluid.getHoverName().getContents());
            line.append(": " + formatter.format(fluid.getAmount()) + " mB");
            if (!control.creatureSelected() && i == control.selectedIndex()) {
                line.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            tooltip.accept(line);
        }
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        for (int i = 0; i < creatures.size(); i++) {
            InfinityBucketCreature creature = creatures.get(i);
            MutableComponent line = Component.translatable("tooltip.avaritia.infinity_bucket.creature",
                    creature.displayName(), creature.typeId().toString());
            if (control.creatureSelected() && i == control.selectedIndex()) {
                line.append(" ").append(Component.translatable("tooltip.avaritia.infinity_bucket.selected"));
            }
            tooltip.accept(line);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        migrateLegacy(stack);
        if (entity instanceof Player player && player.getInventory().getSelectedItem() == stack) {
            NumberFormat formatter = DecimalFormat.getInstance();
            InfinityBucketCreature creature = getSelectedCreature(stack);
            if (creature != null) {
                player.sendOverlayMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message_creature", creature.displayName()));
                return;
            }
            FluidStack selected = getSelectedFluid(stack);
            player.sendOverlayMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message",
                    selected.getHoverName().getString(), formatter.format(selected.getAmount())));
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
                UUID nonce = stampMenuNonce(stack);
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, opener) -> new InfinityBucketMenu(id, inventory, slot, nonce),
                        Component.translatable("container.avaritia.infinity_bucket")),
                        buf -> {
                            buf.writeInt(slot);
                            buf.writeUUID(nonce);
                        });
            }
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos hitPos = hit.getBlockPos();
        Direction side = hit.getDirection();
        if (!level.mayInteract(player, hitPos) || !player.mayUseItemAt(hitPos, side, stack)) {
            return InteractionResult.FAIL;
        }
        if (!getControl(stack).creatureSelected() && level.getFluidState(hitPos).isSource()) {
            ItemAccess access = heldBucketAccess(player, hand);
            ResourceHandler<FluidResource> bucket = access.getCapability(Capabilities.Fluid.ITEM);
            if (bucket != null && level.getBlockState(hitPos).getBlock() instanceof BucketPickup
                    && !FluidUtil.tryPickupFluid(bucket, player, level, hitPos, side).isEmpty()) {
                syncHeldBucket(player, hand, stack);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        if (tryOutputToWorld(player, level, hand, stack, hit)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return canCaptureAt(player, stack, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        return tryCapture(held, player, target) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private boolean tryOutputToWorld(Player player, Level level, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        BlockPos hitPos = hit.getBlockPos();
        Direction side = hit.getDirection();
        BlockState hitState = level.getBlockState(hitPos);

        InfinityBucketCreature selectedCreature = getSelectedCreature(stack);
        if (selectedCreature != null) {
            BlockPos releasePos = hitState.getFluidState().is(FluidTags.WATER) ? hitPos : hitPos.relative(side);
            return tryRelease(stack, player, (ServerLevel) level, releasePos, selectedCreature);
        }

        ItemAccess access = heldBucketAccess(player, hand);
        ResourceHandler<FluidResource> bucket = access.getCapability(Capabilities.Fluid.ITEM);
        if (bucket == null) {
            return false;
        }
        ResourceHandler<FluidResource> block = level.getCapability(Capabilities.Fluid.BLOCK, hitPos, side);
        if (block == null) {
            block = level.getCapability(Capabilities.Fluid.BLOCK, hitPos, null);
        }
        if (block != null) {
            FluidStack selected = getSelectedFluid(stack);
            if (selected.isEmpty()) {
                return false;
            }
            FluidResource resource = FluidResource.of(selected);
            try (Transaction tx = Transaction.openRoot()) {
                int moved = ResourceHandlerUtil.move(bucket, block, resource::equals, Integer.MAX_VALUE, tx);
                if (moved > 0) {
                    tx.commit();
                    syncHeldBucket(player, hand, stack);
                    return true;
                }
            }
            return false;
        }

        FluidStack selected = getSelectedFluid(stack);
        if (selected.isEmpty() || selected.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }
        FluidResource resource = FluidResource.of(selected);
        BlockPos placePos = canBlockContainFluid(player, level, hitPos, hitState, resource.getFluid())
                ? hitPos : hitPos.relative(side);
        if (!level.mayInteract(player, placePos) || !player.mayUseItemAt(placePos, side, stack)) {
            return false;
        }
        if (!placeWorldFluid(player, level, hand, placePos, bucket, resource)) {
            return false;
        }
        syncHeldBucket(player, hand, stack);
        return true;
    }

    private boolean canBlockContainFluid(Player player, Level level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, state, fluid);
    }

    private boolean placeWorldFluid(Player player, Level level, InteractionHand hand, BlockPos pos,
                                    ResourceHandler<FluidResource> bucket, FluidResource resource) {
        if (resource.isEmpty()) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && !PlayerUtils.hasEditPermission(serverPlayer, pos)) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = bucket.extract(resource, FluidType.BUCKET_VOLUME, tx);
            if (extracted != FluidType.BUCKET_VOLUME) {
                return false;
            }
            if (!placeProtectedFluid(player, level, hand, pos, resource)) {
                return false;
            }
            tx.commit();
            FluidUtil.triggerSoundAndGameEvent(resource, level, Vec3.atCenterOf(pos), player, false);
            return true;
        }
    }

    private boolean placeProtectedFluid(Player player, Level level, InteractionHand hand, BlockPos pos, FluidResource resource) {
        Fluid fluid = resource.getFluid();
        var fluidType = resource.getFluidType();
        FluidStack stack = resource.toStack(FluidType.BUCKET_VOLUME);
        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, player.getItemInHand(hand),
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        BlockState dest = level.getBlockState(pos);
        boolean replaceable = dest.canBeReplaced(context);
        boolean canContain = dest.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, dest, fluid);
        if (!dest.isAir() && !replaceable && !canContain) {
            return false;
        }
        BlockState placedState = fluidType.getBlockForFluidState(level, pos, fluid.defaultFluidState());
        if (!canContain && (placedState == null || placedState.isAir())) {
            placedState = fluid.defaultFluidState().createLegacyBlock();
            if (placedState.isAir()) {
                return false;
            }
        }
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos, Block.UPDATE_ALL_IMMEDIATE);
        boolean placed;
        if (canContain) {
            placed = ((LiquidBlockContainer) dest.getBlock()).placeLiquid(level, pos, dest, fluid.defaultFluidState());
        } else {
            if (!level.isClientSide() && replaceable && !dest.liquid()) {
                level.destroyBlock(pos, true);
            }
            placed = level.setBlock(pos, placedState, Block.UPDATE_ALL_IMMEDIATE);
        }
        if (!placed) {
            snapshot.restore();
            return false;
        }
        if (EventHooks.onBlockPlace(player, snapshot, player.getDirection())) {
            snapshot.restore();
            return false;
        }
        return true;
    }

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
        Entity entity = mobBucket.type.create(level, EntitySpawnReason.BUCKET);
        if (!(entity instanceof LivingEntity living) || !(entity instanceof Bucketable bucketable)) {
            return false;
        }
        try {
            EntityType.createDefaultStackConfig(level, source, player).accept(entity);
            CustomData bucketData = source.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            bucketable.loadFromBucketTag(bucketData.copyTag());
            bucketable.setFromBucket(true);
            if (!isCapturable(living)) {
                return false;
            }
            CompoundTag tag = saveCreatureData(living, level);
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
            Fluid fluid = mobBucket.getContent();
            if (fluid != Fluids.EMPTY) {
                try (Transaction tx = Transaction.openRoot()) {
                    ItemAccess access = ItemAccess.forStack(proposed);
                    ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
                    if (handler == null) {
                        return false;
                    }
                    int inserted = handler.insert(FluidResource.of(fluid), FluidType.BUCKET_VOLUME, tx);
                    if (inserted != FluidType.BUCKET_VOLUME) {
                        notifyOverCapacity(player);
                        return false;
                    }
                    tx.commit();
                }
            }
            setControl(proposed, getControl(proposed).selectCreature(creatures.size() - 1));
            copyStoredState(proposed, stack);
            return true;
        } finally {
            entity.discard();
        }
    }

    public static ItemStack tryExtractMobBucket(ItemStack stack, ItemStack destination, Player player, int creatureIndex) {
        if (!(player.level() instanceof ServerLevel level) || !(stack.getItem() instanceof InfinityBucketItem)
                || stack.getCount() != 1 || destination.getCount() != 1 || !destination.is(Items.BUCKET)) {
            return ItemStack.EMPTY;
        }
        List<InfinityBucketCreature> creatures = getCreatures(stack);
        if (creatureIndex < 0 || creatureIndex >= creatures.size()) {
            return ItemStack.EMPTY;
        }
        InfinityBucketCreature creature = creatures.get(creatureIndex);
        ItemStack filled = toMobBucket(creature, level);
        if (filled.isEmpty() || !(filled.getItem() instanceof MobBucketItem mobBucket)) {
            return ItemStack.EMPTY;
        }
        ItemStack proposed = stack.copy();
        Fluid fluid = mobBucket.getContent();
        if (fluid != Fluids.EMPTY) {
            try (Transaction tx = Transaction.openRoot()) {
                ItemAccess access = ItemAccess.forStack(proposed);
                ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
                if (handler == null) {
                    return ItemStack.EMPTY;
                }
                FluidResource resource = FluidResource.of(fluid);
                int drained = handler.extract(resource, FluidType.BUCKET_VOLUME, tx);
                if (drained != FluidType.BUCKET_VOLUME) {
                    return ItemStack.EMPTY;
                }
                tx.commit();
            }
        }
        List<InfinityBucketCreature> next = getCreatures(proposed);
        if (creatureIndex >= next.size() || !next.get(creatureIndex).equals(creature)) {
            return ItemStack.EMPTY;
        }
        next.remove(creatureIndex);
        if (!trySetCreatures(proposed, next)) {
            return ItemStack.EMPTY;
        }
        copyStoredState(proposed, stack);
        return filled;
    }

    private static ItemStack toMobBucket(InfinityBucketCreature creature, ServerLevel level) {
        EntityType<?> type = creature.entityType();
        if (type == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = creature.entityData().copy();
        tag.putString("id", creature.typeId().toString());
        Entity entity = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag),
                level, EntitySpawnReason.BUCKET).orElseGet(() -> type.create(level, EntitySpawnReason.BUCKET));
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
        setControl(stack, getControl(stack).selectCreature(creatures.size() - 1));
        if (target instanceof Bucketable bucketable) {
            target.playSound(bucketable.getPickupSound(), 1.0F, 1.0F);
        } else {
            level.playSound(player, target.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
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
        Optional<Entity> created = EntityType.create(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag),
                level, EntitySpawnReason.BUCKET);
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
        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        return true;
    }

    private static ItemAccess heldBucketAccess(Player player, InteractionHand hand) {
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
        return ItemAccess.forPlayerSlot(player, slot);
    }

    private static void syncHeldBucket(Player player, InteractionHand hand, ItemStack stack) {
        ItemStack current = player.getItemInHand(hand);
        if (current != stack && current.is(stack.getItem())) {
            copyStoredState(current, stack);
            player.setItemInHand(hand, stack);
        }
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

    public static UUID stampMenuNonce(ItemStack stack) {
        UUID nonce = UUID.randomUUID();
        stack.set(ModDataComponents.INFINITY_BUCKET_MENU_NONCE.get(), nonce);
        return nonce;
    }

    public static void clearMenuNonce(ItemStack stack, UUID nonce) {
        UUID current = stack.get(ModDataComponents.INFINITY_BUCKET_MENU_NONCE.get());
        if (nonce.equals(current)) {
            stack.remove(ModDataComponents.INFINITY_BUCKET_MENU_NONCE.get());
        }
    }
}
