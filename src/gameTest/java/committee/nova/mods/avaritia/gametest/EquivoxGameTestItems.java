package committee.nova.mods.avaritia.gametest;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Isolated recipe outputs: no other recipe can mask a missing compatibility mapper. */
public final class EquivoxGameTestItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("avaritia_gametest");
    public static final DeferredItem<Item> SHAPED = ITEMS.registerSimpleItem("equivox_shaped");
    public static final DeferredItem<Item> SHAPELESS = ITEMS.registerSimpleItem("equivox_shapeless");
    public static final DeferredItem<Item> COMPRESSOR = ITEMS.registerSimpleItem("equivox_compressor");
    public static final DeferredItem<Item> SMITHING = ITEMS.registerSimpleItem("equivox_smithing");
    public static final DeferredItem<Item> ALTERNATIVES = ITEMS.registerSimpleItem("equivox_alternatives");
    public static final DeferredItem<Item> REMAINDER = ITEMS.registerSimpleItem("equivox_remainder");
    public static final DeferredItem<Item> COMPONENTS = ITEMS.registerSimpleItem("equivox_components");

    private EquivoxGameTestItems() {
    }
}
