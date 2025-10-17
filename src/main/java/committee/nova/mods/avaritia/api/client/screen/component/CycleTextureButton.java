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
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);;
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
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int xDiffTex;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;
    private final int xOffset;
    private final int yOffset;
    private final int usedTextureWidth;
    private final int usedTextureHeight;

    CycleTextureButton(
                       Component message, Component name,
                       int index, T value, ValueListSupplier<T> values, Function<T, Component> valueStringifier,
                       Function<CycleTextureButton<T>, MutableComponent> narrationProvider,
                       OnValueChange<T> onValueChange,
                       OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean displayOnlyValue,
                       ResourceLocation resourceLocation,
                       int x, int y, int width, int height,
                       int xTexStart, int yTexStart, int xOffset, int yOffset,
                       int textureWidth, int textureHeight, int xDiffTex, int yDiffTex
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

        this.resourceLocation = resourceLocation;
        this.usedTextureWidth = width;
        this.usedTextureHeight = height;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xDiffTex = xDiffTex;
        this.yDiffTex = yDiffTex;
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

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
        T t = list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int delta) {
        List<T> list = this.values.getSelectedList();
        return (T)list.get(Mth.positiveModulo(this.index + delta, list.size()));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.enableDepthTest();
        if (this.isHoveredOrFocused()) {
            guiGraphics.blit(this.resourceLocation, this.getXOffset(), this.getYOffset(), (float)this.xTexStart + this.xDiffTex * this.index, this.yTexStart + this.yDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
        } else guiGraphics.blit(this.resourceLocation, this.getXOffset(), this.getYOffset(), (float)this.xTexStart + this.xDiffTex * this.index, this.yTexStart, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
    }

    private int getXOffset() {
        return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
    }

    private int getYOffset() {
        return this.getY() + (this.height / 2 - this.usedTextureHeight / 2) + this.yOffset;
    }

    @Override
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
        return this.displayOnlyValue ? this.valueStringifier.apply(value) : this.createFullName(value);
    }

    private MutableComponent createFullName(T value) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(value));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
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

    public static <T> Builder<T> builder(Function<T, Component> valueStringifier) {
        return new Builder<T>(valueStringifier);
    }

    public static Builder<Boolean> booleanBuilder(Component componentOn, Component componentOff) {
        return (new Builder((o) -> (boolean) o ? componentOn : componentOff)).withValues(BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder() {
        return (new Builder((o) -> (boolean)o ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)).withValues(BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder(boolean initialValue) {
        return onOffBuilder().withInitialValue(initialValue);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = (p_168964_) -> null;
        private Function<CycleTextureButton<T>, MutableComponent> narrationProvider = CycleTextureButton::createDefaultNarrationMessage;
        private ValueListSupplier<T> values = ValueListSupplier.<T>create(ImmutableList.of());
        private boolean displayOnlyValue;

        private int x = 0;
        private int y = 0;
        private int width = 20;
        private int height = 20;
        private int xTexStart;
        private int yTexStart;
        private int xDiffTex;
        private int yDiffTex;
        private int textureWidth;
        private int textureHeight;
        private int xOffset;
        private int yOffset;

        public Builder(Function<T, Component> valueStringifier) {
            this.valueStringifier = valueStringifier;
        }

        public Builder<T> withValues(Collection<T> values) {
            return this.withValues(ValueListSupplier.create(values));
        }

        @SafeVarargs
        public final Builder<T> withValues(T... values) {
            return this.withValues(ImmutableList.copyOf(values));
        }

        public Builder<T> withValues(List<T> defaultList, List<T> selectedList) {
            return this.withValues(ValueListSupplier.create(DEFAULT_ALT_LIST_SELECTOR, defaultList, selectedList));
        }

        public Builder<T> withValues(BooleanSupplier altListSelector, List<T> defaultList, List<T> selectedList) {
            return this.withValues(ValueListSupplier.create(altListSelector, defaultList, selectedList));
        }

        public Builder<T> withValues(ValueListSupplier<T> values) {
            this.values = values;
            return this;
        }

        public Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public Builder<T> withInitialValue(T initialValue) {
            this.initialValue = initialValue;
            int i = this.values.getDefaultList().indexOf(initialValue);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public Builder<T> withCustomNarration(Function<CycleTextureButton<T>, MutableComponent> narrationProvider) {
            this.narrationProvider = narrationProvider;
            return this;
        }

        public Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public Builder<T> bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
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

        public Builder<T> xDiffTex(int xDiffTex) {
            this.xDiffTex = xDiffTex;
            return this;
        }

        public Builder<T> yDiffTex(int yDiffTex) {
            this.yDiffTex = yDiffTex;
            return this;
        }

        public Builder<T> textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public CycleTextureButton<T> create(ResourceLocation resourceLocation, Component name) {
            return this.create(resourceLocation, name, (p_168946_, p_168947_) -> {
            });
        }

        public CycleTextureButton<T> create(ResourceLocation resourceLocation, Component name, OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T t = this.initialValue != null ? this.initialValue : list.get(this.initialIndex);
                Component component = this.valueStringifier.apply(t);
                Component component1 = this.displayOnlyValue ? component : CommonComponents.optionNameValue(name, component);
                return new CycleTextureButton<T>(component1, name, this.initialIndex, t, this.values, this.valueStringifier, this.narrationProvider, onValueChange, this.tooltipSupplier, this.displayOnlyValue,
                        resourceLocation, x, y, width, height, this.xTexStart, this.yTexStart, this.xOffset, this.yOffset, this.textureWidth, this.textureHeight, this.xDiffTex, this.yDiffTex
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

}
