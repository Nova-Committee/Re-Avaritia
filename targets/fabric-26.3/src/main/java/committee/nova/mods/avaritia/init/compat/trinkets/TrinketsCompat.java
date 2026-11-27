package committee.nova.mods.avaritia.init.compat.trinkets;

import committee.nova.mods.avaritia.init.registry.ModItems;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Loaded only after Trinkets is present; passive item behavior uses the native gameplay hooks.
 *  适配 Trinkets Updated（eu.pb4.trinkets）：
 *  - 无尽鞘翅可装备性由 data/trinkets/tags/item/chest/back.json 标签驱动（仅能进背饰槽）；
 *  - 装备音效通过 TrinketCallback.getEquipSound 提供；
 *  - 饰品栏查询走 TrinketAttachment（TrinketInventory 实现 Container，可直接复用遍历）。 */
public final class TrinketsCompat {
    private TrinketsCompat() {}

    public static void initialize() {
        TrinketCallback.setCallback(ModItems.infinity_elytra.get(), new TrinketCallback() {
            @Override
            public Holder<SoundEvent> getEquipSound(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
                return SoundEvents.ARMOR_EQUIP_ELYTRA;
            }
        });
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        var attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return ItemStack.EMPTY;
        // Read live inventories without allocating the API's equipped-pair list every tick.
        for (var group : attachment.getInventory().values()) {
            for (var inventory : group.values()) {
                ItemStack stack = findIn(inventory, predicate);
                if (!stack.isEmpty()) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack findBack(LivingEntity entity, Predicate<ItemStack> predicate) {
        var attachment = TrinketsApi.getAttachment(entity);
        if (attachment == null) return ItemStack.EMPTY;
        var chest = attachment.getInventory().get("chest");
        if (chest == null) return ItemStack.EMPTY;
        var back = chest.get("back");
        return back == null ? ItemStack.EMPTY : findIn(back, predicate);
    }

    private static ItemStack findIn(Container inventory, Predicate<ItemStack> predicate) {
        for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
