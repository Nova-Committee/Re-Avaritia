package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Avaritia;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.NeutronRingMenu;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static committee.nova.mods.avaritia.client.AvaritiaClient.RING_KEY;

public class NeutronRingScreen extends BaseContainerScreen<NeutronRingMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Const.MOD_ID, "textures/gui/neutron_ring.png");

    public NeutronRingScreen(NeutronRingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BACKGROUND, 256, 276, 256, 276);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyEvent);
        if (RING_KEY.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        } else return super.keyPressed(keyEvent);
    }
}
