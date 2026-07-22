package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.common.component.ClusterContainerContents;
import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import committee.nova.mods.avaritia.core.chest.NullChestHandler;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 只保存通道引用的无限箱方块实体；物品全部位于全局 SavedData。 */
public class InfinityChestTile extends BlockEntity implements LidBlockEntity, ResourceHandler<ItemResource> {
    private static final String REFERENCE_KEY = "InfinityChestReference";
    private static final String CLUSTER_IMPORTED_KEY = "ClusterContainerImported";
    private static final String PENDING_CLUSTER_KEY = "PendingClusterContainer";

    @Nullable
    private UUID owner;
    private boolean locked;
    private String filter = "";
    private byte sortType = InfinityChestReference.DEFAULT_SORT_TYPE;
    private UUID channelId = UUID.randomUUID();
    private boolean clusterContainerImported;
    private List<ItemStack> pendingClusterItems = List.of();
    private ChestHandler cachedChest = NullChestHandler.INSTANCE;

    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_CLOSE,
                    SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof InfinityChestMenu menu && menu.getTile() == InfinityChestTile.this;
        }
    };

    public InfinityChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.INFINITY_CHEST_TILE.get(), pos, state);
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getFilter() {
        return filter;
    }

    public byte getSortType() {
        return sortType;
    }

    public UUID getChannelID() {
        return channelId;
    }

    public UUID getChestID() {
        return channelId;
    }

    public ChestHandler getChest() {
        if (level != null && !level.isClientSide() && owner != null) {
            cachedChest = ServerChestManager.get(level.getServer()).getOrCreateChest(owner, channelId);
        }
        return cachedChest;
    }

    public void ensureIdentity(UUID defaultOwner) {
        if (owner == null) {
            owner = defaultOwner;
            locked = false;
            channelId = UUID.randomUUID();
            cachedChest = NullChestHandler.INSTANCE;
            setChanged();
        }
        getChest();
    }

    public boolean canPlayerModify(Player player) {
        return InfinityChestReference.canModify(owner, locked, player.getUUID());
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setChanged();
    }

    public void setFilter(String filter) {
        this.filter = InfinityChestReference.sanitizeFilter(filter).toLowerCase(Locale.ROOT);
        setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType >= 0 && sortType <= 7
                ? sortType
                : InfinityChestReference.DEFAULT_SORT_TYPE;
        setChanged();
    }

    public void applyReference(InfinityChestReference reference) {
        owner = reference.owner();
        locked = reference.locked();
        filter = InfinityChestReference.sanitizeFilter(reference.filter());
        sortType = reference.sortType();
        channelId = reference.channelId();
        clusterContainerImported = true;
        pendingClusterItems = List.of();
        cachedChest = NullChestHandler.INSTANCE;
        getChest();
        setChanged();
    }

    public @Nullable InfinityChestReference createReference() {
        return owner == null ? null : new InfinityChestReference(owner, locked, filter, sortType, channelId);
    }

    public void writeMenuData(FriendlyByteBuf buffer) {
        if (owner == null) {
            throw new IllegalStateException("Infinity chest identity must exist before opening its menu");
        }
        buffer.writeBlockPos(worldPosition);
        buffer.writeUUID(owner);
        buffer.writeBoolean(locked);
        buffer.writeUtf(filter, InfinityChestReference.MAX_FILTER_LENGTH);
        buffer.writeByte(sortType);
        buffer.writeUUID(channelId);
    }

    /** 放置时恢复引用，或将当前分支的 300 槽组件一次性导入新通道。 */
    public void initializePlaced(@Nullable LivingEntity placer, ItemStack stack) {
        if (level == null || level.isClientSide()) {
            return;
        }
        List<ItemStack> source = stack.getOrDefault(ModDataComponents.CLUSTER_CONTAINER,
                ClusterContainerContents.EMPTY).nonEmptyStream().toList();
        InfinityChestReference reference = readReference(stack);
        if (reference != null) {
            applyReference(reference);
        }
        if (!source.isEmpty()) {
            pendingClusterItems = source;
            clusterContainerImported = false;
            setChanged();
        }
        if (owner == null && placer != null) {
            ensureIdentity(placer.getUUID());
        }
        if (owner == null || clusterContainerImported) {
            return;
        }

        source = pendingClusterItems;

        ChestHandler chest = getChest();
        try (Transaction transaction = Transaction.openRoot()) {
            for (ItemStack sourceStack : source) {
                int inserted = chest.insert(ItemResource.of(sourceStack), sourceStack.getCount(), transaction);
                if (inserted != sourceStack.getCount()) {
                    setChanged();
                    return;
                }
            }
            transaction.commit();
        }
        clusterContainerImported = true;
        pendingClusterItems = List.of();
        setChanged();
    }

    public static @Nullable InfinityChestReference readReference(ItemStack stack) {
        InfinityChestReference reference = stack.get(ModDataComponents.INFINITY_CHEST_REFERENCE);
        if (reference != null) {
            return reference;
        }
        TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        reference = blockEntityData == null ? null : readReference(blockEntityData.copyTagWithoutId());
        if (reference != null) {
            return reference;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : readReference(customData.copyTag());
    }

    public static @Nullable InfinityChestReference readReference(CompoundTag input) {
        return InfinityChestReference.readLegacyTag(input);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        InfinityChestReference reference = input.read(REFERENCE_KEY, InfinityChestReference.CODEC)
                .orElseGet(() -> readLegacyReference(input));
        if (reference != null) {
            owner = reference.owner();
            locked = reference.locked();
            filter = reference.filter();
            sortType = reference.sortType();
            channelId = reference.channelId();
            cachedChest = NullChestHandler.INSTANCE;
        }
        clusterContainerImported = input.getBooleanOr(CLUSTER_IMPORTED_KEY, reference != null);
        pendingClusterItems = input.read(PENDING_CLUSTER_KEY, ClusterContainerContents.CODEC)
                .orElse(ClusterContainerContents.EMPTY)
                .nonEmptyStream()
                .toList();
        if (!pendingClusterItems.isEmpty()) {
            clusterContainerImported = false;
        } else if (!clusterContainerImported) {
            NonNullList<ItemStack> legacyItems = NonNullList.withSize(300, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(input, legacyItems);
            pendingClusterItems = legacyItems.stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
        }
    }

    private static @Nullable InfinityChestReference readLegacyReference(ValueInput input) {
        UUID owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        UUID channel = input.read("channelID", UUIDUtil.CODEC)
                .or(() -> input.read("chestID", UUIDUtil.CODEC))
                .or(() -> input.read("channelId", UUIDUtil.CODEC))
                .orElse(null);
        if (owner == null || channel == null) {
            return null;
        }
        return new InfinityChestReference(
                owner,
                input.getBooleanOr("locked", false),
                input.getStringOr("filter", ""),
                (byte) input.getIntOr("sortType", InfinityChestReference.DEFAULT_SORT_TYPE),
                channel
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        InfinityChestReference reference = createReference();
        if (reference != null) {
            output.store(REFERENCE_KEY, InfinityChestReference.CODEC, reference);
        }
        output.putBoolean(CLUSTER_IMPORTED_KEY, clusterContainerImported);
        if (!clusterContainerImported && !pendingClusterItems.isEmpty()) {
            output.store(PENDING_CLUSTER_KEY, ClusterContainerContents.CODEC,
                    ClusterContainerContents.fromItems(pendingClusterItems));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        super.applyImplicitComponents(componentInput);
        InfinityChestReference reference = componentInput.get(ModDataComponents.INFINITY_CHEST_REFERENCE);
        if (reference != null) {
            applyReference(reference);
        }
        ClusterContainerContents contents = componentInput.getOrDefault(
                ModDataComponents.CLUSTER_CONTAINER, ClusterContainerContents.EMPTY);
        pendingClusterItems = contents.nonEmptyStream().toList();
        if (!pendingClusterItems.isEmpty()) {
            clusterContainerImported = false;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        InfinityChestReference reference = createReference();
        if (reference != null) {
            builder.set(ModDataComponents.INFINITY_CHEST_REFERENCE, reference);
        }
        if (!clusterContainerImported && !pendingClusterItems.isEmpty()) {
            builder.set(ModDataComponents.CLUSTER_CONTAINER,
                    ClusterContainerContents.fromItems(pendingClusterItems));
        }
    }

    public void startOpen(ContainerUser user) {
        if (!remove && level != null && !user.getLivingEntity().isSpectator()) {
            openersCounter.incrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState(),
                    user.getContainerInteractionRange());
        }
    }

    public void stopOpen(ContainerUser user) {
        if (!remove && level != null && !user.getLivingEntity().isSpectator()) {
            openersCounter.decrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openersCounter.recheckOpeners(level, worldPosition, getBlockState());
        }
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, InfinityChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            chestLidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return chestLidController.getOpenness(partialTicks);
    }

    @Override
    public int size() {
        return getChest().size();
    }

    @Override
    public ItemResource getResource(int index) {
        return getChest().getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return getChest().getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return getChest().getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return getChest().isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getChest().insert(index, resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return getChest().insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getChest().extract(index, resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return getChest().extract(resource, amount, transaction);
    }
}
