package com.avaritia.common.tile;

import com.avaritia.api.common.crafting.ICompressorRecipe;
import com.avaritia.api.common.crafting.ShapelessCraftingInput;
import com.avaritia.api.common.inventory.OnContentsChangedFunction;
import com.avaritia.api.common.tile.BaseInventoryTileEntity;
import com.avaritia.api.common.wrapper.ItemStackWrapper;
import com.avaritia.api.iface.ITileIO;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.api.utils.lang.Localizable;
import com.avaritia.common.block.compressor.NeutronCompressorBlock;
import com.avaritia.common.menu.NeutronCompressorMenu;
import com.avaritia.core.io.SideConfiguration;
import com.avaritia.core.io.TileIOHandler;
import com.avaritia.init.handler.NetworkHandler;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModRecipeTypes;
import com.avaritia.init.registry.ModTileEntities;
import com.avaritia.init.registry.enums.CompressorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:39
 * Version: 1.0
 */
public class NeutronCompressorTile extends BaseInventoryTileEntity implements WorldlyContainer, ITileIO {
    // 面配置系统
    private SideConfiguration sideConfig = new SideConfiguration();
    // 主动IO操作计时器
    private int activeIOtick = 0;
    private static final int ACTIVE_IO_INTERVAL = 20; // 每秒执行一次主动IO
    // IO处理器
    private final TileIOHandler ioHandler = new TileIOHandler(this, NeutronCompressorBlock.FACING);

    private final ItemStackWrapper inventory;
    private final ItemStackWrapper recipeInventory;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private ICompressorRecipe recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private int materialCount;
    private int progress;
    private boolean recipeLocked = false;
    private ICompressorRecipe lockedRecipe = null;
    private CompressorTier tier;

    public NeutronCompressorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.neutron_compressor_tile.get(), pos, state);
        this.inventory = createInventoryHandler((slot) -> this.setChanged());
        this.recipeInventory = ItemStackWrapper.create(1);
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
        return createInventoryHandler(null);
    }

    public static ItemStackWrapper createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return ItemStackWrapper.create(2, onContentsChanged, builder -> {
            builder.setOutputSlots(0);
            builder.setCanExtract((slot) -> slot == 1 || slot == 0);
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronCompressorTile tile) {
        if (level == null || tile == null) return;

        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        if (!level.isClientSide()) {
            // 处理主动IO操作
            tile.activeIOtick++;
            if (tile.activeIOtick >= ACTIVE_IO_INTERVAL) {
                tile.activeIOtick = 0;
                tile.ioHandler.handleActiveIO();
            }

            // 处理材料输入
            if (!input.isEmpty()) {
                if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                    tile.materialStack = input.copy();
                    tile.setChangedFast();
                }
            }
        }

        // 设置配方库存并获取配方
        tile.recipeInventory.setStackInSlot(0, tile.materialStack);
        // 获取配方(双端都获取)
        if (tile.recipeLocked && tile.lockedRecipe != null) {
            // 锁定状态下使用锁定的配方
            tile.recipe = tile.lockedRecipe.matches(tile.toCraftingInput(), level) ? tile.lockedRecipe : null;
        } else {
            // 正常状态查找配方
            if (tile.recipe == null || !tile.recipe.matches(tile.toCraftingInput(), level)) {
                tile.recipe = findRecipe(level, tile.toCraftingInput());
            }
        }
        // 处理材料消耗和进度(仅服务端)
        if (!level.isClientSide() && tile.recipe != null) {
            int requiredAmount = Mth.ceil(tile.recipe.getInputCount() * tile.tier.inputAmplifier);

            // 如果有输入且材料不足，尝试消耗材料
            if (!input.isEmpty() && tile.materialCount < requiredAmount) {
                if (tile.doesItemMatchMaterialStack(input, tile.materialStack)) {
                    int consumeAmount = Math.min(
                        input.getCount(),
                        requiredAmount - tile.materialCount
                    );

                    input.shrink(consumeAmount);
                    tile.materialCount += consumeAmount;
                    tile.setChangedFast();
                }
            }

            // 处理合成进度
            if (tile.materialCount >= requiredAmount) {
                // 材料足够，增加进度
                tile.setProgress(tile.progress + 1);
                tile.setChangedFast();

                // 检查是否完成
                if (tile.progress >= tile.recipe.getTimeCost() * tile.tier.timeAmplifier) {
                    CraftingInput craftingInput = tile.recipeInventory.toShapelessCraftingInput();
                    var baseResult = tile.recipe.assemble(craftingInput);
                    var result = baseResult.copyWithCount(baseResult.getCount() * tile.tier.outputAmplifier);

                    if (ItemUtils.canCombineStacks(result, output)) {
                        tile.updateResult(result);
                        tile.setProgress(0);
                        tile.materialCount -= Mth.ceil(tile.recipe.getInputCount() * tile.tier.inputAmplifier);

                        if (tile.materialCount <= 0) {
                            tile.materialStack = ItemStack.EMPTY;
                        }
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
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.materialCount = input.getIntOr("MaterialCount", 0);
        this.materialStack = input.read("MaterialStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.setProgress(input.getIntOr("Progress", 0));
        this.recipeLocked = input.getBooleanOr("RecipeLocked", false);
        input.read("SideConfig", CompoundTag.CODEC).ifPresent(tag -> this.sideConfig = SideConfiguration.fromNBT(tag));
        this.lockedRecipe = null;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("MaterialCount", this.materialCount);
        output.store("MaterialStack", ItemStack.OPTIONAL_CODEC, this.materialStack);
        output.putInt("Progress", this.progress);
        output.putBoolean("RecipeLocked", this.recipeLocked);
        output.store("SideConfig", CompoundTag.CODEC, sideConfig.toNBT());
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
        return this.recipe != null;
    }

    public ICompressorRecipe getActiveRecipe() {
        return this.recipe;
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
        return this.materialCount > 0 && this.hasRecipe() &&
                (this.materialCount < this.getActiveRecipe().getInputCount() * this.tier.inputAmplifier);
    }

    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, ItemUtils.grow(result, stack.getCount()));
        }
    }


    // 从别的容器输入到本容器的验证
    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        if (slot == 1) { // 输入槽
            return canInsertItem(stack);
        }
        return false; // 默认不允许放置
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
    public void extractFromHandler(ResourceHandler<ItemResource> externalHandler, Direction fromSide) {
        var inputSlot = this.inventory.getStackInSlot(1);

        for (int i = 0; i < externalHandler.size(); i++) {
            ItemResource resource = externalHandler.getResource(i);
            if (resource.isEmpty()) continue;

            ItemStack stack = resource.toStack(externalHandler.getAmountAsInt(i));
            if (stack.isEmpty()) continue;

            // 先检查当前输入槽是否与待插入物品一致
            if (!inputSlot.isEmpty() && !ItemUtils.areStacksSameType(stack, inputSlot)) {
                continue;
            }

            // 检查输入物品是否在配方中
            if (!canInsertItem(stack)) continue;

            // 检查是否与当前材料类型匹配
            if (!materialStack.isEmpty() && !doesItemMatchMaterialStack(stack, materialStack)) {
                continue;
            }

            ICompressorRecipe activeRecipe = this.getActiveRecipe();
            if (!materialStack.isEmpty() && activeRecipe == null) {
                continue;
            }

            // 计算可转移的数量
            int maxTransfer = Math.min(stack.getCount(), 64); // 每次最多转移64个
            int spaceInInput = materialStack.isEmpty() ? 64 : (int) (activeRecipe.getInputCount() * this.tier.inputAmplifier - materialCount);

            int inputCount = inputSlot.isEmpty() ? 64 : inputSlot.getMaxStackSize() - inputSlot.getCount();

            if (spaceInInput + inputCount <= 0) break;

            int transferAmount = Math.min(maxTransfer, spaceInInput + inputCount);
            ItemStack extractedStack;
            try (var tx = Transaction.openRoot()) {
                int extracted = externalHandler.extract(i, resource, transferAmount, tx);
                if (extracted <= 0) {
                    continue;
                }
                tx.commit();
                extractedStack = resource.toStack(extracted);
            }

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
    public void insertToHandler(ResourceHandler<ItemResource> externalHandler, Direction toSide) {
        // 检查输出槽是否有物品
        var outputSlot = this.inventory.getStackInSlot(0);
        if (outputSlot.isEmpty()) return;

        try (var tx = Transaction.openRoot()) {
            int transferred = externalHandler.insert(ItemResource.of(outputSlot), outputSlot.getCount(), tx);
            if (transferred <= 0) {
                return;
            }
            outputSlot.shrink(transferred);
            tx.commit();
            this.setChanged();
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction direction) {
        if (sideConfig.getSideMode(direction).canInput()) {
            return new int[]{1};
        } else if (sideConfig.getSideMode(direction).canOutput()) {
            return new int[]{0};
        } else return new int[1];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (stack.isEmpty()) {
            return false;
        }
        if (index == 1 && ioHandler.shouldAllowPassiveIO(direction)) { //input
            var inputSlot = this.getInventory().getStackInSlot(1);
            if (inputSlot.isEmpty()) {
                return true;
            }

            if (!hasRecipe()) {
                return false;
            }

            // 配方匹配检查
            if (!this.materialStack.isEmpty() && !doesItemMatchMaterialStack(stack, materialStack)) {
                return false;
            }
            if (!canInsertItem(stack)) {
                return false;
            }

            return ItemUtils.areStacksSameType(stack, inputSlot);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == 0 && ioHandler.shouldAllowPassiveIO(direction);
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

    public boolean canInsertItem(ItemStack stack) {
        if (this.recipeLocked && this.lockedRecipe != null) {
            return doesItemMatchRecipeIngredient(stack, this.lockedRecipe);
        } else {
            var compressorRecipe = findRecipe(level, new ShapelessCraftingInput(List.of(stack)));
            return doesItemMatchRecipeIngredient(stack, compressorRecipe);
        }
    }

    private static ICompressorRecipe findRecipe(Level level, CraftingInput input) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.recipeAccess()
                .getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), input, level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private boolean doesItemMatchRecipeIngredient(ItemStack stack, ICompressorRecipe recipe) {
        if (recipe == null) return false;
        Ingredient ingredient = recipe.getInput();
        return ingredient.test(stack);
    }

    private boolean doesItemMatchMaterialStack(ItemStack stack, ItemStack materialStack) {
        if (materialStack.isEmpty()) return true;

        if (this.recipe != null) {
            return doesItemMatchRecipeIngredient(stack, this.recipe);
        }

        return ItemStack.isSameItemSameComponents(stack, materialStack);
    }
}
