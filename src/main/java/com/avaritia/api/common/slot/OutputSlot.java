package com.avaritia.api.common.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:16
 * Version: 1.0
 */
public class OutputSlot extends ResourceHandlerSlot {
    public OutputSlot(ResourceHandler<ItemResource> itemHandler, IndexModifier<ItemResource> modifier, int index, int xPosition, int yPosition) {
        super(itemHandler, modifier, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}
