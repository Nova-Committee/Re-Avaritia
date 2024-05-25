package committee.nova.mods.avaritia.init.compact.rei.category;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.screen.CompressorScreen;
import committee.nova.mods.avaritia.init.compact.rei.ServerREIPlugin;
import committee.nova.mods.avaritia.init.compact.rei.display.CompressorDisplay;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class CompressorCategory implements DisplayCategory<CompressorDisplay> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/compressor.png");
    @Override
    public CategoryIdentifier<? extends CompressorDisplay> getCategoryIdentifier() {
        return ServerREIPlugin.COMPRESSOR;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.avaritia.compressor");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModBlocks.neutron_compressor.get());
    }

    @Override
    public List<Widget> setupDisplay(CompressorDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 81, bounds.getCenterY() - 35);
        List<Widget> widgets = Lists.newArrayList();

        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createTexturedWidget(TEXTURE, startPoint.x + 14, startPoint.y + 17, 0, 0, 130, 40));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 32, startPoint.y + 28)).entries(display.getInputEntries().get(0)).markInput().disableBackground());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 112, startPoint.y + 28)).entries(display.getOutputEntries().get(0)).markOutput().disableBackground());

        widgets.add(Widgets.createDrawableWidget((context, mouseX, mouseY, delta) -> {
            context.blit(CompressorScreen.BACKGROUND, startPoint.x + 57, startPoint.y + 27, 176, 16, 16, (int) ((System.currentTimeMillis() / display.getTimeCost()) % 15 ));
        }));

        widgets.add(Widgets.createLabel(new Point(startPoint.x + 37, startPoint.y + 46), Component.literal(String.valueOf(display.getTimeCost()))));
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 50;
    }

    @Override
    public int getDisplayWidth(CompressorDisplay display) {
        return 140;
    }

    @Override
    public int getMaximumDisplaysPerPage() {
        return 3;
    }
}
