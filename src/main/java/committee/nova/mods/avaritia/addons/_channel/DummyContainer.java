package committee.nova.mods.avaritia.addons._channel;

import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import committee.nova.mods.avaritia.util.StorageUtils.ViewType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 03:26
 * @Description:
 */
public class DummyContainer extends SimpleContainer {
    public final ArrayList<String[]> sortedObject = new ArrayList<>();
    public final ArrayList<String[]> viewingObject = new ArrayList<>();
    public final HashMap<Integer, FluidStack> fluidStacks = new HashMap<>();
    public final ArrayList<String> formatCount = new ArrayList<>();
    private final ChannelMenu menu;
    protected ArrayList<Item> sortedItems = new ArrayList<>();
    protected ArrayList<Fluid> sortedFluids = new ArrayList<>();
    protected ArrayList<String> sortedEnergies = new ArrayList<>();
    private double scrollTo = 0.0D;

    public DummyContainer(ChannelMenu menu) {
        super(99);
        this.menu = menu;
    }

    public void onChangeViewType() {
        sortedObject.clear();
        switch (this.menu.viewType) {
            case ViewType.ALL -> {
                sortedItems.forEach(s -> sortedObject.add(new String[]{"item", StorageUtils.getItemId(s)}));
                sortedFluids.forEach(s -> sortedObject.add(new String[]{"fluid", StorageUtils.getFluidId(s)}));
                sortedEnergies.forEach(s -> sortedObject.add(new String[]{"energy", s}));
            }
            case ViewType.Items -> sortedItems.forEach(s -> sortedObject.add(new String[]{"item", StorageUtils.getItemId(s)}));
            case ViewType.Fluids -> {
                sortedFluids.forEach(s -> sortedObject.add(new String[]{"fluid", StorageUtils.getFluidId(s)}));
                sortedEnergies.forEach(s -> sortedObject.add(new String[]{"energy", s}));
            }
        }
        scrollOffset(0);
    }

    public void onScrollTo(double scrollTo) {
        this.scrollTo = scrollTo;
        scrollOffset(0);
    }

    public double getScrollOn() {
        return scrollTo;
    }

    public void scrollOffset(int offset) {
        if (sortedObject.size() <= (this.menu.craftingMode ? 77 : 99)) {
            viewingObject.clear();
            viewingObject.addAll(sortedObject);
        }
        else {
            int i = (int) Math.ceil(sortedObject.size() / 11.0D);
            i -= this.menu.craftingMode ? 7 : 9;
            int j = Math.round(i * (float) scrollTo);
            if (offset != 0) {
                j += offset;
                j = Math.max(0, Math.min(i, j));
                scrollTo = (double) j / (double) i;
            }
            viewingObject.clear();
            viewingObject.addAll(sortedObject.subList(j * 11, Math.min(sortedObject.size(), j * 11 + (this.menu.craftingMode ? 77 : 99))));
        }
        updateDummySlots(true);
    }

    public double onMouseScrolled(boolean isUp) {
        if (isUp) scrollOffset(-1);
        else scrollOffset(1);
        return scrollTo;
    }

    public void refreshContainer(boolean fullUpdate) {
        if (!this.menu.level.isClientSide) return;
        if ((fullUpdate || this.menu.sortType >= 6) && !this.menu.LShifting) {
            sortedItems = new ArrayList<>(this.menu.channel.storageItems.keySet());
            sortedFluids = new ArrayList<>(this.menu.channel.storageFluids.keySet());
            sortedEnergies = new ArrayList<>(this.menu.channel.storageEnergies.keySet());
            if (!this.menu.filter.isEmpty()) {
                ArrayList<Item> temp = new ArrayList<>();
                ArrayList<Fluid> temp1 = new ArrayList<>();
                ArrayList<String> temp2 = new ArrayList<>();
                char head = this.menu.filter.charAt(0);
                if (head == '*') {
                    String s = this.menu.filter.substring(1);
                    for (Item item : sortedItems) if (StorageUtils.getItemId(item).contains(s)) temp.add(item);
                    for (Fluid fluid : sortedFluids) if (StorageUtils.getFluidId(fluid).contains(s)) temp1.add(fluid);
                    for (String energyName : sortedEnergies) if (energyName.contains(s)) temp2.add(energyName);
                }
                else if (head == '$') {
                    String s = this.menu.filter.substring(1);
                    for (Item itemName : sortedItems) {
                        ItemStack itemStack = new ItemStack(itemName);
                        ArrayList<String> tags = new ArrayList<>();
                        itemStack.getTags().forEach(itemTagKey -> tags.add(itemTagKey.location().getPath()));
                        for (String tag : tags) {
                            if (tag.contains(s)) {
                                temp.add(itemName);
                                break;
                            }
                        }
                    }
                }
                else {
                    for (Item item : sortedItems) {
                        if (StorageUtils.getItemId(item).contains(this.menu.filter)) temp.add(item);
                        else {
                            ItemStack itemStack = new ItemStack(item);
                            if (itemStack.getDisplayName().getString().toLowerCase().contains(this.menu.filter)) temp.add(item);
                        }
                    }
                    for (Fluid fluid : sortedFluids) {
                        if (StorageUtils.getFluidId(fluid).contains(this.menu.filter)) temp1.add(fluid);
                        else {
                            FluidStack fluidStack = new FluidStack(fluid, 1);
                            if (fluidStack.getDisplayName().getString().toLowerCase().contains(this.menu.filter)) temp1.add(fluid);
                        }
                    }
                    for (String energyName : sortedEnergies) {
                        if (energyName.contains(this.menu.filter)) temp2.add(energyName);
                        else {
                            ItemStack itemStack = new ItemStack(StorageUtils.getItem(energyName));
                            if (itemStack.getDisplayName().getString().toLowerCase().contains(this.menu.filter)) temp2.add(energyName);
                        }
                    }
                }
                sortedItems = temp;
                sortedFluids = temp1;
                sortedEnergies = temp2;
            }
            switch (this.menu.sortType) {
                case SortUtils.Sort.ID_ASCENDING -> {
                    sortedItems.sort((a, b) -> SortUtils.sortFromRightID(StorageUtils.getItemId(a), StorageUtils.getItemId(b)));
                    sortedFluids.sort((a, b) -> SortUtils.sortFromRightID(StorageUtils.getFluidId(a), StorageUtils.getFluidId(b)));
                    sortedEnergies.sort(SortUtils::sortFromRightID);
                }
                case SortUtils.Sort.ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromRightID(StorageUtils.getItemId(a), StorageUtils.getItemId(b))));
                    sortedFluids.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromRightID(StorageUtils.getFluidId(a), StorageUtils.getFluidId(b))));
                    sortedEnergies.sort(Collections.reverseOrder(SortUtils::sortFromRightID));
                }
                case SortUtils.Sort.NAMESPACE_ID_ASCENDING -> {
                    sortedItems.sort(Comparator.comparing(StorageUtils::getItemId));
                    sortedFluids.sort(Comparator.comparing(StorageUtils::getFluidId));
                    sortedEnergies.sort(String::compareTo);
                }
                case SortUtils.Sort.NAMESPACE_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder(Comparator.comparing(StorageUtils::getItemId)));
                    sortedFluids.sort(Collections.reverseOrder(Comparator.comparing(StorageUtils::getFluidId)));
                    sortedEnergies.sort(Collections.reverseOrder(String::compareTo));
                }
                case SortUtils.Sort.MIRROR_ID_ASCENDING -> {
                    sortedItems.sort((a, b) -> SortUtils.sortFromMirrorID(StorageUtils.getItemId(a), StorageUtils.getItemId(b)));
                    sortedFluids.sort((a, b) -> SortUtils.sortFromMirrorID(StorageUtils.getFluidId(a), StorageUtils.getFluidId(b)));
                    sortedEnergies.sort(SortUtils::sortFromMirrorID);
                }
                case SortUtils.Sort.MIRROR_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromMirrorID(StorageUtils.getItemId(a), StorageUtils.getItemId(b))));
                    sortedFluids.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromMirrorID(StorageUtils.getFluidId(a), StorageUtils.getFluidId(b))));
                    sortedEnergies.sort(Collections.reverseOrder(SortUtils::sortFromMirrorID));
                }
                case SortUtils.Sort.COUNT_ASCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromCount(StorageUtils.getItemId(a), StorageUtils.getItemId(b), SortUtils.convertKeys(this.menu.channel.storageItems, StorageUtils::getItemId), false)));
                    sortedFluids.sort((s1, s2) -> SortUtils.sortFromCount(StorageUtils.getFluidId(s1), StorageUtils.getFluidId(s2), SortUtils.convertKeys(this.menu.channel.storageFluids, StorageUtils::getFluidId), false));
                    sortedEnergies.sort((s1, s2) -> SortUtils.sortFromCount(s1, s2, this.menu.channel.storageEnergies, false));
                }
                case SortUtils.Sort.COUNT_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((a, b) -> SortUtils.sortFromCount(StorageUtils.getItemId(a), StorageUtils.getItemId(b), SortUtils.convertKeys(this.menu.channel.storageItems, StorageUtils::getItemId), true)));
                    sortedFluids.sort((s1, s2) -> SortUtils.sortFromCount(StorageUtils.getFluidId(s1), StorageUtils.getFluidId(s2), SortUtils.convertKeys(this.menu.channel.storageFluids, StorageUtils::getFluidId), true));
                    sortedEnergies.sort((s1, s2) -> SortUtils.sortFromCount(s1, s2, this.menu.channel.storageEnergies, true));
                }
            }
            onChangeViewType();
            return;
        }
        updateDummySlots(fullUpdate);
    }

    public void updateDummySlots(boolean fullUpdate) {
        formatCount.clear();
        if (fullUpdate) fluidStacks.clear();
        for (int j = 0; j < (this.menu.craftingMode ? 77 : 99); j++) {
            if (j < viewingObject.size() && viewingObject.get(j) != null) {
                String id = viewingObject.get(j)[1];
                if (viewingObject.get(j)[0].equals("fluid")) {
                    if (fullUpdate) {
                        this.setItem(j, new ItemStack(StorageUtils.getFluid(id).getBucket()));
                        fluidStacks.put(j, new FluidStack(StorageUtils.getFluid(id), 1));
                    }
                    if (!this.menu.channel.storageFluids.containsKey(StorageUtils.getFluid(id))) {
                        formatCount.add(j, "§c0");
                        continue;
                    }
                    long count = this.menu.channel.storageFluids.get(StorageUtils.getFluid(id));
                    if (count < 1000L) formatCount.add(j, count + "mB");
                    else if (count < Long.MAX_VALUE) {
                        String stringCount = StorageUtils.DECIMAL_FORMAT.format(count);
                        stringCount = stringCount.substring(0, 4);
                        if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                        stringCount = stringCount.replace(",", ".");
                        if (count < 1000000L) stringCount += "";
                        else if (count < 1000000000L) stringCount += "K";
                        else if (count < 1000000000000L) stringCount += "M";
                        else if (count < 1000000000000000L) stringCount += "G";
                        else if (count < 1000000000000000000L) stringCount += "T";
                        else stringCount += "P";
                        formatCount.add(j, stringCount);
                    }
                    else formatCount.add(j, "MAX");
                }
                else {
                    //叠堆数为1避开原版的数字渲染
                    if (fullUpdate) this.setItem(j, new ItemStack(StorageUtils.getItem(id)));
                    long count;
                    if (viewingObject.get(j)[0].equals("item")) {
                        if (this.menu.channel.storageItems.containsKey(StorageUtils.getItem(id))) {
                            count = this.menu.channel.storageItems.get(StorageUtils.getItem(id));
                        }
                        else {
                            formatCount.add(j, "§c0");
                            continue;
                        }
                    }
                    else {
                        if (this.menu.channel.storageEnergies.containsKey(id)) {
                            count = this.menu.channel.storageEnergies.get(id);
                        }
                        else {
                            formatCount.add(j, "§c0");
                            continue;
                        }
                    }
                    if (count < 1000L) formatCount.add(j, String.valueOf(count));
                    else if (count < Long.MAX_VALUE) {
                        String stringCount = StorageUtils.DECIMAL_FORMAT.format(count);
                        stringCount = stringCount.substring(0, 4);
                        if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                        stringCount = stringCount.replace(",", ".");
                        if (count < 1000000L) stringCount += "K";
                        else if (count < 1000000000L) stringCount += "M";
                        else if (count < 1000000000000L) stringCount += "G";
                        else if (count < 1000000000000000L) stringCount += "T";
                        else if (count < 1000000000000000000L) stringCount += "P";
                        else stringCount += "E";
                        formatCount.add(j, stringCount);
                        // 9,223,372,036,854,775,807L
                        // e  p   t   g   m   k
                    }
                    else formatCount.add(j, "MAX");
                }
            }
            else this.setItem(j, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

}
