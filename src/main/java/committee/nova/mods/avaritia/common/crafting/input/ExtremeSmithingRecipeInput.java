package committee.nova.mods.avaritia.common.crafting.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 21:50
 * @Description:
 */
public record ExtremeSmithingRecipeInput(ItemStack template, ItemStack base, ItemStack addition1, ItemStack addition2, ItemStack addition3) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int p_346205_) {
        return switch (p_346205_) {
            case 0 -> this.template;
            case 1 -> this.base;
            case 2 -> this.addition1;
            case 3 -> this.addition2;
            case 4 -> this.addition3;
            default -> throw new IllegalArgumentException("Recipe does not contain slot " + p_346205_);
        };
    }

    @Override
    public int size() {
        return 5;
    }

    public List<ItemStack> getAdditions() {
        return List.of(
                this.addition1,
                this.addition2,
                this.addition3
        );
    }

    @Override
    public boolean isEmpty() {
        return this.template.isEmpty() && this.base.isEmpty() && this.addition1.isEmpty() && this.addition2.isEmpty() && this.addition3.isEmpty();
    }
}
