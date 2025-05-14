package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.common.wrappers.StorageItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/21 12:49
 * @Description:
 */
public class StorageUtils {
    public static final class Action {
        public static final int LEFT_CLICK_DUMMY_SLOT = 0;
        public static final int Right_CLICK_DUMMY_SLOT = 1;
        public static final int LEFT_SHIFT_DUMMY_SLOT = 2;
        public static final int Right_SHIFT_DUMMY_SLOT = 3;
        public static final int THROW_ONE = 4;
        public static final int THROW_STICK = 5;
        public static final int LEFT_DRAG = 6;
        public static final int RIGHT_DRAG = 7;
        public static final int CLONE = 8;
        public static final int DRAG_CLONE = 9;
    }

    public static class ViewType {
        public static final byte ALL = 0;
        public static final byte Items = 1;
        public static final byte Fluids = 2;
    }

    public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(",###");
    private static final HashMap<Item, String> ITEM_ID_MAP = new HashMap<>();
    private static final HashMap<String, Item> ID_ITEM_MAP = new HashMap<>();
    private static final HashMap<Fluid, String> FLUID_ID_MAP = new HashMap<>();
    private static final HashMap<String, Fluid> ID_FLUID_MAP = new HashMap<>();


    public static String getItemId(Item item) {
        if (ITEM_ID_MAP.containsKey(item)) return ITEM_ID_MAP.get(item);
        else {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            ITEM_ID_MAP.put(item, id);
            ID_ITEM_MAP.put(id, item);
            return id;
        }
    }

    public static Item getItem(String id) {
        if (ID_ITEM_MAP.containsKey(id)) return ID_ITEM_MAP.get(id);
        else {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(id));
            if (item == null || item.equals(Items.AIR)) return Items.AIR;
            ID_ITEM_MAP.put(id, item);
            ITEM_ID_MAP.put(item, id);
            return item;
        }
    }

    public static String getFluidId(Fluid fluid) {
        if (FLUID_ID_MAP.containsKey(fluid)) return FLUID_ID_MAP.get(fluid);
        else {
            String id = BuiltInRegistries.FLUID.getKey(fluid).toString();
            FLUID_ID_MAP.put(fluid, id);
            ID_FLUID_MAP.put(id, fluid);
            return id;
        }
    }

    public static Fluid getFluid(String id) {
        if (ID_FLUID_MAP.containsKey(id)) return ID_FLUID_MAP.get(id);
        else {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(id));
            if (fluid == null) return Fluids.EMPTY;
            ID_FLUID_MAP.put(id, fluid);
            FLUID_ID_MAP.put(fluid, id);
            return fluid;
        }
    }


    public static Int2ObjectMap<StorageItem> newContainers() {
        Int2ObjectOpenHashMap<StorageItem> containers = new Int2ObjectOpenHashMap<>();
        containers.defaultReturnValue(StorageItem.EMPTY);
        return containers;
    }

    public static void saveAllItems(CompoundTag nbt, Int2ObjectMap<StorageItem> containers) {
        ListTag list = new ListTag();

        for (Int2ObjectMap.Entry<StorageItem> storageItemEntry : containers.int2ObjectEntrySet()) {
            int index = storageItemEntry.getIntKey();
            StorageItem item = storageItemEntry.getValue();
            if (!item.isEmpty()) {
                CompoundTag compound = item.serializeNBT();
                compound.putInt("Index", index);
                list.add(compound);
            }
        }
        nbt.put("Items", list);
    }

    public static void loadAllItems(CompoundTag nbt, Int2ObjectMap<StorageItem> containers) {
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);

        for(int i = 0; i < list.size(); ++i) {
            CompoundTag compound = list.getCompound(i);
            int index = compound.getInt("Index");
            StorageItem item = StorageItem.read(compound);
            if (!item.isEmpty()) {
                containers.put(index, item);
            }
        }

    }
}
