package com.avaritia.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.avaritia.Avaritia;
import com.avaritia.api.client.screen.BaseContainerScreen;
import com.avaritia.common.menu.NeutronRingMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.avaritia.client.AvaritiaForgeClient.RING_KEY;

public class NeutronRingScreen extends BaseContainerScreen<NeutronRingMenu> {
    private static final Identifier BACKGROUND = Identifier.of(Avaritia.MOD_ID, "textures/gui/neutron_ring.png");

    public NeutronRingScreen(NeutronRingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BACKGROUND, 256, 276, 256, 276);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (RING_KEY.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        } else return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
