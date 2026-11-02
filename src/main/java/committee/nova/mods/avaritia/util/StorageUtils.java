package committee.nova.mods.avaritia.util;

import committee.nova.mods.avaritia.core.io.StorageLifecycle;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.function.Supplier;

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
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            ITEM_ID_MAP.put(item, id);
            ID_ITEM_MAP.put(id, item);
            return id;
        }
    }

    public static Item getItem(String id) {
        id = getBaseItemId(id);
        if (ID_ITEM_MAP.containsKey(id)) return ID_ITEM_MAP.get(id);
        else {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
            if (item == null || item.equals(Items.AIR)) return Items.AIR;
            ID_ITEM_MAP.put(id, item);
            // Storage variant keys must never replace an item's canonical registry ID.
            getItemId(item);
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
            Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(id));
            if (fluid == null) return Fluids.EMPTY;
            ID_FLUID_MAP.put(id, fluid);
            FLUID_ID_MAP.put(fluid, id);
            return fluid;
        }
    }

    // === 完整组件序列化（26.3 数据组件物品） ===

    private static final RegistryOps<Tag> FALLBACK_OPS = RegistryAccess
            .fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);
    @Nullable
    private static volatile Supplier<HolderLookup.Provider> clientRegistryProvider;

    /**
     * 由客户端入口注册，供连接专用服务器的客户端解析注册表感知组件（附魔等）。
     */
    public static void setClientRegistryProvider(Supplier<HolderLookup.Provider> provider) {
        clientRegistryProvider = provider;
    }

    /**
     * 当前可用的注册表序列化上下文：优先服务端，其次客户端世界，最后内置注册表。
     */
    public static RegistryOps<Tag> registryOps() {
        MinecraftServer server = StorageLifecycle.getServer();
        if (server != null) {
            return server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        }
        Supplier<HolderLookup.Provider> supplier = clientRegistryProvider;
        if (supplier != null) {
            HolderLookup.Provider provider = supplier.get();
            if (provider != null) return provider.createSerializationContext(NbtOps.INSTANCE);
        }
        return FALLBACK_OPS;
    }

    /**
     * 序列化完整 ItemStack（含全部组件与数量）。失败返回 null。
     */
    @Nullable
    public static CompoundTag saveStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Tag tag = ItemStack.CODEC.encodeStart(registryOps(), stack).result().orElse(null);
        return tag instanceof CompoundTag compound ? compound : null;
    }

    /**
     * 序列化物品身份快照（数量固定为 1），用于身份键与重建模板。
     */
    @Nullable
    public static CompoundTag saveStackSnapshot(ItemStack stack) {
        return saveStack(stack.copyWithCount(1));
    }

    /**
     * 从完整快照重建物品（含全部组件与快照中的数量）。失败返回 EMPTY。
     */
    public static ItemStack parseStack(@Nullable Tag tag) {
        if (!(tag instanceof CompoundTag)) return ItemStack.EMPTY;
        return ItemStack.CODEC.parse(registryOps(), tag).result().orElse(ItemStack.EMPTY);
    }

    /**
     * 为带组件物品生成唯一身份键。
     * 格式: itemId#hash (带任意非默认组件) 或 itemId (与原型一致)。
     * 哈希基于数量为 1 的完整 ItemStack 快照，与
     * {@link ItemStack#isSameItemSameComponents} 语义一致：
     * 同 id 但组件不同（附魔、命名、潜影盒内容等）的物品不会合并。
     */
    public static String getNbtItemId(ItemStack item) {
        if (item.isEmpty()) return "minecraft:air";
        String itemId = getItemId(item.getItem());
        if (item.getComponentsPatch().isEmpty()) {
            return itemId;
        }
        CompoundTag snapshot = saveStackSnapshot(item);
        if (snapshot == null) {
            return itemId + "#error";
        }
        return itemId + "#" + hashNbt(snapshot);
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
