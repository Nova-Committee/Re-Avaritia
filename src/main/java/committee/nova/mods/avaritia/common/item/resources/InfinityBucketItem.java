package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.common.capability.wrappers.InfinityBucketWrapper;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午10:36
 * @Description:
 */
public class InfinityBucketItem extends ResourceItem {
    public static final String FLUIDS_NBT = "Fluids";
    public static final String FLUID_ID_KEY = "Id";
    public static final String FLUID_AMOUNT_KEY = "Amount";

    public InfinityBucketItem() {
        super(ModRarities.LEGEND, "infinity_bucket", true, new Properties().stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new InfinityBucketWrapper(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        List<FluidStack> fluids = getFluids(pStack);
        NumberFormat formater = DecimalFormat.getInstance();
        for (FluidStack fluid : fluids) {
            MutableComponent component = MutableComponent.create(fluid.getDisplayName().getContents());
            component.append(": " + formater.format(fluid.getAmount()) + "mL");
            pTooltipComponents.add(component);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pEntity instanceof Player player && player.getInventory().getSelected() == pStack) {
            FluidStack firstContained = getFluids(pStack).stream().findFirst().orElse(FluidStack.EMPTY);
            NumberFormat formater = DecimalFormat.getInstance();
            String displayName = firstContained.getDisplayName().getString();
            String amount = formater.format(firstContained.getAmount());
            player.displayClientMessage(Component.translatable("tooltip.infinity_bucket.message", displayName, amount), true);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching()) {
            List<FluidStack> fluids = getFluids(itemStack);
            if (fluids.isEmpty()) {
                return InteractionResultHolder.fail(itemStack);
            }
            FluidStack firstContained = fluids.remove(0);
            fluids.add(firstContained);
            setFluids(itemStack, fluids);
            return InteractionResultHolder.success(itemStack);
        }

        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(itemStack).resolve().orElse(null);
        if (fluidHandler == null) {
            return InteractionResultHolder.pass(itemStack);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = pLevel.getBlockState(hitPos);
        Block hitBlock = hitState.getBlock();
        boolean canPickUp = hitBlock instanceof IFluidBlock || hitBlock instanceof BucketPickup;
        if (pLevel.mayInteract(pPlayer, hitPos) && canPickUp) {
            FluidActionResult pickUpResult = FluidUtil.tryPickUpFluid(itemStack, pPlayer, pLevel, hitPos, hitResult.getDirection());
            if (pickUpResult.isSuccess()) {
                return InteractionResultHolder.success(pickUpResult.getResult());
            }
        }

        BlockPos placePos = hitPos.offset(hitResult.getDirection().getNormal());
        if (pLevel.mayInteract(pPlayer, placePos)) {
            FluidStack drained = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() == 1000) {
                FluidActionResult placeResult = FluidUtil.tryPlaceFluid(pPlayer, pLevel, pUsedHand, placePos, itemStack, drained);
                if (placeResult.isSuccess()) {
                    fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    return InteractionResultHolder.success(placeResult.getResult());
                }
            }
        }

        return InteractionResultHolder.fail(itemStack);
    }

    public static List<FluidStack> getFluids(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null)
            return new ArrayList<>();

        if (!nbt.contains(FLUIDS_NBT, Tag.TAG_LIST))
            return new ArrayList<>();

        return nbt.getList(FLUIDS_NBT, Tag.TAG_COMPOUND).stream()
                .filter(tag -> tag.getId() == Tag.TAG_COMPOUND)
                .map(tag -> loadFluidStackFromNBT((CompoundTag) tag))
                .collect(Collectors.toList());
    }

    public static void setFluids(ItemStack stack, List<FluidStack> fluids) {
        ListTag listTag = new ListTag();
        for (FluidStack fluid : fluids) {
            listTag.add(writeFluidStackToNBT(fluid, new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put(FLUIDS_NBT, listTag);
        stack.setTag(tag);
    }

    @NotNull
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return FluidStack.EMPTY;
        }

        if (!nbt.contains(FLUID_ID_KEY, Tag.TAG_STRING)) {
            return FluidStack.EMPTY;
        }

        ResourceLocation fluidName = new ResourceLocation(nbt.getString(FLUID_ID_KEY));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid == null) {
            return FluidStack.EMPTY;
        }

        int amount = nbt.getInt(FLUID_AMOUNT_KEY);
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
        ResourceLocation fluidName = ForgeRegistries.FLUIDS.getKey(fluid);
        if (fluidName == null) return "";
        return fluidName.toString();
    }
}
