package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.util.ItemUtils;
import committee.nova.mods.avaritia.api.util.lang.Localizable;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.menu.CompressorMenu;
import committee.nova.mods.avaritia.common.tile.config.SideConfiguration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.init.registry.enums.CompressorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 17:39
 * Version: 1.0
 */
public class NeutronCompressorTile extends BaseInventoryTileEntity {
    // 新的面配置系统，替代原来的boolean控制
    private SideConfiguration sideConfig = new SideConfiguration();

    // 主动IO操作计时器
    private int activeIOtick = 0;
    private static final int ACTIVE_IO_INTERVAL = 20; // 每秒执行一次主动IO

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

        // 处理主动IO操作
        if (!level.isClientSide()) {
            tile.activeIOtick++;
            if (tile.activeIOtick >= ACTIVE_IO_INTERVAL) {
                tile.activeIOtick = 0;
                tile.handleActiveIO();
            }
        }

        if (tile.recipeLocked && tile.lockedRecipe != null) {
            // 锁定状态下使用锁定的配方
            tile.recipe = tile.lockedRecipe.matches(tile.recipeInventory.toIInventory(), level) ? tile.lockedRecipe : null;
        } else {
            // 正常状态查找配方
            if (tile.recipe == null || !tile.recipe.matches(tile.recipeInventory.toIInventory(), level)) {
                tile.recipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.COMPRESSOR_RECIPE.get(), tile.recipeInventory.toIInventory(), level).orElse(null);
            }
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
        this.recipeLocked = tag.getBoolean("RecipeLocked");
        // 加载面配置
        if (tag.contains("SideConfig")) {
            this.sideConfig = SideConfiguration.fromNBT(tag.getCompound("SideConfig"));
        }
        this.lockedRecipe = null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.serializeNBT());
        tag.putInt("Progress", this.progress);
        tag.putBoolean("RecipeLocked", this.recipeLocked);

        // 保存面配置
        tag.put("SideConfig", sideConfig.toNBT());
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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // 检查被动输入输出配置
        if (side != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            // 获取方块的实际朝向
            Direction blockFacing = getBlockFacing();
            if (blockFacing != null) {
                // 将绝对方向转换为相对方向进行配置检查
                Direction relativeSide = getRelativeDirectionFromAbsolute(side, blockFacing);
                if (relativeSide != null && !sideConfig.isPassive(relativeSide)) {
                    return LazyOptional.empty();
                }
            } else {
                // 如果无法获取朝向，使用原始逻辑
                if (!sideConfig.isPassive(side)) {
                    return LazyOptional.empty();
                }
            }
        }
        return super.getCapability(cap, side);
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

    // 新增方法：配方锁定相关
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

    // 新增方法：材料弹出相关
    public void clearMaterials() {
        this.materialStack = ItemStack.EMPTY;
        this.materialCount = 0;
        this.progress = 0;
        this.setChangedAndDispatch();
    }

    public boolean canEjectMaterials() {
        return this.materialCount > 0 &&
               (this.recipe == null ||
                this.materialCount < this.recipe.getInputCount() * this.tier.inputAmplifier);
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

    // ==================== 新增的面配置相关方法 ====================

    /**
     * 设置方块配置
     */
    public void setSideConfiguration(SideConfiguration config) {
        this.sideConfig = config;
        this.setChangedAndDispatch();

        // 同步给客户端
        if (!this.level.isClientSide()) {
            NetworkHandler.sendSideConfigSync(this.level, this.worldPosition, config);
        }
    }

    /**
     * 获取方块配置
     */
    public SideConfiguration getSideConfiguration() {
        return this.sideConfig;
    }


    /**
     * 处理主动输入输出操作
     */
    private void handleActiveIO() {
        if (level == null) return;

        // 处理主动输入
        for (Direction side : sideConfig.getActiveInputSides()) {
            handleActiveInput(side);
        }

        // 处理主动输出
        for (Direction side : sideConfig.getActiveOutputSides()) {
            handleActiveOutput(side);
        }
    }

    /**
     * 处理从指定方向的主动输入
     */
    private void handleActiveInput(Direction side) {
        // 获取方块的实际朝向
        Direction blockFacing = getBlockFacing();
        if (blockFacing == null) return;

        // 根据方块朝向转换相对方向
        Direction actualDirection = getRelativeDirection(side, blockFacing);
        if (actualDirection == null) return;

        BlockPos targetPos = worldPosition.relative(actualDirection);
        BlockEntity targetTile = level.getBlockEntity(targetPos);

        if (targetTile != null) {
            // 尝试从目标方块抽取物品
             targetTile.getCapability(ForgeCapabilities.ITEM_HANDLER, actualDirection.getOpposite())
                    .ifPresent(targetHandler -> extractFromHandler(targetHandler, actualDirection));
        }
    }

    /**
     * 获取方块的朝向
     */
    private Direction getBlockFacing() {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(NeutronCompressorBlock.FACING)) {
                return state.getValue(NeutronCompressorBlock.FACING);
            }
        }
        return null;
    }

    /**
     * 根据方块朝向将相对方向转换为绝对方向
     * @param relativeSide 相对方向（FRONT, BACK, LEFT, RIGHT, UP, DOWN）
     * @param blockFacing 方块的朝向
     * @return 转换后的绝对方向
     */
    private Direction getRelativeDirection(Direction relativeSide, Direction blockFacing) {
        return switch (relativeSide) {
            // 前方 = 方块朝向
            case NORTH -> blockFacing;
            // 后方 = 方块朝向的对面
            case SOUTH -> blockFacing.getOpposite();
            // 右侧 = 方块朝向顺时针90度
            case EAST -> blockFacing.getCounterClockWise();
            // 左侧 = 方块朝向逆时针90度
            case WEST -> blockFacing.getClockWise();
            // 上下方向保持不变
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
    }


    /**
     * 根据方块朝向将绝对方向转换为相对方向
     * 这是 getRelativeDirection 的反向操作
     */
    private Direction getRelativeDirectionFromAbsolute(Direction absoluteDirection, Direction blockFacing) {
        // 如果绝对方向与方块朝向相同，则它是前方
        if (absoluteDirection == blockFacing) return Direction.NORTH;
        if (absoluteDirection == blockFacing.getOpposite()) return Direction.SOUTH;
        if (absoluteDirection == blockFacing.getCounterClockWise()) return Direction.EAST;
        if (absoluteDirection == blockFacing.getClockWise()) return Direction.WEST;
        if (absoluteDirection == Direction.UP) return Direction.UP;
        if (absoluteDirection == Direction.DOWN) return Direction.DOWN;
        return null;
    }

    /**
     * 处理到指定方向的主动输出
     */
    private void handleActiveOutput(Direction side) {
        // 获取方块的实际朝向
        Direction blockFacing = getBlockFacing();
        if (blockFacing == null) return;

        // 根据方块朝向转换相对方向
        Direction actualDirection = getRelativeDirection(side, blockFacing);
        if (actualDirection == null) return;

        BlockPos targetPos = worldPosition.relative(actualDirection);
        BlockEntity targetTile = level.getBlockEntity(targetPos);

        if (targetTile != null) {
            // 尝试将物品插入到目标方块
            targetTile.getCapability(ForgeCapabilities.ITEM_HANDLER, actualDirection.getOpposite())
                    .ifPresent(targetHandler -> {
                        insertToHandler(targetHandler, actualDirection);
                    });
        }
    }

    /**
     * 从外部物品处理器抽取物品
     */
    private void extractFromHandler(IItemHandler externalHandler, Direction fromSide) {
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
            int spaceInInput = materialStack.isEmpty() ? 64 : (int) (this.recipe.getInputCount() * this.tier.inputAmplifier - materialCount);

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
    private void insertToHandler(IItemHandler externalHandler, Direction toSide) {
        var outputSlot = this.inventory.getStackInSlot(0);
        if (outputSlot.isEmpty()) return;

        ItemStack remaining = outputSlot.copy();

        for (int i = 0; i < externalHandler.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack insertResult = externalHandler.insertItem(i, remaining, false);
            int transferred = remaining.getCount() - insertResult.getCount();

            if (transferred > 0) {
                // 更新输出槽
                outputSlot.shrink(transferred);
                remaining = insertResult;
            }
        }

        if (!remaining.equals(outputSlot)) {
            this.setChanged();
        }
    }
}
