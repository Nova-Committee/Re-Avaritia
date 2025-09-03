package committee.nova.mods.avaritia.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.render.FluidItemRender;
import committee.nova.mods.avaritia.api.client.widget.SimpleScrollBar;
import committee.nova.mods.avaritia.common.menu.ChannelMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SFilterChannelPack;
import committee.nova.mods.avaritia.core.channel.ClientChannel;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/24 00:39
 * @Description:
 */
public class BlackHoleChestScreen extends AbstractContainerScreen<ChannelMenu> {
    @Setter
    @Getter
    private int blitOffset;

    private static final ResourceLocation GUI_IMG = Res.BLACK_HOLE_CHANNEL_PANEL;
    private final String ownerName;
    private String[] lastHoveredObject = new String[2];
    private long lastCount = 0;
    private String lastFormatCountTemp = "";
    private SortButton sortButton;
    private ViewTypeButton viewTypeButton;
    private ItemScrollBar scrollBar;
    private EditBox searchBox;
    private CraftToChannelButton craftToChannelButton;
    private CraftToInventoryButton craftToInventoryButton;
    private CraftAndDropButton craftAndDropButton;

    public BlackHoleChestScreen(ChannelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 218;
        this.imageHeight = 256;
        this.ownerName = ClientChannelManager.getInstance().getUserName(this.getMenu().owner);
    }

    public void blit(GuiGraphics pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        pPoseStack.blit(GUI_IMG, pX, pY, this.blitOffset, (float) pUOffset, (float) pVOffset, pUWidth, pVHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int i, int j) {
    }


    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - imageWidth + 4) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        this.scrollBar = new ItemScrollBar(leftPos + 198, topPos + 17, 16, menu.craftingMode ? 118 : 152);
        this.scrollBar.setScrolledOn(menu.channelDummyContainer.getScrollOn());
        this.addRenderableWidget(scrollBar);
        this.addRenderableWidget(new ToggleCraftingButton(this.leftPos + 198, this.topPos + 195));
        this.addRenderableWidget(new ToggleLockButton(this.leftPos + 198, this.topPos + 211));
        this.addRenderableWidget(new ChannelButton(this.leftPos + 198, this.topPos + 227));
        this.sortButton = new SortButton(this.leftPos + 198, this.topPos + 243);
        this.addRenderableWidget(sortButton);
        this.viewTypeButton = new ViewTypeButton(this.leftPos + 198, this.topPos + 259);
        this.addRenderableWidget(viewTypeButton);
        this.searchBox = new EditBox(this.font, leftPos + 104, topPos + 4, 90, 12, Component.translatable("gui.avaritia.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setBordered(false);
        this.searchBox.setValue(menu.filter);
        this.addRenderableWidget(searchBox);
        this.craftToChannelButton = new CraftToChannelButton(leftPos + 179, topPos + 146);
        this.craftToInventoryButton = new CraftToInventoryButton(leftPos + 179, topPos + 159);
        this.craftAndDropButton = new CraftAndDropButton(leftPos + 179, topPos + 172);
        this.craftToChannelButton.active = menu.craftingMode;
        this.craftToChannelButton.visible = menu.craftingMode;
        this.craftToInventoryButton.active = menu.craftingMode;
        this.craftToInventoryButton.visible = menu.craftingMode;
        this.craftAndDropButton.active = menu.craftingMode;
        this.craftAndDropButton.visible = menu.craftingMode;
        this.addRenderableWidget(craftToChannelButton);
        this.addRenderableWidget(craftToInventoryButton);
        this.addRenderableWidget(craftAndDropButton);
        menu.channelDummyContainer.refreshContainer(true);
        menu.craftModeSetter = () -> {
            if (!menu.craftingMode) toggleCraftingMode();
        };
    }

    @Override
    public void render(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderFluids(poseStack);
        this.renderDummyCount(poseStack);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTick, int mouseX, int mouseY) {
        this.blit(stack, this.leftPos, this.topPos, 0, 0, imageWidth, 6);
        if (this.menu.craftingMode) {
            this.blit(stack, this.leftPos, this.topPos, 0, 0, imageWidth, 68);//前三行
            this.blit(stack, this.leftPos, this.topPos + 68, 0, 17, imageWidth, 51);//再加三行
            this.blit(stack, this.leftPos, this.topPos + 119, 0, 17, imageWidth, 17);//再加一行
            this.blit(stack, this.leftPos, this.topPos + 136, 0, 69, imageWidth, 141);//物品栏

//            Slot helmetSlot = this.menu.slots.get(36);
//            if (helmetSlot.isActive() && helmetSlot.getItem().isEmpty()) {
//                this.blit(stack, this.leftPos + helmetSlot.x, this.topPos + helmetSlot.y, 0, 199, 16, 16);
//            }
//            Slot chestplateSlot = this.menu.slots.get(37);
//            if (chestplateSlot.isActive() && chestplateSlot.getItem().isEmpty()) {
//                this.blit(stack, this.leftPos + chestplateSlot.x, this.topPos + chestplateSlot.y, 16, 199, 16, 16);
//            }
//            Slot leggingsSlot = this.menu.slots.get(38);
//            if (leggingsSlot.isActive() && leggingsSlot.getItem().isEmpty()) {
//                this.blit(stack, this.leftPos + leggingsSlot.x, this.topPos + leggingsSlot.y, 32, 199, 16, 16);
//            }
//            Slot bootsSlot = this.menu.slots.get(39);
//            if (bootsSlot.isActive() && bootsSlot.getItem().isEmpty()) {
//                this.blit(stack, this.leftPos + bootsSlot.x, this.topPos + bootsSlot.y, 48, 199, 16, 16);
//            }
//            Slot lhandSlot = this.menu.slots.get(40);
//            if (lhandSlot.isActive() && lhandSlot.getItem().isEmpty()) {
//                this.blit(stack, this.leftPos + lhandSlot.x, this.topPos + lhandSlot.y, 64, 199, 16, 16);
//            }

        } else {
            this.blit(stack, this.leftPos, this.topPos, 0, 0, imageWidth, 68);//前三行
            this.blit(stack, this.leftPos, this.topPos + 68, 0, 17, imageWidth, 51);//再加三行
            this.blit(stack, this.leftPos, this.topPos + 119, 0, 17, imageWidth, 51);//再加三行
            this.blit(stack, this.leftPos, this.topPos + 170, 0, 122, imageWidth, 6);//间隔
            this.blit(stack, this.leftPos, this.topPos + 176, 0, 122, imageWidth, 6);//间隔
            this.blit(stack, this.leftPos, this.topPos + 182, 0, 122, imageWidth, 6);//间隔
            this.blit(stack, this.leftPos, this.topPos + 188, 0, 122, imageWidth, 4);//间隔
            this.blit(stack, this.leftPos, this.topPos + 192, 0, 125, imageWidth, 85);//物品栏
        }
    }

    private void renderFluids(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 300.0D);
        menu.channelDummyContainer.fluidStacks.forEach((integer, fluidStack) -> {
            Slot slot = menu.slots.get(integer + 51);
            FluidItemRender.renderFluid(fluidStack, poseStack, slot.x, slot.y, 0);
        });
        poseStack.popPose();
    }

    public void renderDummyCount(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        for (int i = 0; i < menu.channelDummyContainer.formatCount.size(); i++) {
            Slot slot = menu.slots.get(i + 51);
            String count = menu.channelDummyContainer.formatCount.get(i);
            this.setBlitOffset(100);
            RenderSystem.enableDepthTest();
            float fontSize = 0.5F;
            poseStack.pushPose();
            poseStack.translate(leftPos + slot.x, topPos + slot.y, 300.0D);
            poseStack.scale(fontSize, fontSize, 1.0F);
            guiGraphics.drawString(font, count,
                    (int) ((16 - this.font.width(count) * fontSize) / fontSize),
                    (int) ((16 - this.font.lineHeight * fontSize) / fontSize),
                    16777215, false);
            poseStack.popPose();
            this.setBlitOffset(0);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pPoseStack, int pX, int pY) {
        if (this.hoveredSlot != null) {
            if (hoveredSlot.index >= 51) {
                if (menu.getCarried().getCount() == 1)
                    renderObjectStorageTooltip(pPoseStack, pX, pY);
                else
                    renderCounterTooltip(pPoseStack, pX, pY);
            } else if (!hoveredSlot.getItem().isEmpty() && menu.getCarried().isEmpty())
                pPoseStack.renderTooltip(font, this.hoveredSlot.getItem(), pX, pY);
        } else {
            if (isInsideEditBox(pX, pY)) {
                List<Component> list = new ArrayList<>();
                list.add(Component.translatable("gui.avaritia.search.tip1"));
                list.add(Component.translatable("gui.avaritia.search.tip2"));
                list.add(Component.translatable("gui.avaritia.search.tip3"));
                pPoseStack.renderComponentTooltip(font, list, pX, pY);
            }
        }
    }

    private void renderCounterTooltip(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
        if ((hoveredSlot.index - 51) >= menu.channelDummyContainer.viewingObject.size()) return;
        String[] hoveredObject = menu.channelDummyContainer.viewingObject.get(hoveredSlot.index - 51);
        List<Component> components = Lists.newArrayList();
        long count;
        if (hoveredObject[0].equals("item")) {
            components = getTooltipFromItem(minecraft, hoveredSlot.getItem());
            count = menu.channel.getRealItemAmount(hoveredObject[1]);
        } else if (hoveredObject[0].equals("fluid")) {
            components.add(Component.translatable("block." + hoveredObject[1].replace(':', '.')));
            if (this.minecraft.options.advancedItemTooltips)
                components.add(Component.literal(hoveredObject[1]).withStyle(ChatFormatting.DARK_GRAY));
            count = menu.channel.getRealFluidAmount(hoveredObject[1]);
        } else {
            components.add(hoveredSlot.getItem().getHoverName());
            count = menu.channel.getRealEnergyAmount(hoveredObject[1]);
        }
        if (!Arrays.equals(hoveredObject, lastHoveredObject)) {
            String formatCount = StorageUtils.DECIMAL_FORMAT.format(count);
            components.add(Component.literal(formatCount));
            this.lastHoveredObject = hoveredObject;
            this.lastCount = count;
            this.lastFormatCountTemp = formatCount;
        } else if (count == lastCount) {
            components.add(Component.literal(lastFormatCountTemp));
        } else {
            String formatCount = StorageUtils.DECIMAL_FORMAT.format(count);
            long count2 = count - lastCount;
            String formatCount2 = StorageUtils.DECIMAL_FORMAT.format(count2);
            if (count2 >= 0) formatCount += "  |  +§a" + formatCount2;
            else formatCount += "  |  §c" + formatCount2;
            components.add(Component.literal(formatCount));
            lastCount = count;
            lastFormatCountTemp = formatCount;
        }
        pPoseStack.renderTooltip(font, components, hoveredSlot.getItem().getTooltipImage(), pMouseX, pMouseY);
    }


    private void renderObjectStorageTooltip(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
        ItemStack carried = menu.getCarried();
        boolean hasCapability = carried.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()
                || carried.getCapability(ForgeCapabilities.ENERGY).isPresent()
                || carried.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
        if (hasCapability) {
            List<Component> components = Lists.newArrayList();
            if ((hoveredSlot.index - 51) < menu.channelDummyContainer.viewingObject.size()) {
                String[] hoveredObject = menu.channelDummyContainer.viewingObject.get(hoveredSlot.index - 51);
                if (hoveredObject[0].equals("fluid")) {
                    components.add(Component.translatable("gui.avaritia.capability.tip1",
                            Component.translatable("block." + hoveredObject[1].replace(':', '.')).getString()
                    ));
                } else {
                    components.add(Component.translatable("gui.avaritia.capability.tip1", hoveredSlot.getItem().getHoverName()));
                }
            }
            components.add(Component.translatable("gui.avaritia.capability.tip2"));
            components.add(Component.translatable("gui.avaritia.capability.tip3"));
            pPoseStack.renderTooltip(font, components, ItemStack.EMPTY.getTooltipImage(), pMouseX, pMouseY);
        } else renderCounterTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (searchBox.isFocused()) searchBox.tick();
    }

    @Override
    public void onClose() {
        NetworkHandler.CHANNEL.sendToServer(new C2SFilterChannelPack(menu.containerId, menu.filter));
        ((ClientChannel) menu.channel).removeListener();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean lshift = InputConstants.isKeyDown(getMinecraft().getWindow().getWindow(), InputConstants.KEY_LSHIFT);
        if (pButton == 1) {
            //搜索框
            if (searchBox.isMouseOver(pMouseX, pMouseY)) {
                menu.filter = "";
                searchBox.setValue("");
                menu.channelDummyContainer.refreshContainer(true);
                searchBox.setFocused(true);
                searchBox.setEditable(true);
            } else if (craftToChannelButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 6);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 7);
            } else if (craftToInventoryButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 11);
            } else if (craftAndDropButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 14);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 15);
            }
        } else {
            if (craftToChannelButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 9);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 8);
            } else if (craftToInventoryButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 13);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 12);
            } else if (craftAndDropButton.isMouseOver(pMouseX, pMouseY)) {
                if (lshift) minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 17);
                else minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 16);
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (scrollBar.isScrolling()) scrollBar.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        scrollBar.mouseReleased(pMouseX, pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (searchBox.isFocused()) {
            if (pKeyCode >= InputConstants.KEY_0 && pKeyCode <= InputConstants.KEY_Z) return true;
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) menu.LShifting = true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (searchBox.isFocused()) {
            String s = searchBox.getValue().toLowerCase();
            if (!s.equals(menu.filter)) {
                menu.filter = s;
                menu.channelDummyContainer.refreshContainer(true);
            }
        }
        if (pKeyCode == InputConstants.KEY_LSHIFT) {
            menu.LShifting = false;
            menu.channelDummyContainer.refreshContainer(true);
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pMouseX >= leftPos + 5 && pMouseX <= leftPos + 214 && pMouseY >= topPos + 17 && pMouseY <= topPos + 18 + (menu.craftingMode ? 119 : 153) && scrollBar.canScroll()) {
            if (pDelta <= 0) scrollBar.setScrolledOn(menu.channelDummyContainer.onMouseScrolled(false));
            else scrollBar.setScrolledOn(menu.channelDummyContainer.onMouseScrolled(true));
            return true;
        } else return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= (double) pX && pMouseX < (double) (pX + pWidth) && pMouseY >= (double) pY && pMouseY < (double) (pY + pHeight);
    }

    private boolean isInsideEditBox(double pMouseX, double pMouseY) {
        return pMouseX >= leftPos + 104 && pMouseX <= leftPos + 194 && pMouseY >= topPos + 4 && pMouseY <= topPos + 16;
    }

    private void toggleLock() {
        if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            this.menu.locked = !this.menu.locked;
            this.searchBox.setFocused(false);
            NetworkHandler.CHANNEL.sendToServer(new C2SFilterChannelPack(menu.containerId, menu.filter));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    protected void toggleCraftingMode() {
        this.menu.craftingMode = !this.menu.craftingMode;
        this.menu.channelDummyContainer.refreshContainer(true);
        this.searchBox.setFocused(false);
        this.craftToChannelButton.active = menu.craftingMode;
        this.craftToChannelButton.visible = menu.craftingMode;
        this.craftToInventoryButton.active = menu.craftingMode;
        this.craftToInventoryButton.visible = menu.craftingMode;
        this.craftAndDropButton.active = menu.craftingMode;
        this.craftAndDropButton.visible = menu.craftingMode;
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        scrollBar.setHeight(menu.craftingMode ? 118 : 152);
        scrollBar.setScrollTagSize();
    }

    private void cycleSort() {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            menu.reverseSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
        } else {
            menu.nextSort();
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }
    }

    private void changeViewType() {
        menu.changeViewType();
        minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 4);
    }

    private String getSortKey(int sortType) {
        return switch (sortType) {
            case SortUtils.Sort.ID_ASCENDING, SortUtils.Sort.ID_DESCENDING -> "gui.avaritia.sort.id";
            case SortUtils.Sort.NAMESPACE_ID_ASCENDING, SortUtils.Sort.NAMESPACE_ID_DESCENDING ->
                    "gui.avaritia.sort.nid";
            case SortUtils.Sort.MIRROR_ID_ASCENDING, SortUtils.Sort.MIRROR_ID_DESCENDING ->
                    "gui.avaritia.sort.mirror_id";
            case SortUtils.Sort.COUNT_ASCENDING, SortUtils.Sort.COUNT_DESCENDING -> "gui.avaritia.sort.count";
            default -> "";
        };
    }

    private void channelButtonPress() {
        minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 5);
    }

    private class ItemScrollBar extends SimpleScrollBar {

        private int lastObjectListSize;

        public ItemScrollBar(int x, int y, int weight, int height) {
            super(x, y, weight, height);
            this.setScrollTagSize();
            this.lastObjectListSize = menu.channelDummyContainer.sortedObject.size();
        }

        public void setScrollTagSize() {
            double v = (double) this.height * ((menu.craftingMode ? 7.0D : 9.0D) / Math.ceil(menu.channelDummyContainer.sortedObject.size() / 11.0D));
            this.setScrollTagSize(v);
        }

        @Override
        public void draggedTo(double scrolledOn) {
            menu.channelDummyContainer.onScrollTo(scrolledOn);
        }

        @Override
        public void beforeRender() {
            if (menu.channelDummyContainer.sortedObject.size() != lastObjectListSize) {
                setScrollTagSize();
                this.lastObjectListSize = menu.channelDummyContainer.sortedObject.size();
            }
        }
    }

    private class ToggleCraftingButton extends ImageButton {

        public ToggleCraftingButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 27, GUI_IMG, pButton -> toggleCraftingMode());
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            int uOffset = menu.craftingMode ? 235 : 219;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), uOffset, this.yTexStart, this.width, this.height, 256, 256);
        }
    }


    private class ToggleLockButton extends ImageButton {

        public ToggleLockButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 43, GUI_IMG, pButton -> toggleLock());
            MutableComponent componentA = Component.translatable("gui.avaritia.owner", "§a" + menu.player.getGameProfile().getName());
            MutableComponent componentB = Component.translatable("gui.avaritia.owner", "§c" + ownerName);
            MutableComponent componentC = Component.translatable("gui.avaritia.owner", ownerName);
            if (menu.owner.equals(menu.player.getUUID())) setTooltip(Tooltip.create(componentA));
            else if (menu.locked) setTooltip(Tooltip.create(componentB));
            else setTooltip(Tooltip.create(componentC));
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            int uOffset = menu.locked ? 235 : 219;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), uOffset, this.yTexStart, this.width, this.height, 256, 256);
        }
    }

    private class ChannelButton extends ImageButton {
        private final List<FormattedCharSequence> tips = new ArrayList<>();

        public ChannelButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 59, GUI_IMG, pButton -> channelButtonPress());
            if (menu.channelOwner.equals(menu.player.getUUID())) {
                tips.add(Component.translatable("gui.avaritia.channel.tip1", "§a" + menu.channel.getName()).getVisualOrderText());
                tips.add(Component.translatable("gui.avaritia.channel.tip2", "§a" + ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            } else if (!menu.channelOwner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
                tips.add(Component.translatable("gui.avaritia.channel.tip1", "§c" + menu.channel.getName()).getVisualOrderText());
                tips.add(Component.translatable("gui.avaritia.channel.tip2", "§c" + ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            } else {
                tips.add(Component.translatable("gui.avaritia.channel.tip1", menu.channel.getName()).getVisualOrderText());
                tips.add(Component.translatable("gui.avaritia.channel.tip2", ClientChannelManager.getInstance().getUserName(menu.channelOwner)).getVisualOrderText());
            }
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            if (this.isHovered) setTooltipForNextRenderPass(tips);
        }
    }

    private class SortButton extends ImageButton {

        public SortButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 75, GUI_IMG, pButton -> cycleSort());
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            int vOffset = menu.sortType * 16 + this.yTexStart;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, vOffset, this.width, this.height, 256, 256);
            list.add(Component.translatable(getSortKey(menu.sortType)).getVisualOrderText());
            if (menu.sortType % 2 == 0)
                list.add(Component.translatable("gui.avaritia.sort.ascending").getVisualOrderText());
            else list.add(Component.translatable("gui.avaritia.sort.descending").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.line").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.sort.tip2").getVisualOrderText());
            if (sortButton.isHoveredOrFocused()) setTooltipForNextRenderPass(list);
        }
    }

    private class ViewTypeButton extends ImageButton {

        public ViewTypeButton(int pX, int pY) {
            super(pX, pY, 16, 16, 219, 203, GUI_IMG, pButton -> changeViewType());
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            int vOffset = 16 * menu.viewType + this.yTexStart;
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, vOffset, this.width, this.height, 256, 256);
            if (menu.viewType == 0) list.add(Component.translatable("gui.avaritia.view.all").getVisualOrderText());
            else if (menu.viewType == 1)
                list.add(Component.translatable("gui.avaritia.view.item").getVisualOrderText());
            else list.add(Component.translatable("gui.avaritia.view.fluid").getVisualOrderText());
            if (viewTypeButton.isHoveredOrFocused()) setTooltipForNextRenderPass(list);
        }
    }

    private class CraftToChannelButton extends ImageButton {

        public CraftToChannelButton(int x, int y) {
            super(x, y, 16, 9, 219, 0, GUI_IMG, pButton -> {
            });

        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            list.add(Component.translatable("gui.avaritia.craft.channel").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip2").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip3").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip4").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }

    private class CraftToInventoryButton extends ImageButton {
        public CraftToInventoryButton(int x, int y) {
            super(x, y, 16, 9, 219, 18, GUI_IMG, pButton -> {
            });
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            list.add(Component.translatable("gui.avaritia.craft.inv").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip2").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip3").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip4").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }

    private class CraftAndDropButton extends ImageButton {
        public CraftAndDropButton(int x, int y) {
            super(x, y, 16, 9, 219, 9, GUI_IMG, pButton -> {
            });
        }

        @Override
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            List<FormattedCharSequence> list = new ArrayList<>();
            pPoseStack.blit(GUI_IMG, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.width, this.height, 256, 256);
            list.add(Component.translatable("gui.avaritia.craft.drop").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip1").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip2").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip3").getVisualOrderText());
            list.add(Component.translatable("gui.avaritia.craft.tip4").getVisualOrderText());
            if (this.isHovered) setTooltipForNextRenderPass(list);
        }
    }

}
