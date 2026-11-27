package committee.nova.mods.avaritia.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.mods.avaritia.api.util.vec.Vector3;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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

    /** 26.3 动作栏消息（替代旧 Player#displayClientMessage(msg, true)）；客户端调用为空操作，由服务端下发 */
    public static void sendActionbar(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        }
    }

    public static String getId(Item item) {
        Identifier resource = BuiltInRegistries.ITEM.getKey(item);
        if (resource == null) return "minecraft:air";
        else return resource.toString();
    }

    public static String getId(ItemStack itemStack) {
        return getId(itemStack.getItem()) + getNbtString(itemStack);
    }

    public static Item getItem(String id) {
        String resourceId = id;
        if (id.contains("{") && id.endsWith("}")) resourceId = resourceId.substring(0, id.indexOf("{"));
        Identifier identifier = Identifier.tryParse(resourceId);
        return identifier == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(identifier);
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
                CompoundTag nbt = TagParser.parseCompoundFully(nbtString);
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            } catch (Exception e) {
                if (throwException) throw e;
                LOGGER.error("Failed to parse NBT data", e);
            }
        }
        return itemStack;
    }

    public static String getNbtString(ItemStack itemStack) {
        String json = "";
        if (!itemStack.getComponentsPatch().isEmpty() && itemStack.get(DataComponents.CUSTOM_DATA) != null) {
            json = itemStack.get(DataComponents.CUSTOM_DATA).copyTag().toString();
        }
        return json;
    }

    public static @NonNull ItemStack deserialize(JsonObject json) {
        ItemStack itemStack;
        try {
            String itemId = json.get("item").getAsString();
            int count = json.get("count").getAsInt();
            count = Math.max(count, 1);
            Identifier identifier = Identifier.tryParse(itemId);
            Item item = identifier == null ? Items.AIR : BuiltInRegistries.ITEM.getValue(identifier);
            if (item == null) {
                throw new JsonParseException("Unknown item ID: " + itemId);
            }
            itemStack = new ItemStack(item, count);

            // 如果存在NBT数据，则解析
            if (json.has("nbt")) {
                try {
                    CompoundTag nbt = TagParser.parseCompoundFully(json.get("nbt").getAsString());
                    itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
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
            if (reward.get(DataComponents.CUSTOM_DATA) != null) {
                json.addProperty("nbt", getNbtString(reward));
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
            UseRemainder remainder = stack.get(DataComponents.USE_REMAINDER);
            return container && remainder != null ? remainder.convertInto().create() : ItemStack.EMPTY;
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
        return !stack1.isEmpty() && !stack2.isEmpty() && (stack1.getItem() == stack2.getItem() && (stack2.getDamageValue() == stack1.getDamageValue()) && ItemStack.isSameItemSameComponents(stack2, stack1));
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
        CustomData tag1 = stack1.get(DataComponents.CUSTOM_DATA);
        CustomData tag2 = stack2.get(DataComponents.CUSTOM_DATA);
        if (tag1 == null) {
            return true;
        } else if (tag2 == null) {
            return false;
        } else {
            Set<String> stack1Keys = tag1.copyTag().keySet();
            Set<String> stack2Keys = tag2.copyTag().keySet();
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
        var random = level.getRandom();
        item.setDeltaMovement(random.nextGaussian() * 0.05, random.nextGaussian() * 0.05 + 0.2F, random.nextGaussian() * 0.05);
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
        var random = world.getRandom();
        double xVelocity = random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
        double yVelocity = random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
        double zVelocity = random.nextFloat() * velocity + (1.0D - velocity) * 0.5D;
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

    @SafeVarargs
    public static void clearEnchants(ItemStack stack, ResourceKey<Enchantment>... enchantments) {
        if (enchantments.length == 0) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        } else {
            ItemEnchantments.Mutable currentEnchants = new ItemEnchantments.Mutable(stack.getEnchantments());
            boolean modified = false;

            for (ResourceKey<Enchantment> enchantment : enchantments) {
                for (Holder<Enchantment> holder : currentEnchants.keySet()) {
                    if (enchantment.equals(holder.unwrapKey().orElse(null)) && currentEnchants.getLevel(holder) > 0) {
                        currentEnchants.set(holder, 0);
                        modified = true;
                    }
                }
            }

            if (modified) {
                stack.set(DataComponents.ENCHANTMENTS, currentEnchants.toImmutable());
            }
        }
    }

}

