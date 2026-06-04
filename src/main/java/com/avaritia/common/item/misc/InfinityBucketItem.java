package com.avaritia.common.item.misc;

import com.avaritia.init.registry.ModItems;

import com.avaritia.api.iface.item.IItemCapability;
import com.avaritia.common.item.resources.ResourceItem;
import com.avaritia.common.wrappers.InfinityBucketWrapper;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public InfinityBucketItem() {
        super(ModRarities.LEGEND.getValue(), true, ModItems.properties().stacksTo(1));
    }

    public static List<FluidStack> getFluids(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty())
            return new ArrayList<>();

        if (!nbt.contains(FLUIDS_NBT))
            return new ArrayList<>();

        return nbt.getListOrEmpty(FLUIDS_NBT).stream()
                .filter(tag -> tag.getId() == Tag.TAG_COMPOUND)
                .map(tag -> loadFluidStackFromNBT((CompoundTag) tag))
                .filter(fluid -> !fluid.isEmpty())
                .collect(Collectors.toList());
    }

    public static void setFluids(ItemStack stack, List<FluidStack> fluids) {
        ListTag listTag = new ListTag();
        for (FluidStack fluid : fluids) {
            listTag.add(writeFluidStackToNBT(fluid, new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put(FLUIDS_NBT, listTag);
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, (nbt) -> nbt.update(tag1 -> tag1.merge(tag)));
    }

    @NotNull
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return FluidStack.EMPTY;
        }

        if (!nbt.contains(FLUID_ID_KEY)) {
            return FluidStack.EMPTY;
        }

        Identifier fluidName = Identifier.tryParse(nbt.getStringOr(FLUID_ID_KEY, ""));
        if (fluidName == null) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidName);

        int amount = nbt.getIntOr(FLUID_AMOUNT_KEY, 0);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    @NotNull
    public static CompoundTag writeFluidStackToNBT(FluidStack fluidStack, CompoundTag nbt) {
        nbt.putString(FLUID_ID_KEY, getFluidName(fluidStack));
        nbt.putInt(FLUID_AMOUNT_KEY, fluidStack.getAmount());
        return nbt;
    }

    @NotNull
    public static String getFluidName(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid);
        return fluidName.toString();
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Fluid.ITEM, (stack, context) -> new InfinityBucketWrapper(stack), this);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, display, tooltip, pIsAdvanced);
        List<FluidStack> fluids = getFluids(pStack);
        NumberFormat formater = DecimalFormat.getInstance();
        for (FluidStack fluid : fluids) {
            MutableComponent component = MutableComponent.create(fluid.getHoverName().getContents());
            component.append(": " + formater.format(fluid.getAmount()) + " mB");
            tooltip.accept(component);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull ServerLevel pLevel, @NotNull Entity pEntity,  EquipmentSlot slot) {
        super.inventoryTick(pStack, pLevel, pEntity, slot);
        if (pEntity instanceof Player player && player.getInventory().getSelectedItem() == pStack) {
            FluidStack firstContained = getFluids(pStack).stream().findFirst().orElse(FluidStack.EMPTY);
            NumberFormat formater = DecimalFormat.getInstance();
            String displayName = firstContained.getHoverName().getString();
            String amount = formater.format(firstContained.getAmount());
            player.sendOverlayMessage(Component.translatable("tooltip.avaritia.infinity_bucket.message", displayName, amount));
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching()) {
            List<FluidStack> fluids = getFluids(itemStack);
            if (fluids.isEmpty()) {
                return InteractionResult.FAIL;
            }
            FluidStack firstContained = fluids.remove(0);
            fluids.add(firstContained);
            setFluids(itemStack, fluids);
            return InteractionResult.SUCCESS;
        }

        ResourceHandler<FluidResource> fluidHandler = ItemAccess.forStack(itemStack)
                .oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = pLevel.getBlockState(hitPos);
        Block hitBlock = hitState.getBlock();
        boolean canPickUp =
                hitBlock instanceof LiquidBlock ||
                hitBlock instanceof BucketPickup;
        if (pLevel.mayInteract(pPlayer, hitPos) && canPickUp) {
            FluidStack pickedUp = FluidUtil.tryPickupFluid(fluidHandler, pPlayer, pLevel, hitPos, hitResult.getDirection());
            if (!pickedUp.isEmpty()) {
                return InteractionResult.SUCCESS;
            }
        }

        BlockPos placePos = hitPos.offset(hitResult.getDirection().getUnitVec3i());
        if (pLevel.mayInteract(pPlayer, placePos)) {
            FluidStack placed = FluidUtil.tryPlaceFluid(fluidHandler, pPlayer, pLevel, pUsedHand, placePos);
            if (!placed.isEmpty()) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }
}
