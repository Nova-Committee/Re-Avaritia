package committee.nova.mods.avaritia.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * @author cnlimiter
 */
public class ItemReal {
    public ItemStack item;
    public long count;

    public ItemReal(ItemStack item, long count) {
        this.item = item;
        this.count = count;
    }

    public CompoundTag toNbt(CompoundTag tag){
        tag.put("item", item.save(new CompoundTag()));
        tag.putLong("count", count);
        return tag;
    }

    public void fromNbt(CompoundTag tag){
        this.item = ItemStack.of(tag.getCompound("item"));
        this.count = tag.getLong("count");
    }
}