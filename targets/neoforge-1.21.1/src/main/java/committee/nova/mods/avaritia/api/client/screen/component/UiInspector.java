package committee.nova.mods.avaritia.api.client.screen.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import committee.nova.mods.avaritia.Const;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.Writer;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Development-only geometry observations, not a replacement for Minecraft's event dispatch. */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class UiInspector {
    private static final boolean ENABLED = !FMLLoader.isProduction() && Boolean.getBoolean("avaritia.ui.inspect");

    private UiInspector() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static <T extends AbstractWidget> T name(T widget, String id) {
        if (ENABLED) {
            State.names.put(widget, id);
        }
        return widget;
    }

    public static void region(String id, ScreenRectangle bounds, @Nullable ScreenRectangle clip, boolean interactive) {
        if (collecting()) {
            State.current.add(id, State.current.root(), "region", true, bounds, clip, bounds, true, interactive, false, 0);
        }
    }

    public static void region(String id, int x, int y, int width, int height, @Nullable ScreenRectangle clip, boolean interactive) {
        if (collecting()) {
            region(id, new ScreenRectangle(x, y, width, height), clip, interactive);
        }
    }

    public static void popup(String id, ScreenRectangle bounds) {
        if (collecting()) {
            State.current.popups.add(id);
            State.current.add(id, State.current.root(), "popup", true, bounds, null, bounds, true, true, false, 0);
        }
    }

    public static void row(String group, @Nullable Object businessKey, int sourceIndex,
                           int x, int y, int width, int height, @Nullable ScreenRectangle clip, boolean interactive) {
        if (!collecting()) {
            return;
        }
        Layer layer = State.current;
        String id = businessKey == null ? group + "/frame/" + State.frameId + "/index/" + sourceIndex
                : group + "/key/" + layer.state.keys.computeIfAbsent(businessKey, ignored -> layer.state.keys.size() + 1);
        ScreenRectangle bounds = new ScreenRectangle(x, y, width, height);
        layer.add(id, group, "region", businessKey != null, bounds, clip, bounds, true, interactive, false, 0);
    }

    private static boolean collecting() {
        return ENABLED && State.current != null;
    }

    // Loading the production subscriber does not allocate the inspector's maps or frame graph.
    private static final class State {
        private static final Map<AbstractWidget, String> names = new WeakHashMap<>();
        private static final Map<AbstractWidget, Long> widgetIds = new WeakHashMap<>();
        private static final Map<Screen, ScreenState> screens = new WeakHashMap<>();
        private static final List<Layer> layers = new ArrayList<>();
        private static long nextScreen;
        private static long nextWidget;
        private static long frameId;
        private static long layoutRevision;
        private static long exportSequence;
        private static final long session = System.currentTimeMillis();
        private static boolean overlay;
        private static boolean requested;
        private static boolean writing;
        private static boolean collectFrame;
        private static Layer current;
    }

    private static final class ScreenState {
        private final long instance = ++State.nextScreen;
        private final Map<Object, Integer> keys = new HashMap<>();
        private List<Stamp> previous = List.of();
    }

    private record Element(String id, String parentId, long screenInstance, @Nullable Integer containerId,
                           String source, boolean stableId, ScreenRectangle bounds, @Nullable ScreenRectangle clipBounds,
                           ScreenRectangle hitBounds, ScreenRectangle visibleBounds, int layer,
                           boolean visible, boolean enabled, boolean focused, boolean inputBlocked) {
        private Element blocked(boolean blocked) {
            return new Element(id, parentId, screenInstance, containerId, source, stableId, bounds, clipBounds,
                    hitBounds, visibleBounds, layer, visible, enabled, focused, blocked);
        }
    }

    private record Stamp(Element element, long widgetIdentity) {
    }

    private record Issue(String code, String elementId) {
    }

    private static final class Layer {
        private final Screen screen;
        private final ScreenState state;
        private final ScreenRectangle viewport;
        private final Integer containerId;
        private final int index;
        private final List<Stamp> entries = new ArrayList<>();
        private final Set<String> popups = new HashSet<>();

        private Layer(Screen screen) {
            this.screen = screen;
            state = State.screens.computeIfAbsent(screen, ignored -> new ScreenState());
            viewport = new ScreenRectangle(0, 0, screen.width, screen.height);
            containerId = screen instanceof AbstractContainerScreen<?> container ? container.getMenu().containerId : null;
            index = State.layers.size();
            add(root(), "", "region", true, viewport, null, viewport, true, false, false, 0);
        }

        private String root() {
            return "screen/" + state.instance;
        }

        private void add(String id, String parent, String source, boolean stable, ScreenRectangle bounds,
                         @Nullable ScreenRectangle clip, ScreenRectangle hit, boolean visible,
                         boolean enabled, boolean focused, long widgetIdentity) {
            ScreenRectangle visibleBounds = intersect(bounds, viewport);
            if (clip != null) {
                visibleBounds = intersect(visibleBounds, clip);
            }
            entries.add(new Stamp(new Element(id, parent, state.instance, containerId, source, stable, bounds, clip,
                    hit, visibleBounds, index, visible && positive(visibleBounds), enabled, focused, false), widgetIdentity));
        }

        private void finish() {
            boolean background = screen != Minecraft.getInstance().screen;
            for (int i = 0; i < entries.size(); i++) {
                Stamp stamp = entries.get(i);
                Element element = stamp.element;
                boolean blocked = background || !popups.isEmpty()
                        && !popups.contains(element.id) && !popups.contains(element.parentId);
                entries.set(i, new Stamp(element.blocked(blocked), stamp.widgetIdentity));
            }
            if (!entries.equals(state.previous)) {
                ++State.layoutRevision;
                state.previous = List.copyOf(entries);
            }
        }
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (ENABLED) {
            ++State.layoutRevision;
            State.current = null;
        }
    }

    @SubscribeEvent
    public static void onClosing(ScreenEvent.Closing event) {
        if (ENABLED) {
            State.screens.remove(event.getScreen());
            State.current = null;
            State.layers.clear();
        }
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        if (!ENABLED) {
            return;
        }
        ++State.frameId;
        State.current = null;
        State.layers.clear();
        State.collectFrame = State.overlay || State.requested;
        if (Minecraft.getInstance().screen == null) {
            State.screens.clear();
            State.requested = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        if (!ENABLED || !State.collectFrame) {
            return;
        }
        State.current = new Layer(event.getScreen());
        State.layers.add(State.current);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (!collecting() || State.current.screen != event.getScreen()) {
            return;
        }
        Layer layer = State.current;
        collectWidgets(layer, layer.screen.children(), "", null, true, Collections.newSetFromMap(new IdentityHashMap<>()));
        if (layer.screen instanceof AbstractContainerScreen<?> container) {
            ScreenRectangle panel = new ScreenRectangle(container.getGuiLeft(), container.getGuiTop(), container.getXSize(), container.getYSize());
            layer.add("container.panel", layer.root(), "region", true, panel, null, panel, true, false, false, 0);
            for (Slot slot : container.getMenu().slots) {
                if (slot.isActive()) {
                    ScreenRectangle icon = new ScreenRectangle(container.getGuiLeft() + slot.x, container.getGuiTop() + slot.y, 16, 16);
                    layer.add("slot/" + slot.index, layer.root(), "slot", true, icon, null,
                            PortableLayout.inset(icon, -1, -1, -1, -1), true, true, false, 0);
                }
            }
        }
        layer.finish();
        State.current = null;
        if (layer.screen != Minecraft.getInstance().screen) {
            return;
        }
        // Drop maps belonging to screens no longer in the visible native layer stack.
        Set<Screen> live = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Layer visible : State.layers) {
            live.add(visible.screen);
        }
        State.screens.keySet().removeIf(screen -> !live.contains(screen));
        List<Element> elements = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();
        for (Layer visible : State.layers) {
            Set<String> ids = new HashSet<>();
            for (Stamp stamp : visible.entries) {
                Element element = stamp.element;
                elements.add(element);
                if (!ids.add(element.id)) {
                    issues.add(new Issue("duplicate_id", element.id));
                }
                if (element.bounds.width() < 0 || element.bounds.height() < 0) {
                    issues.add(new Issue("invalid_bounds", element.id));
                }
            }
        }
        GuiGraphics graphics = event.getGuiGraphics();
        if (State.requested) {
            State.requested = false;
            capture(graphics, layer, List.copyOf(elements), List.copyOf(issues));
        } else if (State.overlay) {
            annotate(graphics, elements, issues);
        }
        State.layers.clear();
    }

    private static void collectWidgets(Layer layer, List<? extends GuiEventListener> children, String path,
                                       @Nullable ScreenRectangle clip, boolean parentVisible, Set<GuiEventListener> seen) {
        for (int i = 0; i < children.size(); i++) {
            GuiEventListener child = children.get(i);
            if (!seen.add(child)) {
                continue;
            }
            String childPath = path.isEmpty() ? Integer.toString(i) : path + "/" + i;
            ScreenRectangle childClip = clip;
            boolean visible = parentVisible;
            if (child instanceof AbstractWidget widget) {
                String name = State.names.get(widget);
                ScreenRectangle bounds = widget.getRectangle();
                visible &= widget.visible;
                long identity = State.widgetIds.computeIfAbsent(widget, ignored -> ++State.nextWidget);
                layer.add(name == null ? "widget/" + childPath : name, layer.root(), "widget", name != null,
                        bounds, clip, bounds, visible, widget.active, widget.isFocused(), identity);
                if (child instanceof AbstractSelectionList<?>) {
                    childClip = clip == null ? bounds : intersect(clip, bounds);
                }
            }
            if (child instanceof ContainerEventHandler container) {
                collectWidgets(layer, container.children(), childPath, childClip, visible, seen);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!ENABLED || event.getScreen() != Minecraft.getInstance().screen) {
            return;
        }
        int modifiers = event.getModifiers() & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_SUPER);
        if (modifiers != (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) {
            return;
        }
        if (event.getKeyCode() == GLFW.GLFW_KEY_F8) {
            State.overlay = !State.overlay;
            event.setCanceled(true);
        } else if (event.getKeyCode() == GLFW.GLFW_KEY_F9) {
            if (State.writing || State.requested) {
                notifyUser("UI inspection export in progress");
            } else {
                State.requested = true;
            }
            event.setCanceled(true);
        }
    }

    private static boolean positive(ScreenRectangle bounds) {
        return bounds.width() > 0 && bounds.height() > 0;
    }

    private static ScreenRectangle intersect(ScreenRectangle a, ScreenRectangle b) {
        ScreenRectangle result = positive(a) && positive(b) ? a.intersection(b) : null;
        return result == null ? ScreenRectangle.empty() : result;
    }

    private static void annotate(GuiGraphics graphics, List<Element> elements, List<Issue> issues) {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        double mouseX = windowToGui(minecraft.mouseHandler.xpos(), window.getGuiScaledWidth(), window.getScreenWidth());
        double mouseY = windowToGui(minecraft.mouseHandler.ypos(), window.getGuiScaledHeight(), window.getScreenHeight());
        Element hovered = null;
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 9000);
        for (Element element : elements) {
            if (!element.visible) {
                continue;
            }
            outline(graphics, element.bounds, element.inputBlocked ? 0xFF888888 : 0xFF55FF55);
            if (element.clipBounds != null) {
                outline(graphics, element.clipBounds, 0xFF55FFFF);
            }
            outline(graphics, element.hitBounds, 0xFFFFFF55);
            if (!element.inputBlocked && !element.parentId.isEmpty()
                    && PortableLayout.contains(visibleHit(element, window.getGuiScaledWidth(), window.getGuiScaledHeight()), mouseX, mouseY)
                    && (hovered == null || element.layer > hovered.layer
                    || element.bounds.width() * (long) element.bounds.height() <= hovered.bounds.width() * (long) hovered.bounds.height())) {
                hovered = element;
            }
        }
        List<String> lines = new ArrayList<>();
        lines.add("GUI " + String.format(java.util.Locale.ROOT, "%.2f, %.2f", mouseX, mouseY)
                + " | frame " + State.frameId + " revision " + State.layoutRevision);
        lines.add("bounds green / clip cyan / hit yellow / blocked gray | issues " + issues.size());
        if (hovered != null) {
            lines.add(hovered.id);
            lines.add("bounds " + describe(hovered.bounds) + " clip " + describe(hovered.clipBounds));
            lines.add("hit " + describe(hovered.hitBounds) + " visible=" + hovered.visible
                    + " enabled=" + hovered.enabled + " focused=" + hovered.focused + " blocked=" + hovered.inputBlocked);
        }
        int y = Math.max(0, window.getGuiScaledHeight() - lines.size() * 10 - 4);
        graphics.fill(0, y, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0xDD000000);
        for (String line : lines) {
            graphics.drawString(minecraft.font, minecraft.font.plainSubstrByWidth(line, Math.max(0, window.getGuiScaledWidth() - 4)), 2, y + 2, 0xFFFFFFFF, false);
            y += 10;
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    private static String describe(@Nullable ScreenRectangle bounds) {
        return bounds == null ? "none" : bounds.left() + "," + bounds.top() + " " + bounds.width() + "x" + bounds.height();
    }

    private static void outline(GuiGraphics graphics, ScreenRectangle bounds, int color) {
        if (positive(bounds)) {
            graphics.renderOutline(bounds.left(), bounds.top(), bounds.width(), bounds.height(), color);
        }
    }

    private record WindowInfo(int guiWidth, int guiHeight, double guiScale, int windowWidth, int windowHeight,
                              int framebufferWidth, int framebufferHeight) {
        private static WindowInfo capture(Window window) {
            return new WindowInfo(window.getGuiScaledWidth(), window.getGuiScaledHeight(), window.getGuiScale(),
                    window.getScreenWidth(), window.getScreenHeight(), window.getWidth(), window.getHeight());
        }
    }

    private record Export(String screenId, long screenInstance, long layoutRevision, long frameId, WindowInfo window,
                          String plain, String annotated, boolean pixelMappingValid, List<Element> elements, List<Issue> issues) {
    }

    private static void capture(GuiGraphics graphics, Layer top, List<Element> elements, List<Issue> issues) {
        Minecraft minecraft = Minecraft.getInstance();
        NativeImage plain = null;
        NativeImage annotated = null;
        try {
            graphics.flush();
            plain = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
            annotate(graphics, elements, issues);
            graphics.flush();
            annotated = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
            WindowInfo window = WindowInfo.capture(minecraft.getWindow());
            String prefix = "inspection-" + State.session + "-" + ++State.exportSequence;
            boolean valid = plain.getWidth() == window.framebufferWidth && plain.getHeight() == window.framebufferHeight
                    && annotated.getWidth() == window.framebufferWidth && annotated.getHeight() == window.framebufferHeight;
            Export export = new Export(top.screen.getClass().getName(), top.state.instance, State.layoutRevision, State.frameId,
                    window, prefix + "-plain.png", prefix + "-annotated.png", valid, elements, issues);
            Path directory = Path.of(System.getProperty("avaritia.ui.inspectOutput", "build/ui-inspection")).toAbsolutePath();
            NativeImage plainImage = plain;
            NativeImage annotatedImage = annotated;
            State.writing = true;
            Util.ioPool().execute(() -> writeExport(directory, prefix, export, plainImage, annotatedImage));
            plain = null;
            annotated = null;
        } catch (Exception error) {
            State.writing = false;
            failed(error);
        } finally {
            if (plain != null) {
                plain.close();
            }
            if (annotated != null) {
                annotated.close();
            }
        }
    }

    private static void writeExport(Path directory, String prefix, Export export, NativeImage plain, NativeImage annotated) {
        try (plain; annotated) {
            Files.createDirectories(directory);
            Files.write(directory.resolve(export.plain), plain.asByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Files.write(directory.resolve(export.annotated), annotated.asByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Path json = directory.resolve(prefix + ".json");
            try (Writer writer = Files.newBufferedWriter(json, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                serialize(export, writer);
            }
            Minecraft.getInstance().execute(() -> notifyUser("UI inspection exported: " + json));
        } catch (Exception error) {
            failed(error);
        } finally {
            Minecraft.getInstance().execute(() -> State.writing = false);
        }
    }

    private static void serialize(Export export, Writer output) throws IOException {
        JsonWriter writer = Const.GSON.newJsonWriter(output);
        writer.setSerializeNulls(true);
        Const.GSON.getAdapter(JsonObject.class).write(writer, toJson(export));
        writer.flush();
    }

    private static void failed(Exception error) {
        Const.LOGGER.error("UI inspection export failed", error);
        Minecraft.getInstance().execute(() -> notifyUser("UI inspection export failed"));
    }

    private static void notifyUser(String message) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(message), false);
        Const.LOGGER.info(message);
    }

    // Input scaling uses the OS client area; rendering and GL scissor use framebuffer scale instead.
    private static double windowToGui(double position, int guiSize, int windowSize) {
        return windowSize > 0 ? position * guiSize / windowSize : 0;
    }

    private static double guiToWindow(double position, int guiSize, int windowSize) {
        return guiSize > 0 ? position * windowSize / guiSize : 0;
    }

    private static ScreenRectangle visibleHit(Element element, int guiWidth, int guiHeight) {
        ScreenRectangle hit = intersect(element.hitBounds, new ScreenRectangle(0, 0, guiWidth, guiHeight));
        return element.clipBounds == null ? hit : intersect(hit, element.clipBounds);
    }

    private static JsonObject rectangle(ScreenRectangle bounds) {
        JsonObject json = new JsonObject();
        json.addProperty("x", bounds.left());
        json.addProperty("y", bounds.top());
        json.addProperty("width", bounds.width());
        json.addProperty("height", bounds.height());
        return json;
    }

    private static JsonObject framebufferBounds(ScreenRectangle bounds, WindowInfo window) {
        JsonObject json = new JsonObject();
        json.addProperty("x", bounds.left() * window.guiScale);
        json.addProperty("y", bounds.top() * window.guiScale);
        json.addProperty("width", bounds.width() * window.guiScale);
        json.addProperty("height", bounds.height() * window.guiScale);
        return json;
    }

    private static JsonObject scissorPixels(ScreenRectangle bounds, WindowInfo window) {
        return rectangle(new ScreenRectangle((int) (bounds.left() * window.guiScale),
                (int) (window.framebufferHeight - bounds.bottom() * window.guiScale),
                Math.max(0, (int) (bounds.width() * window.guiScale)), Math.max(0, (int) (bounds.height() * window.guiScale))));
    }

    private static JsonObject toJson(Export export) {
        JsonObject json = new JsonObject();
        json.addProperty("schemaVersion", 1);
        json.addProperty("screenId", export.screenId);
        json.addProperty("screenInstance", export.screenInstance);
        json.addProperty("layoutRevision", export.layoutRevision);
        json.addProperty("frameId", export.frameId);
        json.add("window", Const.GSON.toJsonTree(export.window));
        JsonObject images = new JsonObject();
        images.addProperty("plain", export.plain);
        images.addProperty("annotated", export.annotated);
        json.add("images", images);
        json.addProperty("pixelMappingValid", export.pixelMappingValid);
        JsonArray elements = new JsonArray();
        for (Element element : export.elements) {
            JsonObject node = new JsonObject();
            node.addProperty("id", element.id);
            node.addProperty("parentId", element.parentId);
            node.addProperty("screenInstance", element.screenInstance);
            if (element.containerId == null) {
                node.add("containerId", JsonNull.INSTANCE);
            } else {
                node.addProperty("containerId", element.containerId);
            }
            node.addProperty("source", element.source);
            node.addProperty("stableId", element.stableId);
            node.add("bounds", rectangle(element.bounds));
            node.add("clipBounds", element.clipBounds == null ? JsonNull.INSTANCE : rectangle(element.clipBounds));
            node.add("hitBounds", rectangle(element.hitBounds));
            node.add("visibleBounds", rectangle(element.visibleBounds));
            node.addProperty("layer", element.layer);
            node.addProperty("visible", element.visible);
            node.addProperty("enabled", element.enabled);
            node.addProperty("focused", element.focused);
            node.addProperty("inputBlocked", element.inputBlocked);
            if (export.pixelMappingValid) {
                node.add("framebufferBounds", framebufferBounds(element.bounds, export.window));
                if (element.clipBounds != null) {
                    node.add("scissorPixels", scissorPixels(element.clipBounds, export.window));
                }
            }
            ScreenRectangle click = visibleHit(element, export.window.guiWidth, export.window.guiHeight);
            if (export.issues.isEmpty() && element.visible && element.enabled && !element.inputBlocked && positive(click)) {
                double x = click.left() + click.width() / 2.0;
                double y = click.top() + click.height() / 2.0;
                JsonObject suggestion = new JsonObject();
                suggestion.addProperty("guiX", x);
                suggestion.addProperty("guiY", y);
                suggestion.addProperty("windowX", guiToWindow(x, export.window.guiWidth, export.window.windowWidth));
                suggestion.addProperty("windowY", guiToWindow(y, export.window.guiHeight, export.window.windowHeight));
                node.add("suggestedClick", suggestion);
            }
            elements.add(node);
        }
        json.add("elements", elements);
        json.add("issues", Const.GSON.toJsonTree(export.issues));
        return json;
    }
}
