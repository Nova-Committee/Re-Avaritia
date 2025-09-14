package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.RecipeGeneratorMenu;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: cnlimiter
 */
public class RecipeGeneratorTile extends BaseTileEntity {
    public final SimpleContainer containers = new SimpleContainer(82);


    public RecipeGeneratorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.recipe_generator_tile.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia.recipe_generator_table").build();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new RecipeGeneratorMenu(i, inventory, this.worldPosition);
    }

    public ItemStack getStackInSlot(int index) {
        return this.containers.getItem(index);
    }

    public void setItem(int index, ItemStack stack) {
        this.containers.setItem(index, stack);
        this.setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.containers.clearContent();
        ListTag listtag = tag.getList("Items", 10);
        for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j < this.containers.items.size()) {
                this.containers.items.set(j, ItemStack.of(compoundtag));
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.containers.items);
    }
}
