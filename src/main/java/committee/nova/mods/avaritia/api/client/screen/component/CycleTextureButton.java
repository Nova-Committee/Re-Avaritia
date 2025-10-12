package committee.nova.mods.avaritia.api.client.screen.component;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * @author cnlimiter
 */
@OnlyIn(Dist.CLIENT)
public class CycleTextureButton<T> extends AbstractButton {
    public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private final Component name;
    private int index;
    private T value;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleTextureButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final boolean displayOnlyValue;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

    protected final ResourceLocation resourceLocation;
    protected final int yDiffTex;
    protected final int xDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;
    private final int xOffset;
    private final int yOffset;
    private final int usedTextureWidth;
    private final int usedTextureHeight;

    private final List<T> textureCoordinates;
    private final Function<T, TextureCoordinate> textureMapper;

    CycleTextureButton(int x, int y, int width, int height, Component message, Component name,
                       int index, T value, ValueListSupplier<T> values, Function<T, Component> valueStringifier,
                       Function<CycleTextureButton<T>, MutableComponent> narrationProvider,
                       CycleTextureButton.OnValueChange<T> onValueChange,
                       OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean displayOnlyValue,
                       ResourceLocation resourceLocation, int xOffset, int yOffset,
                       int xDiffTex, int yDiffTex, int usedTextureWidth, int usedTextureHeight,
                       int textureWidth, int textureHeight, List<T> textureCoordinates, Function<T, TextureCoordinate> textureMapper
                       
                       ) {
        super(x, y, width, height, message);
        this.name = name;
        this.index = index;
        this.value = value;
        this.values = values;
        this.valueStringifier = valueStringifier;
        this.narrationProvider = narrationProvider;
        this.onValueChange = onValueChange;
        this.displayOnlyValue = displayOnlyValue;
        this.tooltipSupplier = tooltipSupplier;
        this.updateTooltip();

        this.textureCoordinates = textureCoordinates;
        this.textureMapper = textureMapper;
        this.resourceLocation = resourceLocation;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xDiffTex = xDiffTex;
        this.yDiffTex = yDiffTex;
        this.usedTextureWidth = usedTextureWidth;
        this.usedTextureHeight = usedTextureHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }



    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染纹理
        if (!textureCoordinates.isEmpty() && textureMapper != null) {
            T textureValue = textureCoordinates.get(index);
            TextureCoordinate coord = textureMapper.apply(textureValue);
            this.renderTexture(guiGraphics, this.resourceLocation, this.getXOffset(), this.getYOffset(),
                    coord.xTexStart, coord.yTexStart, this.xDiffTex, this.yDiffTex,
                    this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
        }

        // 再调用父类渲染（如需要显示文本）
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y,
                              int uOffset, int vOffset, int textureXDifference, int textureYDifference,
                              int width, int height, int textureWidth, int textureHeight) {
        int v = vOffset;
        int u = uOffset;
        RenderSystem.enableDepthTest();
        if (textureYDifference != 0){
            if (!this.isActive()) {
                v = vOffset + textureYDifference * 2;
            } else if (this.isHoveredOrFocused()) {
                v = vOffset + textureYDifference;
            }
        }

        if (textureXDifference != 0) {
            if (!this.isActive()) {
                u = uOffset + textureXDifference * 2;
            } else if (this.isHoveredOrFocused()) {
                u = uOffset + textureXDifference;
            }
        }
        guiGraphics.blit(texture, x, y, (float)u, (float)v, width, height, textureWidth, textureHeight);
    }

    private int getXOffset() {
        return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
    }

    private int getYOffset() {
        return this.getY() + this.yOffset;
    }
    
    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }

    }

    private void cycleValue(int delta) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + delta, list.size());
        T t = (T)list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int delta) {
        List<T> list = this.values.getSelectedList();
        return (T)list.get(Mth.positiveModulo(this.index + delta, list.size()));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > (double)0.0F) {
            this.cycleValue(-1);
        } else if (delta < (double)0.0F) {
            this.cycleValue(1);
        }

        return true;
    }

    public void setValue(T value) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(value);
        if (i != -1) {
            this.index = i;
        }

        this.updateValue(value);
    }

    private void updateValue(T value) {
        Component component = this.createLabelForValue(value);
        this.setMessage(component);
        this.value = value;
        this.updateTooltip();
    }

    private Component createLabelForValue(T value) {
        return (Component)(this.displayOnlyValue ? (Component)this.valueStringifier.apply(value) : this.createFullName(value));
    }

    private MutableComponent createFullName(T value) {
        return CommonComponents.optionNameValue(this.name, (Component)this.valueStringifier.apply(value));
    }

    public T getValue() {
        return this.value;
    }

    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            T t = (T)this.getCycledValue(1);
            Component component = this.createLabelForValue(t);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }

    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
    }

    public static <T> CycleTextureButton.Builder<T> builder(Function<T, Component> valueStringifier) {
        return new CycleTextureButton.Builder<T>(valueStringifier);
    }
    

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = (p_168964_) -> null;
        private Function<CycleTextureButton<T>, MutableComponent> narrationProvider = CycleTextureButton::createDefaultNarrationMessage;
        private CycleTextureButton.ValueListSupplier<T> values = CycleTextureButton.ValueListSupplier.<T>create(ImmutableList.of());
        private boolean displayOnlyValue;

        private List<T> textureCoordinates = java.util.List.of();
        private Function<T, TextureCoordinate> textureMapper;
        private int xTexStart = 0;
        private int yTexStart = 0;
        private int xDiffTex = 0;
        private int yDiffTex = 0;
        private int usedTextureWidth = 16;
        private int usedTextureHeight = 16;
        private int textureWidth = 256;
        private int textureHeight = 256;
        private int xOffset = 0;
        private int yOffset = 0;
        private ResourceLocation resourceLocation;
        private OnValueChange<T> onValueChange = (button, value) -> {};

        public Builder(Function<T, Component> valueStringifier) {
            this.valueStringifier = valueStringifier;
        }

        public CycleTextureButton.Builder<T> withValues(Collection<T> values) {
            return this.withValues(CycleTextureButton.ValueListSupplier.create(values));
        }

        @SafeVarargs
        public final CycleTextureButton.Builder<T> withValues(T... values) {
            return this.withValues(ImmutableList.copyOf(values));
        }

        public CycleTextureButton.Builder<T> withValues(List<T> defaultList, List<T> selectedList) {
            return this.withValues(CycleTextureButton.ValueListSupplier.create(CycleTextureButton.DEFAULT_ALT_LIST_SELECTOR, defaultList, selectedList));
        }

        public CycleTextureButton.Builder<T> withValues(BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
            return this.withValues(CycleTextureButton.ValueListSupplier.create(altListSelector, defaultList, selectedList));
        }

        public CycleTextureButton.Builder<T> withValues(CycleTextureButton.ValueListSupplier<T> values) {
            this.values = values;
            return this;
        }

        public CycleTextureButton.Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public CycleTextureButton.Builder<T> withInitialValue(T initialValue) {
            this.initialValue = initialValue;
            int i = this.values.getDefaultList().indexOf(initialValue);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public CycleTextureButton.Builder<T> withCustomNarration(Function<CycleTextureButton<T>, MutableComponent> narrationProvider) {
            this.narrationProvider = narrationProvider;
            return this;
        }

        public CycleTextureButton.Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public Builder<T> withTextureCoordinates(List<T> textureCoordinates, Function<T, TextureCoordinate> textureMapper) {
            this.textureCoordinates = textureCoordinates;
            this.textureMapper = textureMapper;
            return this;
        }

        public Builder<T> texStart(int x, int y) {
            this.xTexStart = x;
            this.yTexStart = y;
            return this;
        }

        public Builder<T> offset(int x, int y) {
            this.xOffset = x;
            this.yOffset = y;
            return this;
        }

        public Builder<T> yDiffTex(int yDiffTex) {
            this.yDiffTex = yDiffTex;
            return this;
        }

        public Builder<T> xDiffTex(int xDiffTex) {
            this.xDiffTex = xDiffTex;
            return this;
        }

        public Builder<T> usedTextureSize(int width, int height) {
            this.usedTextureWidth = width;
            this.usedTextureHeight = height;
            return this;
        }

        public Builder<T> textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public Builder<T> texture(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
            return this;
        }

        public Builder<T> withValueChangeCallback(OnValueChange<T> onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public CycleTextureButton<T> create(int x, int y, int width, int height, Component name) {
            return this.create(x, y, width, height, name, (p_168946_, p_168947_) -> {
            });
        }

        public CycleTextureButton<T> create(int x, int y, int width, int height, Component name, CycleTextureButton.OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T t = (T)(this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
                Component component = this.valueStringifier.apply(t);
                Component component1 = this.displayOnlyValue ? component : CommonComponents.optionNameValue(name, component);
                return new CycleTextureButton<T>(x, y, width, height, component1, name, this.initialIndex, t, this.values, this.valueStringifier, this.narrationProvider, onValueChange, this.tooltipSupplier, this.displayOnlyValue,
                        this.resourceLocation, this.xOffset, this.yOffset,
                        this.xDiffTex, this.yDiffTex,
                        this.usedTextureWidth, this.usedTextureHeight,
                        this.textureWidth, this.textureHeight,
                        this.textureCoordinates, this.textureMapper
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface ValueListSupplier<T> {
        List<T> getSelectedList();

        List<T> getDefaultList();

        static <T> ValueListSupplier<T> create(Collection<T> values) {
            final List<T> list = ImmutableList.copyOf(values);
            return new ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }

        static <T> ValueListSupplier<T> create(final BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
            final List<T> list = ImmutableList.copyOf(defaultList);
            final List<T> list1 = ImmutableList.copyOf(selectedList);
            return new ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return altListSelector.getAsBoolean() ? list1 : list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange<T> {
        void onValueChange(CycleTextureButton<T> var1, T var2);
    }

    public record TextureCoordinate(int xTexStart, int yTexStart) {
    }
}
