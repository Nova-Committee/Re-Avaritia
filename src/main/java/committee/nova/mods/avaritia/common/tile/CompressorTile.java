package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.item.BaseItemStackHandler;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.util.ItemStackUtils;
import committee.nova.mods.avaritia.util.lang.Localizable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
public class CompressorTile extends BaseInventoryTileEntity implements MenuProvider {
    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private CompressorRecipe recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean ejecting = false;
    private final SimpleContainerData data = new SimpleContainerData(1);

    public CompressorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.compressor_tile.get(), pos, state);
        this.inventory = createInventoryHandler(null);
        this.recipeInventory = new BaseItemStackHandler(1);
    }

    public static BaseItemStackHandler createInventoryHandler(Runnable onContentsChanged) {
        var inventory = new BaseItemStackHandler(2, onContentsChanged);

        inventory.setOutputSlots(0);
        inventory.setSlotValidator((slot, stack) -> slot == 1);

        return inventory;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CompressorTile tile) {
        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        tile.recipeInventory.setStackInSlot(0, tile.materialStack);

        if (tile.recipe == null || !tile.recipe.matches(tile.recipeInventory)) {
            tile.recipe = (CompressorRecipe) level.getRecipeManager().getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), tile.recipeInventory.toIInventory(), level).orElse(null);
        }

        if (!level.isClientSide()) {
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();

                    tile.setChangedFast();
                }

                if (tile.recipe != null && tile.materialCount < tile.recipe.getInputCount()) {
                    if (ItemStackUtils.areStacksEqual(input, tile.materialStack)) {
                        int consumeAmount = input.getCount();

                        consumeAmount = Math.min(consumeAmount, tile.recipe.getInputCount() - tile.materialCount);


                        input.shrink(consumeAmount);
                        tile.materialCount += consumeAmount;

                        tile.setChangedFast();

                    }
                }
            }

            if (tile.recipe != null) {
                if (tile.materialCount >= tile.recipe.getInputCount()) {
                    tile.progress++;
                    tile.data.set(0, tile.progress);
                    if (tile.progress >= tile.recipe.getTimeRequire()) {
                        var result = tile.recipe.assemble(tile.inventory);

                        if (ItemStackUtils.canCombineStacks(result, output)) {
                            tile.updateResult(result);
                            tile.progress = 0;
                            tile.materialCount -= tile.recipe.getInputCount();

                            if (tile.materialCount <= 0) {
                                tile.materialStack = ItemStack.EMPTY;
                            }

                            tile.setChangedFast();
                        }
                    }
                }
            }

            if (tile.ejecting) {
                if (tile.materialCount > 0 && !tile.materialStack.isEmpty() && (output.isEmpty() || ItemStackUtils.areStacksEqual(tile.materialStack, output))) {
                    int addCount = Math.min(tile.materialCount, tile.materialStack.getMaxStackSize() - output.getCount());
                    if (addCount > 0) {
                        var toAdd = ItemStackUtils.withSize(tile.materialStack, addCount, false);

                        tile.updateResult(toAdd);
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
    public @NotNull BaseItemStackHandler getInventory() {
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
        return Localizable.of("container.compressor").build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return CompressorMenu.create(windowId, playerInventory, this.inventory, this.getBlockPos());
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

    public int getProgress() {
        return this.progress;
    }

    public boolean hasRecipe() {
        return this.recipe != null;
    }

    public CompressorRecipe getActiveRecipe() {
        return this.recipe;
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return this.recipe.getInputCount();

        return 0;
    }

    public int getTimeRequired() {
        if (this.hasRecipe())
            return this.recipe.getTimeRequire();
        return 0;
    }


    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, ItemStackUtils.grow(result, stack.getCount()));
        }
    }
}
