package committee.nova.mods.avaritia.util.recipes;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:39
 * Version: 1.0
 */
public class IngredientsCache {
    private static final IngredientsCache INSTANCE = new IngredientsCache();
    private final Map<ResourceLocation, List<List<Component>>> lists;

    private IngredientsCache() {
        this.lists = new HashMap<>();
    }

    public static IngredientsCache getInstance() {
        return INSTANCE;
    }

    private static List<List<Component>> createIngredientsList(NonNullList<Ingredient> ingredients) {
        var lists = new ArrayList<ItemList>();

        for (var ingredient : ingredients) {
            var items = Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).toList();
            var matched = false;

            // increment quantity if there's already a matching list
            for (var list : lists) {
                if (list.containsAll(items)) {
                    list.quantity++;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                lists.add(new ItemList(items));
            }
        }

        return lists.stream()
                .map(s -> s.items.stream()
                        .map(i -> (Component) Component.literal(s.quantity + "x ").append(i.getDefaultInstance().getHoverName()))
                        .toList()
                )
                .toList();
    }

    public List<Component> getIngredientsList(ResourceLocation id, NonNullList<Ingredient> ingredients) {
        return this.lists.computeIfAbsent(id, r -> createIngredientsList(ingredients))
                .stream()
                .map(l -> {
                    var index = (System.currentTimeMillis() / 2000L) % l.size();
                    return l.get(Math.toIntExact(index));
                })
                .toList();
    }

    public void onResourceManagerReload(ResourceManager manager) {
        this.lists.clear();
    }

    private static class ItemList {
        public final List<Item> items;
        private final Set<Item> itemSet;
        public int quantity;

        public ItemList(List<Item> items) {
            this.itemSet = new HashSet<>(items);
            this.items = items;
            this.quantity = 1;
        }

        public boolean containsAll(Collection<Item> items) {
            return this.itemSet.containsAll(items);
        }
    }
}
