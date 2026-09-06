package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.StringInputScreen;
import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.Text;
import committee.nova.mods.avaritia.client.render.NeutronSpacePreviewRenderer;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.common.net.C2SNeutronRingPack;
import committee.nova.mods.avaritia.common.net.S2CNeutronRingOpenPack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Left space list, right rotatable 3D preview. List actions live on a right-click OperationMenu. */
public final class NeutronRingManageScreen extends Screen {
    private final List<S2CNeutronRingOpenPack.Entry> spaces;
    private final int hand;
    private final UUID storageId;
    @Nullable
    private String selectedId;
    private SpaceList list;
    private int previewX;
    private int previewY;
    private int previewW;
    private int previewH;
    private float yaw = 35.0F;
    private float pitch = 25.0F;
    private boolean draggingPreview;
    private final OperationMenu menu = new OperationMenu();

    public NeutronRingManageScreen(S2CNeutronRingOpenPack packet) {
        super(Component.translatable("gui.avaritia.neutron_ring.title"));
        this.spaces = new ArrayList<>(packet.spaces());
        this.selectedId = packet.selectedId().orElse(null);
        this.hand = packet.hand();
        this.storageId = packet.storageId();
    }

    @Override
    protected void init() {
        int pad = 8;
        int footer = 28;
        int listY = 22;
        int listX = pad;
        int listW = Math.min(168, Math.max(96, width / 3));
        int listH = Math.max(48, height - listY - footer - pad);
        previewX = listX + listW + 8;
        previewY = listY;
        previewW = Math.max(40, width - previewX - pad);
        previewH = listH;
        list = addRenderableWidget(new SpaceList(listW, listH, listY));
        list.setX(listX);
        if (selectedId != null) {
            list.children().stream()
                    .filter(entry -> entry.space.id().equals(selectedId))
                    .findFirst()
                    .ifPresent(list::setSelected);
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width - pad - 80, height - footer + 2, 80, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (spaces.isEmpty()) {
            graphics.drawWordWrap(font, Component.translatable("gui.avaritia.neutron_ring.empty"),
                    previewX, previewY + 8, Math.max(40, previewW - 8), 0xAAAAAA);
        } else {
            S2CNeutronRingOpenPack.Entry shown = selected().orElse(null);
            if (shown == null) {
                graphics.drawWordWrap(font, Component.translatable("gui.avaritia.neutron_ring.choose"),
                        previewX, previewY + 8, Math.max(40, previewW - 8), 0xAAAAAA);
            } else {
                NeutronSpacePreview preview = shown.preview();
                graphics.drawString(font, Component.translatable("gui.avaritia.neutron_ring.preview_size",
                        preview.sizeX(), preview.sizeY(), preview.sizeZ(), preview.blocks()),
                        previewX, previewY, 0xC0C0C0, false);
                NeutronSpacePreviewRenderer.draw(graphics, preview, previewX, previewY + 12,
                        previewW, Math.max(8, previewH - 12), yaw, pitch);
            }
        }
        menu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && insidePreview(mouseX, mouseY)) {
            draggingPreview = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingPreview = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPreview && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            yaw += (float) dragX;
            pitch = Math.max(-80.0F, Math.min(80.0F, pitch + (float) dragY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean insidePreview(double mouseX, double mouseY) {
        return mouseX >= previewX && mouseX <= previewX + previewW
                && mouseY >= previewY && mouseY <= previewY + previewH;
    }

    private void send(int action, String id, String name) {
        PacketDistributor.sendToServer(new C2SNeutronRingPack(action, id, name, hand, storageId,
                NeutronRingContents.Size.DEFAULT));
    }

    private java.util.Optional<S2CNeutronRingOpenPack.Entry> selected() {
        return spaces.stream().filter(entry -> entry.id().equals(selectedId)).findFirst();
    }

    private void openSpaceMenu(S2CNeutronRingOpenPack.Entry space, double mouseX, double mouseY) {
        List<OperationMenu.Entry> entries = new ArrayList<>();
        entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.rename", () ->
                minecraft.setScreen(new StringInputScreen(this,
                        Text.i18n("gui.avaritia.neutron_ring.rename").setShadow(true),
                        Text.i18n("gui.avaritia.neutron_ring.rename"),
                        "", space.name(), value -> {
                            if (!value.isBlank()) {
                                send(C2SNeutronRingPack.RENAME, space.id(), value.trim());
                            }
                        }))));
        entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.delete", () ->
                send(C2SNeutronRingPack.DELETE, space.id(), "")));
        if (space.id().equals(selectedId)) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.neutron_ring.deselect", () -> {
                selectedId = null;
                list.setSelected(null);
                send(C2SNeutronRingPack.DESELECT, "", "");
            }));
        }
        menu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private final class SpaceList extends ObjectSelectionList<SpaceList.SpaceEntry> {
        SpaceList(int width, int height, int y) {
            super(NeutronRingManageScreen.this.minecraft, width, height, y, 24);
            NeutronRingManageScreen.this.spaces.forEach(entry -> addEntry(new SpaceEntry(entry)));
        }

        @Override
        public int getRowWidth() {
            return Math.max(80, this.getWidth() - 8);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.getWidth() - 6;
        }

        private final class SpaceEntry extends ObjectSelectionList.Entry<SpaceEntry> {
            private final S2CNeutronRingOpenPack.Entry space;

            private SpaceEntry(S2CNeutronRingOpenPack.Entry space) {
                this.space = space;
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                boolean selected = space.id().equals(selectedId);
                graphics.drawString(font, space.name(), left + 4, top + 6,
                        selected ? 0xFFFFAA00 : 0xFFFFFF, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    list.setSelected(this);
                    openSpaceMenu(space, mouseX, mouseY);
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    list.setSelected(this);
                    selectedId = space.id();
                    send(C2SNeutronRingPack.SELECT, selectedId, "");
                    return true;
                }
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(space.name());
            }
        }
    }
}
