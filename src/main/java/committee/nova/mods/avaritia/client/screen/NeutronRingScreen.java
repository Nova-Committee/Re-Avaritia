package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.NeutronRingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.RING_KEY;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 下午1:39
 * @Description:
 */
public class NeutronRingScreen extends BaseContainerScreen<NeutronRingMenu> {

    public NeutronRingScreen(NeutronRingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, Res.NEUTRON_RING_TEX, 256, 276, 256, 276);
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
