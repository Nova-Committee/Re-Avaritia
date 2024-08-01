package committee.nova.mods.avaritia.util;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/1 下午2:39
 * @Description: from mekanism.common.integration.projecte.IngredientHelper
 */
public class IngredientUtils {
    private final IMappingCollector<NormalizedSimpleStack, Long> mapper;
    private Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
    private boolean isValid = true;

    public IngredientUtils(IMappingCollector<NormalizedSimpleStack, Long> mapper) {
        this.mapper = mapper;
    }

    public void resetHelper() {
        this.isValid = true;
        this.ingredientMap = new HashMap<>();
    }

    public void put(NormalizedSimpleStack stack, int amount) {
        if (this.isValid) {
            if (this.ingredientMap.containsKey(stack)) {
                long newAmount = (long) this.ingredientMap.get(stack) + (long)amount;
                if (newAmount <= 2147483647L && newAmount >= -2147483648L) {
                    this.ingredientMap.put(stack, (int)newAmount);
                } else {
                    this.isValid = false;
                }
            } else {
                this.ingredientMap.put(stack, amount);
            }
        }

    }

    public void put(NormalizedSimpleStack stack, long amount) {
        if (amount <= 2147483647L && amount >= -2147483648L) {
            this.put(stack, (int)amount);
        } else {
            this.isValid = false;
        }

    }
    public void put(FluidStack stack) {
        this.put(NSSFluid.createFluid(stack), stack.getAmount());
    }

    public void put(ItemStack stack) {
        this.put(NSSItem.createItem(stack), stack.getCount());
    }

    public boolean addAsConversion(NormalizedSimpleStack output, int outputAmount) {
        if (this.isValid) {
            this.mapper.addConversion(outputAmount, output, this.ingredientMap);
            return true;
        } else {
            return false;
        }
    }

    public boolean addAsConversion(NormalizedSimpleStack output, long outputAmount) {
        return outputAmount <= 2147483647L && this.addAsConversion(output, (int) outputAmount);
    }

    public boolean addAsConversion(FluidStack stack) {
        return this.addAsConversion(NSSFluid.createFluid(stack), stack.getAmount());
    }

    public boolean addAsConversion(ItemStack stack) {
        return this.addAsConversion(NSSItem.createItem(stack), stack.getCount());
    }
}
