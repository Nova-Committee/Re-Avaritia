package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Vanilla renders the original tab icons, scrollable contents and inventory on either page. */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class ClientCreativeInventoryMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow protected abstract void selectTab(CreativeModeTab tab);
    @Unique private boolean avaritia$modPage;
    protected ClientCreativeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    @Unique private boolean avaritia$visible(CreativeModeTab tab) {
        return tab.getType() == CreativeModeTab.Type.SEARCH || tab.getType() == CreativeModeTab.Type.INVENTORY
                || (tab.column() >= 7) == avaritia$modPage;
    }
    @Inject(method="init", at=@At("TAIL"))
    private void avaritia$pages(CallbackInfo ci) {
        avaritia$modPage = selectedTab.column() >= 7;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            avaritia$modPage = false;
            selectTab(CreativeModeTabs.getDefaultTab());
        }).bounds(leftPos + imageWidth - 28, topPos + 5, 12, 12).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            CreativeModeTabs.tabs().stream().filter(tab -> tab.column() >= 7).findFirst().ifPresent(tab -> {
                avaritia$modPage = true;
                selectTab(tab);
            });
        }).bounds(leftPos + imageWidth - 14, topPos + 5, 12, 12).build());
    }
    @Inject(method="getTabX", at=@At("HEAD"), cancellable=true)
    private void avaritia$tabPosition(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
        if (tab.column() >= 7) cir.setReturnValue((tab.column() - 7) * 27);
    }
    @Inject(method="checkTabClicked", at=@At("HEAD"), cancellable=true)
    private void avaritia$click(CreativeModeTab tab, double x, double y, CallbackInfoReturnable<Boolean> cir) {
        if (!avaritia$visible(tab)) cir.setReturnValue(false);
    }
    @Inject(method="checkTabHovering", at=@At("HEAD"), cancellable=true)
    private void avaritia$hover(GuiGraphics graphics, CreativeModeTab tab, int x, int y, CallbackInfoReturnable<Boolean> cir) {
        if (!avaritia$visible(tab)) cir.setReturnValue(false);
    }
    @Redirect(method="renderTabButton", at=@At(value="INVOKE",target="Lnet/minecraft/world/item/CreativeModeTab;column()I"))
    private int avaritia$tabSprite(CreativeModeTab tab) {
        return tab.column() >= 7 ? tab.column() - 7 : tab.column();
    }
    @Inject(method="renderTabButton", at=@At("HEAD"), cancellable=true)
    private void avaritia$render(GuiGraphics graphics, CreativeModeTab tab, CallbackInfo ci) {
        if (!avaritia$visible(tab)) ci.cancel();
    }
}
