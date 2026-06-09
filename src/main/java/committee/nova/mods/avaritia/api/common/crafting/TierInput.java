package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/23 00:13
 * @Description:
 */
public class TierInput extends CraftingInput {
    private final int tier;
    // these are hoisted from CraftInput.Positioned
    private final int top;
    private final int left;

    private TierInput(int width, int height, List<ItemStack> items, int tier, int top, int left) {
        super(width, height, items);
        this.tier = tier;
        this.top = top;
        this.left = left;
    }

    public int tier() {
        return this.tier;
    }

    public int top() {
        return this.top;
    }

    public int left() {
        return this.left;
    }

    public static TierInput of(int width, int height, List<ItemStack> items, int tier) {
        var positioned = CraftingInput.ofPositioned(width, height, items);
        var input = positioned.input();
        return new TierInput(input.width(), input.height(), input.items(), tier, positioned.top(), positioned.left());
    }
}
