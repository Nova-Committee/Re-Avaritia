package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.init.registry.enums.CompressorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:39
 * Version: 1.0
 */
public class NeutronCompressorTile extends BaseInventoryTileEntity {
    private final ItemStackWrapper inventory;
    private final ItemStackWrapper recipeInventory;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private ICompressorRecipe recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean ejecting = false;
    private CompressorTier tier;

    public NeutronCompressorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_compressor_tile.get(), pos, state);
        this.inventory = createInventoryHandler();
        this.recipeInventory = new ItemStackWrapper(1);
        if (state.is(ModBlocks.neutron_compressor.get())) {
            tier = CompressorTier.DEFAULT;
        } else if (state.is(ModBlocks.dense_neutron_compressor.get())) {
            tier = CompressorTier.DENSE;
        } else if (state.is(ModBlocks.denser_neutron_compressor.get())) {
            tier = CompressorTier.DENSER;
        } else if (state.is(ModBlocks.densest_neutron_compressor.get())) {
            tier = CompressorTier.DENSEST;
        }
    }

    public static ItemStackWrapper createInventoryHandler() {
        var inventory = new ItemStackWrapper(2);
        inventory.setOutputSlots(0);
        return inventory;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronCompressorTile tile) {

        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        tile.recipeInventory.setStackInSlot(0, tile.materialStack);

        if (tile.recipe == null || !tile.recipe.matches(tile.recipeInventory.toIInventory(), level)) {
            tile.recipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), tile.recipeInventory.toIInventory(), level).orElse(null);
        }

        if (!level.isClientSide()) {
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();

                    tile.setChangedFast();
                }

                if (tile.recipe != null && tile.materialCount < tile.recipe.getInputCount() * tile.tier.inputAmplifier) {
                    if (ItemUtils.areStacksSameType(input, tile.materialStack)) {
                        int consumeAmount = input.getCount();

                        consumeAmount = Math.min(consumeAmount, Mth.ceil(tile.recipe.getInputCount() * tile.tier.inputAmplifier) - tile.materialCount);


                        input.shrink(consumeAmount);
                        tile.materialCount += consumeAmount;

                        tile.setChangedFast();

                    }
                }
            }

            if (tile.recipe != null) {
                if (tile.materialCount >= tile.recipe.getInputCount() * tile.tier.inputAmplifier) {
                    tile.progress++;
                    tile.data.set(0, tile.progress);
                    if (tile.progress >= tile.recipe.getTimeCost() * tile.tier.timeAmplifier) {
                        var result = tile.recipe.assemble(tile.inventory.toIInventory(), level.registryAccess());

                        if (ItemUtils.canCombineStacks(result, output)) {
                            tile.updateResult(result, tile.tier.outputAmplifier);
                            tile.progress = 0;
                            tile.materialCount -= Mth.ceil(tile.recipe.getInputCount() * tile.tier.inputAmplifier);

                            if (tile.materialCount <= 0) {
                                tile.materialStack = ItemStack.EMPTY;
                            }

                            tile.setChangedFast();
                        }
                    }
                }
            }

            if (tile.ejecting) {
                if (tile.materialCount > 0 && !tile.materialStack.isEmpty() && (output.isEmpty() || ItemUtils.areStacksSameType(tile.materialStack, output))) {
                    int addCount = Math.min(tile.materialCount, tile.materialStack.getMaxStackSize() - output.getCount());
                    if (addCount > 0) {
                        var toAdd = ItemUtils.withSize(tile.materialStack, addCount, false);

                        tile.updateResult(toAdd, tile.tier.outputAmplifier);
                        tile.materialCount -= addCount;

                        if (tile.materialCount < 1) {
                            tile.materialStack = ItemStack.EMPTY;
                            tile.ejecting = false;
                        }

                        if (tile.progress > 0)
                            tile.progress = 0;

                        tile.setChangedFast();
                    }
                }
            }
        }


        tile.dispatchIfChanged();
    }

    @Override
    public @NotNull ItemStackWrapper getInventory() {
        return this.inventory;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.materialCount = tag.getInt("MaterialCount");
        this.materialStack = ItemStack.of(tag.getCompound("MaterialStack"));
        this.progress = tag.getInt("Progress");
        this.ejecting = tag.getBoolean("Ejecting");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.serializeNBT());
        tag.putInt("Progress", this.progress);
        tag.putBoolean("Ejecting", this.ejecting);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia." + tier.name).build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        return new CompressorMenu(windowId, playerInventory, this.inventory, this.getBlockPos(), this.data);
    }

    public CompressorTier getTier() {
        return tier;
    }

    public void setTier(CompressorTier tier) {
        this.tier = tier;
    }

    public ItemStack getMaterialStack() {
        return this.materialStack;
    }

    public boolean hasMaterialStack() {
        return !this.materialStack.isEmpty();
    }

    public int getMaterialCount() {
        return this.materialCount;
    }

    public boolean isEjecting() {
        return this.ejecting;
    }

    public void toggleEjecting() {
        if (this.materialCount > 0) {
            this.ejecting = !this.ejecting;
            this.setChangedAndDispatch();
        }
    }

    public boolean hasRecipe() {
        return this.recipe != null;
    }

    public ICompressorRecipe getActiveRecipe() {
        return this.recipe;
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return Mth.ceil(this.recipe.getInputCount() * this.tier.inputAmplifier);
        return 0;
    }

    public int getTimeRequired() {
        if (this.hasRecipe())
            return Mth.ceil(this.recipe.getTimeCost() * this.tier.timeAmplifier);
        return 0;
    }


    private void updateResult(ItemStack stack, int outputAmplifier) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack.copyWithCount(outputAmplifier));
        } else {
            this.inventory.setStackInSlot(0, ItemUtils.grow(result, stack.getCount() * outputAmplifier));
        }
    }
}
