package committee.nova.mods.avaritia.init.compat.trinkets;

import committee.nova.mods.avaritia.init.registry.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Loaded only after Trinkets is present; passive item behavior uses the native gameplay hooks. */
final class TrinketsCompat {
    private TrinketsCompat() {}

    static void initialize() {
        TrinketsApi.registerTrinket(ModItems.infinity_elytra.get(), new Trinket() {
            @Override
            public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
                var type = slot.inventory().getSlotType();
                return "chest".equals(type.getGroup()) && "back".equals(type.getName());
            }

            @Override
            public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_ELYTRA, 1, 1);
            }
        });
    }

    static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        var component = TrinketsApi.getTrinketComponent(entity).orElse(null);
        if (component == null) return ItemStack.EMPTY;
        // Read live inventories without allocating the API's equipped-pair list every tick.
        for (var group : component.getInventory().values()) {
            for (var inventory : group.values()) {
                ItemStack stack = findIn(inventory, predicate);
                if (!stack.isEmpty()) return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    static ItemStack findBack(LivingEntity entity, Predicate<ItemStack> predicate) {
        var component = TrinketsApi.getTrinketComponent(entity).orElse(null);
        if (component == null) return ItemStack.EMPTY;
        var chest = component.getInventory().get("chest");
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
