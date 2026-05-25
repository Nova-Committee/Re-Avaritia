package com.avaritia.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Temporary 26.1.2 port placeholder for the item filter UI.
 * Full behavior depends on the item-filter network/client utilities migrated in later tasks.
 */
public class ItemFilterScreen extends Screen {
    public ItemFilterScreen() {
        super(Component.literal("ItemFilterScreen"));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractTransparentBackground(graphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
