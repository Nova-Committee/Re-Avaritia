package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
每次添加配置的时候都需要在initConfigEntries添加一个配置项,然后在resetToDefaults中添加一个默认值
关于默认值的自动化我没太搞懂...
 */
public class AvaritiaConfigScreen extends Screen {
    private final Screen parent;
    private final List<ConfigEntry<?>> configEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 40;
    private static final int MARGIN = 20;
    private static final int START_Y = 50;
    private Button resetButton;
    private Button backButton;

    // 滚动条变量
    private boolean isDraggingScrollbar = false;
    private int scrollbarX;
    private int scrollbarY;
    private int scrollbarHeight;
    private int scrollbarHandleHeight;
    private int maxScrollOffset;
    private double scrollVelocity = 0.0;
    private long lastScrollTime = 0;
    private static final double FRICTION = 0.92;
    private static final double MIN_VELOCITY = 0.1;

    public AvaritiaConfigScreen(Screen parent) {
        super(Component.translatable("title.avaritia.config.title"));
        this.parent = parent;
        initConfigEntries();
    }

    private void initConfigEntries() {
        addCategoryHeader("config.avaritia.category.tools");

        addBooleanEntry("is_merge_matter_cluster", ModConfig.isMergeMatterCluster,
                Component.translatable("config.avaritia.is_merge_matter_cluster.tooltip"),
                ModConfig.isMergeMatterCluster::set, ModConfig.isMergeMatterCluster);
        addIntEntry("sword_range_damage", ModConfig.swordRangeDamage, 100, 100000,
                Component.translatable("config.avaritia.sword_range_damage.tooltip"),
                ModConfig.swordRangeDamage::set, ModConfig.swordRangeDamage);
        addIntEntry("sword_attack_range", ModConfig.swordAttackRange, 8, 64,
                Component.translatable("config.avaritia.sword_attack_range.tooltip"),
                ModConfig.swordAttackRange::set, ModConfig.swordAttackRange);
        addBooleanEntry("is_sword_attack_item_entity", ModConfig.isSwordAttackItemEntity,
                Component.translatable("config.avaritia.is_sword_attack_item_entity.tooltip"),
                ModConfig.isSwordAttackItemEntity::set, ModConfig.isSwordAttackItemEntity);
        addBooleanEntry("is_sword_attack_projectile", ModConfig.isSwordAttackProjectile,
                Component.translatable("config.avaritia.is_sword_attack_projectile.tooltip"),
                ModConfig.isSwordAttackProjectile::set, ModConfig.isSwordAttackProjectile);
        addBooleanEntry("is_sword_attack_lightning", ModConfig.isSwordAttackLightning,
                Component.translatable("config.avaritia.is_sword_attack_lightning.tooltip"),
                ModConfig.isSwordAttackLightning::set, ModConfig.isSwordAttackLightning);
        addBooleanEntry("is_sword_attack_endless", ModConfig.isSwordAttackEndless,
                Component.translatable("config.avaritia.is_sword_attack_endless.tooltip"),
                ModConfig.isSwordAttackEndless::set, ModConfig.isSwordAttackEndless);
        addIntEntry("sub_arrow_damage", ModConfig.subArrowDamage, 100, 100000,
                Component.translatable("config.avaritia.sub_arrow_damage.tooltip"),
                ModConfig.subArrowDamage::set, ModConfig.subArrowDamage);
        addIntEntry("axe_chain_count", ModConfig.axeChainCount, 16, 128,
                Component.translatable("config.avaritia.axe_chain_count.tooltip"),
                ModConfig.axeChainCount::set, ModConfig.axeChainCount);
        addIntEntry("pickaxe_break_range", ModConfig.pickAxeBreakRange, 2, 32,
                Component.translatable("config.avaritia.pickaxe_break_range.tooltip"),
                ModConfig.pickAxeBreakRange::set, ModConfig.pickAxeBreakRange);
        addIntEntry("shovel_break_range", ModConfig.shovelBreakRange, 2, 32,
                Component.translatable("config.avaritia.shovel_break_range.tooltip"),
                ModConfig.shovelBreakRange::set, ModConfig.shovelBreakRange);
        addIntEntry("singularity_time_required", ModConfig.singularityTimeRequired, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.singularity_time_required.tooltip"),
                ModConfig.singularityTimeRequired::set, ModConfig.singularityTimeRequired);
        addDoubleEntry("growth_soul_farmland", ModConfig.growthSoulFarmland, 0.0, 1.0,
                Component.translatable("config.avaritia.growth_soul_farmland.tooltip"),
                ModConfig.growthSoulFarmland::set, ModConfig.growthSoulFarmland);
        addIntEntry("blade_slash_damage", ModConfig.bladeSlashDamage, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.blade_slash_damage.tooltip"),
                ModConfig.bladeSlashDamage::set, ModConfig.bladeSlashDamage);
        addIntEntry("blade_slash_radius", ModConfig.bladeSlashRadius, 5, 100,
                Component.translatable("config.avaritia.blade_slash_radius.tooltip"),
                ModConfig.bladeSlashRadius::set, ModConfig.bladeSlashRadius);
        addIntEntry("endest_pearl_absorption_limit", ModConfig.endestPearlAbsorptionLimit, 16, 4096,
                Component.translatable("config.avaritia.endest_pearl_absorption_limit.tooltip"),
                ModConfig.endestPearlAbsorptionLimit::set, ModConfig.endestPearlAbsorptionLimit);

        addCategoryHeader("config.avaritia.category.emc");
        addIntEntry("neutron_pile_emc", ModConfig.neutronPileEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_pile_emc.tooltip"),
                ModConfig.neutronPileEmc::set, ModConfig.neutronPileEmc);
        addIntEntry("blaze_cube_emc", ModConfig.blazeCubeEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.blaze_cube_emc.tooltip"),
                ModConfig.blazeCubeEmc::set, ModConfig.blazeCubeEmc);
        addIntEntry("vanilla_totem_emc", ModConfig.vanillaTotemEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.vanilla_totem_emc.tooltip"),
                ModConfig.vanillaTotemEmc::set, ModConfig.vanillaTotemEmc);
        addBooleanEntry("enable_projecte_singularity_count_boost", ModConfig.enableProjectESingularityCountBoost,
                Component.translatable("config.avaritia.enable_projecte_singularity_count_boost.tooltip"),
                ModConfig.enableProjectESingularityCountBoost::set, ModConfig.enableProjectESingularityCountBoost);

        addCategoryHeader("config.avaritia.category.channel");
        addIntEntry("max_channels_pre_player", ModConfig.MAX_CHANNELS_PRE_PLAYER, 4, 64,
                Component.translatable("config.avaritia.max_channels_pre_player.tooltip"),
                ModConfig.MAX_CHANNELS_PRE_PLAYER::set, ModConfig.MAX_CHANNELS_PRE_PLAYER);
        addIntEntry("max_public_channels", ModConfig.MAX_PUBLIC_CHANNELS, 32, 1024,
                Component.translatable("config.avaritia.max_public_channels.tooltip"),
                ModConfig.MAX_PUBLIC_CHANNELS::set, ModConfig.MAX_PUBLIC_CHANNELS);
        addIntEntry("channel_fast_update_rate", ModConfig.CHANNEL_FAST_UPDATE_RATE, 1, 40,
                Component.translatable("config.avaritia.channel_fast_update_rate.tooltip"),
                ModConfig.CHANNEL_FAST_UPDATE_RATE::set, ModConfig.CHANNEL_FAST_UPDATE_RATE);
        addIntEntry("channel_full_update_rate", ModConfig.CHANNEL_FULL_UPDATE_RATE, 20, 1200,
                Component.translatable("config.avaritia.channel_full_update_rate.tooltip"),
                ModConfig.CHANNEL_FULL_UPDATE_RATE::set, ModConfig.CHANNEL_FULL_UPDATE_RATE);

        addCategoryHeader("config.avaritia.category.misc");
        addBooleanEntry("use_advance_tooltips", ModConfig.useAdvanceTooltips,
                Component.translatable("config.avaritia.use_advance_tooltips.tooltip"),
                ModConfig.useAdvanceTooltips::set, ModConfig.useAdvanceTooltips);
        addDoubleEntry("endless_item_entity_speed", ModConfig.immortalItemEntitySpeed, 1.0, 50.0,
                Component.translatable("config.avaritia.endless_item_entity_speed.tooltip"),
                ModConfig.immortalItemEntitySpeed::set, ModConfig.immortalItemEntitySpeed);
        addDoubleEntry("endless_item_entity_range", ModConfig.immortalItemEntityRange, 1.0, 10000.0,
                Component.translatable("config.avaritia.endless_item_entity_range.tooltip"),
                ModConfig.immortalItemEntityRange::set, ModConfig.immortalItemEntityRange);
        addDoubleEntry("infinity_elytra_flying_speed", ModConfig.infinityElytraFlyingSpeed, 0.0, 100.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_speed.tooltip"),
                ModConfig.infinityElytraFlyingSpeed::set, ModConfig.infinityElytraFlyingSpeed);
        addDoubleEntry("infinity_elytra_flying_damage_range", ModConfig.infinityElytraFlyingRangeDamage, 0.0, 10000.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_damage_range.tooltip"),
                ModConfig.infinityElytraFlyingRangeDamage::set, ModConfig.infinityElytraFlyingRangeDamage);
        addBooleanEntry("infinity_helmet_night_vision", ModConfig.InfinityArmorNightVision,
                Component.translatable("config.avaritia.infinity_helmet_night_vision.tooltip"),
                ModConfig.InfinityArmorNightVision::set, ModConfig.InfinityArmorNightVision);
        addDoubleEntry("boot_speed_base", ModConfig.bootSpeedBase, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_base.tooltip"),
                ModConfig.bootSpeedBase::set, ModConfig.bootSpeedBase);
        addDoubleEntry("boot_speed_flying_multiplier", ModConfig.bootSpeedFlyingMultiplier, 0.1, 5.0,
                Component.translatable("config.avaritia.boot_speed_flying_multiplier.tooltip"),
                ModConfig.bootSpeedFlyingMultiplier::set, ModConfig.bootSpeedFlyingMultiplier);
        addDoubleEntry("boot_speed_swimming_multiplier", ModConfig.bootSpeedSwimmingMultiplier, 0.1, 5.0,
                Component.translatable("config.avaritia.boot_speed_swimming_multiplier.tooltip"),
                ModConfig.bootSpeedSwimmingMultiplier::set, ModConfig.bootSpeedSwimmingMultiplier);
        addDoubleEntry("boot_speed_sneaking_multiplier", ModConfig.bootSpeedSneakingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_sneaking_multiplier.tooltip"),
                ModConfig.bootSpeedSneakingMultiplier::set, ModConfig.bootSpeedSneakingMultiplier);
        addDoubleEntry("boot_speed_backward_multiplier", ModConfig.bootSpeedBackwardMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_backward_multiplier.tooltip"),
                ModConfig.bootSpeedBackwardMultiplier::set, ModConfig.bootSpeedBackwardMultiplier);
        addDoubleEntry("boot_speed_strafing_multiplier", ModConfig.bootSpeedStrafingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_strafing_multiplier.tooltip"),
                ModConfig.bootSpeedStrafingMultiplier::set, ModConfig.bootSpeedStrafingMultiplier);
        addDoubleEntry("boot_speed_sprinting_multiplier", ModConfig.bootSpeedSprintingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_sprinting_multiplier.tooltip"),
                ModConfig.bootSpeedSprintingMultiplier::set, ModConfig.bootSpeedSprintingMultiplier);
    }

    private void resetToDefaults() {
        for (ConfigEntry<?> entry : configEntries) {
            entry.resetToDefault();
        }
    }

    private void addBooleanEntry(String titleKey, ModConfigSpec.BooleanValue configValue,
                                 Component description, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier) {
        configEntries.add(new BooleanConfigEntry(Component.translatable("config.avaritia." + titleKey), description, configValue.get(), onValueChange, valueSupplier, configValue));
    }

    private void addIntEntry(String titleKey, ModConfigSpec.IntValue configValue, int min, int max,
                             Component description, Consumer<Integer> onValueChange, Supplier<Integer> valueSupplier) {
        configEntries.add(new NumberConfigEntry<>(Component.translatable("config.avaritia." + titleKey), description, configValue.get(), min, max, onValueChange, valueSupplier, configValue, Integer::parseInt));
    }

    private void addDoubleEntry(String titleKey, ModConfigSpec.DoubleValue configValue, double min, double max,
                                Component description, Consumer<Double> onValueChange, Supplier<Double> valueSupplier) {
        configEntries.add(new NumberConfigEntry<>(Component.translatable("config.avaritia." + titleKey), description, configValue.get(), min, max, onValueChange, valueSupplier, configValue, Double::parseDouble));
    }

    private void addCategoryHeader(String translationKey) {
        configEntries.add(new CategoryHeaderEntry(Component.translatable(translationKey)));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        resetButton = addRenderableWidget(Button.builder(Component.translatable("controls.reset"), btn -> {
            resetToDefaults();
            updateWidgetValues();
            ModConfig.save();
        }).bounds(width / 2 - 102, height - 30, 100, 20).build());

        backButton = addRenderableWidget(Button.builder(Component.translatable("gui.back"), btn -> {
            ModConfig.save();
            minecraft.setScreen(parent);
        }).bounds(width / 2 + 2, height - 30, 100, 20).build());

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int x = MARGIN;
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y && y < height - 40) {
                entry.initWidgets(this, x, y, width - 2 * MARGIN);
            }
        }
    }

    private void updateWidgetValues() {
        for (ConfigEntry<?> entry : configEntries) {
            entry.updateWidgetValue();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (Math.abs(scrollVelocity) > MIN_VELOCITY) {
            int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset + scrollVelocity));
            scrollVelocity *= FRICTION;
            if (Math.abs(scrollVelocity) < MIN_VELOCITY) {
                scrollVelocity = 0;
            }
            init();
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y - 20 && y < height - 20) {
                entry.extract(graphics, mouseX, mouseY, MARGIN, y, width - 2 * MARGIN, ENTRY_HEIGHT, font);
            }
        }

        if (configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            maxScrollOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            int visibleHeight = height - START_Y - 40;
            scrollbarHeight = visibleHeight;
            scrollbarHandleHeight = Math.max(20, visibleHeight * visibleHeight / (configEntries.size() * ENTRY_HEIGHT));
            int scrollBarYOffset = maxScrollOffset == 0 ? 0 : (int) ((double) scrollOffset / maxScrollOffset * (visibleHeight - scrollbarHandleHeight));
            scrollbarY = START_Y + scrollBarYOffset;
            scrollbarX = width - 8;
            graphics.fill(scrollbarX, START_Y, scrollbarX + 4, START_Y + scrollbarHeight, 0x88888888);
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHandleHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        long currentTime = System.currentTimeMillis();
        double scrollDelta = -scrollY * ENTRY_HEIGHT / 8.0;
        if (currentTime - lastScrollTime < 200) {
            scrollVelocity += scrollDelta * 0.5;
        } else {
            scrollVelocity = scrollDelta;
        }
        lastScrollTime = currentTime;
        int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
        scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset + scrollDelta));
        init();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            if (event.x() >= scrollbarX && event.x() <= scrollbarX + 4 && event.y() >= scrollbarY && event.y() <= scrollbarY + scrollbarHandleHeight) {
                isDraggingScrollbar = true;
                return true;
            } else if (event.x() >= scrollbarX && event.x() <= scrollbarX + 4 && event.y() >= START_Y && event.y() <= START_Y + scrollbarHeight) {
                int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
                double clickPosition = (event.y() - START_Y) / scrollbarHeight;
                scrollOffset = (int) (clickPosition * maxOffset);
                scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
                init();
                isDraggingScrollbar = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDraggingScrollbar && configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            scrollVelocity = 0;
            int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            double positionRatio = (event.y() - START_Y - (double) scrollbarHandleHeight / 2) / (scrollbarHeight - scrollbarHandleHeight);
            scrollOffset = (int) (positionRatio * maxOffset);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
            init();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        ModConfig.save();
        minecraft.setScreen(parent);
    }

    private abstract static class ConfigEntry<T> {
        final Component title;
        final Component description;
        T currentValue;
        final Consumer<T> onValueChange;
        final Supplier<T> valueSupplier;
        final ModConfigSpec.ConfigValue<T> configValue;

        ConfigEntry(Component title, Component description, T initialValue, Consumer<T> onValueChange, Supplier<T> valueSupplier, ModConfigSpec.ConfigValue<T> configValue) {
            this.title = title;
            this.description = description;
            this.currentValue = initialValue;
            this.onValueChange = onValueChange;
            this.valueSupplier = valueSupplier;
            this.configValue = configValue;
        }

        abstract void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width);
        abstract void extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, Font font);
        abstract void updateWidgetValue();

        void updateValue(T newValue) {
            this.currentValue = newValue;
            if (this.onValueChange != null) {
                this.onValueChange.accept(newValue);
            }
            if (this.configValue != null) {
                this.configValue.set(newValue);
                ModConfig.save();
            }
        }

        void resetToDefault() {
            if (this.configValue != null) {
                updateValue(this.configValue.getDefault());
            }
        }
    }

    private static class CategoryHeaderEntry extends ConfigEntry<Void> {
        CategoryHeaderEntry(Component title) {
            super(title, Component.empty(), null, null, null, null);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
        }

        @Override
        void extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            graphics.text(font, title, x, y + 5, 0xFFFFFFA0);
            graphics.fill(x, y + 20, x + width, y + 22, 0xFFA0A0A0);
        }

        @Override
        void updateWidgetValue() {
        }
    }

    private static class BooleanConfigEntry extends ConfigEntry<Boolean> {
        private Button checkBox;

        BooleanConfigEntry(Component title, Component description, Boolean initialValue, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier, ModConfigSpec.ConfigValue<Boolean> configValue) {
            super(title, description, initialValue, onValueChange, valueSupplier, configValue);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            checkBox = Button.builder(getButtonText(), btn -> {
                boolean newValue = !currentValue;
                updateValue(newValue);
                btn.setMessage(getButtonText());
            }).bounds(x + width - 100, y + 10, 100, 20).build();
            screen.addRenderableWidget(checkBox);
        }

        @Override
        void extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            graphics.text(font, title, x, y + 5, 0xFFFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                graphics.text(font, wrappedDesc.get(i), x, y + 20 + i * 10, 0xFFAAAAAA);
            }
        }

        private Component getButtonText() {
            return currentValue ? Component.translatable("gui.yes") : Component.translatable("gui.no");
        }

        @Override
        void updateWidgetValue() {
            if (checkBox != null) {
                currentValue = valueSupplier.get();
                checkBox.setMessage(getButtonText());
            }
        }
    }

    private static class NumberConfigEntry<T extends Number> extends ConfigEntry<T> {
        private RangedEditBox editBox;
        private final double min;
        private final double max;
        private final Parser<T> parser;

        NumberConfigEntry(Component title, Component description, T initialValue, double min, double max, Consumer<T> onValueChange, Supplier<T> valueSupplier, ModConfigSpec.ConfigValue<T> configValue, Parser<T> parser) {
            super(title, description, initialValue, onValueChange, valueSupplier, configValue);
            this.min = min;
            this.max = max;
            this.parser = parser;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max);
            editBox.setMaxLength(15);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    parser.parse(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(parser.parse(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void extract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            graphics.text(font, title, x, yPos + 5, 0xFFFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                graphics.text(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xFFAAAAAA);
            }
        }

        @Override
        void updateWidgetValue() {
            if (editBox != null) {
                currentValue = valueSupplier.get();
                editBox.setValue(String.valueOf(currentValue));
            }
        }
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(String value);
    }

    private static class RangedEditBox extends EditBox {
        private final double min;
        private final double max;

        public RangedEditBox(Font font, int x, int y, int width, int height, Component component, double min, double max) {
            super(font, x, y, width, height, component);
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            boolean result = super.keyPressed(event);
            if (event.isConfirmation()) {
                validateAndCorrectValue();
            }
            return result;
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && this.isFocused()) {
                validateAndCorrectValue();
            }
            super.setFocused(focused);
        }

        private void validateAndCorrectValue() {
            String text = getValue();
            if (!text.isEmpty()) {
                try {
                    double val = Double.parseDouble(text);
                    if (val > max) {
                        setValue(String.valueOf(max));
                        moveCursorTo(0, false);
                    } else if (val < min) {
                        setValue(String.valueOf(min));
                        moveCursorTo(0, false);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
