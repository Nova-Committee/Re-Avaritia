package committee.nova.mods.avaritia.common.container;

import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** 15×7 只读投影视图；真实数量始终来自 long 镜像。 */
public final class InfinityChestContainer extends SimpleContainer {
    public static final int WIDTH = 15;
    public static final int HEIGHT = 7;
    public static final int SIZE = WIDTH * HEIGHT;

    private final InfinityChestMenu menu;
    private final List<ChestHandler.StoredItem> sorted = new ArrayList<>();
    private final List<ChestHandler.StoredItem> visible = new ArrayList<>();
    private final List<String> formattedVisible = new ArrayList<>();
    private double scroll;

    public InfinityChestContainer(InfinityChestMenu menu) {
        super(SIZE);
        this.menu = menu;
    }

    public void refresh(boolean structureChanged) {
        sorted.clear();
        String query = menu.getFilter().toLowerCase(Locale.ROOT);
        menu.getChest().entries().stream()
                .filter(entry -> matches(entry.resource(), query))
                .sorted(comparator(menu.getSortType()))
                .forEach(sorted::add);
        clampScroll();
        rebuildVisible();
    }

    public ItemResource resource(int slot) {
        return slot >= 0 && slot < visible.size() ? visible.get(slot).resource() : ItemResource.EMPTY;
    }

    public long amount(int slot) {
        return slot >= 0 && slot < visible.size() ? visible.get(slot).amount() : 0L;
    }

    public String formattedAmount(int slot) {
        return slot >= 0 && slot < formattedVisible.size() ? formattedVisible.get(slot) : "0";
    }

    public int totalVariants() {
        return sorted.size();
    }

    public boolean canScroll() {
        return maximumRowOffset() > 0;
    }

    public double getScroll() {
        return scroll;
    }

    public double scrollRows(int rows) {
        int maximum = maximumRowOffset();
        if (maximum == 0) {
            scroll = 0.0D;
        } else {
            int current = (int) Math.round(scroll * maximum);
            current = Math.max(0, Math.min(maximum, current + rows));
            scroll = (double) current / maximum;
        }
        rebuildVisible();
        return scroll;
    }

    public void scrollTo(double value) {
        scroll = Math.max(0.0D, Math.min(1.0D, value));
        rebuildVisible();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        ItemResource resource = resource(slot);
        return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public static String formatAmount(long amount) {
        if (amount <= 0L) return "0";
        if (amount == Long.MAX_VALUE) return "MAX";
        if (amount < 1_000L) return Long.toString(amount);
        String[] suffixes = {"K", "M", "G", "T", "P", "E"};
        double scaled = amount;
        int suffix = -1;
        while (scaled >= 1_000.0D && suffix + 1 < suffixes.length) {
            scaled /= 1_000.0D;
            suffix++;
        }
        String number = scaled >= 100.0D
                ? String.format(Locale.ROOT, "%.0f", scaled)
                : scaled >= 10.0D
                ? String.format(Locale.ROOT, "%.1f", scaled)
                : String.format(Locale.ROOT, "%.2f", scaled);
        if (number.indexOf('.') >= 0) {
            number = number.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return number + suffixes[Math.max(0, suffix)];
    }

    public static String formatExactAmount(long amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }

    private void rebuildVisible() {
        int start = maximumRowOffset() == 0 ? 0 : (int) Math.round(scroll * maximumRowOffset()) * WIDTH;
        visible.clear();
        visible.addAll(sorted.subList(Math.min(start, sorted.size()), Math.min(start + SIZE, sorted.size())));
        formattedVisible.clear();
        visible.stream().map(ChestHandler.StoredItem::amount).map(InfinityChestContainer::formatAmount)
                .forEach(formattedVisible::add);
    }

    private int maximumRowOffset() {
        int rows = (sorted.size() + WIDTH - 1) / WIDTH;
        return Math.max(0, rows - HEIGHT);
    }

    private void clampScroll() {
        if (maximumRowOffset() == 0) {
            scroll = 0.0D;
        } else {
            scroll = Math.max(0.0D, Math.min(1.0D, scroll));
        }
    }

    private static boolean matches(ItemResource resource, String query) {
        if (query.isEmpty()) return true;
        if (query.charAt(0) == '*') {
            return resource.typeHolder().getRegisteredName().toLowerCase(Locale.ROOT).contains(query.substring(1));
        }
        if (query.charAt(0) == '$') {
            String tagQuery = query.substring(1);
            return resource.test(stack -> stack.typeHolder().tags()
                    .anyMatch(tag -> tag.location().toString().toLowerCase(Locale.ROOT).contains(tagQuery)));
        }
        String id = resource.typeHolder().getRegisteredName().toLowerCase(Locale.ROOT);
        String name = resource.getHoverName().getString().toLowerCase(Locale.ROOT);
        return id.contains(query) || name.contains(query);
    }

    private static Comparator<ChestHandler.StoredItem> comparator(byte sortType) {
        Comparator<ChestHandler.StoredItem> byFullId = Comparator.comparing(
                entry -> entry.resource().typeHolder().getRegisteredName());
        Comparator<ChestHandler.StoredItem> byPath = Comparator.comparing(
                entry -> path(entry.resource().typeHolder().getRegisteredName()));
        Comparator<ChestHandler.StoredItem> byMirror = (left, right) -> compareMirrored(
                left.resource().typeHolder().getRegisteredName(), right.resource().typeHolder().getRegisteredName());
        Comparator<ChestHandler.StoredItem> byCount = Comparator.comparingLong(ChestHandler.StoredItem::amount)
                .thenComparing(byFullId);
        return switch (sortType) {
            case 1 -> byPath.reversed();
            case 2 -> byFullId;
            case 3 -> byFullId.reversed();
            case 4 -> byMirror;
            case 5 -> byMirror.reversed();
            case 6 -> byCount;
            case 7 -> byCount.reversed();
            default -> byPath;
        };
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    static int compareMirrored(String left, String right) {
        int leftIndex = left.length() - 1;
        int rightIndex = right.length() - 1;
        while (leftIndex >= 0 && rightIndex >= 0) {
            int compared = Character.compare(left.charAt(leftIndex--), right.charAt(rightIndex--));
            if (compared != 0) return compared;
        }
        return Integer.compare(left.length(), right.length());
    }
}
