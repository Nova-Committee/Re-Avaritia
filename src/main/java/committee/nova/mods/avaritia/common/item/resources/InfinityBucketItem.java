package committee.nova.mods.avaritia.common.item.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/13 上午10:36
 * @Description:
 */
public class InfinityBucketItem extends ResourceItem {

    public InfinityBucketItem() {
        super(Rarity.EPIC, "infinity_bucket", true,new Properties().stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, Integer.MAX_VALUE);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandler -> {
            FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            pTooltipComponents.add(Component.literal(fluidStack.getDisplayName().getString() + ": " + fluidStack.getAmount() + "mL"));
        });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        tryMigrateOldData(itemStack);

        IFluidHandlerItem fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (fluidHandler == null) return InteractionResultHolder.pass(itemStack);

        BlockHitResult hitResult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(itemStack);

        BlockPos hitPos = hitResult.getBlockPos();
        BlockState hitState = pLevel.getBlockState(hitPos);
        if (pLevel.mayInteract(pPlayer, hitPos) && hitState.getBlock() instanceof BucketPickup drainable) {
            ItemStack pickupResult = drainable.pickupBlock(pLevel, hitPos, hitState);
            if (!pickupResult.isEmpty() && pickupResult.getItem() instanceof BucketItem bucketItem) {
                FluidStack picked = new FluidStack(bucketItem.getFluid(), 1000);
                if (fluidHandler.fill(picked, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                    fluidHandler.fill(picked, IFluidHandler.FluidAction.EXECUTE);
                    playFillingSound(pPlayer, pLevel, hitPos, picked.getFluid());
                    return InteractionResultHolder.success(itemStack);
                }
            }
        }

        BlockPos placePos = hitPos.offset(hitResult.getDirection().getNormal());
        BlockState placeState = pLevel.getBlockState(placePos);
        if (pLevel.mayInteract(pPlayer, placePos)) {
            if (fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE).getAmount() == 1000) {
                Fluid drained = fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE).getFluid();
                if (!pLevel.isClientSide && placeState.canBeReplaced(drained)) {
                    pLevel.destroyBlock(placePos, true);
                }
                if (pLevel.setBlock(placePos, drained.defaultFluidState().createLegacyBlock(), 11) && !placeState.getFluidState().isSource()) {
                    this.playEmptyingSound(pPlayer, pLevel, placePos, drained);
                }
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pEntity instanceof Player player && player.getInventory().getSelected() == pStack) {
            pStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandler -> {
                FluidStack fluidStack = fluidHandler.getFluidInTank(0);
                player.displayClientMessage(Component.translatable("tooltip.infinity_bucket.message", fluidStack.getDisplayName().getString(), String.format("%,d", fluidStack.getAmount())), true);
            });
        }
    }

    protected void playEmptyingSound(Player player, LevelAccessor world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }

    protected void playFillingSound(Player player, LevelAccessor world, BlockPos pos, Fluid fluid) {
        SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
    }

    /**
     * @since 1.20.1-1.3.8.3
     */
    private static void tryMigrateOldData(ItemStack itemStack) {
        final String FLUIDS_NBT = "Fluids";
        final String FLUID_ID_KEY = "Id";
        final String FLUID_AMOUNT_KEY = "Amount";

        if (itemStack.hasTag() && itemStack.getTag().contains(FLUIDS_NBT)) {
            ListTag items = itemStack.getTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag compound = items.getCompound(i);
                ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
                int amount = compound.getInt(FLUID_AMOUNT_KEY);
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
                FluidStack fluidStack = new FluidStack(fluid, amount);
                itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(fluidHandlerItem -> {
                    fluidHandlerItem.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                });
            }
            itemStack.removeTagKey(FLUIDS_NBT);
        }
    }
}
