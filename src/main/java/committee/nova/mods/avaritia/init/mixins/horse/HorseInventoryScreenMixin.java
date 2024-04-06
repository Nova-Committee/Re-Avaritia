package committee.nova.mods.avaritia.init.mixins.horse;

import committee.nova.mods.avaritia.api.iface.IChestHorse;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HorseInventoryScreenMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 22:31
 */
@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin extends AbstractContainerScreen<HorseInventoryMenu> {

    @Shadow @Final private AbstractHorse horse;

    @Shadow @Final private static ResourceLocation HORSE_INVENTORY_LOCATION;

    public HorseInventoryScreenMixin(HorseInventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", opcode = 0, shift = At.Shift.AFTER))
    public void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo ci){
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        if (this.horse instanceof IChestHorse chestHorse && chestHorse.avaritia_forge$hasChest()) {
            pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i + 79, j + 17, 0, this.imageHeight, chestHorse.avaritia_forge$getInventoryColumns() * 18, 54);
        }
    }
}
