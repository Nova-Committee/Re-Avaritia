package committee.nova.mods.avaritia.api.common.container;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * ContainerRange
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/11 下午11:58
 */
public class FaceContainer {
    public Container inv;
    public Direction face;
    public WorldlyContainer sidedInv;
    public int[] slots;

    public FaceContainer(Container inv, Direction side) {
        this.inv = inv;
        this.face = side;
        if (inv instanceof WorldlyContainer) {
            sidedInv = (WorldlyContainer) inv;
            slots = sidedInv.getSlotsForFace(face);
        } else {
            slots = new int[inv.getContainerSize()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
        }
    }

    public FaceContainer(Container inv) {
        this(inv, Direction.DOWN);
    }

    public FaceContainer(Container inv, int firstSlot, int lastSlot) {
        this.inv = inv;
        slots = new int[lastSlot - firstSlot];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = firstSlot + i;
        }
    }

    public FaceContainer(Container inv, FaceContainer access) {
        this.inv = inv;
        this.slots = access.slots;
        this.face = access.face;
        if (inv instanceof WorldlyContainer) {
            sidedInv = (WorldlyContainer) inv;
        }
    }

    public boolean canInsertItem(int slot, @Nonnull ItemStack item) {
        return sidedInv == null ? inv.canPlaceItem(slot, item) : sidedInv.canPlaceItemThroughFace(slot, item, face);
    }

    public boolean canExtractItem(int slot, @Nonnull ItemStack item) {
        return sidedInv == null ? inv.canPlaceItem(slot, item) : sidedInv.canTakeItemThroughFace(slot, item, face);
    }

    public int lastSlot() {
        int last = 0;
        for (int slot : slots) {
            if (slot > last) {
                last = slot;
            }
        }
        return last;
    }
}
