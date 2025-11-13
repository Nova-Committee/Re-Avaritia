package committee.nova.mods.avaritia.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * @Project: Avaritia
 * @author cnlimiter
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

    public static String getItemId(ItemStack item) {
        return getItemId(item.getItem());
    }

    public static String getItemId(Item item) {
        if (ITEM_ID_MAP.containsKey(item)) return ITEM_ID_MAP.get(item);
        else {
            String id = ForgeRegistries.ITEMS.getKey(item).toString();
            ITEM_ID_MAP.put(item, id);
            ID_ITEM_MAP.put(id, item);
            return id;
        }
    }

    public static Item getItem(String id) {
        if (ID_ITEM_MAP.containsKey(id)) return ID_ITEM_MAP.get(id);
        else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item == null || item.equals(Items.AIR)) return Items.AIR;
            ID_ITEM_MAP.put(id, item);
            ITEM_ID_MAP.put(item, id);
            return item;
        }
    }

    public static String getFluidId(Fluid fluid) {
        if (FLUID_ID_MAP.containsKey(fluid)) return FLUID_ID_MAP.get(fluid);
        else {
            String id = ForgeRegistries.FLUIDS.getKey(fluid).toString();
            FLUID_ID_MAP.put(fluid, id);
            ID_FLUID_MAP.put(id, fluid);
            return id;
        }
    }

    public static Fluid getFluid(String id) {
        if (ID_FLUID_MAP.containsKey(id)) return ID_FLUID_MAP.get(id);
        else {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
            if (fluid == null) return Fluids.EMPTY;
            ID_FLUID_MAP.put(id, fluid);
            FLUID_ID_MAP.put(fluid, id);
            return fluid;
        }
    }

    /**
     * 为NBT物品生成唯一标识符
     * 格式: itemId#hash (带NBT) 或 itemId (无NBT)
     */
    public static String getNbtItemId(ItemStack item) {
        if (item.isEmpty()) return "minecraft:air";
        String itemId = getItemId(item.getItem());
        if (!item.hasTag()) {
            return itemId;
        }
        String nbtHash = hashNbt(item.getTag());
        return itemId + "#" + nbtHash;
    }

    /**
     * 从NBT物品ID中提取基础物品ID
     */
    public static String getBaseItemId(String nbtItemId) {
        int hashIndex = nbtItemId.indexOf('#');
        return hashIndex == -1 ? nbtItemId : nbtItemId.substring(0, hashIndex);
    }

    /**
     * 计算NBT标签的哈希值
     */
    public static String hashNbt(Tag nbtTag) {
        if (nbtTag == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (nbtTag instanceof CompoundTag) {
                NbtIo.writeCompressed((CompoundTag) nbtTag, baos);
            } else {
                // 如果不是CompoundTag，创建临时的CompoundTag
                CompoundTag tempTag = new CompoundTag();
                tempTag.put("data", nbtTag);
                NbtIo.writeCompressed(tempTag, baos);
            }
            byte[] bytes = baos.toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(bytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16); // 取前16位作为哈希
        } catch (Exception e) {
            return "error";
        }
    }
}
