package committee.nova.mods.avaritia.common.tile.compressor;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.inventory.CachedRecipe;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.menu.NeutronCompressorMenu;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.core.io.TileIOHandler;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:39
 * Version: 1.0
 */
public class NeutronCompressorTile extends BaseInventoryTileEntity implements WorldlyContainer, ITileIO {
    // 新的面配置系统，替代原来的boolean控制
    private SideConfiguration sideConfig = new SideConfiguration();
    // 主动IO操作计时器
    private int activeIOtick = 0;
    private static final int ACTIVE_IO_INTERVAL = 20; // 每秒执行一次主动IO
    // IO处理器
    private final TileIOHandler ioHandler = new TileIOHandler(this, NeutronCompressorBlock.FACING);

    private final ItemStackWrapper inventory;
    private final ItemStackWrapper recipeInventory;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private final CachedRecipe<CraftingInput, ICompressorRecipe> recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean recipeLocked = false;
    private ICompressorRecipe lockedRecipe = null;
    private CompressorTier tier;

    public NeutronCompressorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_compressor_tile.get(), pos, state);
        this.inventory = createInventoryHandler();
        this.recipeInventory = ItemStackWrapper.create(1);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.COMPRESSOR_RECIPE.get());
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
        return ItemStackWrapper.create(2, builder -> {
            builder.setOutputSlots(0);
            builder.setCanInsert((slot, stack) -> slot == 1);
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronCompressorTile tile) {
        if (level == null || tile == null) return;

        var recipe = tile.getActiveRecipe();
        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        // 处理主动IO操作
        if (!level.isClientSide()) {
            tile.activeIOtick++;
            if (tile.activeIOtick >= ACTIVE_IO_INTERVAL) {
                tile.activeIOtick = 0;
                tile.handleActiveIO();
            }
        }

        if (!level.isClientSide()) {
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();

                    tile.setChangedFast();
                }

                if (recipe != null && tile.materialCount < recipe.getInputCount() * tile.tier.inputAmplifier) {
                    if (ItemUtils.areStacksSameType(input, tile.materialStack)) {
                        int consumeAmount = input.getCount();

                        consumeAmount = Math.min(consumeAmount, Mth.ceil(recipe.getInputCount() * tile.tier.inputAmplifier) - tile.materialCount);

                        input.shrink(consumeAmount);
                        tile.materialCount += consumeAmount;

                        tile.setChangedFast();
                    }
                }
            }

            if (recipe != null) {
                if (tile.materialCount >= recipe.getInputCount() * tile.tier.inputAmplifier) {
                    tile.progress++;
                    tile.data.set(0, tile.progress);
                    if (tile.progress >= recipe.getTimeCost() * tile.tier.timeAmplifier) {

                        CraftingInput craftingInput = tile.recipeInventory.toShapelessCraftingInput();
                        var baseResult = recipe.assemble(craftingInput, level.registryAccess());
                        var result = baseResult.copyWithCount(baseResult.getCount() * tile.tier.outputAmplifier);

                        if (ItemUtils.canCombineStacks(result, output)) {
                            tile.updateResult(result);
                            tile.progress = 0;
                            tile.materialCount -= Mth.ceil(recipe.getInputCount() * tile.tier.inputAmplifier);

                            if (tile.materialCount <= 0) {
                                tile.materialStack = ItemStack.EMPTY;
                            }

                            tile.setChangedFast();
                        }
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
        this.setProgress(tag.getInt("Progress"));
        this.recipeLocked = tag.getBoolean("RecipeLocked");
        if (tag.contains("SideConfig")) {
            this.sideConfig = SideConfiguration.fromNBT(tag.getCompound("SideConfig"));
        }
        this.lockedRecipe = null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.saveOptional(registries));
        tag.putInt("Progress", this.progress);
        tag.putBoolean("RecipeLocked", this.recipeLocked);
        tag.put("SideConfig", sideConfig.toNBT());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia." + tier.name).build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        return new NeutronCompressorMenu(windowId, playerInventory, this.inventory, this.getBlockPos(), this.data);
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

    public boolean hasRecipe() {
        return this.recipe.exists();
    }

    public ICompressorRecipe getActiveRecipe() {
        if (this.level == null || this.materialStack.isEmpty())
            return null;

        this.recipeInventory.setStackInSlot(0, this.materialStack);

        if (this.recipeLocked && this.lockedRecipe != null) {
            // 锁定状态下使用锁定的配方
            return this.lockedRecipe.matches(this.recipeInventory.toShapelessCraftingInput(), level) ? this.lockedRecipe : null;
        } else return this.recipe.checkAndGet(this.toCraftingInput(), this.level);
    }

    private CraftingInput toCraftingInput() {
        return this.recipeInventory.toShapelessCraftingInput();
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return Mth.ceil(this.getActiveRecipe().getInputCount() * this.tier.inputAmplifier);
        return 0;
    }

    public int getTimeRequired() {
        if (this.hasRecipe())
            return Mth.ceil(this.getActiveRecipe().getTimeCost() * this.tier.timeAmplifier);
        return 0;
    }

    private void setProgress(int progress)
    {
        this.progress = progress;
        this.data.set(0, this.progress);
    }

    public boolean isRecipeLocked() {
        return this.recipeLocked;
    }

    public void setRecipeLock(boolean locked, ICompressorRecipe recipe) {
        this.recipeLocked = locked;
        this.lockedRecipe = locked && recipe != null ? recipe : null;
        this.setChangedAndDispatch();
    }

    public ICompressorRecipe getLockedRecipe() {
        return this.lockedRecipe;
    }

    public void clearMaterials() {
        this.materialStack = ItemStack.EMPTY;
        this.materialCount = 0;
        this.setProgress(0);
        this.setChangedAndDispatch();
    }

    public boolean canEjectMaterials() {
        return this.materialCount > 0 &&
                (this.recipe == null ||
                        this.materialCount < this.getActiveRecipe().getInputCount() * this.tier.inputAmplifier);
    }

    // 新增方法：输入槽锁定验证
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == 1) { // 输入槽
            if (this.recipeLocked && this.lockedRecipe != null) {
                // 锁定状态下，只接受锁定配方的材料
                var ingredients = this.lockedRecipe.getIngredients();
                if (!ingredients.isEmpty()) {
                    var ingredient = ingredients.get(0);
                    var items = ingredient.getItems();
                    return items.length > 0 && stack.is(items[0].getItem());
                }
                return false;
            }
        }
        return true; // 默认允许放置
    }

    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, ItemUtils.grow(result, stack.getCount()));
        }
    }

    private void handleActiveIO() {
        ioHandler.handleActiveIO();
    }

    // IO接口实现方法
    /**
     * 获取方块配置
     */
    @Override
    public SideConfiguration getSideConfiguration() {
        return this.sideConfig;
    }

    @Override
    public void setSideConfiguration(SideConfiguration config) {
        this.sideConfig = config;
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.worldPosition, config);
        }
    }


    @Override
    public void cycleSideModeForNeutronCollector(Direction direction) {
        SideConfiguration.SideMode current = sideConfig.getSideMode(direction);
        SideConfiguration.SideMode nextMode;

        if (current == SideConfiguration.SideMode.OFF) {
            nextMode = SideConfiguration.SideMode.PASSIVE_MIXIN;
        } else if (current == SideConfiguration.SideMode.PASSIVE_MIXIN) {
            nextMode = SideConfiguration.SideMode.ACTIVE_INPUT;
        } else if (current == SideConfiguration.SideMode.ACTIVE_INPUT) {
            nextMode = SideConfiguration.SideMode.ACTIVE_OUTPUT;
        } else if (current == SideConfiguration.SideMode.ACTIVE_OUTPUT) {
            nextMode = SideConfiguration.SideMode.ACTIVE_MIXIN;
        } else {
            // 默认从PASSIVE_OUTPUT开始
            nextMode = SideConfiguration.SideMode.OFF;
        }

        sideConfig.setSideMode(direction, nextMode);
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.worldPosition, sideConfig);
        }
    }

    @Override
    public void setIOChange() {
        this.setChangedAndDispatch();
    }

    @Override
    public void extractFromHandler(IItemHandler externalHandler, Direction fromSide) {
        var inputSlot = this.inventory.getStackInSlot(1);

        // 检查当前输入槽是否已满或材料类型不匹配
        if (!inputSlot.isEmpty() && !ItemUtils.areStacksSameType(inputSlot, materialStack)) {
            return;
        }

        for (int i = 0; i < externalHandler.getSlots(); i++) {
            ItemStack stack = externalHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // 检查是否与当前材料类型匹配
            if (!materialStack.isEmpty() && !ItemUtils.areStacksSameType(stack, materialStack)) {
                continue;
            }

            // 计算可转移的数量
            int maxTransfer = Math.min(stack.getCount(), 64); // 每次最多转移64个
            int spaceInInput = materialStack.isEmpty() ? 64 : (int) (this.getActiveRecipe().getInputCount() * this.tier.inputAmplifier - materialCount);

            int inputCount = inputSlot.isEmpty() ? 64 : inputSlot.getMaxStackSize() - inputSlot.getCount();

            if (spaceInInput + inputCount <= 0) break;

            int transferAmount = Math.min(maxTransfer, spaceInInput + inputCount);
            ItemStack extractedStack = externalHandler.extractItem(i, transferAmount, false);

            if (!extractedStack.isEmpty()) {
                if (materialStack.isEmpty()) {
                    materialStack = extractedStack.copy();
                }

                // 将物品放入输入槽
                if (inputSlot.isEmpty()) {
                    this.inventory.setStackInSlot(1, extractedStack.copy());
                } else {
                    inputSlot.grow(extractedStack.getCount());
                }
                this.setChanged();
                break;
            }
        }
    }

    /**
     * 向外部物品处理器插入物品
     */
    @Override
    public void insertToHandler(IItemHandler externalHandler, Direction toSide) {
        // 检查输出槽是否有物品
        var outputSlot = this.inventory.getStackInSlot(0);
        if (outputSlot.isEmpty()) return;

        ItemStack remaining = outputSlot.copy();

        for (int i = 0; i < externalHandler.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack insertResult = externalHandler.insertItem(i, remaining, false);
            int transferred = remaining.getCount() - insertResult.getCount();

            if (transferred > 0) {
                outputSlot.shrink(transferred);
                remaining = insertResult;
            }
        }

        if (!remaining.equals(outputSlot)) {
            this.setChanged();
        }
    }



    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        if (direction == Direction.UP) {
            return new int[] { 1 }; //input
        } else {
            return new int[] { 0 }; //output
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (stack.isEmpty()) {
            return false;
        }
        if (index == 1) { //input
            if (this.getInventory().getStackInSlot(1).isEmpty()) {
                return true;
            }

            if (!hasRecipe()) {
                return false;
            }

            if (!this.materialStack.isEmpty()) {
                return ItemStack.isSameItemSameComponents(this.getInventory().getStackInSlot(1), this.materialStack);
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == 0 && direction != Direction.UP;
    }

    @Override
    public int getContainerSize() {
        return this.getInventory().getSlots();
    }

    @Override
    public boolean isEmpty() {
        return this.getInventory().getStacks().isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().getItem(0);
        } else {
            return this.getInventory().toRecipeInventory().getItem(1);
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().removeItem(0, amount);
        } else if (slot == 1) {
            return this.getInventory().toRecipeInventory().removeItem(1, amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            return this.getInventory().toRecipeInventory().removeItemNoUpdate(0);
        } else if (slot == 1) {
            return this.getInventory().toRecipeInventory().removeItemNoUpdate(1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            this.getInventory().toRecipeInventory().setItem(0, stack);
        } else if (slot == 1) {
            this.getInventory().toRecipeInventory().setItem(1, stack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        BlockPos blockPos = this.getBlockPos();
        return player.distanceToSqr(blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {

    }
}
