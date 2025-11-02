package committee.nova.mods.avaritia.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.mods.avaritia.api.util.vec.Vector3;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static committee.nova.mods.avaritia.Const.LOGGER;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 14:18
 * Version: 1.0
 */
public class ItemUtils {
    public ItemUtils() {
    }

    public static String getId(Item item) {
        ResourceLocation resource = ForgeRegistries.ITEMS.getKey(item);
        if (resource == null) return "minecraft:air";
        else return resource.toString();
    }

    public static String getId(ItemStack itemStack) {
        return getId(itemStack.getItem()) + getNbtString(itemStack);
    }

    public static Item getItem(String id) {
        String resourceId = id;
        if (id.contains("{") && id.endsWith("}")) resourceId = resourceId.substring(0, id.indexOf("{"));
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(resourceId));
    }

    public static ItemStack getItemStack(String id) {
        ItemStack result = new ItemStack(Items.AIR);
        try {
            result = getItemStack(id, false);
        } catch (CommandSyntaxException ignored) {
        }
        return result;
    }

    public static ItemStack getItemStack(String id, boolean throwException) throws CommandSyntaxException {
        Item item = getItem(id);
        if (item == null) {
            throw new RuntimeException("Unknown item ID: " + id);
        }
        ItemStack itemStack = new ItemStack(item);
        if (id.contains("{") && id.endsWith("}") && !id.endsWith("{}")) {
            try {
                String nbtString = id.substring(id.indexOf("{"));
                CompoundTag nbt = TagParser.parseTag(nbtString);
                itemStack.setTag(nbt);
            } catch (Exception e) {
                if (throwException) throw e;
                LOGGER.error("Failed to parse NBT data", e);
            }
        }
        return itemStack;
    }

    public static String getNbtString(ItemStack itemStack) {
        String json = "";
        if (itemStack.hasTag() && itemStack.getTag() != null) {
            json = itemStack.getTag().toString();
        }
        return json;
    }

    public static @NonNull ItemStack deserialize(JsonObject json) {
        ItemStack itemStack;
        try {
            String itemId = json.get("item").getAsString();
            int count = json.get("count").getAsInt();
            count = Math.max(count, 1);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) {
                throw new JsonParseException("Unknown item ID: " + itemId);
            }
            itemStack = new ItemStack(item, count);

            // 如果存在NBT数据，则解析
            if (json.has("nbt")) {
                try {
                    CompoundTag nbt = TagParser.parseTag(json.get("nbt").getAsString());
                    itemStack.setTag(nbt);
                } catch (CommandSyntaxException e) {
                    throw new JsonParseException("Failed to parse NBT data", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize item reward", e);
            itemStack = new ItemStack(Items.AIR);
        }
        return itemStack;
    }

    public static JsonObject serialize(ItemStack reward) {
        JsonObject json = new JsonObject();
        try {
            json.addProperty("item", getId(reward.getItem()));
            json.addProperty("count", reward.getCount());

            // 如果物品有NBT数据，则序列化
            if (reward.hasTag()) {
                if (reward.getTag() != null) {
                    json.addProperty("nbt", getNbtString(reward));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to serialize item reward", e);
            json.addProperty("item", getId(Items.AIR));
            json.addProperty("count", 0);
        }
        return json;
    }

    public String getDisplayName(JsonObject json) {
        return deserialize(json).getDisplayName().getString().replaceAll("\\[(.*)]", "$1");
    }

    public static String getDisplayName(ItemStack itemStack) {
        return itemStack.getDisplayName().getString().replaceAll("\\[(.*)]", "$1");
    }

    public static String getDisplayName(Item item) {
        return new ItemStack(item).getDisplayName().getString().replaceAll("\\[(.*)]", "$1");
    }

    public static ItemStack mapEquals(ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack itemStack : map.keySet()) {
            if (itemStack.getItem() == stack.getItem()) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack withSize(ItemStack stack, int size, boolean container) {
        if (size <= 0) {
            return container && stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
        } else {
            stack = stack.copy();
            stack.setCount(size);
            return stack;
        }
    }

    public static ItemStack grow(ItemStack stack, int amount) {
        return withSize(stack, stack.getCount() + amount, false);
    }

    public static ItemStack shrink(ItemStack stack, int amount, boolean container) {
        return stack.isEmpty() ? ItemStack.EMPTY : withSize(stack, stack.getCount() - amount, container);
    }

    /**
     * Copy's an ItemStack.
     *
     * @param stack    Stack to copy.
     * @param quantity Size of the new stack.
     * @return The new stack.
     */
    public static ItemStack copyStack(@Nonnull ItemStack stack, int quantity) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        stack = stack.copy();
        stack.setCount(quantity);
        return stack;
    }

    /**
     * @param stack1 The {@link ItemStack} being compared.
     * @param stack2 The {@link ItemStack} to compare to.
     * @return whether the two items are the same in terms of damage and itemID.
     */
    public static boolean areStacksSameType(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return !stack1.isEmpty() && !stack2.isEmpty() && (stack1.getItem() == stack2.getItem() && (stack2.getDamageValue() == stack1.getDamageValue()) && ItemStack.isSameItemSameTags(stack2, stack1));
    }

    public static boolean canCombineStacks(ItemStack stack1, ItemStack stack2) {
        if (!stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        } else {
            return areStacksSameType(stack1, stack2) && stack1.getCount() + stack2.getCount() <= stack1.getMaxStackSize();
        }
    }

    public static ItemStack combineStacks(ItemStack stack1, ItemStack stack2) {
        return stack1.isEmpty() ? stack2.copy() : grow(stack1, stack2.getCount());
    }

    public static boolean compareTags(ItemStack stack1, ItemStack stack2) {
        if (!stack1.hasTag()) {
            return true;
        } else if (stack1.hasTag() && !stack2.hasTag()) {
            return false;
        } else {
            Set<String> stack1Keys = NBTUtils.getTagCompound(stack1).getAllKeys();
            Set<String> stack2Keys = NBTUtils.getTagCompound(stack2).getAllKeys();
            Iterator<String> iterator = stack1Keys.iterator();

            String key;
            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                key = iterator.next();
                if (!stack2Keys.contains(key)) {
                    return false;
                }
            } while (NbtUtils.compareNbt(NBTUtils.getTag(stack1, key), NBTUtils.getTag(stack2, key), true));

            return false;
        }
    }

    /**
     * Drops an item with basic default random velocity.
     */
    public static void dropItem(ItemStack stack, Level level, Vector3 dropLocation) {
        ItemEntity item = new ItemEntity(level, dropLocation.x, dropLocation.y, dropLocation.z, stack);
        item.setDeltaMovement(level.random.nextGaussian() * 0.05, level.random.nextGaussian() * 0.05 + 0.2F, level.random.nextGaussian() * 0.05);
        level.addFreshEntity(item);
    }

    /**
     * Drops an item in the world at the given BlockPos
     *
     * @param world    World to drop the item.
     * @param pos      Location to drop item.
     * @param stack    ItemStack to drop.
     * @param velocity The velocity to add.
     */
    public static void dropItem(Level world, BlockPos pos, @Nonnull ItemStack stack, double velocity) {
        double xVelocity = world.random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
        double yVelocity = world.random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
        double zVelocity = world.random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
        ItemEntity entityItem = new ItemEntity(world, pos.getX() + xVelocity, pos.getY() + yVelocity, pos.getZ() + zVelocity, stack);
        entityItem.setPickUpDelay(10);
        world.addFreshEntity(entityItem);
    }

    /**
     * Drops an item in the world at the given BlockPos
     *
     * @param world World to drop the item.
     * @param pos   Location to drop item.
     * @param stack ItemStack to drop.
     */
    public static void dropItem(Level world, BlockPos pos, @Nonnull ItemStack stack) {
        dropItem(world, pos, stack, 0.7D);
    }

    /**
     * Drops all the items in an IInventory on the ground.
     *
     * @param world     World to drop the item.
     * @param pos       Position to drop item.
     * @param inventory IInventory to drop.
     */
    public static void dropInventory(Level world, BlockPos pos, Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getCount() > 0) {
                dropItem(world, pos, stack);
            }
        }
    }

    public static void removeEnchant(ItemStack stack, Enchantment pEnchantment) {
        if (!stack.getOrCreateTag().contains("Enchantments", Tag.TAG_LIST)) {
            stack.getOrCreateTag().put("Enchantments", new ListTag());
        }

        ListTag listtag = stack.getOrCreateTag().getList("Enchantments", Tag.TAG_COMPOUND);
        listtag.stream().filter(tag -> {
            CompoundTag compoundtag = (CompoundTag) tag;
            return Enchantment.byId(compoundtag.getShort("id")) == pEnchantment;
        }).forEach(listtag::remove);
    }

    public static void clearEnchants(ItemStack stack) {
        if (stack.getOrCreateTag().contains("Enchantments", Tag.TAG_LIST)) {
            stack.getOrCreateTag().remove("Enchantments");
        }
    }

}
