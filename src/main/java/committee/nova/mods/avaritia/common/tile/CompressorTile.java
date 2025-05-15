package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.inventory.CachedRecipe;
import committee.nova.mods.avaritia.api.common.inventory.OnContentsChangedFunction;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
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
public class CompressorTile extends BaseInventoryTileEntity {
    private final ItemStackWrapper inventory;
    private final ItemStackWrapper recipeInventory;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private final CachedRecipe<CraftingInput, ICompressorRecipe> recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean ejecting = false;

    public CompressorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.compressor_tile.get(), pos, state);
        this.inventory = createInventoryHandler((slot) -> this.setChanged());
        this.recipeInventory = ItemStackWrapper.create(1);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.COMPRESSOR_RECIPE.get());
    }

    public static ItemStackWrapper createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return ItemStackWrapper.create(2, onContentsChanged, builder -> {
            builder.setOutputSlots(0);
            builder.setCanInsert((slot, stack) -> slot == 1);
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CompressorTile tile) {
        var recipe = tile.getActiveRecipe();
        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        if (!level.isClientSide()) {
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();

                    tile.setChangedFast();
                }

                if (recipe != null && tile.materialCount < recipe.getInputCount()) {
                    if (ItemUtils.areStacksSameType(input, tile.materialStack)) {
                        int consumeAmount = input.getCount();

                        consumeAmount = Math.min(consumeAmount, recipe.getInputCount() - tile.materialCount);


                        input.shrink(consumeAmount);
                        tile.materialCount += consumeAmount;

                        tile.setChangedFast();

                    }
                }
            }

            if (recipe != null) {
                if (tile.materialCount >= recipe.getInputCount()) {
                    tile.progress++;
                    tile.data.set(0, tile.progress);
                    if (tile.progress >= recipe.getTimeCost()) {
                        var result = recipe.assemble(tile.toCraftingInput(), level.registryAccess());

                        if (ItemUtils.canCombineStacks(result, output)) {
                            tile.updateResult(result);
                            tile.progress = 0;
                            tile.materialCount -= recipe.getInputCount();

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
    public @NotNull ItemStackWrapper getInventory() {
        return this.inventory;
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.materialCount = tag.getInt("MaterialCount");
        this.materialStack = ItemStack.parseOptional(registries, tag.getCompound("MaterialStack"));
        this.progress = tag.getInt("Progress");
        this.ejecting = tag.getBoolean("Ejecting");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.saveOptional(registries));
        tag.putInt("Progress", this.progress);
        tag.putBoolean("Ejecting", this.ejecting);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container.compressor").build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        return new CompressorMenu(windowId, playerInventory, this.inventory, this.getBlockPos(), this.data);
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
        if (this.level == null)
            return null;

        this.recipeInventory.setStackInSlot(0, this.materialStack);

        return this.recipe.checkAndGet(this.toCraftingInput(), this.level);
    }

    private CraftingInput toCraftingInput() {
        return this.recipeInventory.toShapelessCraftingInput();
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return this.recipe.get().getInputCount();
        return 0;
    }

    public int getTimeRequired() {
        if (this.hasRecipe())
            return this.recipe.get().getTimeCost();
        return 0;
    }


    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, ItemUtils.grow(result, stack.getCount()));
        }
    }
}
