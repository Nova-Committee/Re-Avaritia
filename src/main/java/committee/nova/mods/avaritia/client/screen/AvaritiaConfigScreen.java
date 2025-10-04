package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

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
    private static final double SCROLL_FACTOR = 10.0;

    public AvaritiaConfigScreen(Screen parent) {
        super(Component.translatable("title.avaritia.config.title"));
        this.parent = parent;
        initConfigEntries();
    }

    private void initConfigEntries() {
        addCategoryHeader("config.avaritia.category.tools");

        addBooleanEntry("is_keep_stone", ModConfig.isKeepStone,
                Component.translatable("config.avaritia.is_keep_stone.tooltip"),
                ModConfig.isKeepStone::set, ModConfig.isKeepStone);

        addBooleanEntry("is_merge_matter_cluster", ModConfig.isMergeMatterCluster,
                Component.translatable("config.avaritia.is_merge_matter_cluster.tooltip"),
                ModConfig.isMergeMatterCluster::set, ModConfig.isMergeMatterCluster);

        addIntEntry("sword_range_damage", ModConfig.swordRangeDamage, 100, 100000,
                Component.translatable("config.avaritia.sword_range_damage.tooltip"),
                ModConfig.swordRangeDamage::set, ModConfig.swordRangeDamage);

        addIntEntry("sword_attack_range", ModConfig.swordAttackRange, 8, 64,
                Component.translatable("config.avaritia.sword_attack_range.tooltip"),
                ModConfig.swordAttackRange::set, ModConfig.swordAttackRange);

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

        addDoubleEntry("food_time", ModConfig.foodTime, 0.1, 5.0,
                Component.translatable("config.avaritia.food_time.tooltip"),
                ModConfig.foodTime::set, ModConfig.foodTime);

        addIntEntry("pickaxe_break_range", ModConfig.pickAxeBreakRange, 2, 32,
                Component.translatable("config.avaritia.pickaxe_break_range.tooltip"),
                ModConfig.pickAxeBreakRange::set, ModConfig.pickAxeBreakRange);

        addIntEntry("shovel_break_range", ModConfig.shovelBreakRange, 2, 32,
                Component.translatable("config.avaritia.shovel_break_range.tooltip"),
                ModConfig.shovelBreakRange::set, ModConfig.shovelBreakRange);

        addIntEntry("neutron_collector_product_tick", ModConfig.neutronCollectorProductTick, 1200, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_collector_product_tick.tooltip"),
                ModConfig.neutronCollectorProductTick::set, ModConfig.neutronCollectorProductTick);

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

        addBooleanEntry("internal_infinity_catalyst_craft", ModConfig.internalInfinityCatalystCraft,
                Component.translatable("config.avaritia.internal_infinity_catalyst_craft.tooltip"),
                ModConfig.internalInfinityCatalystCraft::set, ModConfig.internalInfinityCatalystCraft);


        addCategoryHeader("config.avaritia.category.emc");

        addIntEntry("neutron_pile_emc", ModConfig.neutronPileEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_pile_emc.tooltip"),
                ModConfig.neutronPileEmc::set, ModConfig.neutronPileEmc);

        addIntEntry("vanilla_totem_emc", ModConfig.vanillaTotemEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.vanilla_totem_emc.tooltip"),
                ModConfig.vanillaTotemEmc::set, ModConfig.vanillaTotemEmc);

        addCategoryHeader("config.avaritia.category.storage");

        addIntEntry("chest_max_item_size", ModConfig.chestMaxItemSize, 2048, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.chest_max_item_size.tooltip"),
                ModConfig.chestMaxItemSize::set, ModConfig.chestMaxItemSize);

        addBooleanEntry("use_single_page_mode", ModConfig.useSinglePageMode,
                Component.translatable("config.avaritia.use_single_page_mode.tooltip"),
                ModConfig.useSinglePageMode::set, ModConfig.useSinglePageMode);

        addLongEntry("slot_stack_limit", ModConfig.slotStackLimit, 64L, 4294967295L,
                Component.translatable("config.avaritia.slot_stack_limit.tooltip"),
                ModConfig.slotStackLimit::set, ModConfig.slotStackLimit);

        addIntEntry("max_page_limit", ModConfig.maxPageLimit, 2, 79536431,
                Component.translatable("config.avaritia.max_page_limit.tooltip"),
                ModConfig.maxPageLimit::set, ModConfig.maxPageLimit);

        addIntEntry("reset_max_page", ModConfig.resetMaxPage, 1, 79536431,
                Component.translatable("config.avaritia.reset_max_page.tooltip"),
                ModConfig.resetMaxPage::set, ModConfig.resetMaxPage);

        addIntEntry("inventory_rows", ModConfig.inventoryRows, 1, 6,
                Component.translatable("config.avaritia.inventory_rows.tooltip"),
                ModConfig.inventoryRows::set, ModConfig.inventoryRows);

        addCategoryHeader("config.avaritia.category.channel");

        addIntEntry("max_size_pre_channel", ModConfig.MAX_SIZE_PRE_CHANNEL, 2048, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.max_size_pre_channel.tooltip"),
                val -> ModConfig.MAX_SIZE_PRE_CHANNEL.set(val), ModConfig.MAX_SIZE_PRE_CHANNEL);

        addIntEntry("max_channels_pre_player", ModConfig.MAX_CHANNELS_PRE_PLAYER, 4, 64,
                Component.translatable("config.avaritia.max_channels_pre_player.tooltip"),
                val -> ModConfig.MAX_CHANNELS_PRE_PLAYER.set(val), ModConfig.MAX_CHANNELS_PRE_PLAYER);

        addIntEntry("max_public_channels", ModConfig.MAX_PUBLIC_CHANNELS, 32, 1024,
                Component.translatable("config.avaritia.max_public_channels.tooltip"),
                val -> ModConfig.MAX_PUBLIC_CHANNELS.set(val), ModConfig.MAX_PUBLIC_CHANNELS);

        addIntEntry("channel_fast_update_rate", ModConfig.CHANNEL_FAST_UPDATE_RATE, 1, 40,
                Component.translatable("config.avaritia.channel_fast_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FAST_UPDATE_RATE.set(val), ModConfig.CHANNEL_FAST_UPDATE_RATE);

        addIntEntry("channel_full_update_rate", ModConfig.CHANNEL_FULL_UPDATE_RATE, 20, 1200,
                Component.translatable("config.avaritia.channel_full_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FULL_UPDATE_RATE.set(val), ModConfig.CHANNEL_FULL_UPDATE_RATE);

        addCategoryHeader("config.avaritia.category.misc");

        addBooleanEntry("use_advance_tooltips", ModConfig.useAdvanceTooltips,
                Component.translatable("config.avaritia.use_advance_tooltips.tooltip"),
                ModConfig.useAdvanceTooltips::set, ModConfig.useAdvanceTooltips);

        addDoubleEntry("endless_item_entity_speed", ModConfig.endlessItemEntitySpeed, 1.0, 50.0,
                Component.translatable("config.avaritia.endless_item_entity_speed.tooltip"),
                ModConfig.endlessItemEntitySpeed::set, ModConfig.endlessItemEntitySpeed);

        addDoubleEntry("endless_item_entity_range", ModConfig.endlessItemEntityRange, 1.0, 10000.0,
                Component.translatable("config.avaritia.endless_item_entity_range.tooltip"),
                ModConfig.endlessItemEntityRange::set, ModConfig.endlessItemEntityRange);
        addDoubleEntry("infinity_elytra_flying_speed", ModConfig.infinityElytraFlyingSpeed, 0.0, 100.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_speed.tooltip"),
                ModConfig.infinityElytraFlyingSpeed::set, ModConfig.infinityElytraFlyingSpeed);
        addDoubleEntry("infinity_elytra_flying_damage_range", ModConfig.infinityElytraFlyingRangeDamage, 0.0, 10000.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_damage_range.tooltip"),
                ModConfig.infinityElytraFlyingRangeDamage::set,ModConfig.infinityElytraFlyingRangeDamage);
    }

    private void addBooleanEntry(String titleKey, ModConfigSpec.BooleanValue configValue,
                                 Component description, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier) {
        configEntries.add(new BooleanConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                onValueChange,
                valueSupplier
        ));
    }

    private void addIntEntry(String titleKey, ModConfigSpec.IntValue configValue, int min, int max,
                             Component description, Consumer<Integer> onValueChange, Supplier<Integer> valueSupplier) {
        configEntries.add(new IntConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addDoubleEntry(String titleKey, ModConfigSpec.DoubleValue configValue, double min, double max,
                                Component description, Consumer<Double> onValueChange, Supplier<Double> valueSupplier) {
        configEntries.add(new DoubleConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addLongEntry(String titleKey, ModConfigSpec.LongValue configValue, long min, long max,
                              Component description, Consumer<Long> onValueChange, Supplier<Long> valueSupplier) {
        configEntries.add(new LongConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addCategoryHeader(String translationKey) {
        configEntries.add(new CategoryHeaderEntry(
                Component.translatable(translationKey)
        ));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        resetButton = addRenderableWidget(Button.builder(
                Component.translatable("controls.reset"),
                btn -> {
                    resetToDefaults();
                    updateWidgetValues();
                }
        ).bounds(width / 2 - 102, height - 30, 100, 20).build());

        backButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                btn -> minecraft.setScreen(parent)
        ).bounds(width / 2 + 2, height - 30, 100, 20).build());

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int x = MARGIN;
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y && y < height - 40) {
                entry.initWidgets(this, x, y, width - 2 * MARGIN);
            }
        }
    }


    private void resetToDefaults() {
        // Tools 配置项
        ModConfig.isKeepStone.set(ModConfig.isKeepStone.getDefault());
        ModConfig.isMergeMatterCluster.set(ModConfig.isMergeMatterCluster.getDefault());
        ModConfig.swordRangeDamage.set(ModConfig.swordRangeDamage.getDefault());
        ModConfig.swordAttackRange.set(ModConfig.swordAttackRange.getDefault());
        ModConfig.isSwordAttackLightning.set(ModConfig.isSwordAttackLightning.getDefault());
        ModConfig.isSwordAttackEndless.set(ModConfig.isSwordAttackEndless.getDefault());
        ModConfig.subArrowDamage.set(ModConfig.subArrowDamage.getDefault());
        ModConfig.axeChainCount.set(ModConfig.axeChainCount.getDefault());
        ModConfig.foodTime.set(ModConfig.foodTime.getDefault());
        ModConfig.pickAxeBreakRange.set(ModConfig.pickAxeBreakRange.getDefault());
        ModConfig.shovelBreakRange.set(ModConfig.shovelBreakRange.getDefault());
        ModConfig.neutronCollectorProductTick.set(ModConfig.neutronCollectorProductTick.getDefault());
        ModConfig.singularityTimeRequired.set(ModConfig.singularityTimeRequired.getDefault());
        ModConfig.growthSoulFarmland.set(ModConfig.growthSoulFarmland.getDefault());
        ModConfig.bladeSlashDamage.set(ModConfig.bladeSlashDamage.getDefault());
        ModConfig.bladeSlashRadius.set(ModConfig.bladeSlashRadius.getDefault());
        ModConfig.internalInfinityCatalystCraft.set(ModConfig.internalInfinityCatalystCraft.getDefault());

        // EMC 配置项
        ModConfig.neutronPileEmc.set(ModConfig.neutronPileEmc.getDefault());
        ModConfig.vanillaTotemEmc.set(ModConfig.vanillaTotemEmc.getDefault());

        // Storage 配置项
        ModConfig.chestMaxItemSize.set(ModConfig.chestMaxItemSize.getDefault());
        ModConfig.useSinglePageMode.set(ModConfig.useSinglePageMode.getDefault());
        ModConfig.slotStackLimit.set(ModConfig.slotStackLimit.getDefault());
        ModConfig.maxPageLimit.set(ModConfig.maxPageLimit.getDefault());
        ModConfig.resetMaxPage.set(ModConfig.resetMaxPage.getDefault());
        ModConfig.inventoryRows.set(ModConfig.inventoryRows.getDefault());

        // Channel 配置项
        ModConfig.MAX_SIZE_PRE_CHANNEL.set(ModConfig.MAX_SIZE_PRE_CHANNEL.getDefault());
        ModConfig.MAX_CHANNELS_PRE_PLAYER.set(ModConfig.MAX_CHANNELS_PRE_PLAYER.getDefault());
        ModConfig.MAX_PUBLIC_CHANNELS.set(ModConfig.MAX_PUBLIC_CHANNELS.getDefault());
        ModConfig.CHANNEL_FAST_UPDATE_RATE.set(ModConfig.CHANNEL_FAST_UPDATE_RATE.getDefault());
        ModConfig.CHANNEL_FULL_UPDATE_RATE.set(ModConfig.CHANNEL_FULL_UPDATE_RATE.getDefault());

        // Misc 配置项
        ModConfig.useAdvanceTooltips.set(ModConfig.useAdvanceTooltips.getDefault());
        ModConfig.endlessItemEntitySpeed.set(ModConfig.endlessItemEntitySpeed.getDefault());
        ModConfig.endlessItemEntityRange.set(ModConfig.endlessItemEntityRange.getDefault());
        ModConfig.infinityElytraFlyingSpeed.set(ModConfig.infinityElytraFlyingSpeed.getDefault());
        ModConfig.infinityElytraFlyingRangeDamage.set(ModConfig.infinityElytraFlyingRangeDamage.getDefault());
    }

    private void updateWidgetValues() {
        for (ConfigEntry<?> entry : configEntries) {
            entry.updateWidgetValue();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 处理惯性滚动
        if (Math.abs(scrollVelocity) > MIN_VELOCITY) {
            int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset + scrollVelocity));
            scrollVelocity *= FRICTION; // 应用摩擦力

            if (Math.abs(scrollVelocity) < MIN_VELOCITY) {
                scrollVelocity = 0;
            }

            init(); // 重新初始化组件位置
        }
        for (ConfigEntry<?> entry : configEntries) {
            if (entry instanceof IntConfigEntry intEntry && intEntry.editBox != null) {
                intEntry.editBox.customTick();
            } else if (entry instanceof DoubleConfigEntry doubleEntry && doubleEntry.editBox != null) {
                doubleEntry.editBox.customTick();
            } else if (entry instanceof LongConfigEntry longEntry && longEntry.editBox != null) {
                longEntry.editBox.customTick();
            }
        }
        renderBackground(guiGraphics, 0, 0,0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y - 20 && y < height - 20) {
                entry.render(guiGraphics, mouseX, mouseY, MARGIN, y, width - 2 * MARGIN, ENTRY_HEIGHT, font);
            }
        }

        if (configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {

            maxScrollOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            int visibleHeight = height - START_Y - 40;
            scrollbarHeight = visibleHeight;
            scrollbarHandleHeight = Math.max(20, visibleHeight * visibleHeight / (configEntries.size() * ENTRY_HEIGHT));
            int scrollBarYOffset = (int) ((double) scrollOffset / maxScrollOffset * (visibleHeight - scrollbarHandleHeight));
            scrollbarY = START_Y + scrollBarYOffset;
            scrollbarX = width - 8;

            guiGraphics.fill(scrollbarX, START_Y, scrollbarX + 4, START_Y + scrollbarHeight, 0x88888888);
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHandleHeight, 0xFFAAAAAA);
        }

        resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        backButton.render(guiGraphics, mouseX, mouseY, partialTick);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 4 &&
                    mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHandleHeight) {
                isDraggingScrollbar = true;
                return true;
            }
            else if (mouseX >= scrollbarX && mouseX <= scrollbarX + 4 &&
                    mouseY >= START_Y && mouseY <= START_Y + scrollbarHeight) {
                int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
                double clickPosition = (mouseY - START_Y) / scrollbarHeight;
                scrollOffset = (int) (clickPosition * maxOffset);
                scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
                init();
                isDraggingScrollbar = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar && configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            scrollVelocity = 0;
            int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            double positionRatio = (mouseY - START_Y - (double) scrollbarHandleHeight / 2) / (scrollbarHeight - scrollbarHandleHeight);
            scrollOffset = (int) (positionRatio * maxOffset);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
            init();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private abstract static class ConfigEntry<T> {
        final Component title;
        final Component description;
        T currentValue;
        final Consumer<T> onValueChange;
        final Supplier<T> valueSupplier;

        ConfigEntry(Component title, Component description, T initialValue, Consumer<T> onValueChange, Supplier<T> valueSupplier) {
            this.title = title;
            this.description = description;
            this.currentValue = initialValue;
            this.onValueChange = onValueChange;
            this.valueSupplier = valueSupplier;
        }

        abstract void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width);

        abstract void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font);

        abstract void updateWidgetValue();

        void updateValue(T newValue) {
            this.currentValue = newValue;
            if (this.onValueChange != null) {
                this.onValueChange.accept(newValue);
            }
        }
    }

    private static class CategoryHeaderEntry extends ConfigEntry<Void> {
        CategoryHeaderEntry(Component title) {
            super(title, Component.empty(), null, null, null);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            gui.drawString(font, title, x, y + 5, 0xFFFFA0);
            gui.fill(x, y + 20, x + width, y + 22, 0xFFA0A0A0);
        }

        @Override
        void updateWidgetValue() {
        }
    }

    private static class BooleanConfigEntry extends ConfigEntry<Boolean> {
        private Button checkBox;

        BooleanConfigEntry(Component title, Component description, Boolean initialValue, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            checkBox = Button.builder(
                    getButtonText(),
                    btn -> {
                        boolean newValue = !currentValue;
                        updateValue(newValue);
                        btn.setMessage(getButtonText());
                    }
            ).bounds(x + width - 100, y + 10, 100, 20).build();
            screen.addRenderableWidget(checkBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            gui.drawString(font, title, x, y + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, y + 20 + i * 10, 0xAAAAAA);
            }
        }

        private Component getButtonText() {
            return currentValue ?
                    Component.translatable("gui.yes") :
                    Component.translatable("gui.no");
        }


        @Override
        void updateWidgetValue() {
            if (checkBox != null) {
                currentValue = valueSupplier.get();
                checkBox.setMessage(getButtonText());
            }
        }
    }


    private static class IntConfigEntry extends ConfigEntry<Integer> {
        private RangedEditBox editBox;
        private final int min;
        private final int max;

        IntConfigEntry(Component title, Component description, Integer initialValue,
                       int min, int max, Consumer<Integer> onValueChange, Supplier<Integer> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, true);
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Integer.parseInt(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
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

    private static class DoubleConfigEntry extends ConfigEntry<Double> {
        private RangedEditBox editBox;
        private final double min;
        private final double max;

        DoubleConfigEntry(Component title, Component description, Double initialValue,
                          double min, double max, Consumer<Double> onValueChange, Supplier<Double> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, false);
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                if (text.contains(".") && text.indexOf(".") != text.lastIndexOf(".")) return false;
                try {
                    Double.parseDouble(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Double.parseDouble(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
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

    private static class LongConfigEntry extends ConfigEntry<Long> {
        private RangedEditBox editBox;
        private final long min;
        private final long max;

        LongConfigEntry(Component title, Component description, Long initialValue,
                        long min, long max, Consumer<Long> onValueChange, Supplier<Long> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, false);
            editBox.setMaxLength(15);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    Long.parseLong(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Long.parseLong(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
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


    private static class RangedEditBox extends EditBox {
        private final double min;
        private final double max;
        private final boolean isInteger;
        private String lastValue = "";

        public RangedEditBox(Font font, int x, int y, int width, int height, Component component, double min, double max, boolean isInteger) {
            super(font, x, y, width, height, component);
            this.min = min;
            this.max = max;
            this.isInteger = isInteger;
        }

        public void customTick() {
            String currentValue = getValue();
            if (!currentValue.equals(lastValue)) {
                lastValue = currentValue;
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean result = super.keyPressed(keyCode, scanCode, modifiers);

            if (keyCode == 257 || keyCode == 335) {
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
                    if (isInteger) {
                        long val = Long.parseLong(text);
                        if (val > max) {
                            setValue(String.valueOf((long) max));
                            moveCursorTo(0, false);
                        } else if (val < min) {
                            setValue(String.valueOf((long) min));
                            moveCursorTo(0, false);
                        }
                    } else {
                        double val = Double.parseDouble(text);
                        if (val > max) {
                            setValue(String.valueOf(max));
                            moveCursorTo(0, false);
                        } else if (val < min) {
                            setValue(String.valueOf(min));
                            moveCursorTo(0, false);
                        }
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }
    }
}