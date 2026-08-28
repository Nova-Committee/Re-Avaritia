package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.menu.ExtremeSmithingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * 极限锻造台界面。
 */
public class ExtremeSmithingScreen extends ItemCombinerScreen<ExtremeSmithingMenu> {
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground additionalIcon1 = new CyclingSlotBackground(2);
    private final CyclingSlotBackground additionalIcon2 = new CyclingSlotBackground(3);
    private final CyclingSlotBackground additionalIcon3 = new CyclingSlotBackground(4);
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

    private boolean hasRecipeError() {
        return this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem()
                && this.menu.getSlot(2).hasItem() && this.menu.getSlot(3).hasItem()
                && this.menu.getSlot(4).hasItem() && !this.menu.getSlot(this.menu.getResultSlot()).hasItem();
    }

    @Override
    protected void extractErrorIcon(@NotNull GuiGraphicsExtractor graphics, int x, int y) {
        if (this.hasRecipeError()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.EXTREME_SMITHING, x + 65, y + 46, this.imageWidth, 0.0F, 28, 21, 256, 256);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        Optional<SmithingTemplateItem> optional = this.getTemplateItem();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        this.baseIcon.tick(optional.map(SmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        List<Identifier> additionIcons = optional.map(SmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of());
        this.additionalIcon1.tick(additionIcons);
        this.additionalIcon2.tick(additionIcons);
        this.additionalIcon3.tick(additionIcons);
    }

    private Optional<SmithingTemplateItem> getTemplateItem() {
        ItemStack itemStack = this.menu.getSlot(0).getItem();
        if (!itemStack.isEmpty()) {
            Item item = itemStack.getItem();
            if (item instanceof SmithingTemplateItem smithingTemplateItem) {
                return Optional.of(smithingTemplateItem);
            }
        }

        return Optional.empty();
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
        this.baseIcon.extractRenderState(this.menu, graphics, partialTick, this.leftPos, this.topPos);
        this.additionalIcon1.extractRenderState(this.menu, graphics, partialTick, this.leftPos, this.topPos);
        this.additionalIcon2.extractRenderState(this.menu, graphics, partialTick, this.leftPos, this.topPos);
        this.additionalIcon3.extractRenderState(this.menu, graphics, partialTick, this.leftPos, this.topPos);
    }

    private void extractOnboardingTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Optional<Component> optional = Optional.empty();
        if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, mouseX, mouseY)) {
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
