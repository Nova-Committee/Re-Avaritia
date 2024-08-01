package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.common.capability.wrappers.InfinityBucketWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
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
    public static final String FLUIDS_NBT = "Fluids";
    public static final String FLUID_ID_KEY = "Id";
    public static final String FLUID_AMOUNT_KEY = "Amount";

    public InfinityBucketItem() {
        super(Rarity.EPIC, "infinity_bucket", true,new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        ListTag items = pStack.getOrCreateTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag compound = items.getCompound(i);
            ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (id.toLanguageKey().startsWith("gtceu")) {
                pTooltipComponents.add(getGTMFluidName(id).append(Component.literal(": " + String.format("%,d", amount) + "mL")));
            } else {
                pTooltipComponents.add(Component.translatable(id.toLanguageKey("block")).append(Component.literal(": " + String.format("%,d", amount) + "mL")));
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isCrouching()) {
            ListTag items = itemStack.getOrCreateTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
            if (!items.isEmpty()) items.add(items.remove(0));
            itemStack.getOrCreateTag().put(FLUIDS_NBT, items);
            return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
        }
        BlockHitResult result = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (result.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(itemStack);
        BlockState target = pLevel.getBlockState(result.getBlockPos());
        if (pLevel.mayInteract(pPlayer, result.getBlockPos()) && target.getBlock() instanceof BucketPickup drainable) {
            ItemStack r = drainable.pickupBlock(pLevel, result.getBlockPos(), target);
            if (!r.isEmpty() && r.getItem() instanceof BucketItem bucketItem) {
                Fluid fluid = bucketItem.getFluid();
                this.insertFluid(itemStack, fluid, 1000);
                playFillingSound(pPlayer, pLevel, result.getBlockPos(), fluid);
            }
        } else {
            BlockPos newPos = result.getBlockPos().offset(result.getDirection().getNormal());
            if (pLevel.mayInteract(pPlayer, newPos)) {
                Fluid fluid = this.getFirstAndDecrease(itemStack, 1000);
                if (fluid != Fluids.EMPTY) {
                    BlockState here = pLevel.getBlockState(newPos);
                    if (here.getBlock() instanceof LiquidBlockContainer fillable && fluid == Fluids.WATER)
                        fillable.placeLiquid(pLevel, newPos, here, fluid.defaultFluidState());
                    else {
                        if (!pLevel.isClientSide && here.canBeReplaced(fluid))
                            pLevel.destroyBlock(newPos, true);
                        if (pLevel.setBlock(newPos, fluid.defaultFluidState().createLegacyBlock(), 11) && !here.getFluidState().isSource())
                            this.playEmptyingSound(pPlayer, pLevel, newPos, fluid);
                    }
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide);
    }


    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        ListTag items = pStack.getOrCreateTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        if (pEntity instanceof Player player && player.getInventory().getSelected() == pStack && items.size() > 0) {
            CompoundTag compound = items.getCompound(0);
            ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (id.toLanguageKey().contains("gtceu")) {
                player.displayClientMessage(Component.translatable("tooltip.infinity_bucket.message", getGTMFluidName(id), String.format("%,d", amount)), true);
            } else {
                player.displayClientMessage(Component.translatable("tooltip.infinity_bucket.message", Component.translatable(id.toLanguageKey("block")), String.format("%,d", amount)), true);
            }
        }
    }

    public Fluid getFirstAndDecrease(ItemStack stack, long minimumAmount) {
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag compound = items.getCompound(i);
            ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
            if (id.toLanguageKey().contains("gtceu")) return Fluids.EMPTY;
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (amount >= minimumAmount) {
                amount -= minimumAmount;
                if (amount == 0) items.remove(i);
                else compound.putLong(FLUID_AMOUNT_KEY, amount);
                return ForgeRegistries.FLUIDS.getValue(id);
            }
        }
        return Fluids.EMPTY;
    }

    public void insertFluid(ItemStack stack, Fluid fluid, long amount) {
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag compound = items.getCompound(i);
            ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
            if (fluidId.equals(id)) {
                long a = compound.getLong(FLUID_AMOUNT_KEY);
                if (Integer.MAX_VALUE - a >= amount) {
                    a += amount;
                    compound.putLong(FLUID_AMOUNT_KEY, a);
                } else {
                    compound.putLong(FLUID_AMOUNT_KEY, Integer.MAX_VALUE);
                }
                return;
            }
        }
        CompoundTag compound = new CompoundTag();
        compound.putString(FLUID_ID_KEY, fluidId.toString());
        compound.putLong(FLUID_AMOUNT_KEY, amount);
        items.add(compound);
        stack.getOrCreateTag().put(FLUIDS_NBT, items);
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


    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new InfinityBucketWrapper(stack);
    }

    public static void fillFluid(ItemStack stack, Fluid fluid, long amount) {
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, CompoundTag.TAG_COMPOUND);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag compound = items.getCompound(i);
            ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
            if (fluidId.equals(id)) {
                long a = compound.getLong(FLUID_AMOUNT_KEY);
                if (Integer.MAX_VALUE - a >= amount) {
                    a += amount;
                    compound.putLong(FLUID_AMOUNT_KEY, a);
                } else {
                    compound.putLong(FLUID_AMOUNT_KEY, Integer.MAX_VALUE);
                }
                return;
            }
        }
        CompoundTag compound = new CompoundTag();
        compound.putString(FLUID_ID_KEY, fluidId.toString());
        compound.putLong(FLUID_AMOUNT_KEY, amount);
        items.add(compound);
        stack.getOrCreateTag().put(FLUIDS_NBT, items);
    }

    public static FluidStack drainFluid(ItemStack stack, long minimumAmount) {
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, CompoundTag.TAG_COMPOUND);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag compound = items.getCompound(i);
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (amount >= minimumAmount) {
                amount -= minimumAmount;
                ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
                if (amount == 0L) {
                    items.remove(i);
                    return FluidStack.EMPTY;
                }

                compound.putLong(FLUID_AMOUNT_KEY, amount);
                return new FluidStack(ForgeRegistries.FLUIDS.getValue(id), (int)amount);
            }
        }

        return FluidStack.EMPTY;
    }

    public static FluidStack getFirstFluid(ItemStack stack) {
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, CompoundTag.TAG_COMPOUND);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag compound = items.getCompound(i);
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (amount > 0L) {
                ResourceLocation id = new ResourceLocation(compound.getString(FLUID_ID_KEY));
                return new FluidStack(ForgeRegistries.FLUIDS.getValue(id), (int)amount);
            }
        }

        return FluidStack.EMPTY;
    }

    public static void emptyFirstFluid(ItemStack stack) {
        ListTag items = stack.getOrCreateTag().getList(FLUIDS_NBT, CompoundTag.TAG_COMPOUND);

        for(int i = 0; i < items.size(); ++i) {
            CompoundTag compound = items.getCompound(i);
            long amount = compound.getLong(FLUID_AMOUNT_KEY);
            if (amount > 0L) {
                compound.putLong(FLUID_AMOUNT_KEY, 0L);
                break;
            }
        }
    }

    private MutableComponent getGTMFluidName(ResourceLocation id) {
        return (MutableComponent) new FluidStack(ForgeRegistries.FLUIDS.getValue(id),1000).getDisplayName();
    }
}
