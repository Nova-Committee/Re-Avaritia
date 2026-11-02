package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.ExtremeSmithingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 极限锻造台界面。
 */
public class ExtremeSmithingScreen extends ItemCombinerScreen<ExtremeSmithingMenu> {
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private ScreenRectangle errorIcon = ScreenRectangle.empty();
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = Identifier.withDefaultNamespace(
            "container/slot/smithing_template_netherite_upgrade"
    );
    private static final List<Identifier> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");

    public ExtremeSmithingScreen(ExtremeSmithingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ScreenTextures.EXTREME_SMITHING);
        this.titleLabelX = 78;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        this.errorIcon = PortableLayout.translate(new ScreenRectangle(65, 46, 28, 21), this.leftPos, this.topPos);
    }


    private boolean hasRecipeError() {
        return this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem()
                && this.menu.getSlot(2).hasItem() && this.menu.getSlot(3).hasItem()
                && this.menu.getSlot(4).hasItem() && !this.menu.getSlot(this.menu.getResultSlot()).hasItem();
    }

    @Override
    protected void extractErrorIcon(@NotNull GuiGraphicsExtractor graphics, int x, int y) {
        if (this.hasRecipeError()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_SMITHING, this.errorIcon.left(), this.errorIcon.top(), this.imageWidth, 0.0F, this.errorIcon.width(), this.errorIcon.height(), 256, 256);
            UiInspector.region("smithing.error", this.errorIcon, null, true);
        }
    }


    @Override
    public void containerTick() {
        super.containerTick();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.extractOnboardingTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        this.templateIcon.extractRenderState(this.menu, graphics, partialTick, this.leftPos, this.topPos);
    }

    private void extractOnboardingTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Optional<Component> optional = Optional.empty();
        if (this.hasRecipeError() && PortableLayout.contains(this.errorIcon, mouseX, mouseY)) {
            optional = Optional.of(ERROR_TOOLTIP);
        }


        if (this.hoveredSlot != null) {
            ItemStack itemStack = this.menu.getSlot(0).getItem();
            if (itemStack.isEmpty() && this.hoveredSlot.index == 0) {
                optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
            }
        }

        optional.ifPresent(component -> graphics.setTooltipForNextFrame(this.font, this.font.split(component, 115), mouseX, mouseY));
    }
}
